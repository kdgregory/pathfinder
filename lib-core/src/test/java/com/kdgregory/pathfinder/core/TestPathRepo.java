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

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.junit.Test;
import static org.junit.Assert.*;

import com.kdgregory.pathfinder.core.PathRepo.Destination;
import com.kdgregory.pathfinder.core.PathRepo.HttpMethod;


public class TestPathRepo
{
//----------------------------------------------------------------------------
//  Test Data
//----------------------------------------------------------------------------

    private final static String URL_1           = "/foo";
    private final static String URL_2           = "/bar";
    private final static String URL_3           = "/baz";

    private final static MyDestination DEST_1   = new MyDestination();
    private final static MyDestination DEST_2   = new MyDestination();

//----------------------------------------------------------------------------
//  Support Code
//----------------------------------------------------------------------------

    private static class MyDestination
    implements PathRepo.Destination
    {
        // nothing here; we check for instance identity
    }

//----------------------------------------------------------------------------
//  TestCases
//----------------------------------------------------------------------------

    @Test
    public void testMethodStrings() throws Exception
    {
        assertEquals("ALL", "",          HttpMethod.ALL.toString());
        assertEquals("GET", "GET",       HttpMethod.GET.toString());
        assertEquals("POST", "POST",     HttpMethod.POST.toString());
        assertEquals("PUT", "PUT",       HttpMethod.PUT.toString());
        assertEquals("DELETE", "DELETE", HttpMethod.DELETE.toString());
    }


    @Test
    public void testPutAndGetWithExplicitMethod() throws Exception
    {
        PathRepo repo = new PathRepo();

        repo.put(URL_1, HttpMethod.GET, DEST_1);
        assertSame("can retrive", DEST_1, repo.get(URL_1, HttpMethod.GET));
        assertNull("no entry for arbitrary method", repo.get(URL_1, HttpMethod.POST));

        repo.put(URL_1, HttpMethod.POST, DEST_2);
        assertSame("first destination not affected", DEST_1, repo.get(URL_1, HttpMethod.GET));
        assertSame("can retrieve new destination",   DEST_2, repo.get(URL_1, HttpMethod.POST));
        assertNull("no entry for arbitrary method", repo.get(URL_1, HttpMethod.DELETE));

        repo.put(URL_1, HttpMethod.GET, DEST_2);
        assertSame("first destination overwritten", DEST_2, repo.get(URL_1, HttpMethod.GET));

        repo.put(URL_2, HttpMethod.GET, DEST_1);
        assertSame("second url written", DEST_1,    repo.get(URL_2, HttpMethod.GET));
        assertSame("doesn't affect first", DEST_2,  repo.get(URL_1, HttpMethod.GET));
        assertNull("no entry for arbitrary",        repo.get(URL_2, HttpMethod.POST));

        // getting nonexistent URL shouldn't throw NPE
        assertNull(repo.get(URL_3, HttpMethod.GET));
    }


    @Test
    public void testPutAndGetAll() throws Exception
    {
        PathRepo repo = new PathRepo();

        repo.put(URL_1, DEST_1);

        assertEquals("retrieve via ALL",    DEST_1, repo.get(URL_1, HttpMethod.ALL));
        assertEquals("retrieve via GET",    DEST_1, repo.get(URL_1, HttpMethod.GET));
        assertEquals("retrieve via POST",   DEST_1, repo.get(URL_1, HttpMethod.POST));
        assertEquals("retrieve via PUT",    DEST_1, repo.get(URL_1, HttpMethod.PUT));
        assertEquals("retrieve via DELETE", DEST_1, repo.get(URL_1, HttpMethod.DELETE));
    }


    @Test
    public void testPutExplicitOverridesAll() throws Exception
    {
        PathRepo repo = new PathRepo();

        repo.put(URL_1, DEST_1);
        repo.put(URL_1, HttpMethod.GET, DEST_2);

        assertEquals("retrieve via ALL",    DEST_1, repo.get(URL_1, HttpMethod.ALL));
        assertEquals("retrieve via GET",    DEST_2, repo.get(URL_1, HttpMethod.GET));
        assertEquals("retrieve via POST",   DEST_1, repo.get(URL_1, HttpMethod.POST));
        assertEquals("retrieve via PUT",    DEST_1, repo.get(URL_1, HttpMethod.PUT));
        assertEquals("retrieve via DELETE", DEST_1, repo.get(URL_1, HttpMethod.DELETE));
    }


    @Test
    public void testPutAllOverridesExplicit() throws Exception
    {
        PathRepo repo = new PathRepo();

        repo.put(URL_1, HttpMethod.GET, DEST_2);
        repo.put(URL_1, DEST_1);

        assertEquals("retrieve via ALL",    DEST_1, repo.get(URL_1, HttpMethod.ALL));
        assertEquals("retrieve via GET",    DEST_1, repo.get(URL_1, HttpMethod.GET));
        assertEquals("retrieve via POST",   DEST_1, repo.get(URL_1, HttpMethod.POST));
        assertEquals("retrieve via PUT",    DEST_1, repo.get(URL_1, HttpMethod.PUT));
        assertEquals("retrieve via DELETE", DEST_1, repo.get(URL_1, HttpMethod.DELETE));
    }


    @Test
    public void testGetDestinationMap() throws Exception
    {
        PathRepo repo = new PathRepo();
        repo.put(URL_1, DEST_1);
        repo.put(URL_1, HttpMethod.GET, DEST_2);

        Map<HttpMethod,Destination> destMap1 = repo.get(URL_1);
        assertEquals("url 1 entry count", 2, destMap1.size());
        assertSame("contains ALL", DEST_1, destMap1.get(HttpMethod.ALL));
        assertSame("contains GET", DEST_2, destMap1.get(HttpMethod.GET));

        Map<HttpMethod,Destination> destMap2 = repo.get(URL_2);
        assertEquals("url 2 entry count", 0, destMap2.size());
    }


    @Test
    public void testPutDestinationMap() throws Exception
    {
        PathRepo repo = new PathRepo();
        repo.put(URL_1, DEST_2);
        repo.put(URL_1, HttpMethod.GET, DEST_1);

        Map<HttpMethod,Destination> destMap = new HashMap<PathRepo.HttpMethod,PathRepo.Destination>();
        destMap.put(HttpMethod.GET, DEST_2);
        destMap.put(HttpMethod.POST, DEST_1);

        repo.put(URL_1, destMap);

        assertNull("retrieve via ALL",            repo.get(URL_1, HttpMethod.ALL));
        assertEquals("retrieve via GET",  DEST_2, repo.get(URL_1, HttpMethod.GET));
        assertEquals("retrieve via POST", DEST_1, repo.get(URL_1, HttpMethod.POST));
        assertNull("retrieve via PUT",            repo.get(URL_1, HttpMethod.PUT));
        assertNull("retrieve via DELETE",         repo.get(URL_1, HttpMethod.DELETE));
    }


    @Test
    public void testPutDestinationMapMakesCopy() throws Exception
    {
        PathRepo repo = new PathRepo();

        Map<HttpMethod,Destination> destMap = new HashMap<PathRepo.HttpMethod,PathRepo.Destination>();
        destMap.put(HttpMethod.GET, DEST_1);

        repo.put(URL_1, destMap);

        assertNull("retrieve via ALL",            repo.get(URL_1, HttpMethod.ALL));
        assertEquals("retrieve via GET",  DEST_1, repo.get(URL_1, HttpMethod.GET));
        assertNull("retrieve via POST",           repo.get(URL_1, HttpMethod.POST));
        assertNull("retrieve via PUT",            repo.get(URL_1, HttpMethod.PUT));
        assertNull("retrieve via DELETE",         repo.get(URL_1, HttpMethod.DELETE));

        destMap.put(HttpMethod.POST, DEST_1);

        assertNull("retrieve via ALL",            repo.get(URL_1, HttpMethod.ALL));
        assertEquals("retrieve via GET",  DEST_1, repo.get(URL_1, HttpMethod.GET));
        assertNull("retrieve via POST",           repo.get(URL_1, HttpMethod.POST));
        assertNull("retrieve via PUT",            repo.get(URL_1, HttpMethod.PUT));
        assertNull("retrieve via DELETE",         repo.get(URL_1, HttpMethod.DELETE));
    }


    @Test
    public void testIterator() throws Exception
    {
        PathRepo repo = new PathRepo();
        repo.put(URL_1, DEST_1);
        repo.put(URL_2, HttpMethod.GET, DEST_1);
        repo.put(URL_3, HttpMethod.POST, DEST_1);

        Iterator<String> urlItx = repo.iterator();
        assertEquals(URL_2, urlItx.next());
        assertEquals(URL_3, urlItx.next());
        assertEquals(URL_1, urlItx.next());
        assertFalse(urlItx.hasNext());
    }
}