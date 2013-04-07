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

import java.util.Properties;


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
        this.beanId = beanId;
        this.beanName = beanName;
        this.beanClass = beanClass;
    }


//----------------------------------------------------------------------------
//  Public Methods
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
     *  Returns the ID of this bean. If the bean does not define its own ID, this
     *  will be a generated unique ID (that may or may not match what Spring will
     *  generate in a similar situation).
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


    /**
     *  Returns the named property value as a string, <code>null</code> if the
     *  property does not exist or cannot be converted to a string.
     *  <p>
     *  Note that scanned bean definitions do not have accessible properties.
     */
    public String getPropertyAsString(String name)
    {
        return null;
    }


    /**
     *  Returns the name of the bean referred to by the named property. Returns
     *  <code>null</code> if the property does not exist or is not a reference.
     *  <p>
     *  Note that scanned bean definitions do not have accessible properties.
     */
    public String getPropertyAsRefId(String name)
    {
        return null;
    }


    /**
     *  Returns the named property as a <code>Properties</code> object. Returns
     *  <code>null</code> if the property does not exist or cannot be converted.
     *  <p>
     *  Note that scanned bean definitions do not have accessible properties.
     */
    public Properties getPropertyAsProperties(String name)
    {
        return null;
    }


    @Override
    public String toString()
    {
        return getClass().getSimpleName() + "[id=" + getBeanId() + ", class=" + getBeanClass() + "]";
    }
}
