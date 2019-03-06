/**
 * Copyright (C) 2015 Robert Braeutigam.
 *
 * All rights reserved.
 */

package com.vanillasource.vim.engine.main;

/**
 * Start the backend engine with all plugins already on the classpath.
 */
public class Main {
   public static final void main(String[] args) throws Exception {
      new Engine().run();
   }
}


