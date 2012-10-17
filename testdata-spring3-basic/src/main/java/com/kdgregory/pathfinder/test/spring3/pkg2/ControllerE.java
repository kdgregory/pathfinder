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
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;


/**
 *  Controller for testing request parameters. Method {@link #getFoo} uses explicit
 *  parameter definitions, method {@link #getBar} infers parameter types from debug
 *  information.
 */
@Controller
public class ControllerE
{
    @RequestMapping(value="/E1")
    protected ModelAndView getFoo(
            HttpServletRequest request,
            HttpServletResponse response,
            @RequestParam(value="argle",  required=true)                     String argle,
            @RequestParam(value="bargle", required=false)                    Integer bargle,
            @RequestParam(value="wargle", required=false, defaultValue="12") int wargle,
            @RequestParam(value="zargle")                                    Integer zargle)
    throws Exception
    {
        ModelAndView mav = new ModelAndView("simple");
        mav.addObject("reqUrl", request.getRequestURI());
        mav.addObject("controller", getClass().getName());
        mav.addObject("argle", argle);
        mav.addObject("bargle", bargle);
        mav.addObject("wargle", wargle);
        return mav;
    }


    @RequestMapping(value="/E2")
    protected ModelAndView getBar(
            HttpServletRequest request,
            HttpServletResponse response,
            @RequestParam String argle,
            @RequestParam Integer bargle)
    throws Exception
    {
        ModelAndView mav = new ModelAndView("simple");
        mav.addObject("reqUrl", request.getRequestURI());
        mav.addObject("controller", getClass().getName());
        mav.addObject("argle", argle);
        mav.addObject("bargle", bargle);
        return mav;
    }
}
