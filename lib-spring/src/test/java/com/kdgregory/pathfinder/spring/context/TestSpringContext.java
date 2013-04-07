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

package com.kdgregory.pathfinder.spring.context;

import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.junit.Test;
import static org.junit.Assert.*;

import org.apache.log4j.Logger;

import com.kdgregory.pathfinder.core.WarMachine;
import com.kdgregory.pathfinder.spring.AbstractSpringTestcase;
import com.kdgregory.pathfinder.spring.InvalidContextException;
import com.kdgregory.pathfinder.spring.context.BeanDefinition;
import com.kdgregory.pathfinder.spring.context.SpringContext;
import com.kdgregory.pathfinder.spring.context.XmlBeanDefinition;
import com.kdgregory.pathfinder.test.WarNames;
import com.kdgregory.pathfinder.util.TestHelpers;


// note: also tests BeanDefinition
public class TestSpringContext
extends AbstractSpringTestcase
{
    @Test
    public void testGetBeansFromSimpleContext() throws Exception
    {
        // one file, loaded from the classpath
        logger.info("testGetBeansFromSimpleContext()");

        SpringContext context = new SpringContext(null, "classpath:contexts/simpleContext.xml");
        assertEquals("number of beans defined", 4, context.getBeans().size());

        XmlBeanDefinition b1 = (XmlBeanDefinition)context.getBean("simpleUrlMapping");
        assertNotNull("able to find bean by name", b1);
        assertEquals("bean name set",              "simpleUrlMapping",
                                                   b1.getBeanId());
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

        WarMachine war = TestHelpers.createWarMachine(WarNames.SPRING_SIMPLE);
        SpringContext context = new SpringContext(war, "/WEB-INF/spring/servletContext.xml");
        assertEquals("number of beans defined", 4, context.getBeans().size());

        BeanDefinition b1 = context.getBean("urlMapping");
        assertNotNull("able to find bean by name", b1);
        // if we got this far, I'll assume that the assertions from the prior test will all pass
    }


    @Test
    public void testGetProperty() throws Exception
    {
        logger.info("testGetProperty()");

        SpringContext context = new SpringContext(null, "classpath:contexts/propContext.xml");
        BeanDefinition bean = context.getBean("example");
        assertNotNull("able to find bean", bean);

        assertEquals("extracted property as string in attribute", "foo", bean.getPropertyAsString("propAsStringAttribute"));
        assertEquals("extracted property as string in value", "bar", bean.getPropertyAsString("propAsStringValue"));

        assertEquals("extracted property as ref", "example", bean.getPropertyAsRefId("propAsRefId"));
        // FIXME - add test for explicit <ref> element

        Properties propsProp1 = bean.getPropertyAsProperties("propAsSingleValue");
        assertNotNull("able to extract property with name-value props", propsProp1);
        assertEquals("extracted property for 'foo'",   "bar",    propsProp1.get("foo"));
        assertEquals("extracted property for 'argle'", "bargle", propsProp1.get("argle"));

        Properties propsProp2 = bean.getPropertyAsProperties("propAsExplicitProperties");
        assertNotNull("able to extract property with explicit props", propsProp2);
        assertEquals("extracted property for 'foo'",   "bar",    propsProp2.get("foo"));
        assertEquals("extracted property for 'argle'", "bargle", propsProp2.get("argle"));
    }


    @Test
    public void testInheritProperty() throws Exception
    {
        logger.info("testInheritProperty()");

        SpringContext context = new SpringContext(null, "classpath:contexts/inheritProp.xml");
        BeanDefinition bean = context.getBean("inherited");
        assertNotNull("able to find bean", bean);

        assertEquals("extracted property as string in attribute", "foo", bean.getPropertyAsString("propAsStringAttribute"));
        assertEquals("extracted property as string in value", "bar", bean.getPropertyAsString("propAsStringValue"));

        assertEquals("extracted property as ref", "example", bean.getPropertyAsRefId("propAsRefId"));
        // FIXME - add test for explicit <ref> element

        Properties propsProp1 = bean.getPropertyAsProperties("propAsSingleValue");
        assertNotNull("able to extract property with name-value props", propsProp1);
        assertEquals("extracted property for 'foo'",   "bar",    propsProp1.get("foo"));
        assertEquals("extracted property for 'argle'", "bargle", propsProp1.get("argle"));

        Properties propsProp2 = bean.getPropertyAsProperties("propAsExplicitProperties");
        assertNotNull("able to extract property with explicit props", propsProp2);
        assertEquals("extracted property for 'foo'",   "bar",    propsProp2.get("foo"));
        assertEquals("extracted property for 'argle'", "bargle", propsProp2.get("argle"));
    }


    @Test
    public void testInheritClass() throws Exception
    {
        logger.info("testInheritClass()");

        SpringContext ctx = new SpringContext(null, "classpath:contexts/inheritClass.xml");

        assertEquals("com.example.SomeBean", ctx.getBean("concreteBean").getBeanClass());
    }


    @Test(expected=InvalidContextException.class)
    public void testInheritClassFail() throws Exception
    {
        logger.info("testInheritClassFail()");

        SpringContext ctx = new SpringContext(null, "classpath:contexts/inheritClassFail.xml");

        assertEquals("com.example.SomeBean", ctx.getBean("concreteBean").getBeanClass());
    }


    @Test
    public void testParentChildContext() throws Exception
    {
        logger.info("testParentChildContext()");

        SpringContext parent = new SpringContext(null, "classpath:contexts/parentContext.xml");
        SpringContext child = new SpringContext(parent, null, "classpath:contexts/childContext.xml");

        // the child should be able to retrieve a bean defined in its own XML

        BeanDefinition bean1 = child.getBean("simpleUrlMapping");
        assertEquals("child bean class", "org.springframework.web.servlet.handler.SimpleUrlHandlerMapping", bean1.getBeanClass());

        // and that it delegates to the parent if it doesn't have a bean

        BeanDefinition bean2 = child.getBean("simpleControllerB");
        assertEquals("parent bean class", "com.kdgregory.pathfinder.test.spring2.SimpleController", bean2.getBeanClass());

        // the child's bean map should combine both parent and child

        assertEquals("bean count from child, all beans", 4, child.getBeans().size());

        // but the parent's shouldn't

        assertEquals("bean count from parent, all beans", 2, parent.getBeans().size());

        // ditto for class scans

        List<BeanDefinition> list1 = child.getBeansByClass("com.kdgregory.pathfinder.test.spring2.SimpleController");
        assertEquals("bean count from child, class scan", 2, list1.size());
        assertTrue("scan from child has controllerA", list1.contains(child.getBean("simpleControllerA")));
        assertTrue("scan from child has controllerb", list1.contains(child.getBean("simpleControllerB")));

        List<BeanDefinition> list2 = parent.getBeansByClass("com.kdgregory.pathfinder.test.spring2.SimpleController");
        assertEquals("bean count from parent, class scan", 1, list2.size());
        assertTrue("scan from parent has controllerb", list2.contains(child.getBean("simpleControllerB")));
    }


    @Test
    public void testCombinedContext() throws Exception
    {
        logger.info("testCombinedContext()");

        SpringContext ctx1 = new SpringContext(null, "classpath:contexts/combined1.xml,classpath:contexts/combined2.xml");
        assertEquals("processed all files when separated by comma", 4, ctx1.getBeans().size());

        SpringContext ctx3 = new SpringContext(null, "classpath:contexts/combined1.xml \t\n classpath:contexts/combined2.xml");
        assertEquals("processed all files when separated by comma", 4, ctx3.getBeans().size());
    }


    @Test
    public void testComponentScan() throws Exception
    {
        logger.info("testComponentScan()");

        WarMachine war = TestHelpers.createWarMachine(WarNames.SPRING_SCAN);
        SpringContext ctx = new SpringContext(war, "/WEB-INF/spring/servletContext.xml");

        // the test WAR has @Service and @Repository beans in the scan path, but these
        // should be ignored by our scanner

        assertEquals("number of beans", 4, ctx.getBeans().size());
        assertEquals("explicit bean",   "org.springframework.web.servlet.view.UrlBasedViewResolver",
                                        ctx.getBean("viewResolver").getBeanClass());
        assertEquals("@Component",      "com.kdgregory.pathfinder.test.scan.component.MyComponent",
                                        ctx.getBean("myComponent").getBeanClass());
        assertEquals("@Controller #1",  "com.kdgregory.pathfinder.test.scan.controller.ControllerA",
                                        ctx.getBean("myController").getBeanClass());
        assertEquals("@Controller #2",  "com.kdgregory.pathfinder.test.scan.controller.ControllerB",
                                        ctx.getBean("controllerB").getBeanClass());
    }


    @Test
    public void testComponentScanPartial() throws Exception
    {
        logger.info("testComponentScanPartial()");

        WarMachine war = TestHelpers.createWarMachine(WarNames.SPRING_SCAN);
        SpringContext ctx = new SpringContext(war, "/WEB-INF/spring/altContext1.xml");

        // this test just looks at the controller package (plus explicit beans)

        assertEquals("number of beans", 3, ctx.getBeans().size());
        assertEquals("explicit bean",   "org.springframework.web.servlet.view.UrlBasedViewResolver",
                                        ctx.getBean("viewResolver").getBeanClass());
        assertEquals("@Controller #1",  "com.kdgregory.pathfinder.test.scan.controller.ControllerA",
                                        ctx.getBean("myController").getBeanClass());
        assertEquals("@Controller #2",  "com.kdgregory.pathfinder.test.scan.controller.ControllerB",
                                        ctx.getBean("controllerB").getBeanClass());

    }


    @Test
    public void testComponentScanMultiple() throws Exception
    {
        logger.info("testComponentScanMultiple()");

        WarMachine war = TestHelpers.createWarMachine(WarNames.SPRING_SCAN);
        SpringContext ctx = new SpringContext(war, "/WEB-INF/spring/altContext2.xml");

        // this test looks at both controller and component packages, so should be same as full scan

        assertEquals("number of beans", 4, ctx.getBeans().size());
        assertEquals("explicit bean",   "org.springframework.web.servlet.view.UrlBasedViewResolver",
                                        ctx.getBean("viewResolver").getBeanClass());
        assertEquals("@Component",      "com.kdgregory.pathfinder.test.scan.component.MyComponent",
                                        ctx.getBean("myComponent").getBeanClass());
        assertEquals("@Controller #1",  "com.kdgregory.pathfinder.test.scan.controller.ControllerA",
                                        ctx.getBean("myController").getBeanClass());
        assertEquals("@Controller #2",  "com.kdgregory.pathfinder.test.scan.controller.ControllerB",
                                        ctx.getBean("controllerB").getBeanClass());
    }


    @Test
    public void testComponentScanMultipleWithOverlap() throws Exception
    {
        logger.info("testComponentScanMultipleWithOverlap()");

        WarMachine war = TestHelpers.createWarMachine(WarNames.SPRING_SCAN);
        SpringContext ctx = new SpringContext(war, "/WEB-INF/spring/altContext3.xml");

        // we want to verify that beans are only added once

        assertEquals("number of beans", 4, ctx.getBeans().size());
        assertEquals("explicit bean",   "org.springframework.web.servlet.view.UrlBasedViewResolver",
                                        ctx.getBean("viewResolver").getBeanClass());
        assertEquals("@Component",      "com.kdgregory.pathfinder.test.scan.component.MyComponent",
                                        ctx.getBean("myComponent").getBeanClass());
        assertEquals("@Controller #1",  "com.kdgregory.pathfinder.test.scan.controller.ControllerA",
                                        ctx.getBean("myController").getBeanClass());
        assertEquals("@Controller #2",  "com.kdgregory.pathfinder.test.scan.controller.ControllerB",
                                        ctx.getBean("controllerB").getBeanClass());
    }


    @Test
    public void testImportedContext() throws Exception
    {
        logger.info("testImportedContext()");

        WarMachine war = TestHelpers.createWarMachine(WarNames.SPRING_SPLIT_CONFIG);
        SpringContext ctx = new SpringContext(war, "/WEB-INF/spring/servletContext.xml");

        assertEquals("bean from base context",      "com.kdgregory.pathfinder.test.spring2.ControllerA",
                                                    ctx.getBean("controllerA").getBeanClass());
        assertEquals("bean from imported context",  "com.kdgregory.pathfinder.test.spring2.ControllerB",
                                                    ctx.getBean("controllerB").getBeanClass());
    }


    @Test
    public void testImportedContextAbsolutePath() throws Exception
    {
        // absolute path is really relative path for resource resolution
        logger.info("testImportedContextAbsolutePath()");

        WarMachine war = TestHelpers.createWarMachine(WarNames.SPRING_RESOURCES);
        SpringContext ctx = new SpringContext(war, "/WEB-INF/spring/servletContext.xml");

        assertEquals("bean from base context",      "com.kdgregory.pathfinder.test.spring3.pkg1.ControllerA",
                                                    ctx.getBean("controllerA").getBeanClass());
        assertEquals("bean from imported context",  "com.kdgregory.pathfinder.test.spring3.pkg1.ControllerB",
                                                    ctx.getBean("controllerB").getBeanClass());
    }


    @Test
    public void testReferenceByName() throws Exception
    {
        logger.info("testReferenceByName()");

        SpringContext ctx = new SpringContext(null, "classpath:contexts/referenceByName.xml");

        // in keeping with ApplicationContext.getBeanDefinitionNames(), the definition map
        // is ID if available, name if not; each bean appears only once

        Map<String,BeanDefinition> beans = ctx.getBeans();
        assertEquals("number of beans in map",          2, beans.size());
        assertEquals("map holds bean1 by name",         "com.example.SomeBean",     beans.get("bean1").getBeanClass());
        assertEquals("map doesn't hold bean2 by name",   null,                      beans.get("bean2"));
        assertEquals("map holds bean2 by ID",           "com.example.AnotherBean",  beans.get("foobar").getBeanClass());

        // but getting the bean directly can use either name or ID

        assertEquals("context holds bean1 by name",     "com.example.SomeBean",     ctx.getBean("bean1").getBeanClass());
        assertEquals("context holds bean2 by name",     "com.example.AnotherBean",  ctx.getBean("bean2").getBeanClass());
        assertEquals("context holds bean2 by ID",       "com.example.AnotherBean",  ctx.getBean("foobar").getBeanClass());
    }


    @Test
    public void testBeanWithoutNameOrId() throws Exception
    {
        logger.info("testBeanWithoutNameOrId()");

        SpringContext ctx = new SpringContext(null, "classpath:contexts/missingId.xml");

        // this map is keyed by bean name, so having two elements indicates that we gave
        // each of them unique names ... and that's the only guarantee that Spring wants

        Map<String,BeanDefinition> beans = ctx.getBeans();
        assertEquals("bean count", 2, beans.size());
    }
}
