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
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.jar.JarInputStream;
import java.util.zip.ZipEntry;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import org.xml.sax.InputSource;

import org.apache.log4j.Logger;

import net.sf.kdgcommons.collections.CollectionUtil;
import net.sf.kdgcommons.io.IOUtil;
import net.sf.practicalxml.ParseUtil;
import net.sf.practicalxml.xpath.XPathWrapperFactory;
import net.sf.practicalxml.xpath.XPathWrapperFactory.CacheType;

import com.kdgregory.pathfinder.core.WarMachine;


/**
 *  The one and only non-mock implementation of the War Machine.
 */
public class WarMachineImpl
implements WarMachine
{

//----------------------------------------------------------------------------
//  Instance Variables and Constructor
//----------------------------------------------------------------------------

    private Logger logger = Logger.getLogger(getClass());

    private JarFile mappedWar;
    private Document webXml;
    private List<ServletMapping> servletMappings;

    private XPathWrapperFactory xpathFact = new XPathWrapperFactory(CacheType.SIMPLE)
                                            .bindNamespace("j2ee", "http://java.sun.com/xml/ns/j2ee");


    /**
     *  Opens the passed file and performs some basic sanity checks on it.
     *
     *  @throws IllegalArgumentException if the passed file doesn't exist or
     *          doesn't appear to be a WAR.
     */
    public WarMachineImpl(File warFile)
    {
        openFile(warFile);

        // if the file doesn't have web.xml, it's not a war, so failfast
        parseWebXml();
    }

    // the following methods are called by the ctor; broken out for readability

    private void openFile(File warFile)
    {
        try
        {
            logger.debug("opening file: " + warFile);
            mappedWar = new JarFile(warFile);
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
            logger.debug("looking for web.xml");
            JarEntry entry = mappedWar.getJarEntry("WEB-INF/web.xml");
            if (entry == null)
                throw new IllegalArgumentException("missing web.xml");

            logger.debug("parsing web.xml");
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


//----------------------------------------------------------------------------
//  WarMachine implementation
//----------------------------------------------------------------------------

    @Override
    public Document getWebXml()
    {
        return webXml;
    }


    @Override
    public List<ServletMapping> getServletMappings()
    {
        if (servletMappings == null)
            parseServletMappings();

        return Collections.unmodifiableList(servletMappings);
    }


    @Override
    public List<String> getAllFiles()
    {
        List<String> result = new ArrayList<String>(mappedWar.size());
        for (Enumeration<JarEntry> itx = mappedWar.entries() ; itx.hasMoreElements() ; )
        {
            JarEntry entry = itx.nextElement();
            String filename = entry.getName();
            if (! filename.endsWith("/"))
                result.add("/" + filename);
        }
        return result;
    }


    @Override
    public List<String> getPublicFiles()
    {
        List<String> filenames = getAllFiles();
        filenames = CollectionUtil.filter(filenames, "/WEB-INF.*", false);
        filenames = CollectionUtil.filter(filenames, "/META-INF.*", false);
        return filenames;
    }


    @Override
    public List<String> getPrivateFiles()
    {
        List<String> filenames = getAllFiles();
        List<String> result = new ArrayList<String>(filenames.size());
        result.addAll(CollectionUtil.filter(filenames, "/WEB-INF.*", true));
        result.addAll(CollectionUtil.filter(filenames, "/META-INF.*", true));
        return result;
    }


    @Override
    public InputStream openFile(String filename)
    throws IOException
    {
        if (!filename.startsWith("/"))
            return null;

        filename = filename.substring(1);
        JarEntry entry = mappedWar.getJarEntry(filename);
        if (entry == null)
            return null;

        return mappedWar.getInputStream(entry);
    }


    @Override
    public InputStream openClasspathFile(String filename)
    throws IOException
    {
        logger.debug("looking for " + filename + " on classpath");
        if (filename.startsWith("/"))
            filename = filename.substring(1);

        InputStream in = openFile("/WEB-INF/classes/" + filename);
        if (in != null)
        {
            logger.debug("found " + filename + " in WEB-INF/classes");
            return in;
        }

        for (String jarFile : getPrivateFiles())
        {
            if (!jarFile.startsWith("/WEB-INF/lib") || !jarFile.endsWith(".jar"))
                continue;

            logger.trace("looking for " + filename + " in " + jarFile);
            InputStream ii = openFile(jarFile);
            try
            {
                JarInputStream jj = new JarInputStream(ii);
                ZipEntry entry = null;
                while ((entry = jj.getNextEntry()) != null)
                {
                    if (entry.getName().equals(filename))
                    {
                        logger.debug("found " + filename + " in " + jarFile);
                        return jj;
                    }
                }
            }
            catch (Exception ex)
            {
                logger.debug("error processing " + jarFile, ex);
                // fall through to close the file and try the next
            }
            IOUtil.closeQuietly(ii);
        }

        logger.debug("unable to find " + filename + " on classpath");
        return null;
    }


//----------------------------------------------------------------------------
//  Internals
//----------------------------------------------------------------------------

    private void parseServletMappings()
    {
        servletMappings = new ArrayList<ServletMapping>();

        Map<String,Element> servletLookup = new HashMap<String,Element>();
        List<Element> servlets = xpathFact.newXPath("/j2ee:web-app/j2ee:servlet").evaluate(webXml, Element.class);
        logger.debug("found " + servlets.size() + " <servlet> entries");
        for (Element servlet : servlets)
        {
            String servletName = xpathFact.newXPath("j2ee:servlet-name").evaluateAsString(servlet);
            servletLookup.put(servletName, servlet);
        }

        List<Element> mappings = xpathFact.newXPath("/j2ee:web-app/j2ee:servlet-mapping").evaluate(webXml, Element.class);
        logger.debug("found " + mappings.size() + " <servlet-mapping> entries");
        for (Element mapping : mappings)
        {
            String servletName = xpathFact.newXPath("j2ee:servlet-name").evaluateAsString(mapping);
            String mappingUrl = xpathFact.newXPath("j2ee:url-pattern").evaluateAsString(mapping);
            Element servlet = servletLookup.get(servletName);
            if (servlet == null)
                logger.warn("<servlet-mapping> \"" + mappingUrl
                            + "\" does not have <servlet> entry");
            servletMappings.add(new ServletMappingImpl(mappingUrl, servlet));
        }
        Collections.sort(servletMappings);
    }


//----------------------------------------------------------------------------
//  Supporting classes
//----------------------------------------------------------------------------

    private class ServletMappingImpl
    implements ServletMapping
    {
        private String mappingUrl;
        private String servletName;
        private String servletClass;
        private Map<String,String> initParams = new HashMap<String,String>();

        public ServletMappingImpl(String mappingUrl, Element servlet)
        {
            this.mappingUrl = mappingUrl;
            this.servletName = xpathFact.newXPath("j2ee:servlet-name").evaluateAsString(servlet);
            this.servletClass = xpathFact.newXPath("j2ee:servlet-class").evaluateAsString(servlet);

            List<Element> params = xpathFact.newXPath("j2ee:init-param").evaluate(servlet, Element.class);
            for (Element param : params)
            {
                String paramName = xpathFact.newXPath("j2ee:param-name").evaluateAsString(param);
                String paramValue = xpathFact.newXPath("j2ee:param-value").evaluateAsString(param);
                initParams.put(paramName, paramValue);
            }
        }

        @Override
        public String getUrlPattern()
        {
            return mappingUrl;
        }

        @Override
        public String getServletName()
        {
            return servletName;
        }

        @Override
        public String getServletClass()
        {
            return servletClass;
        }

        @Override
        public Map<String,String> getInitParams()
        {
            return Collections.unmodifiableMap(initParams);
        }

        @Override
        public int compareTo(ServletMapping that)
        {
            return getUrlPattern().compareTo(that.getUrlPattern());
        }
    }
}
