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

import org.junit.Test;
import static org.junit.Assert.*;

import com.kdgregory.pathfinder.core.HttpMethod;
import com.kdgregory.pathfinder.core.PathRepo;
import com.kdgregory.pathfinder.core.WarMachine;
import com.kdgregory.pathfinder.core.impl.PathRepoImpl;
import com.kdgregory.pathfinder.servlet.ServletInspector;
import com.kdgregory.pathfinder.test.WarNames;
import com.kdgregory.pathfinder.util.TestHelpers;


/**
 *  Tests annotation-driven configuration. There are multiple testcases, each
 *  exploring a different facet of the WAR (and reflecting the incremental
 *  implementation process).
 */
public class TestSpring3
{
    private static WarMachine machine;
    private PathRepo pathRepo;

//----------------------------------------------------------------------------
//  Support code
//----------------------------------------------------------------------------

    private void processWar(String warName)
    throws Exception
    {
        machine = TestHelpers.createWarMachine(warName);
        pathRepo = new PathRepoImpl();
        new ServletInspector().inspect(machine, pathRepo);
        new SpringInspector().inspect(machine, pathRepo);
    }


//----------------------------------------------------------------------------
//  Testcases
//----------------------------------------------------------------------------

    @Test
    public void testMappingOnMethodOnly() throws Exception
    {
        processWar(WarNames.SPRING3_BASIC);

        // the assertion will throw an NPE if the mapping wasn't found

        SpringDestination dest1 = (SpringDestination)pathRepo.get("/servlet/foo.html", HttpMethod.GET);
        assertNotNull("mapping exists", dest1);
        assertEquals("bean",    "controllerA", dest1.getBeanDefinition().getBeanName());
        assertEquals("class",   "com.kdgregory.pathfinder.test.spring3.pkg1.ControllerA", dest1.getClassName());
        assertEquals("method",  "getFoo", dest1.getMethodName());
    }


    @Test
    public void testMappingOnClassAndMethod() throws Exception
    {
        processWar(WarNames.SPRING3_BASIC);

        SpringDestination dest1 = (SpringDestination)pathRepo.get("/servlet/B/bar.html", HttpMethod.GET);
        assertNotNull("GET mapping exists", dest1);
        assertEquals("GET bean",    "controllerB", dest1.getBeanDefinition().getBeanName());
        assertEquals("GET class",   "com.kdgregory.pathfinder.test.spring3.pkg2.ControllerB", dest1.getClassName());
        assertEquals("GET method",  "getBar", dest1.getMethodName());

        SpringDestination dest2 = (SpringDestination)pathRepo.get("/servlet/B/baz.html", HttpMethod.POST);
        assertNotNull("POST mapping exists", dest2);
        assertEquals("POST bean",   "controllerB", dest2.getBeanDefinition().getBeanName());
        assertEquals("POST class",  "com.kdgregory.pathfinder.test.spring3.pkg2.ControllerB", dest2.getClassName());
        assertEquals("POST method", "setBaz", dest2.getMethodName());
    }


    @Test
    public void testMappingOnClassOnly() throws Exception
    {
        processWar(WarNames.SPRING3_BASIC);

        SpringDestination dest1 = (SpringDestination)pathRepo.get("/servlet/C", HttpMethod.GET);
        assertNotNull("mapping exists", dest1);
        assertEquals("bean",    "controllerC", dest1.getBeanDefinition().getBeanName());
        assertEquals("class",   "com.kdgregory.pathfinder.test.spring3.pkg2.ControllerC", dest1.getClassName());
        assertEquals("method",  "getC", dest1.getMethodName());
    }


    @Test
    public void testRequestMethodSpecification() throws Exception
    {
        processWar(WarNames.SPRING3_BASIC);

        // verify that we add all variants when method isn't specified

        SpringDestination dest1a = (SpringDestination)pathRepo.get("/servlet/foo.html", HttpMethod.GET);
        assertEquals("foo.html GET", "controllerA", dest1a.getBeanDefinition().getBeanName());

        SpringDestination dest1b = (SpringDestination)pathRepo.get("/servlet/foo.html", HttpMethod.POST);
        assertEquals("foo.html POST", "controllerA", dest1b.getBeanDefinition().getBeanName());

        SpringDestination dest1c = (SpringDestination)pathRepo.get("/servlet/foo.html", HttpMethod.PUT);
        assertEquals("foo.html PUT", "controllerA", dest1c.getBeanDefinition().getBeanName());

        SpringDestination dest1d = (SpringDestination)pathRepo.get("/servlet/foo.html", HttpMethod.DELETE);
        assertEquals("foo.html DELETE", "controllerA", dest1d.getBeanDefinition().getBeanName());

        // and that we don't add methods that aren't specified

        SpringDestination dest7 = (SpringDestination)pathRepo.get("/servlet/B/baz.html", HttpMethod.GET);
        assertNull("baz.html GET",  dest7);
    }


    @Test
    public void testDispatcherServletMappedToRoot() throws Exception
    {
        processWar(WarNames.SPRING3_ROOT);

        SpringDestination dest1 = (SpringDestination)pathRepo.get("/foo.html", HttpMethod.GET);
        assertNotNull("method-only mapping exists", dest1);
        assertEquals("method-only bean", "controllerA", dest1.getBeanDefinition().getBeanName());

        SpringDestination dest2 = (SpringDestination)pathRepo.get("/B/bar.html", HttpMethod.GET);
        assertNotNull("class/method GET exists", dest2);
        assertEquals("class/method GET bean", "controllerB", dest2.getBeanDefinition().getBeanName());

        SpringDestination dest3 = (SpringDestination)pathRepo.get("/B/baz.html", HttpMethod.POST);
        assertNotNull("class/method POST exists", dest3);
        assertEquals("class/method POST bean", "controllerB", dest3.getBeanDefinition().getBeanName());

        SpringDestination dest4 = (SpringDestination)pathRepo.get("/C", HttpMethod.GET);
        assertNotNull("class-only mapping exists", dest3);
        assertEquals("class-only bean", "controllerC", dest4.getBeanDefinition().getBeanName());
    }


    @Test
    public void testDispatcherServletMappedWithoutTrailingWildcard() throws Exception
    {
        processWar(WarNames.SPRING3_ROOT);

        SpringDestination dest1 = (SpringDestination)pathRepo.get("/servlet/foo.html", HttpMethod.GET);
        assertNotNull("method-only mapping exists", dest1);
        assertEquals("method-only bean", "controllerA", dest1.getBeanDefinition().getBeanName());

        SpringDestination dest2 = (SpringDestination)pathRepo.get("/servlet/B/bar.html", HttpMethod.GET);
        assertNotNull("class/method GET exists", dest2);
        assertEquals("class/method GET bean", "controllerB", dest2.getBeanDefinition().getBeanName());

        SpringDestination dest3 = (SpringDestination)pathRepo.get("/servlet/B/baz.html", HttpMethod.POST);
        assertNotNull("class/method POST exists", dest3);
        assertEquals("class/method POST bean", "controllerB", dest3.getBeanDefinition().getBeanName());

        SpringDestination dest4 = (SpringDestination)pathRepo.get("/servlet/C", HttpMethod.GET);
        assertNotNull("class-only mapping exists", dest3);
        assertEquals("class-only bean", "controllerC", dest4.getBeanDefinition().getBeanName());
    }
}
