/**
 * Copyright (C) 2015 Robert Braeutigam.
 *
 * All rights reserved.
 */

package com.vanillasource.vim.engine.impl;

import com.vanillasource.vim.engine.PluginContext;
import java.util.List;

/**
 * A bundle is plugin plus supporting libraries.
 */
public interface Bundle {
   String PLUGIN_CLASS_KEY = "Vim-Plugin-Class";
   String EXPORTED_PACKAGES_KEY = "Vim-Exported-Packages";

   /**
    * Load a class from this bundle.
    */
   Class<?> loadClass(String className) throws ClassNotFoundException;

   /**
    * Determine whether this bundle exports the given class.
    */
   boolean isExporting(String className);

   /**
    * Start this bundle with the given other bundles.
    */
   void start(List<Bundle> bundles, PluginContext context);
}
