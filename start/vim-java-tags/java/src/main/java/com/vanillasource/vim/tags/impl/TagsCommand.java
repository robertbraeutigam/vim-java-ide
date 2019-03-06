/**
 * Copyright (C) 2015 Robert Braeutigam.
 *
 * All rights reserved.
 */

package com.vanillasource.vim.tags.impl;

import com.vanillasource.vim.engine.Command;
import com.vanillasource.vim.changes.ChangesTracker;
import com.vanillasource.vim.changes.ChangeEvent;
import com.vanillasource.vim.build.BuildService;
import com.vanillasource.vim.build.Build;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ExecutorService;
import java.io.*;
import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;
import java.nio.file.StandardCopyOption;
import java.nio.file.Files;
import static java.util.Arrays.asList;
import org.apache.log4j.Logger;

public class TagsCommand implements Command {
   private static final int REGENERATE_FILE_COUNT = 5; // More than this many files changed causes a full re-generate
   private static final Logger logger = Logger.getLogger(TagsCommand.class);
   private ChangesTracker changesTracker;
   private BuildService buildService;
   private ExecutorService executor;
   private Map<File, TagsUpdater> updaters = new HashMap<>();

   public TagsCommand(ChangesTracker changesTracker, BuildService buildService) {
      this.changesTracker = changesTracker;
      this.buildService = buildService;
      this.executor = Executors.newFixedThreadPool(1, new ThreadFactory() {
         @Override
         public Thread newThread(Runnable runnable) {
            Thread thread = new Thread(runnable);
            thread.setDaemon(true);
            return thread;
         }
      });
   }

   /**
    * Update the tags file for the build the file belongs to.
    */
   @Override
   public String execute(Map<String, String> parameters) {
      File file = new File(parameters.get("fileName"));
      Build build = buildService.getBuild(file);
      if (build != null) {
         TagsUpdater updater = updaters.get(build.getTopDirectory());
         if (updater == null) {
            updater = new TagsUpdater(build.getTopDirectory());
            updaters.put(build.getTopDirectory(), updater);
         }
         updater.update(file);
      }
      return "";
   }

   public class TagsUpdater {
      private File topDirectory;
      private File tagsFile;

      public TagsUpdater(File topDirectory) {
         this.topDirectory = topDirectory;
         this.tagsFile = new File(topDirectory, ".tags");
      }

      public void update(final File initiatorFile) {
         executor.submit(new Runnable() {
            @Override
            public void run() {
               List<File> changedFiles = getChangedFiles(initiatorFile);
               if (!tagsFile.exists() || changedFiles.size() > REGENERATE_FILE_COUNT) {
                  long startTime = System.currentTimeMillis();
                  regenerateTags(changedFiles);
                  long stopTime = System.currentTimeMillis();
                  logger.info("generated tags file in "+(stopTime-startTime)+" ms");
               } else {
                  long startTime = System.currentTimeMillis();
                  updateTags(changedFiles);
                  long stopTime = System.currentTimeMillis();
                  logger.info("update tags file in "+(stopTime-startTime)+" ms, re-indexed "+changedFiles.size()+" files");
               }
            }
         });
      }

      private List<File> getChangedFiles(File initiatorFile) {
         // For now, we don't use changed files because there are too many directories to watch
         // for this to work
         List<File> changedFiles = new ArrayList<>();
         changedFiles.add(initiatorFile);
         return changedFiles;
      }

      private void regenerateTags(List<File> files) {
         try {
            Runtime.getRuntime().exec(new String[] {
               "ctags", "-o", tagsFile.getAbsolutePath(), "-R", "--exclude=CVS", "--exclude=.svn", "--exclude=.git", "--languages=java", topDirectory.getAbsolutePath()
            }).waitFor();
         } catch (Exception e) {
            logger.error("could not generate tags file", e);
         }
      }

      private void updateTags(List<File> files) {
         try {
            File tmpFile = filterTagsFile(files);
            updateTagsFile(tmpFile, files);
            tagsFile = Files.move(tmpFile.toPath(), tagsFile.toPath(), StandardCopyOption.ATOMIC_MOVE).toFile();
         } catch (Exception e) {
            logger.error("could update tags", e);
         }
      }

      private File filterTagsFile(List<File> files) throws IOException {
         File tmpFile = new File(tagsFile.getAbsolutePath()+".tmp");
         BufferedReader reader = new BufferedReader(new FileReader(tagsFile));
         FileWriter writer = new FileWriter(tmpFile, false);
         try {
            String line = null;
            while ( (line = reader.readLine()) != null) {
               if (!isAbout(files, line)) {
                  writer.write(line+"\n");
               }
            }
         } finally {
            reader.close();
            writer.close();
         }
         return tmpFile;
      }

      private boolean isAbout(List<File> files, String line) {
         for (File file: files) {
            if (line.contains("	"+file.getAbsolutePath()+"	")) {
               return true;
            }
         }
         return false;
      }

      private void updateTagsFile(File tmpTagsFile, List<File> files) throws Exception {
         List<String> commands = new ArrayList<>(files.size()+10);
         commands.addAll(asList("ctags", "-o", tmpTagsFile.getAbsolutePath(), "-a"));
         for (File file: files) {
            commands.add(file.getAbsolutePath());
         }
         Runtime.getRuntime().exec(commands.toArray(new String[] {})).waitFor();
      }
   }
}


