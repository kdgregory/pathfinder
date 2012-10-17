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

package com.kdgregory.pathfinder.test.spring3.pkg2;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;


/**
 *  This controller is used to test mappings with path variables. It also ensures
 *  that we insert a slash between the request-level mapping and the path-level
 *  mapping.
 */
@Controller
@RequestMapping("/D")
public class ControllerD
{
    @RequestMapping(value="{id}", method=RequestMethod.GET)
    protected ModelAndView getD(
            HttpServletRequest request,
            HttpServletResponse response,
            @PathVariable String id)
    throws Exception
    {
        ModelAndView mav = new ModelAndView("simple");
        mav.addObject("reqUrl", request.getRequestURI());
        mav.addObject("controller", getClass().getName());
        mav.addObject("id", id);
        return mav;
    }}
