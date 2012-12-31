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
    public final static String CLASS_DISPATCHER_SERVLET     = "org.springframework.web.servlet.DispatcherServlet";
    public final static String CLASS_CONTEXT_LISTENER       = "org.springframework.web.context.ContextLoaderListener";
    public final static String CLASS_SIMPLE_URL_HANDLER     = "org.springframework.web.servlet.handler.SimpleUrlHandlerMapping";
    public final static String CLASS_BEAN_NAME_HANDLER      = "org.springframework.web.servlet.handler.BeanNameUrlHandlerMapping";
    public final static String CLASS_CLASS_NAME_HANDLER     = "org.springframework.web.servlet.mvc.support.ControllerClassNameHandlerMapping";
    public final static String INTF_CONTROLLER              = "org.springframework.web.servlet.mvc.Controller";
    public final static String ANNO_CONTROLLER              = "org.springframework.stereotype.Controller";
    public final static String ANNO_COMPONENT               = "org.springframework.stereotype.Component";
    public final static String ANNO_REQUEST_MAPPING         = "org.springframework.web.bind.annotation.RequestMapping";
}
