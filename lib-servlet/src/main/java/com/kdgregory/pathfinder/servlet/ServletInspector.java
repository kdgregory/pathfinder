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

package com.kdgregory.pathfinder.servlet;

import java.util.Map;

import com.kdgregory.pathfinder.core.Inspector;
import com.kdgregory.pathfinder.core.PathRepo;
import com.kdgregory.pathfinder.core.PathRepo.Destination;
import com.kdgregory.pathfinder.core.WarMachine;

public class ServletInspector
implements Inspector
{
//----------------------------------------------------------------------------
//  Inspector
//----------------------------------------------------------------------------

    @Override
    public void inspect(WarMachine war, PathRepo paths)
    {
        addServlets(war, paths);
        addJSPandHTML(war, paths);
    }


//----------------------------------------------------------------------------
//  The Destinations that we support
//----------------------------------------------------------------------------

    private static class ServletDestination
    implements Destination
    {
        private String servletClass;

        public ServletDestination(String servletClass)
        {
            this.servletClass = servletClass;
        }

        @Override
        public String toString()
        {
            return servletClass;
        }
    }


    private static class JspDestination
    implements Destination
    {
        private String filename;

        public JspDestination(String filename)
        {
            this.filename = filename;
        }

        @Override
        public String toString()
        {
            return filename;
        }
    }


    private static class HtmlDestination
    implements Destination
    {
        private String filename;

        public HtmlDestination(String filename)
        {
            this.filename = filename;
        }

        @Override
        public String toString()
        {
            return filename;
        }
    }


//----------------------------------------------------------------------------
//  Internals
//----------------------------------------------------------------------------

    private void addServlets(WarMachine war, PathRepo paths)
    {
        for (Map.Entry<String,String> servlet : war.getServletMappings().entrySet())
        {
            paths.put(servlet.getKey(), new ServletDestination(servlet.getValue()));
        }
    }


    private void addJSPandHTML(WarMachine war, PathRepo paths)
    {
        for (String filename : war.getPublicFiles())
        {
            filename = filename.toLowerCase();
            if (filename.endsWith(".jsp"))
                paths.put(filename, new JspDestination(filename));
            if (filename.endsWith(".html"))
                paths.put(filename, new HtmlDestination(filename));
            if (filename.endsWith(".htm"))
                paths.put(filename, new HtmlDestination(filename));
        }
    }

}