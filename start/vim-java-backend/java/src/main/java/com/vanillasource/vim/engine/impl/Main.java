/**
 * Copyright (C) 2015 Robert Braeutigam.
 *
 * All rights reserved.
 */

package com.vanillasource.vim.engine.impl;

import java.util.List;
import java.util.ArrayList;
import java.util.jar.JarFile;
import java.util.jar.Manifest;
import java.net.URI;
import java.net.URL;
import java.io.IOException;
import java.io.File;
import java.io.FileFilter;
import org.apache.log4j.Logger;

/**
 * Start the backend engine with paths to java plugin directories.
 * In these directories, all jar files will be processed, and all
 * which have the Vim-Plugin-Class manifest entry will be initialized.
 */
public class Main {
   private static final Logger logger = Logger.getLogger(Main.class);

   public static final void main(String[] args) throws IOException {
      new Main().run(args);
   }

   public void run(String[] args) throws IOException {
      List<Bundle> bundles = findBundles(args);
      Engine engine = newEngineInstance(bundles);
      engine.run();
   }

   Engine newEngineInstance(List<Bundle> bundles) {
      return new EngineImpl(bundles);
   }

   private List<Bundle> findBundles(String[] paths) {
      List<Bundle> bundles = new ArrayList<>();
      for (String path : paths) {
         bundles.addAll(findBundles(path));
      }
      return bundles;
   }

   private List<Bundle> findBundles(String path) {
      File directory = new File(path);
      List<Bundle> bundles = new ArrayList<>();
      if (directory.isDirectory()) {
         for (File jarFile : directory.listFiles(new JarFileFilter())) {
            try {
               if (isBundle(jarFile)) {
                  logger.info("detected bundle at: "+jarFile);
                  bundles.add(newBundleInstance(jarFile.toURI().toURL()));
               } else {
                  logger.info("skipping jar file: "+jarFile);
               }
            } catch (Exception e) {
               logger.warn("could not load bundle: "+jarFile, e);
            }
         }
      }
      return bundles;
   }

   Bundle newBundleInstance(URL jarFile) throws Exception {
      return new BundleImpl(jarFile);
   }

   private boolean isBundle(File file) throws IOException {
      Manifest manifest = new JarFile(file).getManifest();
      return manifest.getMainAttributes().getValue(Bundle.PLUGIN_CLASS_KEY) != null;
   }

   private static class JarFileFilter implements FileFilter {
      @Override
      public boolean accept(File file) {
         return file.isFile() && file.getName().endsWith(".jar");
      }
   }
}


