/**
 * Copyright (C) 2015 Robert Braeutigam.
 *
 * All rights reserved.
 */

package com.vanillasource.vim.build.gradle;

import com.vanillasource.vim.build.Build;
import com.vanillasource.vim.build.FileMessage;
import java.util.List;
import java.io.File;
import java.util.stream.Collectors;
import java.util.Collections;
import static java.util.Arrays.asList;
import org.apache.log4j.Logger;

public class GradleBuild implements Build {
   private static final Logger logger = Logger.getLogger(GradleBuild.class);
   private File buildFile;
   private File topDirectory;

   public GradleBuild(File buildFile, File topDirectory) {
      this.buildFile = buildFile;
      this.topDirectory = topDirectory;
   }

   @Override
   public String getName() {
      return "Gradle";
   }

   @Override
   public File getBuildDirectory() {
      return buildFile.getParentFile();
   }

   @Override
   public File getTopDirectory() {
      return topDirectory;
   }

   @Override
   public List<FileMessage> compile(File file) {
      logger.warn("compile with gradle not supported");
      return Collections.<FileMessage>emptyList();
   }
}


