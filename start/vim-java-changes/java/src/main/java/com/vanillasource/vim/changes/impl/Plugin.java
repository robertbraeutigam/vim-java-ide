/**
 * Copyright (C) 2015 Robert Braeutigam.
 *
 * All rights reserved.
 */

package com.vanillasource.vim.changes.impl;

import com.vanillasource.vim.engine.PluginContext;
import static com.vanillasource.vim.changes.Keys.CHANGES_TRACKER;
import org.apache.log4j.Logger;
import java.io.IOException;

public class Plugin implements com.vanillasource.vim.engine.Plugin {
   private static final Logger logger = Logger.getLogger(Plugin.class);

   @Override
   public void startPlugin(PluginContext context) {
      try {
         WatchServiceChangesTrackerImpl tracker = new WatchServiceChangesTrackerImpl();
         context.set(CHANGES_TRACKER, tracker);
         context.registerCommand("pollChanges", new PollCommand(tracker));
         context.registerCommand("registerPath", new RegisterCommand(tracker));
      } catch (IOException e) {
         logger.error("could not start changes tracker", e);
      }
   }

   @Override
   public void stopPlugin(PluginContext context) {
   }
}
