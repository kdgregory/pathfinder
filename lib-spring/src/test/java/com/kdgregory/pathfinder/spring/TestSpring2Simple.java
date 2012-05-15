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

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

import com.kdgregory.pathfinder.core.PathRepo;
import com.kdgregory.pathfinder.core.PathRepo.HttpMethod;
import com.kdgregory.pathfinder.core.WarMachine;
import com.kdgregory.pathfinder.servlet.ServletInspector;
import com.kdgregory.pathfinder.spring.SpringInspector.SpringDestination;
import com.kdgregory.pathfinder.spring.test.WarNames;
import com.kdgregory.pathfinder.util.TestHelpers;


/**
 *  This test looks for simple XML-based configuration, where there's a
 *  single config file attached to the servlet. All tests run using the
 *  same WAR, which is accessed via a static variable.
 */
public class TestSpring2Simple
{
    private static WarMachine machine;
    private PathRepo pathRepo;

    @BeforeClass
    public static void loadWar()
    throws Exception
    {
        machine = TestHelpers.createWarMachine(WarNames.SPRING2_SIMPLE);
    }


    @Before
    public void setUp()
    throws Exception
    {
        // SpringInspector relies on ServletInspector running first
        pathRepo = new PathRepo();
        new ServletInspector().inspect(machine, pathRepo);
    }


//----------------------------------------------------------------------------
//  Testcases
//----------------------------------------------------------------------------

    @Test
    public void testDispatcherServletMappingsRemoved() throws Exception
    {
        new SpringInspector().inspect(machine, pathRepo);

        // the original DispatcherServlet mapping should be gone
        assertEquals("/servlet/* removed", 0, pathRepo.get("/servlet/*").size());

        // but the MyServlet mapping should remain
        assertEquals("/servlet2 remains", 1, pathRepo.get("/servlet2").size());
    }


    @Test
    public void testSimpleUrlMappings() throws Exception
    {
        new SpringInspector().inspect(machine, pathRepo);

        SpringDestination dest1 = (SpringDestination)pathRepo.get("/servlet/foo.html", HttpMethod.GET);
        assertEquals("simpleControllerA", dest1.getBeanDefinition().getBeanName());

        SpringDestination dest2 = (SpringDestination)pathRepo.get("/servlet/bar.html", HttpMethod.GET);
        assertEquals("simpleControllerB", dest2.getBeanDefinition().getBeanName());
    }


}
