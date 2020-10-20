# Maven plugin foreach-module
Execute maven goals for modules, not aggregate poms

## Warning

This is a work in progress.

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

## Repository

Sorry, there is no public repository yet where foreach-module can be found.

Install it locally with:

```
mvn install
```

