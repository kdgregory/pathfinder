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

package com.kdgregory.pathfinder.core;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.junit.Test;
import static org.junit.Assert.*;

import com.kdgregory.bcelx.parser.AnnotationParser;
import com.kdgregory.pathfinder.core.impl.ClasspathScannerImpl;
import com.kdgregory.pathfinder.test.WarNames;
import com.kdgregory.pathfinder.util.TestHelpers;


public class TestClasspathScanner
{
    @Test
    public void testUnfiltered() throws Exception
    {
        // this test is identical to TestWarMachine.testGetFilesOnClasspath()
        WarMachine machine = TestHelpers.createWarMachine(WarNames.SERVLET);

        ClasspathScannerImpl scanner = new ClasspathScannerImpl();

        Set<String> files = scanner.scan(machine);
        assertTrue("searching for file under WEB-INF", files.contains("com/example/servlet/SomeServlet.class"));
        assertTrue("searching for file in JAR",        files.contains("net/sf/practicalxml/DomUtil.class"));
    }


    @Test
    public void testBasePackageRecursive() throws Exception
    {
        WarMachine machine = TestHelpers.createWarMachine(WarNames.SERVLET);

        ClasspathScannerImpl scanner = new ClasspathScannerImpl()
                                       .addBasePackage("com.example", true);

        Set<String> files = scanner.scan(machine);
        assertEquals("number of files found", 1, files.size());
        assertTrue("searching for file under WEB-INF", files.contains("com/example/servlet/SomeServlet.class"));
    }


    @Test
    public void testBasePackageNonRecursive() throws Exception
    {
        WarMachine machine = TestHelpers.createWarMachine(WarNames.SERVLET);

        ClasspathScannerImpl scanner = new ClasspathScannerImpl()
                                       .addBasePackage("com.example", false);

        Set<String> files = scanner.scan(machine);
        assertEquals("number of files found", 0, files.size());
    }


    @Test
    public void testMultipleBasePackagesOneAtATime() throws Exception
    {
        WarMachine machine = TestHelpers.createWarMachine(WarNames.SPRING3_BASIC);

        ClasspathScannerImpl scanner = new ClasspathScannerImpl()
                                       .addBasePackage("com.kdgregory.pathfinder.test.spring3.pkg1", false)
                                       .addBasePackage("com.kdgregory.pathfinder.test.spring3.pkg2", false);

        Map<String,Boolean> packages = scanner.getBasePackages();
        assertEquals("packages in scan", 2, packages.size());
        assertEquals("expected pkg1", Boolean.FALSE, packages.get("com/kdgregory/pathfinder/test/spring3/pkg1"));
        assertEquals("expected pkg2", Boolean.FALSE, packages.get("com/kdgregory/pathfinder/test/spring3/pkg2"));

        Set<String> files = scanner.scan(machine);
        assertEquals("number of files found", 4, files.size());
        assertTrue("expected ControllerA", files.contains("com/kdgregory/pathfinder/test/spring3/pkg1/ControllerA.class"));
        assertTrue("expected ControllerB", files.contains("com/kdgregory/pathfinder/test/spring3/pkg2/ControllerB.class"));
        assertTrue("expected ControllerC", files.contains("com/kdgregory/pathfinder/test/spring3/pkg2/ControllerC.class"));
        assertTrue("expected Dummy",       files.contains("com/kdgregory/pathfinder/test/spring3/pkg1/Dummy.class"));
    }


    @Test
    public void testMultipleBasePackagesAllAtOnce() throws Exception
    {
        WarMachine machine = TestHelpers.createWarMachine(WarNames.SPRING3_BASIC);

        ClasspathScannerImpl scanner = new ClasspathScannerImpl()
                                           .addBasePackages(Arrays.asList(
                                               "com.kdgregory.pathfinder.test.spring3.pkg1",
                                               "com.kdgregory.pathfinder.test.spring3.pkg2"),
                                               false);

        Set<String> files = scanner.scan(machine);
        assertEquals("number of files found", 4, files.size());
        assertTrue("expected ControllerA", files.contains("com/kdgregory/pathfinder/test/spring3/pkg1/ControllerA.class"));
        assertTrue("expected ControllerB", files.contains("com/kdgregory/pathfinder/test/spring3/pkg2/ControllerB.class"));
        assertTrue("expected ControllerC", files.contains("com/kdgregory/pathfinder/test/spring3/pkg2/ControllerC.class"));
        assertTrue("expected Dummy",       files.contains("com/kdgregory/pathfinder/test/spring3/pkg1/Dummy.class"));
    }


    @Test
    public void testAnnotationFilter() throws Exception
    {
        WarMachine machine = TestHelpers.createWarMachine(WarNames.SPRING3_BASIC);

        ClasspathScannerImpl scanner = new ClasspathScannerImpl()
                                       .addBasePackage("com.kdgregory.pathfinder.test.spring3")
                                       .setIncludedAnnotations("org.springframework.stereotype.Controller");

        Set<String> files = scanner.scan(machine);
        assertEquals("number of files found", 3, files.size());
        assertTrue("expected ControllerA", files.contains("com/kdgregory/pathfinder/test/spring3/pkg1/ControllerA.class"));
        assertTrue("expected ControllerB", files.contains("com/kdgregory/pathfinder/test/spring3/pkg2/ControllerB.class"));
        assertTrue("expected ControllerC", files.contains("com/kdgregory/pathfinder/test/spring3/pkg2/ControllerC.class"));
        // no Dummy
    }


    @Test
    public void testAnnotationFilterWithRetainedParsedClasses() throws Exception
    {
        WarMachine machine = TestHelpers.createWarMachine(WarNames.SPRING3_BASIC);

        ClasspathScannerImpl scanner = new ClasspathScannerImpl()
                                       .addBasePackage("com.kdgregory.pathfinder.test.spring3")
                                       .setIncludedAnnotations("org.springframework.stereotype.Controller");

        Map<String,AnnotationParser> parsedClasses = new HashMap<String,AnnotationParser>();
        scanner.scan(machine, parsedClasses);
        assertEquals("number of files found", 3, parsedClasses.size());
        assertNotNull("expected ControllerA", parsedClasses.get("com/kdgregory/pathfinder/test/spring3/pkg1/ControllerA.class"));
        assertNotNull("expected ControllerB", parsedClasses.get("com/kdgregory/pathfinder/test/spring3/pkg2/ControllerB.class"));
        assertNotNull("expected ControllerC", parsedClasses.get("com/kdgregory/pathfinder/test/spring3/pkg2/ControllerC.class"));
    }

}
