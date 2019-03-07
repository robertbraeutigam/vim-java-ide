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
import java.util.stream.Collectors;
import java.util.LinkedList;
import java.util.Map;
import com.vanillasource.vim.engine.Command;
import com.vanillasource.vim.build.context.CompilationContext;

public class SuggestImportCommand implements Command {
   private CompilationContext context;

   public SuggestImportCommand(CompilationContext context) {
      this.context = context;
   }

   /**
    * Run a compile task in make-like mode. That is, output errors as separate lines instead of
    * vim commands. It is expected that this will be invoked through the vim 'make' command.
    */
   @Override
   public String execute(Map<String, String> parameters) {
      return context.qualifiedNamesForSimpleName(parameters.get("simpleName"))
         .stream().collect(Collectors.joining(","));
   }
}

