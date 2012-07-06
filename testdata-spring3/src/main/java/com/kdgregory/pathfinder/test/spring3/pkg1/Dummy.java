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

package com.kdgregory.pathfinder.test.spring3.pkg1;

import java.util.HashMap;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;


/**
 *  A class that doesn't have a Controller annotation, but does have request
 *  mappings. The SpringInspector should ignore it.
 */
public class Dummy
{
    // note: same request mapping as ControllerA; should be ignored
    @RequestMapping(value="/foo.html", method=RequestMethod.GET)
    protected ModelAndView basicGet(
            HttpServletRequest request,
            HttpServletResponse response)
    throws Exception
    {
        return new ModelAndView("simple", "data", new HashMap<Object,Object>());
    }
}
