/**
 * Copyright (C) 2015 Robert Braeutigam.
 *
 * All rights reserved.
 */

package com.vanillasource.vim.changes.impl;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import com.vanillasource.vim.engine.Command;
import com.vanillasource.vim.changes.ChangeEvent;
import com.vanillasource.vim.changes.ChangesTracker;
import org.apache.log4j.Logger;

public class PollCommand implements Command {
   private static final Logger logger = Logger.getLogger(PollCommand.class);
   private ChangesTracker tracker;

   public PollCommand(ChangesTracker tracker) {
      this.tracker = tracker;
   }

   public String execute(Map<String, String> parameters) {
      String listenerId = parameters.get("listenerId").toString();
      List<ChangeEvent> events = tracker.pollChanges(listenerId);
      StringBuilder builder = new StringBuilder();
      for (ChangeEvent event: events) {
         if (builder.length() > 0) {
            builder.append(",");
         }
         try {
            builder.append(event.getFile().getCanonicalPath());
            builder.append("|");
            builder.append(event.getChangeType());
         } catch (IOException e) {
            logger.error("could not get canonical path from file: "+event.getFile(), e);
         }
      }
      return builder.toString();
   }
}
