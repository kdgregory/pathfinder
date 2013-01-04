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

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import org.junit.Test;
import static org.junit.Assert.*;

import com.kdgregory.pathfinder.core.InvocationOptions;
import com.kdgregory.pathfinder.test.WarNames;
import com.kdgregory.pathfinder.util.TestHelpers;


public class TestMainOptions
{
//----------------------------------------------------------------------------
//  Support Code
//----------------------------------------------------------------------------

    private String output;

    private void run(String warName, String... options)
    throws Exception
    {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        new Main(InvocationOptions.parseCli(options),
                 TestHelpers.createWarMachine(warName),
                 new PrintStream(out)).run();
        output = new String(out.toByteArray()); // default encoding is OK
    }


    private void assertExpectedUrls(String... urls)
    {
        for (String url : urls)
        {
            url = url + " ";    // output should be space-padded
            assertTrue("expected " + url, output.contains(url));
        }
    }


    private void assertUnexpectedUrls(String... urls)
    {
        for (String url : urls)
        {
            url = url + " ";    // output should be space-padded
            assertFalse("did not expect " + url, output.contains(url));
        }
    }


//----------------------------------------------------------------------------
//  Testcases
//----------------------------------------------------------------------------

    @Test
    public void testDefaultStatic() throws Exception
    {
        run(WarNames.STATIC);
        assertExpectedUrls(
                "/index.html",
                "/index.jsp",
                "/subdir/index.jsp");
        assertUnexpectedUrls(
                "/sample.gif",
                "/css/sample.css");
    }


    @Test
    public void testIgnoreHtml() throws Exception
    {
        run(WarNames.STATIC, InvocationOptions.IGNORE_HTML.getEnableString());
        assertExpectedUrls(
                "/index.jsp",
                "/subdir/index.jsp");
        assertUnexpectedUrls(
                "/index.html",
                "/sample.gif",
                "/css/sample.css");
    }


    @Test
    public void testIgnoreJSP() throws Exception
    {
        run(WarNames.STATIC, InvocationOptions.IGNORE_JSP.getEnableString());
        assertExpectedUrls(
                "/index.html");
        assertUnexpectedUrls(
                "/index.jsp",
                "/subdir/index.jsp",
                "/sample.gif",
                "/css/sample.css");
    }


    @Test
    public void testIncludeStatic() throws Exception
    {
        run(WarNames.STATIC, InvocationOptions.IGNORE_STATIC.getDisableString());
        assertExpectedUrls(
                "/index.html",
                "/index.jsp",
                "/subdir/index.jsp",
                "/sample.gif",
                "/css/sample.css");
        assertUnexpectedUrls();
    }


    @Test
    public void testSpringWithoutParameters() throws Exception
    {
        run(WarNames.SPRING_ANNO);
        assertTrue("controller should not show params",
                   output.contains("com.kdgregory.pathfinder.test.spring3.pkg2.ControllerE.getFoo()"));
    }


    @Test
    public void testSpringWithParameters() throws Exception
    {
        run(WarNames.SPRING_ANNO, InvocationOptions.ENABLE_REQUEST_PARAMS.getEnableString());
        assertTrue("controller should show params",
                   output.contains("com.kdgregory.pathfinder.test.spring3.pkg2.ControllerE.getFoo(java.lang.String argle,"));
    }
}
