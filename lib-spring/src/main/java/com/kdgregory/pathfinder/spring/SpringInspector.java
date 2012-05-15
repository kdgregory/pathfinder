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
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.apache.log4j.Logger;

import com.kdgregory.pathfinder.core.Inspector;
import com.kdgregory.pathfinder.core.PathRepo;
import com.kdgregory.pathfinder.core.PathRepo.Destination;
import com.kdgregory.pathfinder.core.PathRepo.HttpMethod;
import com.kdgregory.pathfinder.core.WarMachine;
import com.kdgregory.pathfinder.core.WarMachine.ServletMapping;


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
//  The Destinations that we support
//----------------------------------------------------------------------------

    public static class SpringDestination
    implements Destination
    {
        private BeanDefinition beanDef;

        public SpringDestination(BeanDefinition beanDef)
        {
            this.beanDef = beanDef;
        }

        /** This method exists primarily for testing */
        public BeanDefinition getBeanDefinition()
        {
            return beanDef;
        }

        @Override
        public String toString()
        {
            return beanDef.getBeanClass();
        }
    }


//----------------------------------------------------------------------------
//  Inspector Implementation
//----------------------------------------------------------------------------

    @Override
    public void inspect(WarMachine war, PathRepo paths)
    {
        logger.info("SpringInspector started");
        List<ServletMapping> springMappings = extractSpringMappings(war, paths);
        logger.debug("extracted " + springMappings.size() + " Spring mappings");
        for (ServletMapping mapping : springMappings)
        {
            String urlPrefix = extractUrlPrefix(mapping.getUrlPattern());
            SpringContext context = new SpringContext(war, mapping.getInitParams().get("contextConfigLocation"));
            processSimpleUrlHandlerMapping(war, context, urlPrefix, paths);
        }
        logger.info("SpringInspector finished");
    }


//----------------------------------------------------------------------------
//  Internals
//----------------------------------------------------------------------------

    private List<ServletMapping> extractSpringMappings(WarMachine war, PathRepo paths)
    {
        List<ServletMapping> result = new ArrayList<ServletMapping>();
        for (ServletMapping servlet : war.getServletMappings())
        {
            if (servlet.getServletClass().equals("org.springframework.web.servlet.DispatcherServlet"))
            {
                result.add(servlet);
                paths.remove(servlet.getUrlPattern(), HttpMethod.ALL);
            }
        }
        return result;
    }


    private String extractUrlPrefix(String urlPattern)
    {
        int trimAt = urlPattern.lastIndexOf("/");
        return (trimAt > 0) ? urlPattern.substring(0, trimAt)
                            : urlPattern;
    }


    private void processSimpleUrlHandlerMapping(WarMachine war, SpringContext context, String urlPrefix, PathRepo paths)
    {
        List<BeanDefinition> defs = context.getBeansByClass("org.springframework.web.servlet.handler.SimpleUrlHandlerMapping");
        logger.debug("found " + defs.size() + " SimpleUrlHandlerMapping beans");

        for (BeanDefinition def : defs)
        {
            Properties mappings = def.getPropertyAsProperties("mappings");
            if ((mappings == null) || mappings.isEmpty())
            {
                logger.debug("SimpleUrlHandlerMapping bean " + def.getBeanName() + " has no mappings");
                continue;
            }

            logger.debug("SimpleUrlHandlerMapping bean " + def.getBeanName() + " has " + mappings.size() + " mappings");
            for (Map.Entry<Object,Object> mapping : mappings.entrySet())
            {
                String url = urlPrefix + mapping.getKey();
                String beanName = String.valueOf(mapping.getValue());
                BeanDefinition bean = context.getBean(beanName);
                logger.debug("mapped " + url + " to bean " + beanName);
                paths.put(url, new SpringDestination(bean));
            }
        }
    }
}
