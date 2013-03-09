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

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import net.sf.kdgcommons.lang.StringUtil;

import com.kdgregory.pathfinder.core.WarMachine;


/**
 *  Attempts to replicate Spring's resource loading logic, retrieving files from
 *  a WAR. Resources with a scheme are handled as expected, Non-schema resources
 *  are treated as file paths within the WAR, relative to either the base of the
 *  WAR or a predefined path.
 *  <p>
 *  In addition to resource loading, provides the {@link #decomposeResourceRefs}
 *  method, which breaks appart a resource list and supports the "classpath*:"
 *  scheme.
 */
public class ResourceLoader
{
    private WarMachine war;
    private String baseDir;


    /**
     *  Constructs an instance that resolves all requests against the specified WAR. If
     *  passed <code>null</code>, resolves against the local filesystem and/or runtime
     *  classpath (useful for testing).
     */
    public ResourceLoader(WarMachine war)
    {
        this(war, "");
    }


    /**
     *  Constructs an instance that resolves all requests against the specified WAR,
     *  relative to the specified base directory.
     */
    public ResourceLoader(WarMachine war, String baseDir)
    {
        this.war = war;

        if (StringUtil.isBlank(baseDir))
            baseDir = "/";
        if (!baseDir.endsWith("/"))
            baseDir += "/";
        this.baseDir = baseDir;
    }


    /**
     *  Returns the named resource as a stream, <code>null</code> if it cannot be found.
     *  Accepts the standard Spring "file:" and "classpath:" schemes. Names without a
     *  scheme are resolved as files, relative to either the base of the WAR or a set
     *  base directory.
     *  <p>
     *  The caller is responsible for closing this stream.
     */
    public InputStream getResourceAsStream(String name)
    throws IOException
    {
        if (name.startsWith("classpath:"))
            return openClasspathResource(name);
        else if (name.startsWith("file:"))
            return openFileResource(name);
        else
            return openWarResource(name);
    }


    /**
     *  Breaks the passed string into zero or more resource references, which may
     *  then be passed to {@link #getResourceAsStream}. The passed reference may
     *  be a comma-delimited list.
     *  <p>
     *  TODO: Support globbing
     */
    public static List<String> decomposeResourceReferences(String resourceConfig)
    {
        if (StringUtil.isBlank(resourceConfig))
            return Collections.<String>emptyList();

        List<String> result = new ArrayList<String>();
        for (String split : resourceConfig.split("(,|\\s)+"))
        {
            split = StringUtil.trim(split);
            if (!StringUtil.isBlank(split))
                result.add(split);
        }

        return result;
    }


//----------------------------------------------------------------------------
//  Internals
//----------------------------------------------------------------------------

    public InputStream openClasspathResource(String name)
    throws IOException
    {
        name = StringUtil.extractRight(name, "classpath:");
        return (war != null)
               ? war.openClasspathFile(name)
               : Thread.currentThread().getContextClassLoader().getResourceAsStream(name);
    }


    public InputStream openFileResource(String name)
    throws IOException
    {
        name = StringUtil.extractRight(name, "file:");
        return new FileInputStream(name);
    }


    public InputStream openWarResource(String name)
    throws IOException
    {
        if (name.startsWith("/"))
            name = name.substring(1);

        return war.openFile(baseDir + name);
    }
}
