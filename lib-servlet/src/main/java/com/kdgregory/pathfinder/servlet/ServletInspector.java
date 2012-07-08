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

import org.apache.log4j.Logger;

import com.kdgregory.pathfinder.core.Inspector;
import com.kdgregory.pathfinder.core.PathRepo;
import com.kdgregory.pathfinder.core.WarMachine;
import com.kdgregory.pathfinder.core.WarMachine.ServletMapping;


public class ServletInspector
implements Inspector
{
    private Logger logger = Logger.getLogger(getClass());

//----------------------------------------------------------------------------
//  Inspector
//----------------------------------------------------------------------------

    @Override
    public void inspect(WarMachine war, PathRepo paths)
    {
        logger.info("ServletInspector started");
        addServlets(war, paths);
        addJSPandHTML(war, paths);
        logger.info("ServletInspector finished");
    }


//----------------------------------------------------------------------------
//  Internals
//----------------------------------------------------------------------------

    private void addServlets(WarMachine war, PathRepo paths)
    {
        for (ServletMapping servlet : war.getServletMappings())
        {
            String servletUrl = servlet.getUrlPattern();
            String servletClass = servlet.getServletClass();

            logger.debug("added servlet: " + servletUrl + " => " + servletClass);
            paths.put(servletUrl, new ServletDestination(servletClass));
        }
    }


    private void addJSPandHTML(WarMachine war, PathRepo paths)
    {
        for (String filename : war.getPublicFiles())
        {
            filename = filename.toLowerCase();
            if (filename.endsWith(".jsp"))
            {
                logger.debug("added JSP: " + filename);
                paths.put(filename, new JspDestination(filename));
            }
            if (filename.endsWith(".html") || filename.endsWith(".htm"))
            {
                logger.debug("added static HTML: " + filename);
                paths.put(filename, new HtmlDestination(filename));
            }
        }
    }

}
