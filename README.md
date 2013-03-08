I really like "rake routes" as a debugging tool, and PathFinder is an attempt
to do the same thing with Java web applications (or at least, it will be when
it's done).

Building
========

Pathfinder is built using [Maven](http://maven.apache.org/). This means that you can clone the repository, type `mvn install`, and everything just works.

Well, almost. There are three dependencies on projects that I also maintain, and it's possible that non-release builds will rely on snapshot versions of these
projects.  All of them are available on the [Sonatype OSS](https://oss.sonatype.org/content/repositories/snapshots/) repository, and the POM contains a reference
to this repository for snapshot builds. If you are using a local repository server, either add this repository to its proxy list, update your `settings.xml` to
ignore it, or build the artifacts yourself from the following sources.

* [Practical XML])(http://sourceforge.net/projects/practicalxml/develop)
* [KDG Commons])(http://sourceforge.net/projects/kdgcommons/develop)
* [BCELX](https://github.com/kdgregory/bcelx)

The first time that you build (or after any major update), you'll have to build the test WARs along with the main project:

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

If you want to be overwhelmed with output, edit the `app-pathfinder/src/main/resources/log4j.properties` to select something other than the "null" appender.


Developing
==========
