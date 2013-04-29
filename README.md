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

    > java -jar app-pathfinder/target/pathfinder-1.0-SNAPSHOT.jar testdata-spring3/target/pathfinder-testdata-spring3-1.0-SNAPSHOT.war 
    /index.jsp                              /index.jsp
    /servlet/B/bar.html            GET      com.kdgregory.pathfinder.test.spring3.pkg2.ControllerB
    /servlet/B/baz.html            POST     com.kdgregory.pathfinder.test.spring3.pkg2.ControllerB
    /servlet/foo.html                       com.kdgregory.pathfinder.test.spring3.pkg1.ControllerA

Currently, any errors or warnings are logged to StdErr (normal output goes to StdOut, so you can redirect as desired). If you want to be
overwhelmed with output, edit the `app-pathfinder/src/main/resources/log4j.properties` to enable DEBUG.


Developing
==========
