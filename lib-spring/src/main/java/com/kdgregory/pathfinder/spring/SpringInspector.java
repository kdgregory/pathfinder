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

import org.apache.bcel.classfile.LocalVariable;
import org.apache.bcel.classfile.Method;
import org.apache.bcel.generic.Type;
import org.apache.log4j.Logger;

import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import net.sf.kdgcommons.lang.ClassUtil;
import net.sf.kdgcommons.lang.StringUtil;

import com.kdgregory.bcelx.classfile.Annotation;
import com.kdgregory.bcelx.classfile.Annotation.ParamValue;
import com.kdgregory.bcelx.parser.AnnotationParser;

import com.kdgregory.pathfinder.core.ClasspathScanner;
import com.kdgregory.pathfinder.core.HttpMethod;
import com.kdgregory.pathfinder.core.Inspector;
import com.kdgregory.pathfinder.core.PathRepo;
import com.kdgregory.pathfinder.core.WarMachine;
import com.kdgregory.pathfinder.core.WarMachine.ServletMapping;
import com.kdgregory.pathfinder.spring.SpringDestination.RequestParameter;


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
//  Inspector
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
        // if there's no root context listener, we're done
        List<String> listeners = war.getWebXmlPath("/j2ee:web-app/j2ee:listener/j2ee:listener-class")
                                 .evaluateAsStringList(war.getWebXml());
        Set<String> listeners2 = new HashSet<String>(listeners);
        if (!listeners2.contains(SpringConstants.CONTEXT_LISTENER_CLASS))
        {
            logger.debug("no root context listener found");
            return null;
        }

        // look for an explicit config file location
        String contextLocation = war.getWebXmlPath("/j2ee:web-app/j2ee:context-param/"
                                              + "j2ee:param-name[text()='contextConfigLocation']/"
                                              + "../j2ee:param-value")
                                              .evaluateAsString(war.getWebXml());

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
            if (servlet.getServletClass().equals(SpringConstants.DISPATCHER_SERVLET_CLASS))
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
        String prefix = (trimAt > 0)
                      ? urlPattern.substring(0, trimAt)
                      : urlPattern;
        if (prefix.equals("/"))
            prefix = "";
        return prefix;
    }


    private void processSimpleUrlHandlerMappings(WarMachine war, SpringContext context, String urlPrefix, PathRepo paths)
    {
        List<BeanDefinition> defs = context.getBeansByClass(SpringConstants.SIMPLE_URL_HANDLER_CLASS);
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
        String className = ap.getParsedClass().getClassName();
        String beanName = getBeanName(className, ap);
        Annotation classMapping = ap.getClassAnnotation(SpringConstants.REQUEST_MAPPING_ANNO_CLASS);
        for (String classPrefix : getMappingUrls(urlPrefix, classMapping))
        {
            logger.debug("updated prefix from controller mapping: " + classPrefix);
            for (Method method : ap.getAnnotatedMethods(SpringConstants.REQUEST_MAPPING_ANNO_CLASS))
            {
                processAnnotatedControllerMethods(beanName, className, method, ap, war, context, classPrefix, paths);
            }
        }
    }


    private void processAnnotatedControllerMethods(
            String beanName, String className, Method method,
            AnnotationParser ap, WarMachine war, SpringContext context, String urlPrefix, PathRepo paths)
    {
        String methodName = method.getName();
        Map<String,RequestParameter> requestParams = processParameterAnnotations(method, ap);

        Annotation anno = ap.getMethodAnnotation(method, SpringConstants.REQUEST_MAPPING_ANNO_CLASS);
        for (String methodUrl : getMappingUrls(urlPrefix, anno))
        {
            for (HttpMethod reqMethod : getRequestMethods(anno))
            {
                paths.put(methodUrl, reqMethod, new SpringDestination(beanName, className, methodName, requestParams));
            }
        }
    }


    private Map<String,RequestParameter> processParameterAnnotations(Method method, AnnotationParser ap)
    {
        Map<String,RequestParameter> result = new TreeMap<String,RequestParameter>();
        Type[] methodParams = method.getArgumentTypes();
        for (int parmIdx = 0 ; parmIdx < methodParams.length ; parmIdx++)
        {
            Annotation paramAnno = ap.getParameterAnnotation(method, parmIdx, RequestParam.class.getName());
            if (paramAnno == null)
                continue;

            RequestParameter param = extractParameterFromAnnotationAlone(paramAnno, methodParams[parmIdx].toString());
            if (param == null)
                param = extractParameterFromAnnotationAndMethod(paramAnno, method, parmIdx);
            if (param == null)
            {
                logger.warn("unable to process annotation for parameter "
                            + parmIdx + " of method " + method.getName()
                            + ": " + paramAnno);
                continue;
            }

            result.put(param.getName(), param);
        }
        return result;
    }


    private String getBeanName(String className, AnnotationParser ap)
    {
        Annotation ctlAnno = ap.getClassAnnotation(SpringConstants.CONTROLLER_ANNO_CLASS);
        if (ctlAnno.getValue() == null)
            return BeanDefinition.classNameToBeanId(className);
        else
            return ctlAnno.getValue().asScalar().toString();
    }


    private List<String> getMappingUrls(String urlPrefix, Annotation requestMapping)
    {
        // note: called at both class and method level; either can be empty/missing
        //     - also, we can't be sure that the controller has the proper number of
        //       leading/trailing slashes on either mapping, so we'll remove and rebuild

        while (urlPrefix.endsWith("/"))
            urlPrefix = urlPrefix.substring(0, urlPrefix.length() - 1);

        if (requestMapping == null)
        {
            return Arrays.asList(urlPrefix);
        }

        ParamValue paramValue = requestMapping.getValue();
        if (paramValue == null)
        {
            return Arrays.asList(urlPrefix);
        }

        List<Object> mappings = paramValue.asListOfObjects();
        if (mappings.size() == 0)
        {
            return Arrays.asList(urlPrefix);
        }

        List<String> result = new ArrayList<String>(mappings.size());
        for (Object mapping0 : mappings)
        {
            String mapping = mapping0.toString();
            while (mapping.startsWith("/"))
                mapping = mapping.substring(1);

            result.add(urlPrefix + "/" + mapping);
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


    private RequestParameter extractParameterFromAnnotationAlone(Annotation anno, String type)
    {
        if (anno.getValue() == null)
            return null;

        String name = anno.getValue().asScalar().toString();
        String dflt = (anno.getParam("defaultValue") != null)
                    ? anno.getParam("defaultValue").asScalar().toString()
                    : "";
        int req0    = (anno.getParam("required") != null)
                    ? ((Integer)anno.getParam("required").asScalar()).intValue()
                    : 1;
        boolean req = (req0 != 0) ? true : false;
        return new RequestParameter(name, type, dflt, req);
    }


    private RequestParameter extractParameterFromAnnotationAndMethod(Annotation anno, Method method, int paramIndex)
    {
        int lvtIndex = paramIndex + 1;  // compensate for this
        LocalVariable[] lvt = method.getLocalVariableTable().getLocalVariableTable();
        if (lvt.length <= lvtIndex)
            return null;

        LocalVariable param = lvt[lvtIndex];
        return new RequestParameter(param.getName(), ClassUtil.internalNameToExternal(param.getSignature()));
    }
}
