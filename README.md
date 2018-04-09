# sbt-jpms

 [ ![Download](https://api.bintray.com/packages/retronym/sbt-plugins/sbt-jpms/images/download.svg) ](https://bintray.com/retronym/sbt-plugins/sbt-jpms/_latestVersion)
 [![Build Status](https://travis-ci.org/retronym/sbt-jpms.svg?branch=master)](https://travis-ci.org/retronym/sbt-jpms)


Provides experimental support for [JEP 261: Module System](http://openjdk.java.net/jeps/261), by using
the `--module-path` and related settings when compiling and executing tests/code from sub projects with
this plugin enabled.

## Caveats

  - Still under development, bugs are likely, and API will change.
  - Compiling module-info.java files in SBT is not supported until [sbt/zinc#522](https://github.com/sbt/zinc/pull/522) is included in a release
  - Forked mode is required
  - `scalac` support for for JPMS is still pending (but will be integrated here once available)

## Usage

### Adding plugin

```
// project/plugins.sbt
addSbtPlugin("com.lightbend.sbt" % "sbt-jpms" % "0.1.0")
```

### Adding plugin settings to a subproject

```
val foo = project.enablePlugins(JpmsPlugin)
```

### Configuring the module name for a scope

`Compile` and `Test` scopes use the same module name. This allows "whitebox testing", where
tests can access non-exported packages. The plugin will add `--patch-module`, `--add-reads`
and `--add-modules` as needed to make this work.

```
jpmsModuleName := Some("acme.foo")
```

### Separate module name for tests

```
jpmsModuleName in Compile := Some("amce.foo")
jpmsModuleName in Compile := Some("amce.foo.tests")
```


Or

```
jpmsModuleName in Compile := Some("amce.foo")
jpmsModuleName in Compile := None // Will consume the module on the classpath
```

### Associating Module Names with library dependencies

```
val junitJupiterApiDep = "org.junit.jupiter" % "junit-jupiter-api" % jupiterVersion % Test jpmsName "org.junit.jupiter.api"
```


Maintainer
----------

This project is maintained by Jason Zaugg (Scala team, Lightbend, [@retronym](https://github.com/retronym))

Contributing
------------

Yes, pull requests and opening issues is very welcome!

Please test your changes using `sbt scripted`.

License
-------

This plugin is released under the **Apache 2.0 License**
