/**
 * Copyright (C) 2015 Robert Braeutigam.
 *
 * All rights reserved.
 */

package com.vanillasource.vim.engine;

/**
 * Response from the engine to vim. All responses must be translateable
 * to vim script, and will be run on the vim side.
 */
public interface Response {
   String toScript();
}


