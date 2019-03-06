/**
 * Copyright (C) 2015 Robert Braeutigam.
 *
 * All rights reserved.
 */

package com.vanillasource.vim.changes.impl;

import com.vanillasource.vim.changes.ChangeListener;
import com.vanillasource.vim.changes.ChangeEvent;
import java.io.File;

/**
 * Tracks changes made to the given files.
 */
public class changes {
   public static final void main(String[] args) throws Exception {
      WatchForestImpl watcher = new WatchForestImpl(new ChangeListener() {
         @Override
         public void handleChange(ChangeEvent event) {
            System.out.println("Event: "+event);
         }
      });
      for (String arg : args) {
         watcher.watch(new File(arg), true);
      }
      Object obj = new Object();
      synchronized (obj) {
         obj.wait();
      }
   }
}


