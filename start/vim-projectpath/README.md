Vim Projectpath Plugin
======================

This plugin to Vim auto-guesses the relevant project paths
mainly for developing software. The plugin introduces following
variables for each buffer read:

 * **b:ProjectVCS**: The type of VCS detected for this project. This is undefined if none found. Possible values: Git, Subversion, Mercurial
 * **b:ProjectVCSRoot**: The root of the VCS project.
 * **b:ProjectBuild**: The type of build infrastructure detected for the project. Undefined if none found, possible values: Maven, Ant, Gradle
 * **b:ProjectBuildRoot**: The topmost directory where the build artifacts are detected.
 * **b:ProjectBuildCurrent**: The submodule directory where the build for the current file seems to be. This may be the same as *b:ProjectBuildRoot* if there are no submodules.

## Installation

If you have [Pathogen](https://github.com/tpope/vim-pathogen), just do

```sh
cd ~/.vim/bundle
git clone git://github.com/robertbraeutigam/vim-projectpath
```

