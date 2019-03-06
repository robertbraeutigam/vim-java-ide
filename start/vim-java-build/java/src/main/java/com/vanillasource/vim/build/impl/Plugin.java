/**
 * Copyright (C) 2015 Robert Braeutigam.
 *
 * All rights reserved.
 */

package com.vanillasource.vim.build.impl;

import com.vanillasource.vim.engine.PluginContext;
import com.vanillasource.vim.changes.ChangesTracker;
import static com.vanillasource.vim.changes.Keys.CHANGES_TRACKER;
import static com.vanillasource.vim.build.Keys.BUILD_SERVICE;

public class Plugin implements com.vanillasource.vim.engine.Plugin {
   @Override
   public void startPlugin(PluginContext context) {
      ChangesTracker changesTracker = context.get(CHANGES_TRACKER);
      BuildServiceImpl buildService = new BuildServiceImpl(changesTracker);
      context.set(BUILD_SERVICE, buildService);
      context.registerCommand("getBuildParameters", new BuildCommand(buildService));
      context.registerCommand("make", new MakeCommand(buildService));
      context.registerCommand("compile", new CompileCommand(buildService));
   }

   @Override
   public void stopPlugin(PluginContext context) {
   }
}
