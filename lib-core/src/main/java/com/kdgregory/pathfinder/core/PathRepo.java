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

import java.util.Collections;
import java.util.Iterator;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;


/**
 *  Holds all of the paths that have been discovered. A path is a tuple of URL,
 *  HTTP method, and destination (which may be a handler class, or a JSP). URLs
 *  are relative to the context root.
 *  <p>
 *  The storage mechanism is a map-of-maps, with the URL as outer key. Inspectors
 *  are allowed (and expected) to completely replace the destinations that belong
 *  to a particular URL. Both maps are sorted, to result in consisted output.
 *  <p>
 *  This class is not intended for use by concurrent threads.
 */
public class PathRepo
implements Iterable<String>
{
    /**
     *  Everybody needs their own enum for HTTP methods, right? Well, yeah,
     *  because there isn't one in the JDK. Also because we have an "all"
     *  method, and a homegrown toString().
     */
    public enum HttpMethod
    {
        ALL(""),
        GET("GET"),
        POST("POST"),
        PUT("PUT"),
        DELETE("DELETE");

        private String stringValue;

        HttpMethod(String stringValue)
        {
            this.stringValue = stringValue;
        }

        @Override
        public String toString()
        {
            return stringValue;
        }
    }


    /**
     *  The destination of a URL. The various inspectors will provide their
     *  own implementations, which may contain private functionality.
     */
    public interface Destination
    {
        /**
         *  Returns a formatted description of this destination. This will
         *  be used for final output.
         */
        @Override
        public String toString();
    }


//----------------------------------------------------------------------------
//  Instance variables and constructor
//----------------------------------------------------------------------------

    private SortedMap<String,SortedMap<HttpMethod,Destination>> urlMap
            = new TreeMap<String,SortedMap<HttpMethod,Destination>>();


//----------------------------------------------------------------------------
//  Public methods
//----------------------------------------------------------------------------

    /**
     *  Stores a destination that responds to all request methods. Will replace
     *  all existing destinations for the URL.
     */
    public void put(String url, Destination dest)
    {
        getOrCreateDestMap(url).clear();
        put(url, HttpMethod.ALL, dest);
    }


    /**
     *  Stores a destination that responds to a specific request method. If
     *  there is an existing "all" entry, it will be overridden for just the
     *  method stored.
     */
    public void put(String url, HttpMethod method, Destination dest)
    {
        SortedMap<HttpMethod,Destination> destMap = getOrCreateDestMap(url);
        destMap.put(method, dest);
    }


    /**
     *  Stores a map of destinations, replacing the existing map for that URL.
     */
    public void put(String url, Map<HttpMethod,Destination> destMap)
    {
        SortedMap<HttpMethod,Destination> internal = getOrCreateDestMap(url);
        internal.clear();
        internal.putAll(destMap);
    }


    /**
     *  Retrieves the destination for a given URL and method. If the URL has
     *  been stored with "ALL" methods, will return that destination unless
     *  the URL has also been stored with a specific method. Returns null if
     *  there's no destination for a URL and method.
     */
    public Destination get(String url, HttpMethod method)
    {
        SortedMap<HttpMethod,Destination> destMap = getOrCreateDestMap(url);
        Destination dest = destMap.get(method);
        if (dest != null)
            return dest;

        return destMap.get(HttpMethod.ALL);
    }


    /**
     *  Retrieves an unmodifiable view of the destination map for a given URL.
     *  The returned map will be sorted by method.
     *  <p>
     *  Note 1: this method will never return null, but the map may be empty.
     *  <p>
     *  Note 2: the map may contain a single mapping, for "ALL"; the caller
     *          is responsible for translating this to individual mappings
     *          if desired.
     */
    public Map<HttpMethod,Destination> get(String url)
    {
        return Collections.unmodifiableMap(getOrCreateDestMap(url));
    }


    /**
     *  Removes the destination(s) associated with the given URL and method.
     *  If the passed method is "ALL", will remove all destinations (even if
     *  they were added individually).
     */
    public void remove(String url, HttpMethod method)
    {
        Map<HttpMethod,Destination> destMap = urlMap.get(url);
        if (destMap == null)
            return;

        if (method.equals(HttpMethod.ALL))
        {
            destMap.clear();
            return;
        }

        if (destMap.containsKey(HttpMethod.ALL))
        {
            Destination dest = destMap.remove(HttpMethod.ALL);
            destMap.put(HttpMethod.GET, dest);
            destMap.put(HttpMethod.POST, dest);
            destMap.put(HttpMethod.PUT, dest);
            destMap.put(HttpMethod.DELETE, dest);
        }

        destMap.remove(method);
    }


    /**
     *  Returns an iterator over the URLs in this repository. These URLs
     *  will be sorted in alphanumeric order.
     */
    @Override
    public Iterator<String> iterator()
    {
        return Collections.unmodifiableMap(urlMap).keySet().iterator();
    }


    /**
     *  Returns a string containing the comma-separated URLs in this repo.
     */
    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder(64 * urlMap.size());
        sb.append("[");
        for (String url : urlMap.keySet())
        {
            if (sb.length() > 1)
                sb.append(",");
            sb.append(url);
        }
        sb.append("]");
        return sb.toString();
    }


//----------------------------------------------------------------------------
//  Private methods
//----------------------------------------------------------------------------

    private SortedMap<HttpMethod,Destination> getOrCreateDestMap(String url)
    {
        SortedMap<HttpMethod,Destination> destMap = urlMap.get(url);
        if (destMap == null)
        {
            destMap = new TreeMap<PathRepo.HttpMethod,PathRepo.Destination>();
            urlMap.put(url, destMap);
        }
        return destMap;
    }
}
