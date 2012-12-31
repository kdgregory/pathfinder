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

package com.kdgregory.pathfinder.test.scan.controller;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;


@Controller("myController")
public class ControllerA
{
    @RequestMapping(value="/foo")
    protected ModelAndView getFoo(
            HttpServletRequest request,
            HttpServletResponse response)
    throws Exception
    {
        ModelAndView mav = new ModelAndView("simple");
        mav.addObject("reqUrl", request.getRequestURI());
        mav.addObject("controller", getClass().getName());
        return mav;
    }
}
