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

package com.kdgregory.pathfinder.spring.inspectors;

import org.junit.Test;
import static org.junit.Assert.*;

import com.kdgregory.pathfinder.core.HttpMethod;
import com.kdgregory.pathfinder.spring.AbstractSpringTestcase;
import com.kdgregory.pathfinder.spring.SpringDestination;
import com.kdgregory.pathfinder.test.WarNames;


/**
 *  This test looks for explicitly configured URL mappings.
 */
public class TestExplicitMappings
extends AbstractSpringTestcase
{
    @Test
    public void testDispatcherServletMappingsRemoved() throws Exception
    {
        processWar(WarNames.SPRING_SIMPLE);

        // the original DispatcherServlet mapping should be gone
        assertEquals("/servlet/* removed", 0, pathRepo.get("/servlet/*").size());

        // but the MyServlet mapping should remain
        assertEquals("/servlet2 remains", 1, pathRepo.get("/servlet2").size());
    }


    @Test
    public void testSimpleUrlMappings() throws Exception
    {
        processWar(WarNames.SPRING_SIMPLE);

        SpringDestination dest1 = (SpringDestination)pathRepo.get("/servlet/foo", HttpMethod.GET);
        assertEquals("controllerA", dest1.getBeanId());
        assertEquals("com.kdgregory.pathfinder.test.spring2.ControllerA", dest1.getBeanClass());

        SpringDestination dest2 = (SpringDestination)pathRepo.get("/servlet/bar", HttpMethod.GET);
        assertEquals("controllerB", dest2.getBeanId());
        assertEquals("com.kdgregory.pathfinder.test.spring2.ControllerB", dest2.getBeanClass());
    }


    @Test
    public void testBeanNameUrlMappings() throws Exception
    {
        processWar(WarNames.SPRING_BEAN_NAME);

        // index.jsp + 2 Spring mappings
        assertEquals("number of mapped URLs", 3, pathRepo.urlCount());

        SpringDestination dest1 = (SpringDestination)pathRepo.get("/servlet/foo", HttpMethod.GET);
        assertEquals("controllerA", dest1.getBeanId());
        assertEquals("com.kdgregory.pathfinder.test.spring2.ControllerA", dest1.getBeanClass());

        SpringDestination dest2 = (SpringDestination)pathRepo.get("/servlet/bar", HttpMethod.GET);
        assertEquals("controllerB", dest2.getBeanId());
        assertEquals("com.kdgregory.pathfinder.test.spring2.ControllerB", dest2.getBeanClass());
    }


    @Test
    public void testClassNameUrlMappings() throws Exception
    {
        processWar(WarNames.SPRING_CLASS_NAME);

        // index.jsp + 2 Spring mappings
        assertEquals("number of mapped URLs", 4, pathRepo.urlCount());

        SpringDestination dest1 = (SpringDestination)pathRepo.get("/servlet/test/foo", HttpMethod.GET);
        assertEquals("controllerA", dest1.getBeanId());
        assertEquals("com.kdgregory.pathfinder.test.spring2.FooController", dest1.getBeanClass());

        SpringDestination dest2 = (SpringDestination)pathRepo.get("/servlet/test/bar", HttpMethod.GET);
        assertEquals("controllerB", dest2.getBeanId());
        assertEquals("com.kdgregory.pathfinder.test.spring2.BarController", dest2.getBeanClass());

        SpringDestination dest3 = (SpringDestination)pathRepo.get("/servlet/test/bazctrl", HttpMethod.GET);
        assertEquals("controllerC", dest3.getBeanId());
        assertEquals("com.kdgregory.pathfinder.test.spring2.BazCtrl", dest3.getBeanClass());
    }
}
