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

import com.kdgregory.pathfinder.core.Destination;

public class SpringDestination
implements Destination
{
    private BeanDefinition beanDef;
    private String method;

    /**
     *  Constructor for mappings read from an XML file.
     */
    public SpringDestination(BeanDefinition beanDef)
    {
        this.beanDef = beanDef;
    }
    
    /**
     *  Constructor for annotated classes.
     */
    public SpringDestination(String className, String method)
    {
        this.beanDef = new BeanDefinition(className);
        this.method  = method;
    }

    public BeanDefinition getBeanDefinition()
    {
        return beanDef;
    }
    
    public String getClassName()
    {
        return beanDef.getBeanClass();
    }
    
    public String getMethodName()
    {
        return method;
    }

    @Override
    public String toString()
    {
        return beanDef.getBeanClass();
    }
}