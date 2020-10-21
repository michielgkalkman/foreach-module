# Maven plugin foreach-module
Execute maven goals for modules, not aggregate poms

## Warning

This is a work in progress.

## Credits
This work is largely based on code from the [Maven Release Plugin](https://maven.apache.org/maven-release/maven-release-plugin/). Thanks!

## Introduction

Suppose you have the pom:

```
<project ...
  <modules>
    <module>x</module>
  </modules>
  ...
</project>
```

and module x is not a aggregate pom.

Then:

```
mvn org.taHjaj.wo:foreach-module:foreach -Dforeach.goals="versions:update-parent"
```

will execute maven goals on the pom of module x.

### Arguments

You can provide arguments using -Dforeach.arguments:

```
mvn org.taHjaj.wo:foreach-module:foreach -Dforeach.arguments="-X" -Dforeach.goals="clean"
```

## Issues

### the plugin does not accept -Dkey=value arguments

No, it does, you just need to provide a space between -D and the key=value pair, like this:

```
mvn org.taHjaj.wo:foreach-module:foreach -Dforeach.arguments="-DdryRun=true" -Dforeach.goals=" release:clean release:prepare release:perform"
```

### It hangs

E.g.

```
mvn org.taHjaj.wo:foreach-module:foreach -Dforeach.arguments="-D dryRun=true" -Dforeach.goals=" release:clean release:prepare release:perform"
```

hangs:

```
[Thread-2] [INFO] org.taHjaj.wo.foreach.ForeachModuleMojo - [main] [INFO] org.apache.maven.plugins.release.PrepareReleaseMojo - [prepare dry-run] 3/17 check-dependency-snapshots
[Thread-2] [INFO] org.taHjaj.wo.foreach.ForeachModuleMojo - [main] [INFO] org.apache.maven.shared.release.phase.CheckDependencySnapshotsPhase - Checking dependencies and plugins for snapshots ...
[Thread-2] [INFO] org.taHjaj.wo.foreach.ForeachModuleMojo - [main] [INFO] org.apache.maven.plugins.release.PrepareReleaseMojo - [prepare dry-run] 4/17 create-backup-poms
[Thread-2] [INFO] org.taHjaj.wo.foreach.ForeachModuleMojo - [main] [INFO] org.apache.maven.plugins.release.PrepareReleaseMojo - [prepare dry-run] 5/17 map-release-versions
```

That is because one of the goals expects input. In case of the release plugin, simply add the -B flag:

```
mvn org.taHjaj.wo:foreach-module:foreach -Dforeach.arguments="-B -D dryRun=true" -Dforeach.goals=" release:clean release:prepare release:perform"
```

## Repository

Sorry, there is no public repository yet where foreach-module can be found.

Copy the sources and install it locally with:

```
mvn install
```

