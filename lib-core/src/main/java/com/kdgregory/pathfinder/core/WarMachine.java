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

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;

import org.w3c.dom.Document;


/**
 *  Extracts information from the WAR, for use by {@link Inspector}s.
 *  <p>
 *  An implementation may choose to fully extract all information at the time
 *  of construction, or may defer operations. They are not required to be
 *  thread-safe.
 */
public interface WarMachine
{
    /**
     *  Returns the <code>web.xml</code> as a parsed XML DOM. Implementations
     *  may return a shared, modifiable instance of the DOM; callers must not
     *  modify the returned object.
     */
    public Document getWebXml();


    /**
     *  Joins the <code>servlet</code> and <code>servlet-mapping</code> entries from
     *  <code>web.xml</code>. The result is ordered alphabetically.
     */
    public List<ServletMapping> getServletMappings();


    /**
     *  Returns a list of all entries in the WAR, prefixed with a leading slash.
     */
    public List<String> getAllFiles();


    /**
     *  Returns a list of the "public" entries in the WAR: those not under
     *  META-INF or WEB-INF. This is where you'll find the JSPs.
     */
    public List<String> getPublicFiles();


    /**
     *  Returns a list of the "private" entries in the WAR: those under META-INF
     *  and WEB-INF.
     */
    public List<String> getPrivateFiles();


    /**
     *  Returns a stream for the named file, <code>null</code> if the file doesn't
     *  exist. You are encouraged to close this stream, but as it isn't a physical
     *  file handle, there isn't a penalty to pay for not closing it.
     */
    public InputStream openFile(String filename)
    throws IOException;


    /**
     *  Opens a file on the WAR's classpath. First looks in <code>WEB-INF/classes</code>,
     *  then in each of the JARs in <code>lib</code>.
     */
    public InputStream openClasspathFile(String filename)
    throws IOException;



//----------------------------------------------------------------------------
//  Supporting Objects
//----------------------------------------------------------------------------

    /**
     *  Servlet mappings are parsed into objects that implement this interface.
     *  Method names are simple translations of the corresponding element name.
     *  <p>
     *  The natural ordering of this interface is the URL pattern.
     */
    public interface ServletMapping
    extends Comparable<ServletMapping>
    {
        public String getUrlPattern();

        public String getServletName();

        public String getServletClass();

        public Map<String,String> getInitParams();
    }
}
