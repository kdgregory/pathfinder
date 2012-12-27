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

import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.apache.bcel.classfile.JavaClass;
import org.apache.log4j.Logger;

import net.sf.kdgcommons.lang.StringUtil;

import com.kdgregory.pathfinder.core.PathRepo;
import com.kdgregory.pathfinder.core.WarMachine;


/**
 *  This class contains the logic for inspecting beans defined in XML.
 */
public class BeanInspector
{
    private Logger logger = Logger.getLogger(getClass());

    private WarMachine war;
    private SpringContext context;
    private PathRepo paths;


    public BeanInspector(WarMachine war, SpringContext context, PathRepo paths)
    {
        this.war = war;
        this.context = context;
        this.paths = paths;
    }

//----------------------------------------------------------------------------
//  Public Methods
//----------------------------------------------------------------------------

    /**
     *  This is the entry point to the class.
     */
    public void inspect(String urlPrefix)
    {
        processSimpleUrlHandlerMappings(urlPrefix);
        processClassNameHandlerMappings(urlPrefix);
        processBeanNameHandlerMappings(urlPrefix);
    }


//----------------------------------------------------------------------------
//  Internals
//----------------------------------------------------------------------------

    private void processSimpleUrlHandlerMappings(String urlPrefix)
    {
        List<BeanDefinition> defs = context.getBeansByClass(SpringConstants.SIMPLE_URL_HANDLER_CLASS);
        logger.debug("found " + defs.size() + " SimpleUrlHandlerMapping beans");

        for (BeanDefinition def : defs)
        {
            Properties mappings = def.getPropertyAsProperties("mappings");
            if ((mappings == null) || mappings.isEmpty())
            {
                logger.warn("SimpleUrlHandlerMapping bean " + def.getBeanId() + " has no mappings");
                continue;
            }

            logger.debug("SimpleUrlHandlerMapping bean " + def.getBeanId() + " has " + mappings.size() + " mappings");
            for (Map.Entry<Object,Object> mapping : mappings.entrySet())
            {
                String url = urlPrefix + mapping.getKey();
                String beanId = String.valueOf(mapping.getValue());
                BeanDefinition bean = context.getBean(beanId);
                logger.debug("mapped " + url + " to bean " + beanId);
                paths.put(url, new SpringDestination(bean));
            }
        }
    }


    private void processClassNameHandlerMappings(String urlPrefix)
    {
        List<BeanDefinition> mappers = context.getBeansByClass(SpringConstants.CLASS_NAME_HANDLER_CLASS);
        if (mappers.size() == 0)
            return;

        logger.debug("found ControllerClassNameHandlerMapping; scanning for beans");

        BeanDefinition mapper = mappers.get(0);
        String mapperPrefix = mapper.getPropertyAsString("pathPrefix");
        if (! StringUtil.isBlank(mapperPrefix))
        {
            urlPrefix = urlPrefix + mapperPrefix.trim();
            if (! urlPrefix.endsWith("/"))
                urlPrefix += "/";
        }

        for (BeanDefinition bean : context.getBeans().values())
        {
            if (!isController(bean))
                continue;

            String beanClass = StringUtil.extractRightOfLast(bean.getBeanClass(), ".")
                               .toLowerCase();
            if (beanClass.endsWith("controller"))
                beanClass = StringUtil.extractLeftOfLast(beanClass, "controller");

            String url = (urlPrefix + beanClass).replace("//", "/");

            // FIXME - for multi-action controller, append "*"

            logger.debug("class-mapped bean: " + bean.getBeanId() + " = " + url);
            paths.put(url, new SpringDestination(bean));
        }
    }


    // FIXME - this should be the default, if no explicit mappings present
    //        - it should be called last, and will need a flag to indicate
    //          whether it should act even if no explicit mapping bean defined
    private void processBeanNameHandlerMappings(String urlPrefix)
    {
        List<BeanDefinition> beans = context.getBeansByClass(SpringConstants.BEAN_NAME_HANDLER_CLASS);
        if (beans.size() == 0)
        {
            logger.debug("did not find BeanNameUrlHandlerMapping");
            return;
        }
        logger.debug("found BeanNameUrlHandlerMapping; scanning for beans with explicit names");

        for (BeanDefinition bean : context.getBeans().values())
        {
            if (! isController(bean))
                continue;

            String beanName = bean.getBeanName();
            if (! beanName.startsWith("/"))
            {
                logger.warn("bean \"" + bean.getBeanId() + "\" implements Controller "
                            + "but does not have a name that can be used by BeanNameUrlHandlerMapping: "
                            + beanName);
                continue;
            }

            logger.debug("name-mapped bean: " + bean.getBeanId() + " = " + beanName);
            String url = urlPrefix + beanName;

            // FIXME - for multi-action controller, append "*"

            paths.put(url, new SpringDestination(bean));   }
    }


    /**
     *  Examines the passed bean's class data to determine whether it implements
     *  <code>Controller</code>. Will examine all reachable superclasses.
     */
    private boolean isController(BeanDefinition bean)
    {
        String className = bean.getBeanClass();
        while (! className.equals("java.lang.Object"))
        {
            JavaClass klass = war.loadClass(className);
            for (String intf : klass.getInterfaceNames())
            {
                if (intf.equals(SpringConstants.CONTROLLER_INTERFACE))
                    return true;
            }
            className = klass.getSuperclassName();
        }
        return false;
    }
}
