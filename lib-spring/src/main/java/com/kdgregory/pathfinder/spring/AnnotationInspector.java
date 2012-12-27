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
import java.util.List;
import java.util.Map;
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

import com.kdgregory.bcelx.classfile.Annotation;
import com.kdgregory.bcelx.classfile.Annotation.ParamValue;
import com.kdgregory.bcelx.parser.AnnotationParser;
import com.kdgregory.pathfinder.core.ClasspathScanner;
import com.kdgregory.pathfinder.core.HttpMethod;
import com.kdgregory.pathfinder.core.PathRepo;
import com.kdgregory.pathfinder.core.WarMachine;
import com.kdgregory.pathfinder.spring.SpringDestination.RequestParameter;

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


/**
 *  This class contains the logic for classpath scans and annotation-driven
 *  bean configuration.
 */
public class AnnotationInspector
{
    private Logger logger = Logger.getLogger(getClass());

    private WarMachine war;
    private SpringContext context;
    private PathRepo paths;


    public AnnotationInspector(WarMachine war, SpringContext context, PathRepo paths)
    {
        this.war = war;
        this.context = context;
        this.paths = paths;
    }

    
//----------------------------------------------------------------------------
//  Public Methods
//----------------------------------------------------------------------------

    /**
     *  This is the entry point for the class.
     */
    public void inspect(String urlPrefix)
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
            processAnnotatedController(filename, parsedClasses.get(filename), urlPrefix);
        }
    }


//----------------------------------------------------------------------------
//  Internals
//----------------------------------------------------------------------------

    private void processAnnotatedController(
            String filename, AnnotationParser ap, String urlPrefix)
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
                processAnnotatedControllerMethods(beanName, className, method, ap, classPrefix);
            }
        }
    }


    private void processAnnotatedControllerMethods(
            String beanName, String className, Method method,
            AnnotationParser ap, String urlPrefix)
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
