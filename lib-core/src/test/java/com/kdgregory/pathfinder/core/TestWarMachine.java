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
import java.util.Map;

import org.w3c.dom.Document;

import org.junit.Test;
import static org.junit.Assert.*;

import org.apache.commons.io.IOUtils;

import net.sf.kdgcommons.io.IOUtil;
import net.sf.practicalxml.DomUtil;

import com.kdgregory.pathfinder.core.impl.WarMachineImpl;
import com.kdgregory.pathfinder.util.TestHelpers;


public class TestWarMachine
{
    private final static String TEST_WAR = "pathfinder-test-war-servlet.war";


    @Test(expected=IllegalArgumentException.class)
    public void testInvalidWarfile() throws Exception
    {
        File warFile = IOUtil.createTempFile("testInvalidWarfile", 0);
        new WarMachineImpl(warFile);
    }


    @Test
    public void testGetWebXml() throws Exception
    {
        WarMachine machine = TestHelpers.createWarMachine(TEST_WAR);

        Document dom = machine.getWebXml();
        assertEquals("web-app", DomUtil.getLocalName(dom.getDocumentElement()));
    }


    @Test
    public void testGetServletMappings() throws Exception
    {
        WarMachine machine = TestHelpers.createWarMachine(TEST_WAR);

        Map<String,String> mappings = machine.getServletMappings();
        assertEquals("number of mappings", 2, mappings.size());
        assertEquals("mapping #1", "com.example.servlet.SomeServlet", mappings.get("/servlet"));
        assertEquals("mapping #2", "com.example.servlet.SomeServlet", mappings.get("/servlet2"));
    }


    @Test
    public void testFileLists() throws Exception
    {
        WarMachine machine = TestHelpers.createWarMachine(TEST_WAR);

        // jar tvf WARFILE | grep -v '/$' | wc -l
        List<String> allFiles =  machine.getAllFiles();
        assertEquals("all files", 9, allFiles.size());
        assertTrue("all files contains /index.jsp",
                   allFiles.contains("/index.jsp"));

        List<String> publicFiles = machine.getPublicFiles();
        assertEquals("public files", 3, publicFiles.size());
        assertTrue("public files should contain /index.jsp",
                    publicFiles.contains("/index.jsp"));
        assertFalse("public files shouldn't contain /WEB-INF/views/hidden.jsp",
                    publicFiles.contains("/WEB-INF/views/hidden.jsp"));

        List<String> privateFiles = machine.getPrivateFiles();
        assertEquals("private files", 6, privateFiles.size());
        assertFalse("private files shouldn't contain /index.jsp",
                    privateFiles.contains("/index.jsp"));
        assertTrue("private files should contain /WEB-INF/views/hidden.jsp",
                    privateFiles.contains("/WEB-INF/views/hidden.jsp"));
    }


    @Test
    public void testOpenFile() throws Exception
    {
        WarMachine machine = TestHelpers.createWarMachine(TEST_WAR);

        InputStream in = machine.openFile("/index.jsp");
        assertNotNull("able to open public file", in);

        String content = IOUtils.toString(in);
        assertTrue("content looks like a JSP", content.contains("<html>"));
    }


    @Test
    public void testOpenFileDoesntThrowWithBogusFilename() throws Exception
    {
        WarMachine machine = TestHelpers.createWarMachine(TEST_WAR);

        assertNull("bogus file returns null", machine.openFile("/bogus.bogus"));
    }


    @Test
    public void testOpenFileRequiresAbsolutePath() throws Exception
    {
        WarMachine machine = TestHelpers.createWarMachine(TEST_WAR);

        assertNull("able to open with relative path", machine.openFile("index.jsp"));
    }


}
