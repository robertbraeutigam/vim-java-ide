/**
 * Copyright (C) 2015 Robert Braeutigam.
 *
 * All rights reserved.
 */

package com.vanillasource.vim.build.impl;

import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.List;
import java.util.LinkedList;
import java.util.Map;
import com.vanillasource.vim.engine.Command;
import com.vanillasource.vim.build.maven.MavenBuildFactory;
import com.vanillasource.vim.build.*;
import com.vanillasource.vim.changes.ChangesTracker;

public class MakeCommand implements Command {
   private BuildService buildService;

   public MakeCommand(BuildService buildService) {
      this.buildService = buildService;
   }

   /**
    * Run a compile task in make-like mode. That is, output errors as separate lines instead of
    * vim commands. It is expected that this will be invoked through the vim 'make' command.
    */
   @Override
   public String execute(Map<String, String> parameters) {
      try {
         File file = new File(parameters.get("fileName"));
         Build build = buildService.getBuild(file);
         if (build != null) {
            return toResponse(build.compile(file));
         }
         return "";
      } catch (IOException e) {
         throw new UncheckedIOException(e);
      }
   }

   private String toResponse(List<FileMessage> messages) {
      final StringBuilder builder = new StringBuilder();
      for (FileMessage message: messages) {
         builder.append(message.getFile().getName()+":"+message.getSeverity().getVimIndicator()+":"+message.getRow()+":"+message.getColumn()+":"+message.getMessage()+"\n");
      }
      return builder.toString();
   }
}

