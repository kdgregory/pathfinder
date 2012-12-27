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


/**
 *  Constants related to Spring implementations: classnames, locations, &c.
 */
package com.kdgregory.pathfinder.spring;

public class SpringConstants
{
    public final static String DISPATCHER_SERVLET_CLASS     = "org.springframework.web.servlet.DispatcherServlet";
    public final static String CONTEXT_LISTENER_CLASS       = "org.springframework.web.context.ContextLoaderListener";
    public final static String SIMPLE_URL_HANDLER_CLASS     = "org.springframework.web.servlet.handler.SimpleUrlHandlerMapping";
    public final static String BEAN_NAME_HANDLER_CLASS      = "org.springframework.web.servlet.handler.BeanNameUrlHandlerMapping";
    public final static String CLASS_NAME_HANDLER_CLASS     = "org.springframework.web.servlet.mvc.support.ControllerClassNameHandlerMapping";
    public final static String CONTROLLER_INTERFACE         = "org.springframework.web.servlet.mvc.Controller";
    public final static String CONTROLLER_ANNO_CLASS        = "org.springframework.stereotype.Controller";
    public final static String REQUEST_MAPPING_ANNO_CLASS   = "org.springframework.web.bind.annotation.RequestMapping";
}
