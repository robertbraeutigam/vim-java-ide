/**
 * Copyright (C) 2015 Robert Braeutigam.
 *
 * All rights reserved.
 */

package com.vanillasource.vim.tags.impl;

import com.vanillasource.vim.engine.PluginContext;
import com.vanillasource.vim.changes.ChangesTracker;
import com.vanillasource.vim.build.BuildService;
import static com.vanillasource.vim.changes.Keys.CHANGES_TRACKER;
import static com.vanillasource.vim.build.Keys.BUILD_SERVICE;

public class Plugin implements com.vanillasource.vim.engine.Plugin {
   @Override
   public void startPlugin(PluginContext context) {
      ChangesTracker changesTracker = context.get(CHANGES_TRACKER);
      BuildService buildService = context.get(BUILD_SERVICE);
      context.registerCommand("updateTags", new TagsCommand(changesTracker, buildService));
   }

   @Override
   public void stopPlugin(PluginContext context) {
   }
}

