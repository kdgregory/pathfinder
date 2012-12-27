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

package com.kdgregory.pathfinder.core;

import java.io.File;
import java.io.InputStream;
import java.util.List;
import java.util.Set;

import org.w3c.dom.Element;

import org.junit.Test;
import static org.junit.Assert.*;

import org.apache.bcel.classfile.JavaClass;
import org.apache.commons.io.IOUtils;

import net.sf.kdgcommons.io.IOUtil;

import com.kdgregory.pathfinder.core.WarMachine.ServletMapping;
import com.kdgregory.pathfinder.core.impl.WarMachineImpl;
import com.kdgregory.pathfinder.test.WarNames;
import com.kdgregory.pathfinder.util.TestHelpers;


public class TestWarMachine
{

//----------------------------------------------------------------------------
//  Support Code
//----------------------------------------------------------------------------

    /**
     *  Asserts that the passed InputStream is a classfile, by looking for the magic
     *  number at its start. Closes the stream after making the assertion.
     */
    public static void assertClassFile(String message, InputStream in)
    throws Exception
    {
        assertEquals(message + ": byte 0", 0xCA, in.read());
        assertEquals(message + ": byte 1", 0xFE, in.read());
        assertEquals(message + ": byte 2", 0xBA, in.read());
        assertEquals(message + ": byte 3", 0xBE, in.read());
        in.close();
    }


//----------------------------------------------------------------------------
//  Testcases
//----------------------------------------------------------------------------

    @Test(expected=IllegalArgumentException.class)
    public void testInvalidWarfile() throws Exception
    {
        File warFile = IOUtil.createTempFile("testInvalidWarfile", 0);
        new WarMachineImpl(warFile);
    }


    @Test
    public void testXpathAgainstWebXml() throws Exception
    {
        WarMachine machine = TestHelpers.createWarMachine(WarNames.SERVLET);

        List<Element> mappings = machine.getWebXmlPath("//j2ee:servlet-mapping")
                                 .evaluate(machine.getWebXml(), Element.class);
        assertEquals("number of servlet mappings", 2, mappings.size());
    }


    @Test
    public void testXpathAgainstWebXml25() throws Exception
    {
        WarMachine machine = TestHelpers.createWarMachine(WarNames.SERVLET_25);

        List<Element> mappings = machine.getWebXmlPath("//j2ee:servlet-mapping")
                                 .evaluate(machine.getWebXml(), Element.class);
        assertEquals("number of servlet mappings", 2, mappings.size());
    }


    @Test
    public void testGetServletMappings() throws Exception
    {
        WarMachine machine = TestHelpers.createWarMachine(WarNames.SERVLET);

        List<ServletMapping> mappings = machine.getServletMappings();
        assertEquals("number of mappings", 2, mappings.size());

        ServletMapping mapping1 = mappings.get(0);
        assertEquals("mapping #0 path", "/servlet",
                     mapping1.getUrlPattern());
        assertEquals("mapping #0 class", "com.example.servlet.SomeServlet",
                     mapping1.getServletClass());
        assertEquals("mapping #0 param:foo", "bar",
                     mapping1.getInitParams().get("foo"));
        assertEquals("mapping #0 argle", "bargle",
                     mapping1.getInitParams().get("argle"));

        ServletMapping mapping2 = mappings.get(1);
        assertEquals("mapping #1 path", "/servlet2",
                     mapping2.getUrlPattern());
        assertEquals("mapping #1 class", "com.example.servlet.SomeServlet",
                     mapping2.getServletClass());
        assertEquals("mapping #1 param:foo", "bar",
                     mapping2.getInitParams().get("foo"));
        assertEquals("mapping #1 argle", "bargle",
                     mapping2.getInitParams().get("argle"));
    }


    @Test
    public void testFileLists() throws Exception
    {
        WarMachine machine = TestHelpers.createWarMachine(WarNames.SERVLET);

        // jar tvf WARFILE | grep -v '/$' | wc -l
        List<String> allFiles =  machine.getAllFiles();
        assertEquals("all files", 10, allFiles.size());
        assertTrue("all files contains /index.jsp",
                   allFiles.contains("/index.jsp"));

        List<String> publicFiles = machine.getPublicFiles();
        assertEquals("public files", 3, publicFiles.size());
        assertTrue("public files should contain /index.jsp",
                    publicFiles.contains("/index.jsp"));
        assertFalse("public files shouldn't contain /WEB-INF/views/hidden.jsp",
                    publicFiles.contains("/WEB-INF/views/hidden.jsp"));

        List<String> privateFiles = machine.getPrivateFiles();
        assertEquals("private files", 7, privateFiles.size());
        assertFalse("private files shouldn't contain /index.jsp",
                    privateFiles.contains("/index.jsp"));
        assertTrue("private files should contain /WEB-INF/views/hidden.jsp",
                    privateFiles.contains("/WEB-INF/views/hidden.jsp"));
    }


    @Test
    public void testGetFilesOnClasspath() throws Exception
    {
        WarMachine machine = TestHelpers.createWarMachine(WarNames.SERVLET);

        Set<String> files = machine.getFilesOnClasspath();
        assertTrue("searching for file under WEB-INF", files.contains("com/example/servlet/SomeServlet.class"));
        assertTrue("searching for file in JAR",        files.contains("net/sf/practicalxml/DomUtil.class"));
    }


    @Test
    public void testGetClassesInPackage() throws Exception
    {
        // note: this test might need to be updated if we use a later version
        //       of PracticalXml (if it adds classes to searched packages)

        WarMachine machine = TestHelpers.createWarMachine(WarNames.SERVLET);

        Set<String> clist1 = machine.getClassesInPackage("com.example.servlet", false);
        assertEquals("number of classes in WEB-INF, non-recursive",  1, clist1.size());
        assertTrue("searching for file in WEB-INF, non-recursive", clist1.contains("com.example.servlet.SomeServlet"));

        Set<String> clist2 = machine.getClassesInPackage("net.sf.practicalxml.xpath", false);
        assertEquals("number of classes in JAR, non-recursive",  13, clist2.size());
        assertTrue("searching for classes in JAR, non-recursive", clist2.contains("net.sf.practicalxml.xpath.XPathWrapper"));

        Set<String> clist3 = machine.getClassesInPackage("net.sf.practicalxml.xpath", true);
        assertEquals("number of classes in JAR, recursive",  17, clist3.size());
        assertTrue("searching for classes in JAR, recursive", clist3.contains("net.sf.practicalxml.xpath.XPathWrapper"));
        assertTrue("searching for classes in JAR, recursive", clist3.contains("net.sf.practicalxml.xpath.function.Constants"));
    }


    @Test
    public void testOpenFile() throws Exception
    {
        WarMachine machine = TestHelpers.createWarMachine(WarNames.SERVLET);

        InputStream in = machine.openFile("/index.jsp");
        assertNotNull("able to open public file", in);

        String content = IOUtils.toString(in);
        assertTrue("content looks like a JSP", content.contains("<html>"));
    }


    @Test
    public void testOpenFileDoesntThrowWithBogusFilename() throws Exception
    {
        WarMachine machine = TestHelpers.createWarMachine(WarNames.SERVLET);

        assertNull("bogus file returns null", machine.openFile("/bogus.bogus"));
    }


    @Test
    public void testOpenFileRequiresAbsolutePath() throws Exception
    {
        WarMachine machine = TestHelpers.createWarMachine(WarNames.SERVLET);

        assertNull("able to open with relative path", machine.openFile("index.jsp"));
    }


    @Test
    public void testOpenFileOnClasspath() throws Exception
    {
        WarMachine machine = TestHelpers.createWarMachine(WarNames.SERVLET);

        // test 1: a servlet class, which will be under WEB-INF and easily recognized
        //         note: relative filename

        InputStream in1 = machine.openClasspathFile("com/example/servlet/SomeServlet.class");
        assertNotNull("able to open classfile in WEB-INF/classes", in1);
        assertClassFile("servlet class", in1);

        // test 2: a classfile that lives in an included JAR (using absolute filename)

        InputStream in2 = machine.openClasspathFile("/net/sf/practicalxml/DomUtil.class");
        assertNotNull("able to open classfile in enclosed JAR", in2);
        assertClassFile("JAR'd class", in2);

        // test 3: a file that shouldn't appear on the classpath

        InputStream in3 = machine.openClasspathFile("web.xml");
        assertNull("shouldn't be able to open file not on classpath", in3);
    }


    @Test
    public void testLoadClass() throws Exception
    {
        WarMachine machine = TestHelpers.createWarMachine(WarNames.SERVLET);

        JavaClass c1 = machine.loadClass("com.example.servlet.SomeServlet");
        assertNotNull("found class in WEB-INF/classes", c1);
        assertEquals("name-check class in WEB-INF/classes",
                     "com.example.servlet.SomeServlet",
                     c1.getClassName());

        JavaClass c2 = machine.loadClass("net.sf.practicalxml.DomUtil");
        assertNotNull("found class in enclosed JAR", c2);
        assertEquals("name-check class in enclosed JAR",
                     "net.sf.practicalxml.DomUtil",
                     c2.getClassName());

        JavaClass c3 = machine.loadClass("java.lang.String");
        assertNull("class that shouldn't be in WAR", c3);
    }


}
