/**
 * Copyright (C) 2015 Robert Braeutigam.
 *
 * All rights reserved.
 */

package com.vanillasource.vim.engine.impl;

import java.net.URLClassLoader;
import java.net.URL;
import java.util.List;
import java.util.ArrayList;

/**
 * A classload which can be extended with new sources
 * of classes, and also can have multiple parents.
 */
public class BundleClassLoader extends URLClassLoader {
   private List<Bundle> bundles = new ArrayList<>();

   public BundleClassLoader(URL baseUrl) {
      super(new URL[] {baseUrl});
   }

   public void addBundles(List<Bundle> bundles) {
      this.bundles.addAll(bundles);
   }

   public void addURL(URL url) {
      super.addURL(url);
   }

   /**
    * Checks first for class in other bundles that have this name exported.
    */
   protected Class<?> findClass(String className) throws ClassNotFoundException {
      for (Bundle bundle : bundles) {
         if (bundle.isExporting(className)) {
            return bundle.loadClass(className);
         }
      }
      return super.findClass(className);
   }
}

