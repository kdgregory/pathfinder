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

package com.kdgregory.pathfinder.servlet;

import org.junit.Test;
import static org.junit.Assert.*;

import com.kdgregory.pathfinder.core.PathRepo;
import com.kdgregory.pathfinder.core.PathRepo.HttpMethod;
import com.kdgregory.pathfinder.core.WarMachine;
import com.kdgregory.pathfinder.test.WarNames;
import com.kdgregory.pathfinder.util.TestHelpers;


public class TestServletInspector
{
    @Test
    public void testHappyPath() throws Exception
    {
        WarMachine machine = TestHelpers.createWarMachine(WarNames.SERVLET);
        PathRepo repo = new PathRepo();

        (new ServletInspector()).inspect(machine, repo);

        assertEquals("com.example.servlet.SomeServlet", repo.get("/servlet", HttpMethod.ALL).toString());
        assertEquals("com.example.servlet.SomeServlet", repo.get("/servlet2", HttpMethod.ALL).toString());
        assertEquals("/index.jsp",                      repo.get("/index.jsp", HttpMethod.ALL).toString());
        assertEquals("/subdir/index.jsp",               repo.get("/subdir/index.jsp", HttpMethod.ALL).toString());
        assertEquals("/index.html",                     repo.get("/index.html", HttpMethod.ALL).toString());

        // and verify that we don't know nothing about the hidden JSP
        assertNull(repo.get("/WEB-INF/views/hidden.jsp", HttpMethod.ALL));
    }
}
