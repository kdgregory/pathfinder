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
import com.kdgregory.pathfinder.test.WarNames;


/**
 *  Spring has lots of choices for where to find its configuration files. This
 *  test tries to exercise them all.
 */
public class TestSpringConfigLocations
extends AbstractSpringTestcase
{
    @Test
    public void testDefaultLocations() throws Exception
    {
        processWar(WarNames.SPRING_DEFAULT_CONFIG);

        // this bean is defined in the servlet context
        SpringDestination dest1 = (SpringDestination)pathRepo.get("/servlet/foo", HttpMethod.GET);
        assertEquals("controllerA", dest1.getBeanId());
        assertEquals("com.kdgregory.pathfinder.test.spring2.ControllerA", dest1.getBeanClass());

        // and this one is defined in the root
        SpringDestination dest2 = (SpringDestination)pathRepo.get("/servlet/bar", HttpMethod.POST);
        assertEquals("controllerB", dest2.getBeanId());
        assertEquals("com.kdgregory.pathfinder.test.spring2.ControllerB", dest2.getBeanClass());
    }


    @Test
    public void testServletContextIsCombinedWithRoot() throws Exception
    {
        processWar(WarNames.SPRING_SPLIT_CONFIG);

        // this one is defined in the servlet context
        SpringDestination dest1 = (SpringDestination)pathRepo.get("/servlet/foo", HttpMethod.GET);
        assertEquals("controllerA", dest1.getBeanId());
        assertEquals("com.kdgregory.pathfinder.test.spring2.ControllerA", dest1.getBeanClass());

        // and this one is defined in the root context
        SpringDestination dest2 = (SpringDestination)pathRepo.get("/servlet/baz", HttpMethod.GET);
        assertEquals("controllerC", dest2.getBeanId());
        assertEquals("com.kdgregory.pathfinder.test.spring2.ControllerC", dest2.getBeanClass());

    }


    @Test
    public void testImportedContext() throws Exception
    {
        processWar(WarNames.SPRING_SPLIT_CONFIG);

        // this one is defined in the servlet context
        SpringDestination dest1 = (SpringDestination)pathRepo.get("/servlet/foo", HttpMethod.GET);
        assertEquals("controllerA", dest1.getBeanId());
        assertEquals("com.kdgregory.pathfinder.test.spring2.ControllerA", dest1.getBeanClass());

        // and this one is defined in the imported context
        SpringDestination dest2 = (SpringDestination)pathRepo.get("/servlet/bar", HttpMethod.GET);
        assertEquals("controllerB", dest2.getBeanId());
        assertEquals("com.kdgregory.pathfinder.test.spring2.ControllerB", dest2.getBeanClass());
    }

}
