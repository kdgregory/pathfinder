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

import java.util.Collections;
import java.util.Map;

import net.sf.kdgcommons.lang.StringUtil;

import com.kdgregory.pathfinder.core.Destination;
import com.kdgregory.pathfinder.core.InvocationOptions;
import com.kdgregory.pathfinder.spring.context.BeanDefinition;


public class SpringDestination
implements Destination
{
    private String beanId;
    private String className;
    private String methodName;
    private Map<String,RequestParameter> requestParams;


    /**
     *  Constructor for mappings read from an XML file; these won't have method
     *  information.
     *
     *  @param  beanDef         Bean definition; used to retrieve bean id, name,
     *                          and class.
     */
    public SpringDestination(BeanDefinition beanDef)
    {
        this.beanId = beanDef.getBeanId();
        this.className = beanDef.getBeanClass();
        this.methodName = "";
        this.requestParams = Collections.emptyMap();
    }


    /**
     *  Constructor for annotated classes, which includes handler method information.
     *
     *  @param  beanDef         Bean definition; used to retrieve bean id, name,
     *                          and class.
     *  @param  methodName      The name of the method invoked for this destination
     *  @param  requestParams   Any method parameters identified with @RequestParam
     */
    public SpringDestination(BeanDefinition beanDef, String methodName, Map<String,RequestParameter> requestParams)
    {
        this(beanDef);
        this.methodName  = methodName;
        this.requestParams = requestParams;
    }


    public String getBeanId()
    {
        return beanId;
    }


    public String getBeanClass()
    {
        return className;
    }


    public String getMethodName()
    {
        return methodName;
    }


    public Map<String,RequestParameter> getParams()
    {
        return requestParams;
    }

    @Override
    public boolean isDisplayed(Map<InvocationOptions,Boolean> options)
    {
        return true;
    }


    @Override
    public String toString()
    {
        if (StringUtil.isBlank(methodName))
            return getBeanClass();
        else
            return getBeanClass() + "." + getMethodName() + "()";
    }


    @Override
    public String toString(Map<InvocationOptions,Boolean> options)
    {
        String base = toString();
        if (! InvocationOptions.SHOW_REQUEST_PARAMS.isEnabled(options))
            return base;

        StringBuilder sb = new StringBuilder(1024)
                           .append(base)
                           .deleteCharAt(base.length() - 1);    // remove trailing ")"
        for (RequestParameter param : requestParams.values())
        {
            if (sb.charAt(sb.length() - 1) != '(')
                sb.append(", ");
            sb.append(param.getType()).append(" ").append(param.getName());
        }
        sb.append(")");
        return sb.toString();
    }


//----------------------------------------------------------------------------
//  Supporting classes
//----------------------------------------------------------------------------

    /**
     *  Extracted information from parameters marked with <code>@RequestParam</code>.
     */
    public static class RequestParameter
    {
        private String name;
        private String type;
        private String defaultValue;
        private boolean required;

        public RequestParameter(String name, String type, String defaultValue, boolean required)
        {
            this.name = name;
            this.type = type;
            this.defaultValue = defaultValue;
            this.required = required;
        }

        public RequestParameter(String name, String type)
        {
            this(name, type, "", true);
        }

        public String getName()
        {
            return name;
        }

        public String getType()
        {
            return type;
        }

        public String getDefaultValue()
        {
            return defaultValue;
        }

        public boolean isRequired()
        {
            return required;
        }

        @Override
        public String toString()
        {
            return "@RequestParam(value=\"" + name + "\""
                 + ", required=" + required
                 + ", defaultValue=\"" + defaultValue + "\""
                 + ") " + type + " " + name;
        }
    }
}