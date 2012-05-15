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

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import org.xml.sax.InputSource;

import org.apache.log4j.Logger;

import net.sf.practicalxml.ParseUtil;
import net.sf.practicalxml.xpath.XPathWrapperFactory;
import net.sf.practicalxml.xpath.XPathWrapperFactory.CacheType;

import com.kdgregory.pathfinder.core.WarMachine;


/**
 *  Holds a Spring application context, and provides methods to query it.
 */
public class SpringContext
{
    private Logger logger = Logger.getLogger(getClass());

    private XPathWrapperFactory xpfact
            = new XPathWrapperFactory(CacheType.SIMPLE)
              .bindNamespace("b", "http://www.springframework.org/schema/beans");


//----------------------------------------------------------------------------
//  Instance Variables and Constructor
//----------------------------------------------------------------------------

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
            Document dom = parseContextFile(war, path);
            extractBeanDefinitions(dom, path);
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
        return beanDefinitions.get(name);
    }


    /**
     *  Returns the set of bean names that implement a specified class.
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

    private List<String> decomposeContextLocation(String contextLocation)
    {
        String[] paths = contextLocation.split(",");
        List<String> result = new ArrayList<String>(paths.length);
        for (String path : paths)
        {
            if (path.startsWith("classpath:"))
                path = path.substring(10);
            result.add(path);
        }
        return result;
    }


    private Document parseContextFile(WarMachine war, String file)
    {
        logger.debug("parsing context file: " + file);

        InputStream in = null;
        try
        {
            in = (war == null) ? getClass().getClassLoader().getResourceAsStream(file)
                               : war.openClasspathFile(file);
            if (in == null)
                throw new IllegalArgumentException("invalid context location: " + file);
            return ParseUtil.parse(new InputSource(in));
        }
        catch (Exception ex)
        {
            if (ex instanceof IllegalArgumentException)
                throw (IllegalArgumentException)ex;
            throw new IllegalArgumentException("invalid context location: " + file, ex);
        }
    }


    private void extractBeanDefinitions(Document dom, String filename)
    {
        List<Element> beans = xpfact.newXPath("/b:beans/b:bean").evaluate(dom, Element.class);
        logger.debug("found " + beans.size() + " bean definitions in " + filename);

        for (Element bean : beans)
        {
            BeanDefinition def = new BeanDefinition(xpfact, bean);
            beanDefinitions.put(def.getBeanName(), def);
            logger.debug("found bean \"" + def.getBeanName() + "\" => " + def.getBeanClass());
        }
    }
}
