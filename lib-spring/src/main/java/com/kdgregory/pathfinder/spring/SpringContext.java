// Copyright (c) Keith D Gregory
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package com.kdgregory.pathfinder.spring;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import org.xml.sax.InputSource;

import org.apache.log4j.Logger;

import net.sf.kdgcommons.io.IOUtil;
import net.sf.kdgcommons.lang.StringUtil;
import net.sf.practicalxml.DomUtil;
import net.sf.practicalxml.ParseUtil;
import net.sf.practicalxml.xpath.XPathWrapperFactory;
import net.sf.practicalxml.xpath.XPathWrapperFactory.CacheType;

import com.kdgregory.pathfinder.core.ClasspathScanner;
import com.kdgregory.pathfinder.core.WarMachine;
import com.kdgregory.pathfinder.core.impl.ClasspathScannerImpl;


/**
 *  Holds a Spring application context, and provides methods to query it.
 */
public class SpringContext
{
    private Logger logger = Logger.getLogger(getClass());

    private XPathWrapperFactory xpfact
            = new XPathWrapperFactory(CacheType.SIMPLE)
              .bindNamespace("b",   "http://www.springframework.org/schema/beans")
              .bindNamespace("ctx", "http://www.springframework.org/schema/context");


//----------------------------------------------------------------------------
//  Instance Variables and Constructor
//----------------------------------------------------------------------------

    private SpringContext parent;
    private Document dom;
    private Map<String,BeanDefinition> beanDefinitions = new HashMap<String,BeanDefinition>();


    /**
     *  Creates a new instance, first parsing <code>contextLocation</code> into
     *  one or more references to classpath resources, then loading them in turn.
     *  If <code>war</code> is <code>null</code>, the resources will be loaded
     *  from the runtime classpath (this is used for testing); otherwise, they
     *  will be loaded from the WAR's classpath (WEB-INF/classes and any JARs).
     *  <p>
     *  At this time, only classpath resources are supported; filesystem and http
     *  reources are not.
     */
    public SpringContext(WarMachine war, String contextLocation)
    {
        for (String path : decomposeContextLocation(contextLocation))
        {
            dom = parseContextFile(war, path);
            processImports(war, path);
            extractBeanDefinitions(path);
        }
    }


    /**
     *  Creates an instance that will delegate bean lookups to its parent if
     *  they are not found locally.
     */
    public SpringContext(SpringContext parent, WarMachine war, String contextLocation)
    {
        this(war, contextLocation);
        this.parent = parent;
    }


//----------------------------------------------------------------------------
//  Public Methods
//----------------------------------------------------------------------------

    /**
     *  Returns an unmodifiable view of the bean definition map.
     */
    public Map<String,BeanDefinition> getBeans()
    {
        if (parent == null)
            return Collections.unmodifiableMap(beanDefinitions);

        Map<String,BeanDefinition> combined = new HashMap<String,BeanDefinition>();
        buildBeanMapFromHierarchy(combined);
        return Collections.unmodifiableMap(combined);
    }


    /**
     *  Returns the definition for the bean with a given name, <code>null</code>
     *  if that bean is not in the context.
     */
    public BeanDefinition getBean(String name)
    {
        BeanDefinition def = beanDefinitions.get(name);
        if ((def == null) && (parent != null))
            def = parent.getBean(name);

        return def;
    }


    /**
     *  Returns the set of bean names that implement a specified class.
     *  The caller is free to modify this list.
     */
    public List<BeanDefinition> getBeansByClass(String className)
    {
        List<BeanDefinition> beans = new ArrayList<BeanDefinition>();
        for (BeanDefinition bean : beanDefinitions.values())
        {
            if (className.equals(bean.getBeanClass()))
                beans.add(bean);
        }

        if (parent != null)
            beans.addAll(parent.getBeansByClass(className));

        return beans;
    }


    /**
     *  Finds all <code>&lt;ctx:component-scan&gt;</code> entries and returns
     *  scanner objects for each. Looks in the current context only, and restricts
     *  the scan to classes annotated with <code>@Controller</code>.
     *  <p>
     *  FIXME - does not support inclusions or exclusions; will need to return
     *          a list of scanner objects
     */
    public List<ClasspathScanner> getComponentScans()
    {
        List<Element> scanDefs = xpfact.newXPath("/b:beans/ctx:component-scan")
                                 .evaluate(dom, Element.class);

        List<ClasspathScanner> result = new ArrayList<ClasspathScanner>(scanDefs.size());
        for (Element elem : scanDefs)
        {
            ClasspathScanner scanner = new ClasspathScannerImpl()
                                       .setIncludedAnnotations(SpringConstants.CONTROLLER_ANNO_CLASS);
            String basePackage = elem.getAttribute("base-package");
            String[] bp2 = basePackage.split(",");
            for (String pkg : bp2)
            {
                scanner.addBasePackage(pkg.trim());
            }
            result.add(scanner);
        }

        return result;
    }


//----------------------------------------------------------------------------
//  Internals
//----------------------------------------------------------------------------

    private void buildBeanMapFromHierarchy(Map<String,BeanDefinition> map)
    {
        // note: if both parent and child declares the same beans, it
        // should be an error; we'll assume that a running WAR won't
        // have errors of that sort

        map.putAll(beanDefinitions);
        if (parent != null)
            parent.buildBeanMapFromHierarchy(map);
    }


    private List<String> decomposeContextLocation(String contextLocation)
    {
        contextLocation = StringUtil.trim(contextLocation);
        String[] paths = contextLocation.split("[,;]|\\s+");
        // FIXME - support wildcards
        return Arrays.asList(paths);
    }


    private Document parseContextFile(WarMachine war, String file)
    {
        logger.debug("parsing context file: " + file);

        InputStream in = null;
        try
        {
            in = openResource(war, file);
            if (in == null)
                throw new IllegalArgumentException("invalid context location: " + file);
            return ParseUtil.parse(new InputSource(in));
        }
        catch (Exception ex)
        {
            if (ex instanceof IllegalArgumentException)
                throw (IllegalArgumentException)ex;
            throw new IllegalArgumentException("unparseable context: " + file, ex);
        }
        finally
        {
            IOUtil.closeQuietly(in);
        }
    }


    private void processImports(WarMachine war, String origFile)
    {
        // < ="services.xml"/>
        List<Element> importDefs = xpfact.newXPath("/b:beans/b:import")
                                   .evaluate(dom, Element.class);
        logger.debug(origFile + " has " + importDefs.size() + " imports");
        for (Element importDef : importDefs)
        {
            String importLoc = importDef.getAttribute("resource");
            if (StringUtil.isEmpty(importLoc))
            {
                logger.warn("missing resource attribute; skipping import");
                continue;
            }
            importLoc = rebaseIncludedResource(origFile, importLoc);

            logger.debug("processing imported file \"" + importLoc + "\" from " + origFile);
            Document importDom = parseContextFile(war, importLoc);
            for (Element child : DomUtil.getChildren(importDom.getDocumentElement()))
            {
                child = (Element)dom.importNode(child, true);
                dom.getDocumentElement().appendChild(child);
            }
        }
    }


    private String rebaseIncludedResource(String origFile, String includedFile)
    {
        // FIXME - I'm not sure if the ":" is valid; could be an absolute Windows path
        if (includedFile.contains(":") || includedFile.startsWith("/"))
            return includedFile;

        String origPath = StringUtil.extractLeftOfLast(origFile, "/");
        if (StringUtil.isEmpty(origPath))
            return includedFile;

        return origPath + "/" + includedFile;
    }


    private InputStream openResource(WarMachine war, String file)
    throws IOException
    {
        if (file.startsWith("classpath:"))
        {
            file = file.substring(10);
            return (war == null) ? getClass().getClassLoader().getResourceAsStream(file)
                                 : war.openClasspathFile(file);
        }
        else
        {
            return war.openFile(file);
        }
    }


    private void extractBeanDefinitions(String filename)
    {
        List<Element> beans = xpfact.newXPath("/b:beans/b:bean").evaluate(dom, Element.class);
        logger.debug("found " + beans.size() + " bean definitions in " + filename);

        for (Element bean : beans)
        {
            BeanDefinition def = new BeanDefinition(xpfact, bean);
            beanDefinitions.put(def.getBeanId(), def);
            logger.debug("found bean \"" + def.getBeanId() + "\" => " + def.getBeanClass());
        }
    }
}
