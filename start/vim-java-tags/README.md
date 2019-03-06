VIM-Java Tags
=============

A VIM-Java Bundle for automatically and asyncronously updating *tags* files. This
bundle does not offer and additional commands, rather it is triggered on saving
any Java source file to update the corresponding *tags* file.

If the tags file is not present yet, it will be automatically generated with
all Java sources included for the project. If it is present, all the tags
of the old file will be removed, and the new tags for the newly saved file are added.

The Plugin used is at: https://github.com/robertbraeutigam/vim-java-tags-plugin

## Install

If you are using [Pathogen](https://github.com/tpope/vim-pathogen), just clone this into the 'bundle' directory.

You also need to install the [VIM-Java Backend](https://github.com/robertbraeutigam/vim-java-backend) and the
[Changes Plugin](https://github.com/robertbraeutigam/vim-java-changes)

## Note on changes

At this time the bundle only reacts to changes from insde VIM. If there is a significant
change in multiple source files (like switching to a completely different branch), simply
remove the *.tags* file generated in the root directory of the project to make the plugin
automatically regenerate it on the next save.
