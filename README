I really like "rake routes" as a debugging tool, and PathFinder is an attempt
to do the same thing with Java web applications (or at least, it will be when
it's done).

Building
========

Pathfinder is built using [Maven](http://maven.apache.org/). This means that you can clone the repository, type `mvn install`, and everything just works.

Well, almost. There are three dependencies on other projects that I also maintain, and it's possible that you'll have to build these dependencies before you can build Pathfinder.

* [Practical XML])(http://sourceforge.net/projects/practicalxml/develop): this is available on Maven Central, and doesn't change too often, so chances are good you don't need to download it.
* [KDG Commons])(http://sourceforge.net/projects/kdgcommons/develop): this is also available on Maven Central, but is being actively modified. Grep the Pathfinder master POM for snapshot versions, or just cross your fingers and build.
* [BCELX](https://github.com/kdgregory/bcelx): still in active development (it's driven by the needs of Pathfinder), so you'll have to download and build. If Git Submodules were a little smarter, it would be automatic.

The first time that you build (or after any major update), you'll have to build the test WARs along with the main project:

    mvn -Ptestdata clean install

Subsequent builds can use the default profile:

    mvn clean install

And that should be it. I only push code that builds and runs on my machine, so any problems are on you.


Running
=======

The end of the build is a "shaded" JAR, containing all dependencies. So you can invoke it using the `java -jar` command:

    java -jar pathfinder/target/pathfinder-1.0-SNAPSHOT.jar WARFILE

Replacing WARFILE with the Spring3 test WAR, you'll see something like the following:

    > java -jar pathfinder/target/pathfinder-1.0-SNAPSHOT.jar testdata-spring3/target/pathfinder-testdata-spring3-1.0-SNAPSHOT.war 
    /index.jsp                              /index.jsp
    /servlet/B/bar.html            GET      com.kdgregory.pathfinder.test.spring3.pkg2.ControllerB
    /servlet/B/baz.html            POST     com.kdgregory.pathfinder.test.spring3.pkg2.ControllerB
    /servlet/foo.html                       com.kdgregory.pathfinder.test.spring3.pkg1.ControllerA

If you want to be overwhelmed with output, take the `log4j.properties` file from any `src/test/resources` directory and put it in `pathfinder/src/main/resources`.


Developing
==========