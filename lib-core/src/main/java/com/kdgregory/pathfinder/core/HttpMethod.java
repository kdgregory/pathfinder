package com.kdgregory.pathfinder.core;

/**
 *  Everybody needs their own enum for HTTP methods, right? Well, yeah,
 *  because there isn't one in the JDK. Also because we have an "all"
 *  method, and a homegrown toString().
 */
public enum HttpMethod
{
    ALL(""),
    GET("GET"),
    POST("POST"),
    PUT("PUT"),
    DELETE("DELETE");

    private String stringValue;

    HttpMethod(String stringValue)
    {
        this.stringValue = stringValue;
    }

    @Override
    public String toString()
    {
        return stringValue;
    }
}