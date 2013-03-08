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

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import com.kdgregory.pathfinder.core.HttpMethod;
import com.kdgregory.pathfinder.spring.AbstractSpringTestcase;
import com.kdgregory.pathfinder.spring.SpringDestination;
import com.kdgregory.pathfinder.test.WarNames;


/**
 *  Tests various <code>servlet-mapping</code> URLs for the dispatcher.
 */
public class TestRootMappings
extends AbstractSpringTestcase
{
    @Test
    public void testDispatcherServletMappedToSlash() throws Exception
    {
        processWar(WarNames.SPRING_ROOT_DISP_1);

        SpringDestination dest = (SpringDestination)pathRepo.get("/foo", HttpMethod.GET);
        assertEquals("com.kdgregory.pathfinder.test.spring3.ControllerA", dest.getBeanClass());
    }


    @Test
    public void testDispatcherServletMappedToSlashStar() throws Exception
    {
        processWar(WarNames.SPRING_ROOT_DISP_2);

        SpringDestination dest = (SpringDestination)pathRepo.get("/foo", HttpMethod.GET);
        assertEquals("com.kdgregory.pathfinder.test.spring3.ControllerA", dest.getBeanClass());
    }

}
