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

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;

import net.sf.kdgcommons.lang.StringUtil;

import com.kdgregory.pathfinder.core.HttpMethod;
import com.kdgregory.pathfinder.core.Inspector;
import com.kdgregory.pathfinder.core.PathRepo;
import com.kdgregory.pathfinder.core.WarMachine;
import com.kdgregory.pathfinder.core.WarMachine.ServletMapping;
import com.kdgregory.pathfinder.spring.context.SpringConstants;
import com.kdgregory.pathfinder.spring.context.SpringContext;
import com.kdgregory.pathfinder.spring.inspectors.AnnotationInspector;
import com.kdgregory.pathfinder.spring.inspectors.BeanInspector;


/**
 *  Finds any mappings to Spring's DispatcherServlet, and replaces them with mappings
 *  found by inspecting the various Spring context files. Supports both XML-driven and
 *  Annotation-driven configuration; does not support (at presnt) Code-driven config.
 */
public class SpringInspector
implements Inspector
{
    private Logger logger = Logger.getLogger(getClass());


//----------------------------------------------------------------------------
//  Inspector
//----------------------------------------------------------------------------

    @Override
    public void inspect(WarMachine war, PathRepo paths)
    {
        logger.info("SpringInspector started");

        SpringContext rootContext = loadRootContext(war);

        List<ServletMapping> springMappings = extractSpringMappings(war, paths);
        logger.debug("extracted " + springMappings.size() + " Spring mappings");
        for (ServletMapping mapping : springMappings)
        {
            String urlPrefix = StringUtil.extractLeftOfLast(mapping.getUrlPattern(), "/");
            String configLoc = mapping.getInitParams().get("contextConfigLocation");
            if (StringUtil.isBlank(configLoc))
            {
                configLoc = "/WEB-INF/" + mapping.getServletName() + "-servlet.xml";
            }

            logger.debug("processing mapping for \"" + urlPrefix + "\" from configFile " + configLoc);
            SpringContext context = new SpringContext(rootContext, war, configLoc);
            new BeanInspector(war, context, paths).inspect(urlPrefix);
            new AnnotationInspector(war, context, paths).inspect(urlPrefix);
        }
        logger.info("SpringInspector finished");
    }


//----------------------------------------------------------------------------
//  Internals
//----------------------------------------------------------------------------

    private SpringContext loadRootContext(WarMachine war)
    {
        // if there's no root context listener, we're done
        List<String> listeners = war.getWebXmlPath("/j2ee:web-app/j2ee:listener/j2ee:listener-class")
                                 .evaluateAsStringList(war.getWebXml());
        Set<String> listeners2 = new HashSet<String>(listeners);
        if (!listeners2.contains(SpringConstants.CLASS_CONTEXT_LISTENER))
        {
            logger.debug("no root context listener found");
            return null;
        }

        // look for an explicit config file location
        String contextLocation = war.getWebXmlPath("/j2ee:web-app/j2ee:context-param/"
                                              + "j2ee:param-name[text()='contextConfigLocation']/"
                                              + "../j2ee:param-value")
                                              .evaluateAsString(war.getWebXml());

        // and fallback to default
        if (StringUtil.isBlank(contextLocation))
        {
            logger.debug("context listener found, but no contextConfigLocation; using default");
            contextLocation = "/WEB-INF/applicationContext.xml";
        }

        logger.debug("root context location: " + contextLocation);
        return new SpringContext(war, contextLocation);
    }


    private List<ServletMapping> extractSpringMappings(WarMachine war, PathRepo paths)
    {
        List<ServletMapping> result = new ArrayList<ServletMapping>();
        for (ServletMapping servlet : war.getServletMappings())
        {
            if (servlet.getServletClass().equals(SpringConstants.CLASS_DISPATCHER_SERVLET))
            {
                result.add(servlet);
                paths.remove(servlet.getUrlPattern(), HttpMethod.ALL);
            }
        }
        return result;
    }
}
