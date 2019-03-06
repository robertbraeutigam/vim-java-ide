VIM-Java Tags Plugin
====================

Enabled automatic updating of the *tags* file for VIM. Plugin uses the `ctags` binary,
so this must be available on the target environment. The plugin should be invoked when
a file was saved.

The plugin has two modes: If the `.tags` file is missing in the root of the project tree,
it generates the full `.tags` file using all Java sources it can find. If the `.tags` file
is present, the tags of the old file are removed, then the new tags appended.

Plugin is asynchronous, so it will not block at all, and will eventually (atomically) replace the `.tags`
file when it's finished updating.
