/*
 * SomeServlet.java
 *
 * Created on April 15, 2012, 7:11 AM
 */

package com.example.servlet;

import java.io.*;
import java.net.*;

import javax.servlet.*;
import javax.servlet.http.*;

/**
 *
 * @author kgregory
 * @version
 */
public class SomeServlet extends HttpServlet
{
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
    throws ServletException, IOException
    {
        response.setContentType("text/plain");
        PrintWriter out = response.getWriter();
        out.println("you reached the servlet via GET");
        out.close();
    }


    protected void doPost(HttpServletRequest request, HttpServletResponse response)
    throws ServletException, IOException
    {
        response.setContentType("text/plain");
        PrintWriter out = response.getWriter();
        out.println("you reached the servlet via POST");
        out.close();
    }


    public String getServletInfo()
    {
        return "Test servlet";
    }
}
