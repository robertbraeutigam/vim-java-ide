/**
 * Copyright (C) 2015 Robert Braeutigam.
 *
 * All rights reserved.
 */

package com.vanillasource.vim.build.maven;

import com.vanillasource.vim.build.factory.CachingBuildFactory;
import com.vanillasource.vim.build.Build;
import com.vanillasource.vim.build.context.CompilationContext;
import com.vanillasource.vim.changes.ChangesTracker;
import java.io.File;

public class MavenBuildFactory extends CachingBuildFactory {
   private final CompilationContext context;
   private ChangesTracker changesTracker;

   public MavenBuildFactory(CompilationContext context, ChangesTracker changesTracker) {
      this.context = context;
      this.changesTracker = changesTracker;
   }

   @Override
   protected Build createBuild(File file) {
      File directory = file.getParentFile();
      File pomFile = null;
      File topDirectory = null;
      while (directory != null) {
         File candidatePomFile = new File(directory, "pom.xml");
         if (candidatePomFile.exists()) {
            topDirectory = directory;
            if (pomFile == null) {
               pomFile = candidatePomFile;
            }
         }
         directory = directory.getParentFile();
      }
      if (pomFile == null) {
         return null;
      }
      return new MavenBuild(context, changesTracker, pomFile, topDirectory);
   }
}

