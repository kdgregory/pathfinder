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

import java.io.IOException;
import java.io.StringReader;
import java.util.List;
import java.util.Properties;

import org.w3c.dom.Element;

import net.sf.kdgcommons.lang.StringUtil;
import net.sf.practicalxml.DomUtil;
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

        String value = propDef.getAttribute("value");
        if (StringUtil.isEmpty(value))
            value = xpfact.newXPath("b:value").evaluateAsString(propDef);

        return value;
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

        Properties ret = tryParsePropertiesFromValue(propDef);
        if (ret == null)
            ret = tryParsePropertiesFromProps(propDef);

        return ret;
    }



    @Override
    public String toString()
    {
        return getClass().getSimpleName() + "[id=" + getBeanName() + ", class=" + getBeanClass() + "]";
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


    private Properties tryParsePropertiesFromValue(Element propDef)
    {
        // single value is what's in the document, so try it first ... note that
        // we can't use a string eval, because it returns empty if the element
        // isn't present
        Element valueElem = xpfact.newXPath("b:value").evaluateAsElement(propDef);
        if (valueElem == null)
            return null;

        String value = DomUtil.getText(valueElem).trim();
        try
        {
            Properties ret = new Properties();
            ret.load(new StringReader(value));
            return ret;
        }
        catch (IOException ex)
        {
            // shouldn't happen; we'll let the null bubble up
            return null;
        }
    }


    private Properties tryParsePropertiesFromProps(Element propDef)
    {
        // FIXME - this will return an empty list if there's no "props" element
        List<Element> props = xpfact.newXPath("b:props/b:prop").evaluate(propDef, Element.class);

        Properties ret = new Properties();
        for (Element prop : props)
        {
            String propName  = prop.getAttribute("key");
            String propValue = prop.getTextContent().trim();
            ret.put(propName, propValue);
        }
        return ret;
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
