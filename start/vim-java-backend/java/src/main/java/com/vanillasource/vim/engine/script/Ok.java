/**
 * Copyright (C) 2015 Robert Braeutigam.
 *
 * All rights reserved.
 */

package com.vanillasource.vim.engine.script;

import com.vanillasource.vim.engine.VimScript;

/**
 * A response which does nothing further on the vim side, it is assumed that
 * the operation was successfully executed.
 */
public class Ok implements VimScript {
   @Override
   public String toScript() {
      return "";
   }
}

