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

package com.kdgregory.pathfinder.spring.context;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

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

import com.kdgregory.bcelx.parser.AnnotationParser;
import com.kdgregory.pathfinder.core.WarMachine;
import com.kdgregory.pathfinder.spring.InvalidContextException;
import com.kdgregory.pathfinder.util.ClasspathScanner;


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

    private Map<String,BeanDefinition> beanDefinitions       = new HashMap<String,BeanDefinition>();
    private Map<String,BeanDefinition> beanDefinitionsById   = new HashMap<String,BeanDefinition>();
    private Map<String,BeanDefinition> beanDefinitionsByName = new HashMap<String,BeanDefinition>();


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
        this(null, war, contextLocation);
    }


    /**
     *  Creates an instance that will append beans defined in the passed
     *  context to those defined by the parent context.
     */
    public SpringContext(SpringContext parent, WarMachine war, String contextLocation)
    {
        if (parent != null)
        {
            beanDefinitions.putAll(parent.beanDefinitions);
            beanDefinitionsById.putAll(parent.beanDefinitionsById);
            beanDefinitionsByName.putAll(parent.beanDefinitionsByName);
        }

        for (String path : ResourceLoader.decomposeResourceReferences(contextLocation))
        {
            Document dom = parseContextFile(war, path, "");
            processImports(war, path, dom);
            processXmlConfig(path, dom);
            processComponentScans(war, dom);
        }
    }


//----------------------------------------------------------------------------
//  Public Methods
//----------------------------------------------------------------------------

    /**
     *  Returns an unmodifiable view of the bean definition map.
     */
    public Map<String,BeanDefinition> getBeans()
    {
        return Collections.unmodifiableMap(beanDefinitions);
    }


    /**
     *  Returns the definition for the bean with a given name, <code>null</code>
     *  if that bean is not in the context.
     */
    public BeanDefinition getBean(String name)
    {
        BeanDefinition def = beanDefinitionsById.get(name);
        if (def == null)
            def = beanDefinitionsByName.get(name);

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

        return beans;
    }


//----------------------------------------------------------------------------
//  Internals
//----------------------------------------------------------------------------

    private Document parseContextFile(WarMachine war, String file, String baseDir)
    {
        logger.debug("parsing context file: " + file);

        InputStream in = null;
        try
        {
            in = new ResourceLoader(war, baseDir).getResourceAsStream(file);
            if (in == null)
                throw new InvalidContextException("invalid context location: " + file);
            return ParseUtil.parse(new InputSource(in));
        }
        catch (Exception ex)
        {
            if (ex instanceof InvalidContextException)
                throw (InvalidContextException)ex;
            throw new InvalidContextException("unparseable context: " + file, ex);
        }
        finally
        {
            IOUtil.closeQuietly(in);
        }
    }


    private void processImports(WarMachine war, String origFile, Document dom)
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

            if (importLoc.startsWith("/"))
            {
                logger.warn("imported context is an absolute path, but Spring treats as relative: " + importLoc);
            }

            logger.debug("processing imported file \"" + importLoc + "\" from " + origFile);
            String baseDir = StringUtil.extractLeftOfLast(origFile, "/");
            Document importDom = parseContextFile(war, importLoc, baseDir);
            for (Element child : DomUtil.getChildren(importDom.getDocumentElement()))
            {
                child = (Element)dom.importNode(child, true);
                dom.getDocumentElement().appendChild(child);
            }
        }
    }


    private void processXmlConfig(String filename, Document dom)
    {
        List<Element> beans = xpfact.newXPath("/b:beans/b:bean").evaluate(dom, Element.class);
        logger.debug("found " + beans.size() + " bean definitions in " + filename);

        for (Element bean : beans)
        {
            XmlBeanDefinition def = new XmlBeanDefinition(xpfact, this, bean);
            addBeanDefinition(def);
        }
    }



    private void processComponentScans(WarMachine war, Document dom)
    {
        for (ClasspathScanner scanner : getComponentScans(dom))
        {
            Collection<AnnotationParser> parsedClasses = scanner.scan(war).values();
            logger.debug("found " + parsedClasses.size() + " classes by component scan");
            for (AnnotationParser parsedClass : parsedClasses)
            {
                ScannedBeanDefinition def = new ScannedBeanDefinition(parsedClass.getParsedClass(), parsedClass);
                addBeanDefinition(def);
            }
        }
    }


    private List<ClasspathScanner> getComponentScans(Document dom)
    {
        List<Element> scanDefs = xpfact.newXPath("/b:beans/ctx:component-scan")
                                 .evaluate(dom, Element.class);

        List<ClasspathScanner> result = new ArrayList<ClasspathScanner>(scanDefs.size());
        for (Element elem : scanDefs)
        {
            ClasspathScanner scanner = new ClasspathScanner()
                                       .addIncludedAnnotation(SpringConstants.ANNO_CONTROLLER)
                                       .addIncludedAnnotation(SpringConstants.ANNO_COMPONENT);
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


    private void addBeanDefinition(BeanDefinition def)
    {
        String beanId = def.getBeanId();
        String beanName = def.getBeanName();

        String preferredId = null;
        if (! StringUtil.isBlank(beanId))
        {
            preferredId = beanId;
            logger.debug("adding bean by ID: \"" + preferredId + "\" => " + def.getBeanClass());
        }
        else if (! StringUtil.isBlank(beanName))
        {
            preferredId = beanName;
            logger.debug("adding bean by name: \"" + preferredId + "\" => " + def.getBeanClass());
        }
        else
        {
            preferredId = UUID.randomUUID().toString();
            logger.debug("adding bean with generated ID: \"" + preferredId + "\" => " + def.getBeanClass());
        }

        if (beanDefinitions.containsKey(preferredId))
        {
            BeanDefinition existing = beanDefinitions.remove(preferredId);
            logger.warn("replacing existing bean with ID \"" + preferredId + "\"");
            if (! StringUtil.isBlank(existing.getBeanId()))
            {
                beanDefinitionsByName.remove(existing.getBeanId());
            }
            if (! StringUtil.isBlank(existing.getBeanName()))
            {
                beanDefinitionsByName.remove(existing.getBeanName());
            }
        }

        beanDefinitions.put(preferredId, def);
        if (! StringUtil.isBlank(def.getBeanId()))
        {
            beanDefinitionsById.put(def.getBeanId(), def);
        }
        if (! StringUtil.isBlank(def.getBeanName()))
        {
            beanDefinitionsByName.put(def.getBeanName(), def);
        }
    }
}
