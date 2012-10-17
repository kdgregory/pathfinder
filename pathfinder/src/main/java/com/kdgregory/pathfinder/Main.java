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

import java.io.File;
import java.util.Map;

import com.kdgregory.pathfinder.core.Destination;
import com.kdgregory.pathfinder.core.HttpMethod;
import com.kdgregory.pathfinder.core.Inspector;
import com.kdgregory.pathfinder.core.PathRepo;
import com.kdgregory.pathfinder.core.WarMachine;
import com.kdgregory.pathfinder.core.impl.PathRepoImpl;
import com.kdgregory.pathfinder.core.impl.WarMachineImpl;
import com.kdgregory.pathfinder.servlet.ServletInspector;
import com.kdgregory.pathfinder.spring.SpringInspector;
import com.kdgregory.pathfinder.util.InvocationOptions;


/**
 *  PathFinder driver program.
 */
public class Main
{
    public static void main(String[] argv)
    throws Exception
    {
        Map<InvocationOptions, Boolean> options = InvocationOptions.parseCli(argv);

        WarMachine machine = openWarOrDie(InvocationOptions.removeInvocationArguments(argv));
        PathRepo repo = new PathRepoImpl();
        applyInspectors(machine, repo, new ServletInspector(),
                                       new SpringInspector());
        dumpRepo(repo, options);
    }


    private static WarMachine openWarOrDie(String[] argv)
    {
        if (argv.length != 1)
        {
            System.err.println("invocation: Main WARFILE");
            System.exit(1);
        }

        WarMachine machine = null;
        try
        {
            machine = new WarMachineImpl(new File(argv[0]));
        }
        catch (Exception ex)
        {
            System.err.println("unable to open WARFILE: " + ex.getMessage());
            System.exit(1);
        }

        return machine;
    }


    private static void applyInspectors(WarMachine machine, PathRepo repo, Inspector... inspectors)
    {
        // this is a really simply method, but it lets us leverage the automatic
        // array creation feature of varargs to keep the code less cluttered
        for (Inspector inspector : inspectors)
            inspector.inspect(machine, repo);
    }


    private static void dumpRepo(PathRepo repo, Map<InvocationOptions, Boolean> options)
    {
        int urlWidth = 16;
        for (String url : repo)
            urlWidth = Math.max(urlWidth, url.length());

        String format = "%-" + urlWidth + "s  %-6s  %s";

        for (String url : repo)
        {
            Map<HttpMethod,Destination> destMap = repo.get(url);
            for (HttpMethod method : destMap.keySet())
            {
                Destination dest = destMap.get(method);
                System.out.println(
                        String.format(format, url, method, dest.toString(options)));
            }
        }
    }
}
