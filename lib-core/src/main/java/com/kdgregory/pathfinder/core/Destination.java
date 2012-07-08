package com.kdgregory.pathfinder.core;

/**
 *  The destination of a URL. The various inspectors will provide their
 *  own implementations, which may contain private functionality.
 */
public interface Destination
{
    /**
     *  Returns a formatted description of this destination. This will
     *  be used for final output.
     */
    @Override
    public String toString();
}