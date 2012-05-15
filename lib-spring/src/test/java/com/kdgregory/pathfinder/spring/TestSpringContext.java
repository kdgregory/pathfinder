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
import java.util.Properties;

import org.junit.Test;
import static org.junit.Assert.*;

import org.apache.log4j.Logger;

import com.kdgregory.pathfinder.core.WarMachine;
import com.kdgregory.pathfinder.spring.test.WarNames;
import com.kdgregory.pathfinder.util.TestHelpers;


// note: also tests BeanDefinition
public class TestSpringContext
{
    private Logger logger = Logger.getLogger(getClass());

    @Test
    public void testGetBeansFromSimpleContext() throws Exception
    {
        // one file, loaded from the classpath
        logger.info("testGetBeansFromSimpleContext()");

        SpringContext context = new SpringContext(null, "classpath:contexts/simpleContext.xml");
        assertEquals("number of beans defined", 4, context.getBeans().size());

        BeanDefinition b1 = context.getBean("simpleUrlMapping");
        assertNotNull("able to find bean by name", b1);
        assertEquals("bean name set",              "simpleUrlMapping",
                                                   b1.getBeanName());
        assertEquals("bean class set",             "org.springframework.web.servlet.handler.SimpleUrlHandlerMapping",
                                                   b1.getBeanClass());
        assertNotNull("bean definition has XML",   b1.getBeanDef());

        List<BeanDefinition> b2list = context.getBeansByClass("org.springframework.web.servlet.handler.SimpleUrlHandlerMapping");
        assertEquals("byCass returned data",         1, b2list.size());
        assertSame("byClass returned expected bean", b1, b2list.get(0));
    }


    @Test
    public void testGetBeansFromSimpleContextInWar() throws Exception
    {
        // the same file, loaded from a WAR
        logger.info("testGetBeansFromSimpleContextInWar()");

        WarMachine war = TestHelpers.createWarMachine(WarNames.SPRING2_SIMPLE);
        SpringContext context = new SpringContext(war, "classpath:servletContext.xml");
        assertEquals("number of beans defined", 4, context.getBeans().size());

        BeanDefinition b1 = context.getBean("simpleUrlMapping");
        assertNotNull("able to find bean by name", b1);
        // if we got this far, I'll assume that the assertions from the prior test will all pass
    }


    @Test
    public void testGetProperty() throws Exception
    {
        logger.info("testGetProperty()");

        SpringContext context = new SpringContext(null, "classpath:contexts/propContext.xml");
        BeanDefinition bean = context.getBean("example");
        assertNotNull("able to load context", bean);

        assertEquals("extracted property as string", "foo", bean.getPropertyAsString("propAsString"));

        assertEquals("extracted property as ref", "example", bean.getPropertyAsRefId("propAsRefId"));
        // FIXME - add test for explicit <ref> element

        Properties propsProp = bean.getPropertyAsProperties("propAsProperties");
        assertNotNull("able to extract Properties property", propsProp);
        assertEquals("extracted property for 'foo'",   "bar",    propsProp.get("foo"));
        assertEquals("extracted property for 'argle'", "bargle", propsProp.get("argle"));
    }
}
