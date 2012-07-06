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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import org.apache.bcel.classfile.JavaClass;
import org.apache.bcel.classfile.Method;
import org.apache.log4j.Logger;

import org.springframework.web.bind.annotation.RequestMethod;

import net.sf.kdgcommons.lang.StringUtil;
import net.sf.practicalxml.xpath.XPathWrapperFactory;

import com.kdgregory.bcelx.classfile.Annotation;
import com.kdgregory.bcelx.classfile.Annotation.ParamValue;
import com.kdgregory.bcelx.parser.AnnotationParser;
import com.kdgregory.pathfinder.core.ClasspathScanner;
import com.kdgregory.pathfinder.core.Inspector;
import com.kdgregory.pathfinder.core.PathRepo;
import com.kdgregory.pathfinder.core.PathRepo.Destination;
import com.kdgregory.pathfinder.core.PathRepo.HttpMethod;
import com.kdgregory.pathfinder.core.WarMachine;
import com.kdgregory.pathfinder.core.WarMachine.ServletMapping;


/**
 *  Finds any mappings to Spring's DispatcherServlet, and replaces them with mappings
 *  found by inspecting the various Spring context files. Supports both XML-driven and
 *  Annotation-driven configuration; does not support (at presnt) Code-driven config.
 */
public class SpringInspector
implements Inspector
{
    private Logger logger = Logger.getLogger(getClass());


//----------------------------------------------------------------------------
//  The Destinations that we support
//----------------------------------------------------------------------------

    public static class SpringDestination
    implements Destination
    {
        private BeanDefinition beanDef;

        public SpringDestination(BeanDefinition beanDef)
        {
            this.beanDef = beanDef;
        }

        /** This method exists primarily for testing */
        public BeanDefinition getBeanDefinition()
        {
            return beanDef;
        }

        @Override
        public String toString()
        {
            return beanDef.getBeanClass();
        }
    }


//----------------------------------------------------------------------------
//  Inspector Implementation
//----------------------------------------------------------------------------

    @Override
    public void inspect(WarMachine war, PathRepo paths)
    {
        logger.info("SpringInspector started");

        SpringContext rootContext = loadRootContext(war);

        List<ServletMapping> springMappings = extractSpringMappings(war, paths);
        logger.debug("extracted " + springMappings.size() + " Spring mappings");
        for (ServletMapping mapping : springMappings)
        {
            String urlPrefix = extractUrlPrefix(mapping.getUrlPattern());
            String configLoc = mapping.getInitParams().get("contextConfigLocation");
            if (StringUtil.isBlank(configLoc))
            {
                configLoc = "/WEB-INF/" + mapping.getServletName() + "-servlet.xml";
            }

            logger.debug("processing mapping for \"" + urlPrefix + "\" from configFile " + configLoc);
            SpringContext context = new SpringContext(rootContext, war, configLoc);
            processSimpleUrlHandlerMappings(war, context, urlPrefix, paths);
            processAnnotationMappings(war, context, urlPrefix, paths);
        }
        logger.info("SpringInspector finished");
    }


//----------------------------------------------------------------------------
//  Internals
//----------------------------------------------------------------------------

    private SpringContext loadRootContext(WarMachine war)
    {
        XPathWrapperFactory xpf = new XPathWrapperFactory()
                                  .bindNamespace("j2ee", "http://java.sun.com/xml/ns/j2ee");

        // if there's no root context listener, we're done
        List<String> listeners = xpf.newXPath("/j2ee:web-app/j2ee:listener/j2ee:listener-class")
                                 .evaluateAsStringList(war.getWebXml());
        Set<String> listeners2 = new HashSet<String>(listeners);
        if (!listeners2.contains("org.springframework.web.context.ContextLoaderListener"))
        {
            logger.debug("no root context listener found");
            return null;
        }

        // look for an explicit config file location
        String contextLocation = xpf.newXPath("/j2ee:web-app/j2ee:context-param/"
                                              + "j2ee:param-name[text()='contextConfigLocation']/"
                                              + "../j2ee:param-value").evaluateAsString(war.getWebXml());

        // and fallback to default
        if (StringUtil.isBlank(contextLocation))
        {
            logger.debug("context listener found, but no contextConfigLocation; using default");
            contextLocation = "/WEB-INF/applicationContext.xml";
        }

        logger.debug("root context location: " + contextLocation);
        return new SpringContext(war, contextLocation);
    }


    private List<ServletMapping> extractSpringMappings(WarMachine war, PathRepo paths)
    {
        List<ServletMapping> result = new ArrayList<ServletMapping>();
        for (ServletMapping servlet : war.getServletMappings())
        {
            if (servlet.getServletClass().equals("org.springframework.web.servlet.DispatcherServlet"))
            {
                result.add(servlet);
                paths.remove(servlet.getUrlPattern(), HttpMethod.ALL);
            }
        }
        return result;
    }


    private String extractUrlPrefix(String urlPattern)
    {
        int trimAt = urlPattern.lastIndexOf("/");
        return (trimAt > 0) ? urlPattern.substring(0, trimAt)
                            : urlPattern;
    }


    private void processSimpleUrlHandlerMappings(WarMachine war, SpringContext context, String urlPrefix, PathRepo paths)
    {
        List<BeanDefinition> defs = context.getBeansByClass("org.springframework.web.servlet.handler.SimpleUrlHandlerMapping");
        logger.debug("found " + defs.size() + " SimpleUrlHandlerMapping beans");

        for (BeanDefinition def : defs)
        {
            Properties mappings = def.getPropertyAsProperties("mappings");
            if ((mappings == null) || mappings.isEmpty())
            {
                logger.debug("SimpleUrlHandlerMapping bean " + def.getBeanName() + " has no mappings");
                continue;
            }

            logger.debug("SimpleUrlHandlerMapping bean " + def.getBeanName() + " has " + mappings.size() + " mappings");
            for (Map.Entry<Object,Object> mapping : mappings.entrySet())
            {
                String url = urlPrefix + mapping.getKey();
                String beanName = String.valueOf(mapping.getValue());
                BeanDefinition bean = context.getBean(beanName);
                logger.debug("mapped " + url + " to bean " + beanName);
                paths.put(url, new SpringDestination(bean));
            }
        }
    }


    private void processAnnotationMappings(WarMachine war, SpringContext context, String urlPrefix, PathRepo paths)
    {
        logger.debug("processing Spring3 annotations");
        Set<String> controllers = new TreeSet<String>();    // TreeSet is nicer to print for debugging

        // FIXME - process explicit beans

        Map<String,AnnotationParser> parsedClasses = new TreeMap<String,AnnotationParser>();
        List<ClasspathScanner> scanners = context.getComponentScans();
        for (ClasspathScanner scanner : scanners)
        {
            Set<String> scanResults = scanner.scan(war, parsedClasses);
            controllers.addAll(scanResults);
        }

        logger.debug("found " + controllers.size() + " beans marked with @Controller");

        for (String filename : controllers)
        {
            processAnnotatedController(filename, parsedClasses.get(filename), war, context, urlPrefix, paths);
        }
    }


    private void processAnnotatedController(
            String filename, AnnotationParser ap,
            WarMachine war, SpringContext context, String urlPrefix, PathRepo paths)
    {
        logger.debug("processing annotations from " + filename);
        logger.debug("initial urlPrefix: " + urlPrefix);
        BeanDefinition beanDef = createBeanDefinition(ap);
        Annotation classMapping = ap.getClassAnnotation("org.springframework.web.bind.annotation.RequestMapping");
        for (String classPrefix : getMappingUrls(urlPrefix, classMapping))
        {
            logger.debug("updated prefix from controller mapping: " + classPrefix);
            for (Method method : ap.getAnnotatedMethods("org.springframework.web.bind.annotation.RequestMapping"))
            {
                processAnnotatedControllerMethods(beanDef, ap, method, war, context, classPrefix, paths);
            }
        }
    }


    private void processAnnotatedControllerMethods(
            BeanDefinition beanDef, AnnotationParser ap, Method method,
            WarMachine war, SpringContext context, String urlPrefix, PathRepo paths)
    {
        Annotation anno = ap.getMethodAnnotation(method, "org.springframework.web.bind.annotation.RequestMapping");
        for (String methodUrl : getMappingUrls(urlPrefix, anno))
        {
            for (HttpMethod reqMethod : getRequestMethods(anno))
            {
                paths.put(methodUrl, reqMethod, new SpringDestination(beanDef));
            }
        }
    }


    private BeanDefinition createBeanDefinition(AnnotationParser ap)
    {
        JavaClass parsedClass = ap.getParsedClass();
        String className = parsedClass.getClassName();

        return new BeanDefinition(className);
    }


    private List<String> getMappingUrls(String urlPrefix, Annotation requestMapping)
    {
        // note: called at both class level, where it might be empty, and
        //       at method level, where it had better not be

        if (requestMapping == null)
        {
            return Arrays.asList(urlPrefix);
        }

        List<Object> mappings = requestMapping.getValue().asListOfObjects();
        if (mappings.size() == 0)
        {
            return Arrays.asList(urlPrefix);
        }

        List<String> result = new ArrayList<String>(mappings.size());
        for (Object mapping : mappings)
        {
            result.add(urlPrefix + mapping);
        }
        return result;
    }


    private List<HttpMethod> getRequestMethods(Annotation requestMapping)
    {
        ParamValue methods = requestMapping.getParam("method");
        if (methods == null)
            return Arrays.asList(HttpMethod.ALL);

        List<HttpMethod> result = new ArrayList<HttpMethod>();
        for (Object method : methods.asListOfObjects())
        {
            switch ((RequestMethod)method)
            {
                case GET :
                    result.add(HttpMethod.GET);
                    break;
                case POST :
                    result.add(HttpMethod.POST);
                    break;
                case PUT :
                    result.add(HttpMethod.PUT);
                    break;
                case DELETE :
                    result.add(HttpMethod.DELETE);
                    break;
                default :
                    throw new UnsupportedOperationException("don't know how to process Spring request method: " + method);
            }
        }
        return result;
    }
}
