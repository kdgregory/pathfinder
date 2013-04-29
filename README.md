I really like "rake routes" as a debugging tool, and PathFinder is an attempt
to do the same thing with Java web applications (or at least, it will be when
it's done).

Building
========

Pathfinder is built using [Maven](http://maven.apache.org/). This means that you
can clone the repository, type `mvn install`, and everything just works.

Well, almost. The first time that you build (or after any major update), you'll
have to build the test WARs along with the main project:

    mvn -Ptestdata clean install

Subsequent builds can use the default profile:

    mvn clean install

And that should be it. I only push code that builds and runs on my machine, so any problems are on you.


Running
=======

The end of the build is a "shaded" JAR, containing all dependencies. So you can invoke it using the `java -jar` command:

    java -jar app-pathfinder/target/pathfinder-1.0-SNAPSHOT.jar WARFILE

Replacing WARFILE with the Spring3 test WAR, you'll see something like the following:

    > java -jar app-pathfinder/target/pathfinder-1.0-SNAPSHOT.jar testdata-spring-anno/target/pathfinder-testdata-spring-anno-1.0-SNAPSHOT.war 
    /index.jsp                   /index.jsp
    /servlet/B/bar.html  GET     com.kdgregory.pathfinder.test.spring3.pkg2.ControllerB.getBar()
    /servlet/B/baz.html  POST    com.kdgregory.pathfinder.test.spring3.pkg2.ControllerB.setBaz()
    /servlet/D/{id}      GET     com.kdgregory.pathfinder.test.spring3.pkg2.ControllerD.getD()
    ...

There are some options that you can use to customize this output, and invoking without a target
WARfile will display help text:

    invocation: Main [OPTIONS] WARFILE
    
    --showJSP (default)
    --hideJSP
    Display client-accessible JSP files as destination URLs.
    
    --showHTML (default)
    --hideHTML
    Display client-accessible static HTML content as destination URLs.
    
    --showStatic
    --hideStatic (default)
    Display client-accessible static content other than HTML (eg, JS and CSS) as destination URLs.
    
    --showRequestParams
    --hideRequestParams (default)
    For mappings that identify individual request parameters (eg, Spring3) show those parameters in the mapping output.
    
    --debug
    Enable debug-level logging to StdErr.
    
    --verbose
    Enable verbose debugging (implies --debug).


Currently, any errors or warnings are logged to StdErr (normal output goes to StdOut, so you can
redirect as desired).

