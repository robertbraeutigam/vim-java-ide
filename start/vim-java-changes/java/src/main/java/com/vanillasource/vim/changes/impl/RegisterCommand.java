/**
 * Copyright (C) 2015 Robert Braeutigam.
 *
 * All rights reserved.
 */

package com.vanillasource.vim.changes.impl;

import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.Map;
import com.vanillasource.vim.engine.Command;
import org.apache.log4j.Logger;
import com.vanillasource.vim.changes.ChangesTracker;

public class RegisterCommand implements Command {
   private static final Logger logger = Logger.getLogger(RegisterCommand.class);
   private ChangesTracker tracker;

   public RegisterCommand(ChangesTracker tracker) {
      this.tracker = tracker;
   }

   @Override
   public String execute(Map<String, String> parameters) {
      try {
         tracker.registerPath(
               parameters.get("listenerId").toString(),
               new File(parameters.get("fileName").toString()),
               Boolean.valueOf(parameters.get("recursive").toString()));
      } catch (IOException e) {
         throw new UncheckedIOException(e);
      }
      return "";
   }
}
