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

import com.kdgregory.pathfinder.core.InvocationOptions;


public class TestInvocationOptions
{
    @Test
    public void testRemoveInvocationArguments() throws Exception
    {
        // note single dash on -baz
        String[] result = InvocationOptions.removeInvocationArguments("foo", "--bar", "-baz");
        assertEquals("arg count after removals", 2, result.length);
        assertEquals("result[0]", "foo", result[0]);
        assertEquals("result[1]", "-baz", result[1]);
    }



    @Test
    public void testParseDefaults() throws Exception
    {
        Map<InvocationOptions,Boolean> opts = InvocationOptions.parseCli();

        assertTrue("--showJSP",            InvocationOptions.SHOW_JSP.isEnabled(opts));
        assertTrue("--showHTML",           InvocationOptions.SHOW_HTML.isEnabled(opts));
        assertFalse("--showStatic",        InvocationOptions.SHOW_STATIC.isEnabled(opts));
        assertFalse("--showRequestParams", InvocationOptions.SHOW_REQUEST_PARAMS.isEnabled(opts));
    }


    @Test
    public void testParseEnabled() throws Exception
    {
        Map<InvocationOptions,Boolean> opts
            = InvocationOptions.parseCli(
                "--showJSP", "--showHTML", "--showStatic",
                "--showRequestParams");

        assertTrue("--showJSP",           InvocationOptions.SHOW_JSP.isEnabled(opts));
        assertTrue("--showHTML",          InvocationOptions.SHOW_HTML.isEnabled(opts));
        assertTrue("--showStatic",        InvocationOptions.SHOW_STATIC.isEnabled(opts));
        assertTrue("--showRequestParams", InvocationOptions.SHOW_REQUEST_PARAMS.isEnabled(opts));
    }


    @Test
    public void testParseDisabled() throws Exception
    {
        Map<InvocationOptions,Boolean> opts
            = InvocationOptions.parseCli(
                "--hideJSP", "--hideHTML", "--hideStatic",
                "--noShowRequestParams");

        assertFalse("--showJSP",           InvocationOptions.SHOW_JSP.isEnabled(opts));
        assertFalse("--showHTML",          InvocationOptions.SHOW_HTML.isEnabled(opts));
        assertFalse("--showStatic",        InvocationOptions.SHOW_STATIC.isEnabled(opts));
        assertFalse("--showRequestParams", InvocationOptions.SHOW_REQUEST_PARAMS.isEnabled(opts));
    }
}
