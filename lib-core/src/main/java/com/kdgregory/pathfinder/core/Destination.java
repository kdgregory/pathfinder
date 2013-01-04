package com.kdgregory.pathfinder.core;

import java.util.Map;


/**
 *  The destination of a URL. The various inspectors will provide their
 *  own implementations, which may contain private functionality.
 */
public interface Destination
{
    /**
     *  Returns an indication of whether this destination should be
     *  displayed at all, given the current invocation options.
     */
    public boolean isDisplayed(Map<InvocationOptions, Boolean> options);

    /**
     *  Returns a formatted description of this destination, taking into
     *  consideration the specified invocation options.
     */
    public String toString(Map<InvocationOptions, Boolean> options);
}