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

package com.kdgregory.pathfinder.util;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import net.sf.kdgcommons.io.IOUtil;

import com.kdgregory.pathfinder.core.WarMachine;
import com.kdgregory.pathfinder.core.impl.WarMachineImpl;


/**
 *  This class contains static utility methods to support testing. It's
 *  easier to drop it here than to create a test JAR.
 */
public abstract class TestHelpers
{
    /**
     *  Extracts a WAR that's stored as a resource on the classpath,
     *  and puts it in a temporary file.
     */
    public static WarMachine createWarMachine(String warName)
    throws IOException
    {
        InputStream in = null;
        try
        {
            in = TestHelpers.class.getClassLoader().getResourceAsStream(warName);
            if (in == null)
                throw new IllegalArgumentException("couldn't find " + warName + " on classpath");
            File warFile = IOUtil.createTempFile(in, warName);
            return new WarMachineImpl(warFile);
        }
        finally
        {
            IOUtil.closeQuietly(in);
        }
    }
}
