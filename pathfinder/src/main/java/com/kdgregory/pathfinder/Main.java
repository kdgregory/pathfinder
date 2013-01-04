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
import java.io.PrintStream;
import java.util.Map;

import net.sf.kdgcommons.lang.UnreachableCodeException;

import com.kdgregory.pathfinder.core.Destination;
import com.kdgregory.pathfinder.core.HttpMethod;
import com.kdgregory.pathfinder.core.InvocationOptions;
import com.kdgregory.pathfinder.core.PathRepo;
import com.kdgregory.pathfinder.core.WarMachine;
import com.kdgregory.pathfinder.core.impl.PathRepoImpl;
import com.kdgregory.pathfinder.core.impl.WarMachineImpl;
import com.kdgregory.pathfinder.servlet.ServletInspector;
import com.kdgregory.pathfinder.spring.SpringInspector;


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
        new Main(options, machine, System.out).run();
    }


//----------------------------------------------------------------------------
//  Implementation -- instantiable so that we can test
//----------------------------------------------------------------------------

    private PrintStream out;
    private Map<InvocationOptions, Boolean> options;
    private WarMachine machine;
    private PathRepo repo;


    public Main(Map<InvocationOptions, Boolean> options, WarMachine machine, PrintStream out)
    {
        this.options = options;
        this.machine = machine;
        this.out = out;
        repo = new PathRepoImpl();
    }


    /**
     *  Invokes the inspectors and dumps the repository. You can call multiple
     *  times, but it's a bit pointless: the inspectors will overwrite whatever
     *  is already in the path repository.
     *  <p>
     *  Note: not only can this throw exceptions, it will also call System.exit
     *  directly.
     */
    public void run()
    throws Exception
    {
        new ServletInspector().inspect(machine, repo);
        new SpringInspector().inspect(machine, repo);
        dumpRepo();
    }


//----------------------------------------------------------------------------
//  Internals
//----------------------------------------------------------------------------

    private static WarMachine openWarOrDie(String[] warName)
    {
        if (warName.length != 1)
        {
            System.err.println();
            System.err.println("invocation: Main [OPTIONS] WARFILE");
            InvocationOptions.dump(System.err);
            System.exit(1);
        }

        try
        {
            return new WarMachineImpl(new File(warName[0]));
        }
        catch (Exception ex)
        {
            System.err.println("unable to open WARFILE: " + ex.getMessage());
            System.exit(1);
            throw new UnreachableCodeException("The compiler[ doesn't know that exit() doesn't return");
        }
    }


    private void dumpRepo()
    {
        int urlWidth = 16;
        for (String url : repo)
            urlWidth = Math.max(urlWidth, url.length());

        String format = "%-" + urlWidth + "s  %-6s  %s\n";

        for (String url : repo)
        {
            Map<HttpMethod,Destination> destMap = repo.get(url);
            for (HttpMethod method : destMap.keySet())
            {
                Destination dest = destMap.get(method);
                if (!dest.isDisplayed(options))
                    continue;
                out.format(format, url, method, dest.toString(options));
            }
        }
    }
}
