VIM-Java Build Plugin
=====================

This VIM-Java Plugin enbales the automatic compilation of Java Sources through VIM.

## Supported Build Systems

Currently the plugin only supports building Maven-based projects.

## How it works

When first called, the plugin starts a maven build ("clean test-compile") to get all the
necessary parameters from Maven like Classpath, Sourcepath and other settings. It also
compiles all sources on this run, however on subsequent calls all the available parameters
are re-used to quickly compile the single source file given.

This way a single compilation takes at most 100ms or so, regardless of project size, which
then can be done online during a VIM save buffer event transparently.

If the *pom.xml* file changes the plugin drops all parameters and invokes a clean maven build again.

The plugin uses the [Changes Plugin](https://github.com/robertbraeutigam/vim-java-changes-plugin)
to detect changes in the pom and all the source files.
