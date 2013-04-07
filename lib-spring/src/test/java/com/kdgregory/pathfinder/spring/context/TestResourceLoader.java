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

import java.io.File;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Collections;

import org.w3c.dom.Document;
import org.xml.sax.InputSource;

import org.junit.After;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

import net.sf.kdgcommons.io.IOUtil;
import net.sf.practicalxml.ParseUtil;
import net.sf.practicalxml.xpath.XPathWrapper;

import com.kdgregory.pathfinder.core.WarMachine;
import com.kdgregory.pathfinder.spring.AbstractSpringTestcase;
import com.kdgregory.pathfinder.test.WarNames;
import com.kdgregory.pathfinder.util.TestHelpers;


// note: also tests BeanDefinition
public class TestResourceLoader
extends AbstractSpringTestcase
{
    private static WarMachine war;  // used by multiple tests, opened once
    private InputStream in;         // reused by every test, closed in @After


    @BeforeClass
    public static void init()
    throws Exception
    {
        war = TestHelpers.createWarMachine(WarNames.SPRING_RESOURCES);
    }


    @After
    public void tearDown()
    throws Exception
    {
        IOUtil.closeQuietly(in);
    }


//----------------------------------------------------------------------------
//  Support Code
//----------------------------------------------------------------------------


//----------------------------------------------------------------------------
//  Testcases
//----------------------------------------------------------------------------

    @Test
    public void testRuntimeClasspath() throws Exception
    {
        logger.info("testRuntimeClasspath()");

        ResourceLoader loader = new ResourceLoader(null);

        in = loader.getResourceAsStream("classpath:contexts/propContext.xml");
        assertNotNull("returned resource", in);

        Document dom = ParseUtil.parse(new InputSource(in));

        String testValue = new XPathWrapper("/b:beans/b:bean/b:property[@name='propAsRefId']/@ref")
                           .bindNamespace("b", "http://www.springframework.org/schema/beans")
                           .evaluateAsString(dom);
        assertEquals("fingerprint on loaded resource", "example", testValue);
    }


    @Test
    public void testWarClasspath() throws Exception
    {
        logger.info("testWarClasspath()");

        ResourceLoader loader = new ResourceLoader(war);

        // note difference in path
        in = loader.getResourceAsStream("classpath:loader/propContext.xml");
        assertNotNull("returned resource", in);

        Document dom = ParseUtil.parse(new InputSource(in));

        String testValue = new XPathWrapper("/b:beans/b:bean/b:property[@name='propAsRefId']/@ref")
                           .bindNamespace("b", "http://www.springframework.org/schema/beans")
                           .evaluateAsString(dom);
        assertEquals("fingerprint on loaded resource", "example", testValue);
    }


    @Test
    public void testFileRelativePath() throws Exception
    {
        logger.info("testFileRelativePath()");

        ResourceLoader loader = new ResourceLoader(null);

        in = loader.getResourceAsStream("file:src/test/resources/contexts/propContext.xml");
        assertNotNull("returned resource", in);

        Document dom = ParseUtil.parse(new InputSource(in));

        String testValue = new XPathWrapper("/b:beans/b:bean/b:property[@name='propAsRefId']/@ref")
                           .bindNamespace("b", "http://www.springframework.org/schema/beans")
                           .evaluateAsString(dom);
        assertEquals("fingerprint on loaded resource", "example", testValue);
    }


    @Test
    public void testFileAbsolutePath() throws Exception
    {
        logger.info("testFileAbsolutePath()");

        ResourceLoader loader = new ResourceLoader(null);

        File file = new File("src/test/resources/contexts/propContext.xml");
        in = loader.getResourceAsStream("file:" + file.getAbsolutePath());
        assertNotNull("returned resource", in);

        Document dom = ParseUtil.parse(new InputSource(in));

        String testValue = new XPathWrapper("/b:beans/b:bean/b:property[@name='propAsRefId']/@ref")
                           .bindNamespace("b", "http://www.springframework.org/schema/beans")
                           .evaluateAsString(dom);
        assertEquals("fingerprint on loaded resource", "example", testValue);
    }


    @Test
    public void testFileIgnoresWar() throws Exception
    {
        logger.info("testFileIgnoresWar()");

        ResourceLoader loader = new ResourceLoader(war);

        in = loader.getResourceAsStream("file:src/test/resources/contexts/propContext.xml");
        assertNotNull("returned resource", in);

        Document dom = ParseUtil.parse(new InputSource(in));

        String testValue = new XPathWrapper("/b:beans/b:bean/b:property[@name='propAsRefId']/@ref")
                           .bindNamespace("b", "http://www.springframework.org/schema/beans")
                           .evaluateAsString(dom);
        assertEquals("fingerprint on loaded resource", "example", testValue);
    }


    @Test
    public void testWarInternalAbsolutePathNoBasedir() throws Exception
    {
        logger.info("testWarInternalAbsolutePathNoBasedir()");

        ResourceLoader loader = new ResourceLoader(war);

        in = loader.getResourceAsStream("/WEB-INF/web.xml");
        assertNotNull("returned resource", in);

        Document dom = ParseUtil.parse(new InputSource(in));

        String testValue = new XPathWrapper("/web:web-app/web:servlet/web:servlet-name")
                           .bindNamespace("web", "http://java.sun.com/xml/ns/j2ee")
                           .evaluateAsString(dom);
        assertEquals("fingerprint on loaded resource", "Dispatcher", testValue);
    }


    @Test
    public void testWarInternalRelativePathNoBasedir() throws Exception
    {
        logger.info("testWarInternalRelativePathNoBasedir()");

        ResourceLoader loader = new ResourceLoader(war);

        // without a basedir, relative paths are relative to the root of the WAR
        in = loader.getResourceAsStream("WEB-INF/web.xml");
        assertNotNull("returned resource", in);

        Document dom = ParseUtil.parse(new InputSource(in));

        String testValue = new XPathWrapper("/web:web-app/web:servlet/web:servlet-name")
                           .bindNamespace("web", "http://java.sun.com/xml/ns/j2ee")
                           .evaluateAsString(dom);
        assertEquals("fingerprint on loaded resource", "Dispatcher", testValue);
    }


    @Test
    public void testWarInternalRelativePathWithBasedir() throws Exception
    {
        logger.info("testWarInternalRelativePathWithBasedir()");

        ResourceLoader loader = new ResourceLoader(war, "/WEB-INF");

        in = loader.getResourceAsStream("web.xml");
        assertNotNull("returned resource", in);

        Document dom = ParseUtil.parse(new InputSource(in));

        String testValue = new XPathWrapper("/web:web-app/web:servlet/web:servlet-name")
                           .bindNamespace("web", "http://java.sun.com/xml/ns/j2ee")
                           .evaluateAsString(dom);
        assertEquals("fingerprint on loaded resource", "Dispatcher", testValue);
    }


    @Test
    public void testWarInternalAbsolutePathWithBasedir() throws Exception
    {
        logger.info("testWarInternalAbsolutePathWithBasedir()");

        // note that absolute paths are really relative: see "FileSystemResource caveats",
        // section 5.7.3 of the Spring Reference Manual (for release 3.1.x)

        ResourceLoader loader = new ResourceLoader(war, "/WEB-INF");

        in = loader.getResourceAsStream("/web.xml");
        assertNotNull("returned resource", in);

        Document dom = ParseUtil.parse(new InputSource(in));

        String testValue = new XPathWrapper("/web:web-app/web:servlet/web:servlet-name")
                           .bindNamespace("web", "http://java.sun.com/xml/ns/j2ee")
                           .evaluateAsString(dom);
        assertEquals("fingerprint on loaded resource", "Dispatcher", testValue);
    }


    @Test
    public void testGetResourceReferencesNoWildcards() throws Exception
    {
        logger.info("testGetResourceReferencesNoWildcards()");

        assertEquals("null spec",
                     Collections.<String>emptyList(),
                     ResourceLoader.decomposeResourceReferences(null));
        assertEquals("empty string",
                     Collections.<String>emptyList(),
                     ResourceLoader.decomposeResourceReferences(""));
        assertEquals("single entry",
                     Arrays.asList("classpath:foo.xml"),
                     ResourceLoader.decomposeResourceReferences("classpath:foo.xml"));
        assertEquals("single entry, with whitespace",
                     Arrays.asList("classpath:foo.xml"),
                     ResourceLoader.decomposeResourceReferences("    classpath:foo.xml  "));
        assertEquals("multiple entries, comma-separated",
                     Arrays.asList("classpath:foo.xml", "/bar.xml"),
                     ResourceLoader.decomposeResourceReferences("    classpath:foo.xml,\n/bar.xml  "));
        assertEquals("multiple entries, whitespace-separated",
                     Arrays.asList("classpath:foo.xml", "/bar.xml"),
                     ResourceLoader.decomposeResourceReferences("    classpath:foo.xml\n/bar.xml  "));
        assertEquals("multiple entries, empty entries ignored",
                     Arrays.asList("classpath:foo.xml", "/bar.xml"),
                     ResourceLoader.decomposeResourceReferences("    classpath:foo.xml, ,/bar.xml  "));
    }

}
