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

public class CompileCommand implements Command {
   private BuildService buildService;

   public CompileCommand(BuildService buildService) {
      this.buildService = buildService;
   }

   /**
    * Run the compile, and add messages to the vim quickfix list.
    */
   @Override
   public String execute(Map<String, String> parameters) {
      try {
         File file = new File(parameters.get("fileName"));
         Build build = buildService.getBuild(file);
         if (build != null) {
            return toQuickfix(build.compile(file));
         }
         return "";
      } catch (IOException e) {
         throw new UncheckedIOException(e);
      }
   }

   private String toQuickfix(List<FileMessage> messages) {
      final StringBuilder builder = new StringBuilder("call setqflist([");
      boolean first = true;
      for (FileMessage message: messages) {
         if (first) {
            first = false;
         } else {
            builder.append(",");
         }
         builder.append("{'filename':'"+message.getFile().getAbsolutePath()+"', 'type':'"+message.getSeverity().getVimIndicator()+"', 'lnum':'"+message.getRow()+"', 'col':'"+message.getColumn()+"', 'text':'"+escape(message.getMessage())+"'}");
      }
      builder.append("], 'r')");
      return builder.toString();
   }

   private String escape(String message) {
      return message.replaceAll("'", "`").replaceAll("\"", "`");
   }
}

