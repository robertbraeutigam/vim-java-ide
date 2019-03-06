/**
 * Copyright (C) 2015 Robert Braeutigam.
 *
 * All rights reserved.
 */

package com.vanillasource.vim.build.factory;

import java.io.File;
import java.util.Map;
import java.util.HashMap;
import com.vanillasource.vim.build.Build;
import com.vanillasource.vim.build.BuildFactory;
import org.apache.log4j.Logger;

public abstract class CachingBuildFactory implements BuildFactory {
   private Logger logger = Logger.getLogger(CachingBuildFactory.class);
   private Map<File, Build> cachedBuilds = new HashMap<>(); // Directory -> Build

   /**
    * Override to use the <code>createBuild()</code> method to check whether this factory supports
    * to build.
    * @return True iff there is a cached build or create build does not return null.
    */
   @Override
   public boolean isResponsibleFor(File file) {
      return getCachedBuild(file) != null || createBuild(file) != null;
   }

   @Override
   public final Build getBuild(File file) {
      Build build = getCachedBuild(file);
      if (build == null) {
         build = createBuild(file);
         cachedBuilds.put(build.getBuildDirectory(), build);
         logger.info("caching build '"+build.getName()+"' to root directory: "+build.getBuildDirectory()+" initiated for file: "+file);
      }
      return build;
   }

   private Build getCachedBuild(File file) {
      File directory = file.getParentFile();
      while (directory != null) {
         Build build = cachedBuilds.get(directory);
         if (build != null) {
            return build;
         }
         directory = directory.getParentFile();
      }
      return null;
   }

   /**
    * @return The build if file can be built, or null if this build implementation does not support
    * the given file.
    */
   protected abstract Build createBuild(File file);
}

