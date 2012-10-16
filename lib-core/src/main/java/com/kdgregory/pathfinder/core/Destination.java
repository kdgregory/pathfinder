package com.kdgregory.pathfinder.core;

import java.util.Map;

import com.kdgregory.pathfinder.util.InvocationOptions;

/**
 *  The destination of a URL. The various inspectors will provide their
 *  own implementations, which may contain private functionality.
 */
public interface Destination
{
    /**
     *  Returns a formatted description of this destination, taking into
     *  consideration the specified invocation options.
     */
    public String toString(Map<InvocationOptions, Boolean> options);
}