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

import com.kdgregory.pathfinder.core.HttpMethod;
import com.kdgregory.pathfinder.core.PathRepo;
import com.kdgregory.pathfinder.core.WarMachine;
import com.kdgregory.pathfinder.core.impl.PathRepoImpl;
import com.kdgregory.pathfinder.servlet.ServletInspector;
import com.kdgregory.pathfinder.test.WarNames;
import com.kdgregory.pathfinder.util.TestHelpers;


/**
 *  This test class verifies that the Spring Inspector will find config files
 *  that use default location.
 */
public class TestSpring2Default
{
    private static WarMachine machine;
    private PathRepo pathRepo;

    @BeforeClass
    public static void loadWar()
    throws Exception
    {
        machine = TestHelpers.createWarMachine(WarNames.SPRING_DEFAULT);
    }


    @Before
    public void setUp()
    throws Exception
    {
        // we run the inspector chain here, assert its actions in the test methods
        pathRepo = new PathRepoImpl();
        new ServletInspector().inspect(machine, pathRepo);
        new SpringInspector().inspect(machine, pathRepo);
    }


//----------------------------------------------------------------------------
//  Testcases
//----------------------------------------------------------------------------

    // there's only one testcase, that checks both default locations ... and
    // we'll never get here if the defaults aren't implemented, because the
    // WAR is inpected at setup
    @Test
    public void testDefaultLocations() throws Exception
    {
        // this bean is defined in the servlet context
        SpringDestination dest1 = (SpringDestination)pathRepo.get("/servlet/foo.html", HttpMethod.GET);
        assertEquals("simpleControllerA", dest1.getBeanDefinition().getBeanName());

        // and this one is defined in the root
        SpringDestination dest2 = (SpringDestination)pathRepo.get("/servlet/bar.html", HttpMethod.POST);
        assertEquals("simpleControllerB", dest2.getBeanDefinition().getBeanName());
    }

}
