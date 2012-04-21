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
import java.util.Map;

import org.w3c.dom.Document;

import org.junit.Test;
import static org.junit.Assert.*;

import net.sf.kdgcommons.io.IOUtil;
import net.sf.practicalxml.DomUtil;

import com.kdgregory.pathfinder.core.impl.WarMachineImpl;
import com.kdgregory.pathfinder.util.TestHelpers;


public class TestWarMachine
{

    @Test(expected=IllegalArgumentException.class)
    public void testInvalidWarfile() throws Exception
    {
        File warFile = IOUtil.createTempFile("testInvalidWarfile", 0);
        new WarMachineImpl(warFile);
    }


    @Test
    public void testGetWebXml() throws Exception
    {
        WarMachine machine = TestHelpers.createWarMachine("pathfinder-test-war-servlet.war");

        Document dom = machine.getWebXml();
        assertEquals("web-app", DomUtil.getLocalName(dom.getDocumentElement()));
    }


    @Test
    public void testGetServletMappings() throws Exception
    {
        WarMachine machine = TestHelpers.createWarMachine("pathfinder-test-war-servlet.war");

        Map<String,String> mappings = machine.getServletMappings();
        assertEquals("number of mappings", 2, mappings.size());
        assertEquals("mapping #1", "com.example.servlet.SomeServlet", mappings.get("/servlet"));
        assertEquals("mapping #2", "com.example.servlet.SomeServlet", mappings.get("/servlet2"));
    }
}
