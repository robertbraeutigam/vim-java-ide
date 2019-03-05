/**
 * Copyright (C) 2015 Robert Braeutigam.
 *
 * All rights reserved.
 */

package com.vanillasource.vim.engine.response;

import com.vanillasource.vim.engine.Response;

/**
 * A response which does nothing further on the vim side, it is assumed that
 * the operation was successfully executed.
 */
public class Ok implements Response {
   @Override
   public String toScript() {
      return "";
   }
}


