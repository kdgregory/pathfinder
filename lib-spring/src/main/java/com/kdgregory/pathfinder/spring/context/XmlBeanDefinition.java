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

import java.io.IOException;
import java.io.StringReader;
import java.util.List;
import java.util.Properties;

import org.w3c.dom.Element;

import net.sf.kdgcommons.lang.StringUtil;
import net.sf.practicalxml.DomUtil;
import net.sf.practicalxml.xpath.XPathWrapperFactory;



/**
 *  Holds information extracted from an XML bean definition.
 */
public class XmlBeanDefinition
extends BeanDefinition
{
    private XPathWrapperFactory xpfact;
    private Element beanDef;

    public XmlBeanDefinition(XPathWrapperFactory xpf, Element def)
    {
        super(DefinitionType.XML, extractBeanId(def), extractBeanName(def), extractBeanClass(def));

        xpfact = xpf;
        beanDef = def;
    }


//----------------------------------------------------------------------------
//  Public methods
//----------------------------------------------------------------------------

    /**
     *  Returns the raw XML of the bean definition.
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


//----------------------------------------------------------------------------
//  Internals
//----------------------------------------------------------------------------

    private static String extractBeanId(Element def)
    {
        return def.getAttribute("id").trim();
    }


    private static String extractBeanName(Element def)
    {
        return def.getAttribute("name").trim();
    }


    private static String extractBeanClass(Element def)
    {
        return def.getAttribute("class").trim();
    }


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
}
