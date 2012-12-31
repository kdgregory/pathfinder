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

import java.util.Map;

import org.junit.Test;
import static org.junit.Assert.*;

import com.kdgregory.bcelx.parser.AnnotationParser;
import com.kdgregory.pathfinder.core.WarMachine;
import com.kdgregory.pathfinder.test.WarNames;
import com.kdgregory.pathfinder.util.ClasspathScanner;
import com.kdgregory.pathfinder.util.TestHelpers;


public class TestClasspathScanner
{
    @Test
    public void testUnfiltered() throws Exception
    {
        // this test is identical to TestWarMachine.testGetFilesOnClasspath()
        WarMachine machine = TestHelpers.createWarMachine(WarNames.SERVLET);

        ClasspathScanner scanner = new ClasspathScanner();

        Map<String,AnnotationParser> result = scanner.scan(machine);
        assertTrue("searching for file under WEB-INF", result.containsKey("com.example.servlet.SomeServlet"));
        assertTrue("searching for file in JAR",        result.containsKey("net.sf.practicalxml.DomUtil"));
    }


    @Test
    public void testBasePackageRecursive() throws Exception
    {
        WarMachine machine = TestHelpers.createWarMachine(WarNames.SERVLET);

        ClasspathScanner scanner = new ClasspathScanner()
                                       .addBasePackage("com.example", true);

        Map<String,AnnotationParser> result = scanner.scan(machine);
        assertEquals("number of files found", 1, result.size());
        assertTrue("searching for file under WEB-INF", result.containsKey("com.example.servlet.SomeServlet"));
    }


    @Test
    public void testBasePackageNonRecursive() throws Exception
    {
        WarMachine machine = TestHelpers.createWarMachine(WarNames.SERVLET);

        ClasspathScanner scanner = new ClasspathScanner()
                                       .addBasePackage("com.example", false);

        Map<String,AnnotationParser> result = scanner.scan(machine);
        assertEquals("number of files found", 0, result.size());
    }


    @Test
    public void testMultipleBasePackagesOneAtATime() throws Exception
    {
        WarMachine machine = TestHelpers.createWarMachine(WarNames.SPRING_ANNO);

        ClasspathScanner scanner = new ClasspathScanner()
                                       .addBasePackage("com.kdgregory.pathfinder.test.spring3.pkg1", false)
                                       .addBasePackage("com.kdgregory.pathfinder.test.spring3.pkg2", false);

        Map<String,Boolean> packages = scanner.getBasePackages();
        assertEquals("packages in scan", 2, packages.size());
        assertEquals("expected pkg1", Boolean.FALSE, packages.get("com.kdgregory.pathfinder.test.spring3.pkg1"));
        assertEquals("expected pkg2", Boolean.FALSE, packages.get("com.kdgregory.pathfinder.test.spring3.pkg2"));

        Map<String,AnnotationParser> result = scanner.scan(machine);
        assertEquals("number of files found", 6, result.size());
        assertTrue("expected ControllerA", result.containsKey("com.kdgregory.pathfinder.test.spring3.pkg1.ControllerA"));
        assertTrue("expected ControllerB", result.containsKey("com.kdgregory.pathfinder.test.spring3.pkg2.ControllerB"));
        assertTrue("expected ControllerC", result.containsKey("com.kdgregory.pathfinder.test.spring3.pkg2.ControllerC"));
        assertTrue("expected ControllerD", result.containsKey("com.kdgregory.pathfinder.test.spring3.pkg2.ControllerD"));
        assertTrue("expected ControllerE", result.containsKey("com.kdgregory.pathfinder.test.spring3.pkg2.ControllerE"));
        assertTrue("expected Dummy",       result.containsKey("com.kdgregory.pathfinder.test.spring3.pkg1.Dummy"));
    }


    @Test
    public void testAnnotationFilter() throws Exception
    {
        WarMachine machine = TestHelpers.createWarMachine(WarNames.SPRING_SCAN);

        ClasspathScanner scanner = new ClasspathScanner()
                                       .addBasePackage("com.kdgregory.pathfinder.test")
                                       .addIncludedAnnotation("org.springframework.stereotype.Controller")
                                       .addIncludedAnnotation("org.springframework.stereotype.Repository");

        Map<String,AnnotationParser> result = scanner.scan(machine);
        assertEquals("number of files found", 3, result.size());
        assertTrue("expected RepositoryA", result.containsKey("com.kdgregory.pathfinder.test.scan.repo.RepositoryA"));
        assertTrue("expected ControllerA", result.containsKey("com.kdgregory.pathfinder.test.scan.controller.ControllerA"));
        assertTrue("expected ControllerB", result.containsKey("com.kdgregory.pathfinder.test.scan.controller.ControllerB"));
        // no Dummy, no @Service, no @Component
    }
}
