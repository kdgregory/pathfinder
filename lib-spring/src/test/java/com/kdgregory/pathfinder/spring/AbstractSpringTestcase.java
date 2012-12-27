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

import com.kdgregory.pathfinder.core.PathRepo;
import com.kdgregory.pathfinder.core.WarMachine;
import com.kdgregory.pathfinder.core.impl.PathRepoImpl;
import com.kdgregory.pathfinder.servlet.ServletInspector;
import com.kdgregory.pathfinder.util.TestHelpers;


/**
 *  Common functionality and data for the Spring tests.
 */
public abstract class AbstractSpringTestcase
{
    protected static WarMachine machine;
    protected PathRepo pathRepo;


//----------------------------------------------------------------------------
//  Support code
//----------------------------------------------------------------------------

    protected void processWar(String warName)
    throws Exception
    {
        machine = TestHelpers.createWarMachine(warName);
        pathRepo = new PathRepoImpl();
        new ServletInspector().inspect(machine, pathRepo);
        new SpringInspector().inspect(machine, pathRepo);
    }

}
