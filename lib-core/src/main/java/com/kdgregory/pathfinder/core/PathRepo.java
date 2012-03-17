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


/**
 *  Holds all of the paths that have been discovered. A path is a tuple of URL,
 *  HTTP method, and handler class (where "class" is broadly defined to include
 *  JSPs).
 */
public class PathRepo
{
    /**
     *  Everybody needs their own enum for HTTP methods, right? Well, yeah, because
     *  there isn't one in the JDK.
     */
    public enum HttpMethod
    {
        GET, POST, PUT, DELETE
    }
    
    
//----------------------------------------------------------------------------
//  Instance variables and constructor
//----------------------------------------------------------------------------
    
    public PathRepo()
    {
    }

    
//----------------------------------------------------------------------------
//  Public methods
//----------------------------------------------------------------------------
    
//----------------------------------------------------------------------------
//  Private methods
//----------------------------------------------------------------------------
}
