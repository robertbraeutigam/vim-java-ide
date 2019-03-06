Vim Java Build
==============

This VIM-Java bundle enables automatic (re-)compilation of Java sources triggered
by saving the Java file. There are no VIM commands exposed directly, rather this
seemlessly blends into the VIM workflow. 

This bundle uses the [Changes Plugin](https://github.com/robertbraeutigam/vim-java-changes)
to watch for filesystem changes, and it always recompiles all sources that changed
since the last save. If no files changed, the local file being saved is always recompiled.

All compilations errors will be added to the Quickfix list, so other plugins can be used to
process / visualize the errors as needed.

Uses the Plugin: https://github.com/robertbraeutigam/vim-java-build-plugin

## Install

If you are using [Pathogen](https://github.com/tpope/vim-pathogen), just clone this into the 'bundle' directory.

You also need to install the [VIM-Java Backend](https://github.com/robertbraeutigam/vim-java-backend) and the
[Changes Plugin](https://github.com/robertbraeutigam/vim-java-changes)

## Usage

There are no additional commands, compile will be triggered on save.

