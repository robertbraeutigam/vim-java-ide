/**
 * Copyright (C) 2015 Robert Braeutigam.
 *
 * All rights reserved.
 */

package com.vanillasource.vim.build.gradle;

import com.vanillasource.vim.build.factory.CachingBuildFactory;
import com.vanillasource.vim.build.Build;
import com.vanillasource.vim.build.context.CompilationContext;
import com.vanillasource.vim.changes.ChangesTracker;
import java.io.File;

public class GradleBuildFactory extends CachingBuildFactory {
   private final CompilationContext context;
   private ChangesTracker changesTracker;

   public GradleBuildFactory(CompilationContext context, ChangesTracker changesTracker) {
      this.context = context;
      this.changesTracker = changesTracker;
   }

   @Override
   protected Build createBuild(File file) {
      File directory = file.getParentFile();
      File buildFile = null;
      File topDirectory = null;
      while (directory != null) {
         File candidateBuildFile = new File(directory, "build.gradle");
         if (candidateBuildFile.exists()) {
            topDirectory = directory;
            if (buildFile == null) {
               buildFile = candidateBuildFile;
            }
         }
         directory = directory.getParentFile();
      }
      if (buildFile == null) {
         return null;
      }
      return new GradleBuild(buildFile, topDirectory);
   }
}

