/**
 * Copyright (C) 2015 Robert Braeutigam.
 *
 * All rights reserved.
 */

package com.vanillasource.vim.build.impl;

import com.vanillasource.vim.build.BuildService;
import com.vanillasource.vim.build.BuildFactory;
import com.vanillasource.vim.build.Build;
import com.vanillasource.vim.build.context.CompilationContext;
import com.vanillasource.vim.changes.ChangesTracker;
import com.vanillasource.vim.build.maven.MavenBuildFactory;
import com.vanillasource.vim.build.gradle.GradleBuildFactory;
import java.io.File;
import java.util.List;
import java.util.LinkedList;

public class BuildServiceImpl implements BuildService {
   private List<BuildFactory> buildFactories = new LinkedList<>();

   public BuildServiceImpl(CompilationContext context, ChangesTracker changesTracker) {
      buildFactories.add(new MavenBuildFactory(context, changesTracker));
      buildFactories.add(new GradleBuildFactory(context, changesTracker));
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
