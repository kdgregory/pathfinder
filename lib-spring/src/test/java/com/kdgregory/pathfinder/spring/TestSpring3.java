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

import java.util.Map;

import org.junit.Test;

import static org.junit.Assert.*;

import com.kdgregory.pathfinder.core.HttpMethod;
import com.kdgregory.pathfinder.spring.SpringDestination.RequestParameter;
import com.kdgregory.pathfinder.test.WarNames;


/**
 *  Tests annotation-driven configuration. There are multiple testcases, each
 *  exploring a different facet of the WAR (and reflecting the incremental
 *  implementation process).
 */
public class TestSpring3
extends AbstractSpringTestcase
{

    @Test
    public void testMappingOnMethodOnly() throws Exception
    {
        processWar(WarNames.SPRING3_BASIC);

        SpringDestination dest = (SpringDestination)pathRepo.get("/servlet/foo.html", HttpMethod.GET);
        assertNotNull("mapping exists", dest);
        assertEquals("bean",    "myController", dest.getBeanName());
        assertEquals("class",   "com.kdgregory.pathfinder.test.spring3.pkg1.ControllerA", dest.getClassName());
        assertEquals("method",  "getFoo", dest.getMethodName());
        assertEquals("toString","com.kdgregory.pathfinder.test.spring3.pkg1.ControllerA.getFoo()", dest.toString());
    }


    @Test
    public void testMappingOnClassAndMethod() throws Exception
    {
        processWar(WarNames.SPRING3_BASIC);

        SpringDestination dest1 = (SpringDestination)pathRepo.get("/servlet/B/bar.html", HttpMethod.GET);
        assertNotNull("GET mapping exists", dest1);
        assertEquals("GET bean",    "controllerB", dest1.getBeanName());
        assertEquals("GET class",   "com.kdgregory.pathfinder.test.spring3.pkg2.ControllerB", dest1.getClassName());
        assertEquals("GET method",  "getBar", dest1.getMethodName());
        assertEquals("toString","com.kdgregory.pathfinder.test.spring3.pkg2.ControllerB.getBar()", dest1.toString());

        SpringDestination dest2 = (SpringDestination)pathRepo.get("/servlet/B/baz.html", HttpMethod.POST);
        assertNotNull("POST mapping exists", dest2);
        assertEquals("POST bean",   "controllerB", dest2.getBeanName());
        assertEquals("POST class",  "com.kdgregory.pathfinder.test.spring3.pkg2.ControllerB", dest2.getClassName());
        assertEquals("POST method", "setBaz", dest2.getMethodName());
        assertEquals("toString","com.kdgregory.pathfinder.test.spring3.pkg2.ControllerB.setBaz()", dest2.toString());
    }


    @Test
    public void testMappingOnClassOnly() throws Exception
    {
        processWar(WarNames.SPRING3_BASIC);

        SpringDestination dest1 = (SpringDestination)pathRepo.get("/servlet/C", HttpMethod.GET);
        assertNotNull("mapping exists", dest1);
        assertEquals("bean",    "controllerC", dest1.getBeanName());
        assertEquals("class",   "com.kdgregory.pathfinder.test.spring3.pkg2.ControllerC", dest1.getClassName());
        assertEquals("method",  "getC", dest1.getMethodName());
        assertEquals("toString","com.kdgregory.pathfinder.test.spring3.pkg2.ControllerC.getC()", dest1.toString());
    }


    @Test
    public void testMappingWithPathVariable() throws Exception
    {
        processWar(WarNames.SPRING3_BASIC);

        SpringDestination est = (SpringDestination)pathRepo.get("/servlet/D/{id}", HttpMethod.GET);
        assertNotNull("mapping exists", est);
        assertEquals("bean",    "controllerD", est.getBeanName());
        assertEquals("class",   "com.kdgregory.pathfinder.test.spring3.pkg2.ControllerD", est.getClassName());
        assertEquals("method",  "getD", est.getMethodName());
        assertEquals("toString","com.kdgregory.pathfinder.test.spring3.pkg2.ControllerD.getD()", est.toString());
    }


    @Test
    public void testRequestMethodSpecification() throws Exception
    {
        processWar(WarNames.SPRING3_BASIC);

        // verify that we add all variants when method isn't specified

        SpringDestination dest1a = (SpringDestination)pathRepo.get("/servlet/foo.html", HttpMethod.GET);
        assertEquals("foo.html GET", "myController", dest1a.getBeanName());

        SpringDestination dest1b = (SpringDestination)pathRepo.get("/servlet/foo.html", HttpMethod.POST);
        assertEquals("foo.html POST", "myController", dest1b.getBeanName());

        SpringDestination dest1c = (SpringDestination)pathRepo.get("/servlet/foo.html", HttpMethod.PUT);
        assertEquals("foo.html PUT", "myController", dest1c.getBeanName());

        SpringDestination dest1d = (SpringDestination)pathRepo.get("/servlet/foo.html", HttpMethod.DELETE);
        assertEquals("foo.html DELETE", "myController", dest1d.getBeanName());

        // and that we don't add methods that aren't specified

        SpringDestination dest7 = (SpringDestination)pathRepo.get("/servlet/B/baz.html", HttpMethod.GET);
        assertNull("baz.html GET",  dest7);
    }


    @Test
    public void testRequestParameters() throws Exception
    {
        processWar(WarNames.SPRING3_BASIC);

        SpringDestination dest = (SpringDestination)pathRepo.get("/servlet/E1", HttpMethod.GET);
        Map<String,RequestParameter> params = dest.getParams();
        assertEquals("#/params", 4, params.size());

        assertEquals("param name: argle",   "argle",             params.get("argle").getName());
        assertEquals("param type: argle",   "java.lang.String",  params.get("argle").getType());
        assertEquals("default val: argle",  "",                  params.get("argle").getDefaultValue());
        assertTrue("required: argle",                            params.get("argle").isRequired());

        assertEquals("param name: bargle",  "bargle",            params.get("bargle").getName());
        assertEquals("param type: bargle",  "java.lang.Integer", params.get("bargle").getType());
        assertEquals("default val: bargle", "",                  params.get("bargle").getDefaultValue());
        assertFalse("required: bargle",                          params.get("bargle").isRequired());

        assertEquals("param name: wargle",  "wargle",            params.get("wargle").getName());
        assertEquals("paramtype : wargle",  "int",               params.get("wargle").getType());
        assertEquals("default val: wargle", "12",                params.get("wargle").getDefaultValue());
        assertFalse("required: wargle",                          params.get("wargle").isRequired());

        // added test: params are required by default
        assertEquals("param name: zargle",  "zargle",            params.get("zargle").getName());
        assertEquals("param type: zargle",  "java.lang.Integer", params.get("zargle").getType());
        assertEquals("default val: zargle", "",                  params.get("zargle").getDefaultValue());
        assertTrue("required: zargle",                           params.get("zargle").isRequired());
    }


    @Test
    public void testInferredRequestParameters() throws Exception
    {
        processWar(WarNames.SPRING3_BASIC);

        SpringDestination dest = (SpringDestination)pathRepo.get("/servlet/E2", HttpMethod.GET);
        Map<String,RequestParameter> params = dest.getParams();
        assertEquals("#/params", 2, params.size());

        assertEquals("param name: argle",   "argle",             params.get("argle").getName());
        assertEquals("param type: argle",   "java.lang.String",  params.get("argle").getType());
        assertEquals("default val: argle",  "",                  params.get("argle").getDefaultValue());
        assertTrue("required: argle",                            params.get("argle").isRequired());

        assertEquals("param name: bargle",  "bargle",            params.get("bargle").getName());
        assertEquals("param type: bargle",  "java.lang.Integer", params.get("bargle").getType());
        assertEquals("default val: bargle", "",                  params.get("bargle").getDefaultValue());
        assertTrue("required: bargle",                           params.get("bargle").isRequired());
    }
}
