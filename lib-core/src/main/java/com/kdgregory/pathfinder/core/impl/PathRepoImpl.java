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

package com.kdgregory.pathfinder.core.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

import com.kdgregory.pathfinder.core.Destination;
import com.kdgregory.pathfinder.core.HttpMethod;
import com.kdgregory.pathfinder.core.PathRepo;


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
public class PathRepoImpl
implements PathRepo
{
    private SortedMap<String,SortedMap<HttpMethod,Destination>> urlMap
            = new TreeMap<String,SortedMap<HttpMethod,Destination>>();


//----------------------------------------------------------------------------
//  PathRepo
//----------------------------------------------------------------------------

    @Override
    public void put(String url, Destination dest)
    {
        getOrCreateDestMap(url).clear();
        put(url, HttpMethod.ALL, dest);
    }


    @Override
    public void put(String url, HttpMethod method, Destination dest)
    {
        SortedMap<HttpMethod,Destination> destMap = getOrCreateDestMap(url);
        destMap.put(method, dest);
    }


    @Override
    public void put(String url, Map<HttpMethod,Destination> destMap)
    {
        SortedMap<HttpMethod,Destination> internal = getOrCreateDestMap(url);
        internal.clear();
        internal.putAll(destMap);
    }


    @Override
    public Destination get(String url, HttpMethod method)
    {
        SortedMap<HttpMethod,Destination> destMap = getOrCreateDestMap(url);
        Destination dest = destMap.get(method);
        if (dest != null)
            return dest;

        return destMap.get(HttpMethod.ALL);
    }


    @Override
    public Map<HttpMethod,Destination> get(String url)
    {
        return Collections.unmodifiableMap(getOrCreateDestMap(url));
    }


    @Override
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


    @Override
    public Iterator<String> iterator()
    {
        List<String> urls = new ArrayList<String>(urlMap.size());
        for (Map.Entry<String,SortedMap<HttpMethod,Destination>> entry : urlMap.entrySet())
        {
            if (entry.getValue().size() > 0)
                urls.add(entry.getKey());
        }
        return urls.iterator();
    }


    @Override
    public int urlCount()
    {
        int count = 0;
        for (Map.Entry<String,SortedMap<HttpMethod,Destination>> entry : urlMap.entrySet())
        {
            count += (entry.getValue().size() > 0) ? 1 : 0;
        }

        return count;
    }


//----------------------------------------------------------------------------
//  Otheer Public Methods
//----------------------------------------------------------------------------

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
            destMap = new TreeMap<HttpMethod,Destination>();
            urlMap.put(url, destMap);
        }
        return destMap;
    }
}
