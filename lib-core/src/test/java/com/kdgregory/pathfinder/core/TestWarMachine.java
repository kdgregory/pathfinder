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
import java.util.Map;

import org.w3c.dom.Document;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

import net.sf.kdgcommons.io.IOUtil;
import net.sf.practicalxml.junit.DomAsserts;
import net.sf.practicalxml.xpath.XPathWrapper;

import com.kdgregory.pathfinder.core.impl.WarMachineImpl;


public class TestWarMachine
{
    private File warFile;
    private WarMachine machine;

    @Before
    public void setUp()
    throws Exception
    {
        String warName = "pathfinder-test-war-servlet.war";
        InputStream simpleWar = getClass().getClassLoader().getResourceAsStream(warName);
        warFile = IOUtil.createTempFile(simpleWar, "TestWarMachine");
        IOUtil.closeQuietly(simpleWar);

        machine = new WarMachineImpl(warFile);
    }


    @After
    public void tearDown()
    throws Exception
    {
        if (warFile != null)
            warFile.delete();
    }


//----------------------------------------------------------------------------
//  TestCases
//----------------------------------------------------------------------------

    @Test(expected=IllegalArgumentException.class)
    public void testInvalidWarfile() throws Exception
    {
        warFile = IOUtil.createTempFile("testInvalidWarfile", 0);
        machine = new WarMachineImpl(warFile);
    }


    @Test
    public void testGetWebXml() throws Exception
    {
        Document dom = machine.getWebXml();

        // need to use XPathWrapper because of the namespace
        XPathWrapper xpath = new XPathWrapper("/ns:web-app/ns:servlet/ns:servlet-name")
                             .bindNamespace("ns", "http://java.sun.com/xml/ns/j2ee");
        DomAsserts.assertEquals("contains one servlet", "MyServlet", dom, xpath);
    }


    @Test
    public void testGetServletMappings() throws Exception
    {
        Map<String,String> mappings = machine.getServletMappings();
        assertEquals("number of mappings", 2, mappings.size());
        assertEquals("mapping #1", "com.example.servlet.SomeServlet", mappings.get("/servlet"));
        assertEquals("mapping #2", "com.example.servlet.SomeServlet", mappings.get("/servlet2"));
    }
}
