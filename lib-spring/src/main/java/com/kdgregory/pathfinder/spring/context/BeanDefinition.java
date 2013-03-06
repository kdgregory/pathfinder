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

import net.sf.kdgcommons.lang.StringUtil;


/**
 *  Common functionality for bean definitions. Subclasses will provide additional
 *  functionality.
 */
public abstract class BeanDefinition
{
    /**
     *  The different ways that a bean can be defined. This may be used to control
     *  code that casts to a subclass.
     */
    public enum DefinitionType { XML, SCAN }


//----------------------------------------------------------------------------
//  Instance variables and constructor
//----------------------------------------------------------------------------

    private DefinitionType type;
    private String beanId;
    private String beanName;
    private String beanClass;

    protected BeanDefinition(DefinitionType type, String beanId, String beanName, String beanClass)
    {
        this.type = type;
        this.beanId = StringUtil.isBlank(beanId) ? classNameToBeanId(beanClass) : beanId;
        this.beanName = StringUtil.isBlank(beanName) ? this.beanId : beanName;
        this.beanClass = beanClass;
    }


//----------------------------------------------------------------------------
//  Common public methods
//----------------------------------------------------------------------------

    /**
     *  Returns the mechanism used to define this bean. Note that beans defined in
     *  XML are not inspected for additional class-level information.
     */
    public DefinitionType getDefinitionType()
    {
        return type;
    }


    /**
     *  Returns the ID of this bean. If the bean does not define its own ID,
     *  will return the bean's simple classname, with first letter lowercased.
     */
    public String getBeanId()
    {
        return beanId;
    }


    /**
     *  Returns the name of this bean. Defaults to the ID if the name is not
     *  specified.
     */
    public String getBeanName()
    {
        return beanName;
    }


    /**
     *  Returns the class of this bean.
     */
    public String getBeanClass()
    {
        return beanClass;
    }


    @Override
    public String toString()
    {
        return getClass().getSimpleName() + "[id=" + getBeanId() + ", class=" + getBeanClass() + "]";
    }


//----------------------------------------------------------------------------
//  Static utility methods
//----------------------------------------------------------------------------

    /**
     *  Converts a Java class name (fully-qualified or not) into a bean name
     *  by lowercasing the first letter. No attempt is made to deal with other
     *  beans that might have the same name.
     */
    public static String classNameToBeanId(String className)
    {
        String beanId = StringUtil.extractRightOfLast(className, ".");
        beanId = beanId.substring(0, 1).toLowerCase() + beanId.substring(1);
        return beanId;
    }
}
