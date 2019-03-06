/**
 * Copyright (C) 2015 Robert Braeutigam.
 *
 * All rights reserved.
 */

package com.vanillasource.vim.engine;

import java.util.Map;

/**
 * Command to be executed from the Vim side. Commands are
 * registered by plugins.
 */
public interface Command {
   VimScript execute(Map<String, String> parameters);
}

