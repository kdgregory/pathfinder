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

package com.kdgregory.pathfinder;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.lang.reflect.Field;

import org.apache.log4j.Logger;
import org.junit.Ignore;
import org.junit.Test;

import com.kdgregory.pathfinder.core.InvocationOptions;
import com.kdgregory.pathfinder.core.WarMachine;
import com.kdgregory.pathfinder.test.WarNames;
import com.kdgregory.pathfinder.util.TestHelpers;


 @Ignore
public class TestCoverage
{
    private Logger logger = Logger.getLogger(getClass());

    @Test
    public void testCoverage() throws Exception
    {
        for (Field namedef : WarNames.class.getFields())
        {
            String warName = String.valueOf(namedef.get(null));
            logger.info("running " + warName);

            WarMachine machine = TestHelpers.createWarMachine(warName);
            PrintStream out = new PrintStream(new ByteArrayOutputStream());
            new Main(InvocationOptions.parseCli(""), machine, out).run();
        }
    }
}
