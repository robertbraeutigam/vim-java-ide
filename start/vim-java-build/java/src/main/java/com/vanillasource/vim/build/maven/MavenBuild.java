/**
 * Copyright (C) 2015 Robert Braeutigam.
 *
 * All rights reserved.
 */

package com.vanillasource.vim.build.maven;

import com.vanillasource.vim.build.Build;
import com.vanillasource.vim.build.FileMessage;
import com.vanillasource.vim.build.util.JavacUtils;
import com.vanillasource.vim.changes.ChangesTracker;
import com.vanillasource.vim.changes.ChangeEvent;
import java.io.*;
import java.util.List;
import java.util.LinkedList;
import java.util.Collections;
import static java.util.Arrays.asList;
import org.apache.log4j.Logger;

public class MavenBuild implements Build {
   private static final Logger logger = Logger.getLogger(MavenBuild.class);
   private File pomFile;
   private File topDirectory;
   private List<String> compileArgumentList;
   private List<File> compileSourcePaths;
   private String compileSourcePathsListenerId;
   private List<String> testCompileArgumentList;
   private List<File> testCompileSourcePaths;
   private String testCompileSourcePathsListenerId;
   private ChangesTracker changesTracker;
   private String pomListenerId;

   public MavenBuild(ChangesTracker changesTracker, File pomFile, File topDirectory) {
      this.changesTracker = changesTracker;
      this.pomFile = pomFile;
      this.topDirectory = topDirectory;
      this.pomListenerId = "maven-build-pom-"+pomFile.getAbsolutePath();
      try {
         changesTracker.registerPath(pomListenerId, pomFile.getParentFile(), false);
      } catch (IOException e) {
         throw new IllegalStateException("could not start listening to pom changes", e);
      }
   }

   @Override
   public String getName() {
      return "Maven";
   }

   @Override
   public File getBuildDirectory() {
      return pomFile.getParentFile();
   }

   @Override
   public File getTopDirectory() {
      return topDirectory;
   }

   @Override
   public List<FileMessage> compile(File file) throws IOException {
      long startTime = System.currentTimeMillis();
      try {
         return compileInternal(file);
      } finally {
         long stopTime = System.currentTimeMillis();
         logger.info("compile finished in "+(stopTime-startTime)+" ms");
      }
   }

   private List<FileMessage> compileInternal(File file) throws IOException {
      if (isApplicable(file)) {
         if (!isCompileInitialized() || isPomChanged()) {
            initializeCompile();
         }
         if (isCompileInitialized()) {
            if (isCompileFile(file)) {
               List<File> changedFiles = getChangedFiles(compileSourcePathsListenerId, file);
               logger.info("compiling: "+changedFiles);
               return JavacUtils.compile(compileArgumentList, changedFiles);
            } else if (isTestCompileFile(file)) {
               List<File> compileChangedFiles = getChangedFiles(compileSourcePathsListenerId);
               if (!compileChangedFiles.isEmpty()) {
                  logger.info("before compiling test, there were compile classes changed: "+compileChangedFiles);
                  return JavacUtils.compile(compileArgumentList, compileChangedFiles);
               }
               List<File> testCompileChangedFiles = getChangedFiles(testCompileSourcePathsListenerId, file);
               logger.info("test compiling: "+testCompileChangedFiles);
               return JavacUtils.compile(testCompileArgumentList, testCompileChangedFiles);
            } else {
               logger.warn("given file "+file+" was neither a compile nor test-compile file");
            }
         }
      }
      return Collections.<FileMessage>emptyList();
   }

   private boolean isCompileFile(File file) {
      return isDescendantOf(compileSourcePaths, file);
   }

   private boolean isTestCompileFile(File file) {
      return isDescendantOf(testCompileSourcePaths, file);
   }

   private boolean isDescendantOf(List<File> directories, File file) {
      for (File directory: directories) {
         if (file.toPath().startsWith(directory.toPath())) {
            return true;
         }
      }
      return false;
   }

   private List<File> getChangedFiles(String listenerId, File... initiatorFiles) {
      List<ChangeEvent> changes = changesTracker.pollChanges(listenerId);
      List<File> changedFiles = new LinkedList<>();
      for (ChangeEvent change: changes) {
         if (change.getFile().getName().endsWith(".java")) {
            changedFiles.add(change.getFile());
         }
      }
      for (File initiatorFile: initiatorFiles) {
         if (!changedFiles.contains(initiatorFile)) {
            changedFiles.add(initiatorFile);
         }
      }
      return changedFiles;
   }

   private boolean isApplicable(File file) {
      return file.getName().endsWith(".java");
   }

   private boolean isCompileInitialized() {
      return compileArgumentList != null;
   }

   private boolean isPomChanged() {
      List<ChangeEvent> changes = changesTracker.pollChanges(pomListenerId);
      for (ChangeEvent change: changes) {
         if (change.getFile().equals(pomFile)) {
            return true;
         }
      }
      return false;
   }

   private void initializeCompile() throws IOException {
      logger.info("initializing maven compile arguments...");
      changesTracker.pollChanges(pomListenerId); // Clear changes, since we get everything here
      Process mavenCompile = Runtime.getRuntime().exec(new String[] { "mvn", "-X", "clean", "test-compile" }, null, getBuildDirectory());
      BufferedReader reader = new BufferedReader(new InputStreamReader(mavenCompile.getInputStream()));
      String line = null;
      Mojo currentMojo = Mojo.Unknown;
      while ( (line = reader.readLine()) != null) {
         if (line.contains("Configuring mojo '")) {
            currentMojo = Mojo.Unknown;
         }
         if (line.matches(".*Configuring mojo 'org.apache.maven.plugins:maven-compiler-plugin:.*:compile'.*")) {
            currentMojo = Mojo.Compile;
         } else if (line.matches(".*Configuring mojo 'org.apache.maven.plugins:maven-compiler-plugin:.*:testCompile'.*")) {
            currentMojo = Mojo.TestCompile;
         }
         if (currentMojo != Mojo.Unknown && line.endsWith("Command line options:")) {
            String options = reader.readLine();
            List<String> argumentList = asList(options.split(" "));
            argumentList = argumentList.subList(1, argumentList.size());
            List<File> sourcePaths = extractSourcePaths(argumentList);
            String listenerId = listenSourcePaths(sourcePaths);
            logger.info("read maven arguments: "+argumentList);
            logger.info("source paths: "+sourcePaths);
            if (currentMojo == Mojo.Compile) {
               // Compile
               compileArgumentList = argumentList;
               compileSourcePaths = sourcePaths;
               compileSourcePathsListenerId = listenerId;
               // Test may not follow, so clear them
               testCompileArgumentList = Collections.<String>emptyList();
               testCompileSourcePaths = Collections.<File>emptyList();
               testCompileSourcePathsListenerId = "";
            } else {
               // Test
               testCompileArgumentList = argumentList;
               testCompileSourcePaths = sourcePaths;
               testCompileSourcePathsListenerId = listenerId;
            }
         }
      }
   }

   enum Mojo {
      Compile, TestCompile, Unknown;
   }

   private List<File> extractSourcePaths(List<String> argumentList) {
      List<File> sourcePaths = new LinkedList<>();
      for (int i=0; i<argumentList.size(); i++) {
         if (argumentList.get(i).equals("-sourcepath")) {
            sourcePaths.addAll(splitPath(argumentList.get(i+1)));
         }
      }
      return sourcePaths;
   }

   private List<File> splitPath(String paths) {
      List<File> files = new LinkedList<>();
      for (String path: paths.split(":")) {
         if (!"".equals(path)) {
            files.add(new File(path));
         }
      }
      return files;
   }

   private String listenSourcePaths(List<File> sourcePaths) {
      try {
         String listenerId = "maven-build-sources-"+sourcePaths;
         for (File sourcePath: sourcePaths) {
            changesTracker.registerPath(listenerId, sourcePath, true);
         }
         changesTracker.pollChanges(listenerId); // Clear as we now should have a clean slate
         return listenerId;
      } catch (IOException e) {
         throw new IllegalStateException("can not track source paths: "+sourcePaths, e);
      }
   }
}


