/**
 * Copyright (C) 2015 Robert Braeutigam.
 *
 * All rights reserved.
 */

package com.vanillasource.vim.build.maven;

import com.vanillasource.vim.build.factory.CachingBuildFactory;
import com.vanillasource.vim.build.Build;
import com.vanillasource.vim.changes.ChangesTracker;
import java.io.File;

public class MavenBuildFactory extends CachingBuildFactory {
   private ChangesTracker changesTracker;

   public MavenBuildFactory(ChangesTracker changesTracker) {
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
      return new MavenBuild(changesTracker, pomFile, topDirectory);
   }
}

