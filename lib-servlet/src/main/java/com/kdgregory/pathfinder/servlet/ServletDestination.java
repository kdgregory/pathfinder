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

import java.util.Map;

import com.kdgregory.pathfinder.core.Destination;
import com.kdgregory.pathfinder.core.InvocationOptions;

class ServletDestination
implements Destination
{
    private String servletClass;

    public ServletDestination(String servletClass)
    {
        this.servletClass = servletClass;
    }

    @Override
    public boolean isDisplayed(Map<InvocationOptions,Boolean> options)
    {
        return true;
    }

    @Override
    public String toString()
    {
        return servletClass;
    }

    @Override
    public String toString(Map<InvocationOptions,Boolean> options)
    {
        // no options (currently) apply
        return toString();
    }
}