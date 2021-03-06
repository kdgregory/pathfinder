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

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

import net.sf.kdgcommons.lang.StringUtil;


/**
 *  Options to control the behavior of the PathFinder. These will be parsed
 *  from the command-line, and loaded into a <code>EnumMap</code> that is
 *  then passed to the various inspectors and output code.
 *  <p>
 *  Each option has two string values representing command-line parameters
 *  that enable and disable the option. Each option also has a default value
 *  (used if neither string is found in the command line), and a description
 *  (used to generate help text).
 */
public enum InvocationOptions
{
    SHOW_JSP(
            "--showJSP", "--hideJSP", true,
            "Display client-accessible JSP files as destination URLs."),

    SHOW_HTML(
            "--showHTML", "--hideHTML", true,
            "Display client-accessible static HTML content as destination URLs."),

    SHOW_STATIC(
            "--showStatic", "--hideStatic", false,
            "Display client-accessible static content other than HTML"
            + " (eg, JS and CSS) as destination URLs."),

    SHOW_REQUEST_PARAMS(
            "--showRequestParams", "--hideRequestParams", false,
            "For mappings that identify individual request parameters (eg, Spring3) "
            + "show those parameters in the mapping output."),

    DEBUG(
            "--debug", "", false,
            "Enable debug-level logging to StdErr."),

    VERBOSE(
            "--verbose", "", false,
            "Enable verbose debugging (implies --debug).");

//----------------------------------------------------------------------------
//  Instance variables and constructor
//----------------------------------------------------------------------------

    private String cliEnable;
    private String cliDisable;
    private boolean defaultValue;
    private String description;

    private InvocationOptions(String cliEnable, String cliDisable, boolean defaultValue, String description)
    {
        this.cliEnable = cliEnable;
        this.cliDisable = cliDisable;
        this.defaultValue = defaultValue;
        this.description = description;
    }


//----------------------------------------------------------------------------
//  Instance methods
//----------------------------------------------------------------------------

    /**
     *  Returns the string value of the "enable" command-line option.
     */
    public String getEnableString()
    {
        return cliEnable;
    }


    /**
     *  Returns the string value of the "disable" command-line option.
     */
    public String getDisableString()
    {
        return cliDisable;
    }


    /**
     *  Given a map of options, returns an indication of whether this
     *  option is enabled. If the option is not in the map, it is
     *  considered disabled.
     */
    public boolean isEnabled(Map<InvocationOptions,Boolean> map)
    {
        Boolean value = map.get(this);
        return (value != null) ? value.booleanValue() : false;
    }


//----------------------------------------------------------------------------
//  Static utility methods Utilities
//----------------------------------------------------------------------------

    /**
     *  Writes all options to StdOut.
     */
    public static void dump(PrintStream out)
    {
        for (InvocationOptions option : InvocationOptions.values())
        {
            out.println();

            out.print("    " + option.cliEnable);
            if (option.defaultValue)
                out.println(" (default)");
            else
                out.println();

            if (! StringUtil.isBlank(option.cliDisable))
            {
                out.print("    " + option.cliDisable);
                if (option.defaultValue)
                    out.println();
                else
                    out.println(" (default)");
            }

            out.println("    " + option.description);
        }
        out.println();
    }


    /**
     *  Processes the command line, removing all arguments that start with "--"
     *  (whether or not they're a define invocation argument).
     */
    public static String[] removeInvocationArguments(String... argv)
    {
        List<String> result = new ArrayList<String>(argv.length);
        for (String arg : argv)
        {
            if (!arg.startsWith("--"))
                result.add(arg);
        }
        return result.toArray(new String[result.size()]);
    }


    /**
     *  Extract all options from the command-line arguments. Missing arguments
     *  will be filled with their default values.
     */
    public static Map<InvocationOptions,Boolean> parseCli(String... argv)
    {
        Map<InvocationOptions,Boolean> result = new EnumMap<InvocationOptions,Boolean>(InvocationOptions.class);
        for (InvocationOptions value : values())
            result.put(value, Boolean.valueOf(value.defaultValue));

        for (String arg : argv)
        {
            for (InvocationOptions option : InvocationOptions.values())
            {
                if (arg.equals(option.cliEnable))
                    result.put(option, Boolean.TRUE);
                else if (arg.equals(option.cliDisable))
                    result.put(option, Boolean.FALSE);
            }
        }
        return result;
    }
}
