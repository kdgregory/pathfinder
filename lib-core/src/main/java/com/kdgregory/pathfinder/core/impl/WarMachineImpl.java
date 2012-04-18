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

package com.kdgregory.pathfinder.core.impl;

import java.io.File;
import java.io.InputStream;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import org.xml.sax.InputSource;

import net.sf.kdgcommons.io.IOUtil;
import net.sf.practicalxml.ParseUtil;
import net.sf.practicalxml.xpath.XPathWrapper;

import com.kdgregory.pathfinder.core.WarMachine;


/**
 *  The one and only non-mock implementation of the War Machine.
 */
public class WarMachineImpl
implements WarMachine
{
    // I'm sure this is defined somewhere in the J2EE API ...
    private final static String NS_SERVLET = "http://java.sun.com/xml/ns/j2ee";

//----------------------------------------------------------------------------
//  Instance Variables and Constructor
//----------------------------------------------------------------------------

    private ZipFile mappedWar;
    private Document webXml;
    private Map<String,String> servletMappings = new HashMap<String,String>();


    /**
     *  Opens the passed file and performs some basic sanity checks on it.
     *
     *  @throws IllegalArgumentException if the passed file doesn't exist or
     *          doesn't appear to be a WAR.
     */
    public WarMachineImpl(File warFile)
    {
        openFile(warFile);
        parseWebXml();
        parseServletMappings();
    }


//----------------------------------------------------------------------------
//  the following methods are called by the ctor; they're broken out because
//  each throws its own IllegalArgumentException
//----------------------------------------------------------------------------

    private void openFile(File warFile)
    {
        try
        {
            mappedWar = new ZipFile(warFile);
        }
        catch (Exception ex)
        {
            throw new IllegalArgumentException("unable to open: " + mappedWar, ex);
        }
    }


    private void parseWebXml()
    {
        InputStream entryStream = null;
        try
        {
            ZipEntry entry = mappedWar.getEntry("WEB-INF/web.xml");
            if (entry == null)
                throw new IllegalArgumentException("missing web.xml");

            entryStream = mappedWar.getInputStream(entry);
            webXml = ParseUtil.parse(new InputSource(entryStream));
        }
        catch (Exception ex)
        {
            if (ex instanceof IllegalArgumentException)
                throw (IllegalArgumentException)ex;
            throw new IllegalArgumentException("unable to extract web.xml", ex);
        }
        finally
        {
            IOUtil.closeQuietly(entryStream);
        }
    }


    private void parseServletMappings()
    {
        // prebuild all XPath; some of them get reused
        XPathWrapper xpServlet      = new XPathWrapper("/ns:web-app/ns:servlet")
                                      .bindNamespace("ns", NS_SERVLET);
        XPathWrapper xpServletName  = new XPathWrapper("ns:servlet-name")
                                      .bindNamespace("ns", NS_SERVLET);
        XPathWrapper xpServletClass = new XPathWrapper("ns:servlet-class")
                                     .bindNamespace("ns", NS_SERVLET);
        XPathWrapper xpMapping      = new XPathWrapper("/ns:web-app/ns:servlet-mapping")
                                      .bindNamespace("ns", NS_SERVLET);
        XPathWrapper xpMappingName  = new XPathWrapper("ns:servlet-name")
                                      .bindNamespace("ns", NS_SERVLET);
        XPathWrapper xpMappingUrl   = new XPathWrapper("ns:url-pattern")
                                      .bindNamespace("ns", NS_SERVLET);

        try
        {
            Map<String,String> servletLookup = new HashMap<String,String>();
            List<Element> servlets = xpServlet.evaluate(webXml, Element.class);
            for (Element servlet : servlets)
            {
                String servletName = xpServletName.evaluateAsString(servlet);
                String servletClass = xpServletClass.evaluateAsString(servlet);
                servletLookup.put(servletName, servletClass);
            }

            List<Element> mappings = xpMapping.evaluate(webXml, Element.class);
            for (Element mapping : mappings)
            {
                String servletName = xpMappingName.evaluateAsString(mapping);
                String mappingUrl = xpMappingUrl.evaluateAsString(mapping);
                String servletClass = servletLookup.get(servletName);
                if (servletClass == null)
                    throw new IllegalArgumentException("<servlet-mapping> \"" + servletName
                                                       + "\" does not have <servlet> entry");
                servletMappings.put(mappingUrl, servletClass);
            }
        }
        catch (Exception ex)
        {
            if (ex instanceof IllegalArgumentException)
                throw (IllegalArgumentException)ex;
            throw new IllegalArgumentException("unable to process servlet mappings", ex);
        }
    }


//----------------------------------------------------------------------------
//  WarMachine implementation
//----------------------------------------------------------------------------

    @Override
    public Document getWebXml()
    {
        return webXml;
    }


    @Override
    public Map<String,String> getServletMappings()
    {
        return Collections.unmodifiableMap(servletMappings);
    }


//----------------------------------------------------------------------------
//  Helper Methods
//----------------------------------------------------------------------------


}
