/**
 * Copyright (C) 2015 Robert Braeutigam.
 *
 * All rights reserved.
 */

package com.vanillasource.vim.build.impl;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.LinkedList;
import java.util.Map;
import com.vanillasource.vim.engine.Command;
import com.vanillasource.vim.build.*;
import com.vanillasource.vim.changes.ChangesTracker;

public class BuildCommand implements Command {
   private BuildService buildService;

   public BuildCommand(BuildService buildService) {
      this.buildService = buildService;
   }

   /**
    * Sets variables to the currently detected build, if there is one.
    */
   @Override
   public String execute(Map<String, String> parameters) {
      File file = new File(parameters.get("fileName"));
      final Build build = buildService.getBuild(file);
      if (build != null) {
         return 
            "let b:BuildName='"+build.getName()+"'\n"+
            "let b:BuildDirectory='"+build.getBuildDirectory()+"'\n"+
            "let b:BuildTopDirectory='"+build.getTopDirectory()+"'";
      }
      return "";
   }
}

