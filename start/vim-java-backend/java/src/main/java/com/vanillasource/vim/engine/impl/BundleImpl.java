/**
 * Copyright (C) 2015 Robert Braeutigam.
 *
 * All rights reserved.
 */

package com.vanillasource.vim.engine.impl;

import java.io.IOException;
import java.io.File;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.jar.JarFile;
import java.util.jar.Manifest;
import java.util.jar.Attributes.Name;
import java.util.List;
import java.util.ArrayList;
import java.net.URISyntaxException;
import java.net.MalformedURLException;
import com.vanillasource.vim.engine.Plugin;
import com.vanillasource.vim.engine.PluginContext;
import org.apache.log4j.Logger;

/**
 * A bundle is plugin plus supporting libraries.
 */
public class BundleImpl implements Bundle {
   private static final Logger logger = Logger.getLogger(Bundle.class);
   private BundleClassLoader classLoader;
   private Plugin plugin;
   private List<String> exportedPackages;

   public BundleImpl(URL baseUrl) 
         throws IOException, URISyntaxException, ClassNotFoundException, InstantiationException, IllegalAccessException {
      classLoader = new BundleClassLoader(baseUrl);
      Manifest manifest = new JarFile(new File(baseUrl.toURI())).getManifest();
      addClassPath(baseUrl, manifest);
      findPlugin(manifest);
      extractExportedPackages(manifest);
      logger.info("loaded bundle at '"+baseUrl+", plugin: "+plugin.getClass().getName()+", exported packages: "+exportedPackages);
   }

   private void addClassPath(URL baseUrl, Manifest manifest) throws URISyntaxException, MalformedURLException {
      String classPath = manifest.getMainAttributes().getValue(Name.CLASS_PATH);
      if (classPath != null) {
         for (String classPathEntry : classPath.split(" ")) {
            classLoader.addURL(baseUrl.toURI().resolve(classPathEntry).toURL());
         }
      }
   }

   private void findPlugin(Manifest manifest) throws ClassNotFoundException, InstantiationException, IllegalAccessException {
      String pluginClass = manifest.getMainAttributes().getValue(PLUGIN_CLASS_KEY);
      if (pluginClass != null) {
         plugin = (Plugin) classLoader.loadClass(pluginClass).newInstance();
      }
   }

   private void extractExportedPackages(Manifest manifest) {
      exportedPackages = new ArrayList<String>();
      String exportedPackagesValue = manifest.getMainAttributes().getValue(EXPORTED_PACKAGES_KEY);
      if (exportedPackagesValue != null) {
         for (String exportedPackage : exportedPackagesValue.split(" ")) {
            exportedPackages.add(exportedPackage);
         }
      }
   }

   @Override
   public boolean isExporting(String className) {
      for (String exportedPackage : exportedPackages) {
         if (className.startsWith(exportedPackage+".") && 
               className.substring((exportedPackage+".").length()).indexOf(".") < 0) {
            return true;
         }
      }
      return false;
   }

   @Override
   public Class<?> loadClass(String className) throws ClassNotFoundException {
      return classLoader.loadClass(className);
   }

   @Override
   public void start(List<Bundle> bundles, PluginContext context) {
      List<Bundle> bundlesCopy = new ArrayList<>(bundles);
      bundlesCopy.remove(this);
      classLoader.addBundles(bundlesCopy);
      plugin.startPlugin(context);
   }
}

