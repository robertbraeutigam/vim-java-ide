/**
 * Copyright (C) 2015 Robert Braeutigam.
 *
 * All rights reserved.
 */

package com.vanillasource.vim.build.impl;

import com.vanillasource.vim.build.BuildService;
import com.vanillasource.vim.build.BuildFactory;
import com.vanillasource.vim.build.Build;
import com.vanillasource.vim.changes.ChangesTracker;
import com.vanillasource.vim.build.maven.MavenBuildFactory;
import java.io.File;
import java.util.List;
import java.util.LinkedList;

public class BuildServiceImpl implements BuildService {
   private List<BuildFactory> buildFactories = new LinkedList<>();

   public BuildServiceImpl(ChangesTracker changesTracker) {
      buildFactories.add(new MavenBuildFactory(changesTracker));
   }

   @Override
   public Build getBuild(File file) {
      for (BuildFactory factory: buildFactories) {
         if (factory.isResponsibleFor(file)) {
            return factory.getBuild(file);
         }
      }
      return null;
   }
}
