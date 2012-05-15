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

import java.util.List;
import java.util.Properties;

import org.w3c.dom.Element;

import net.sf.practicalxml.xpath.XPathWrapperFactory;


/**
 *  Holds information extracted from the bean definition. The amount of
 *  information available depends on how the bean was defined: wehether
 *  by XML or a component scan. All beans will have at least name and
 *  class.
 */
public class BeanDefinition
{
    // this factory is shared with all other definitions from the context
    private XPathWrapperFactory xpfact;

    private String beanName;
    private String beanClass;
    private Element beanDef;


    /**
     *  Called for beans defined in XML; will extract information from the
     *  XML subtree, and retain a reference to the tree.
     */
    public BeanDefinition(XPathWrapperFactory xpf, Element def)
    {
        xpfact = xpf;
        beanName = def.getAttribute("id").trim();
        beanClass = def.getAttribute("class").trim();
        beanDef = def;
    }


    /**
     *  Returns the name of this bean: the <code>id</code> attribute for
     *  XML-configured beans, ?? for annotation-configured beans.
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
     *  For XML-defined beans, returns the raw XML of the bean definition.
     *  @return
     */
    public Element getBeanDef()
    {
        return beanDef;
    }


    /**
     *  Returns the named property value as a string. Returns <code>null</code>
     *  if the named property does not exist or cannot be converted to a string.
     */
    public String getPropertyAsString(String name)
    {
        Element propDef = getPropertyDefinition(name);
        if (propDef == null)
            return null;

        return propDef.getAttribute("value");
    }


    /**
     *  Returns the name of the bean referred to by the named property. Returns
     *  <code>null</code> if the property does not exist or is not a reference.
     */
    public String getPropertyAsRefId(String name)
    {
        Element propDef = getPropertyDefinition(name);
        if (propDef == null)
            return null;

        return propDef.getAttribute("ref");
    }


    /**
     *  Returns the named property as a <code>Properties</code> object. Returns
     *  <code>null</code> if the property does not exist or cannot be converted.
     */
    public Properties getPropertyAsProperties(String name)
    {
        Element propDef = getPropertyDefinition(name);
        if (propDef == null)
            return null;

        Properties ret = new Properties();
        List<Element> props = xpfact.newXPath("b:props/b:prop").evaluate(propDef, Element.class);
        for (Element prop : props)
        {
            String propName  = prop.getAttribute("key");
            String propValue = prop.getTextContent().trim();
            ret.put(propName, propValue);
        }

        return ret;
    }


//----------------------------------------------------------------------------
//  Internals
//----------------------------------------------------------------------------

    private Element getPropertyDefinition(String name)
    {
        // FIXME - consider binding a variable here
        Element propDef = xpfact.newXPath("b:property[@name='" + name+ "']")
                          .evaluateAsElement(getBeanDef());
        return propDef;
    }

}
