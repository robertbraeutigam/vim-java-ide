/**
 * Copyright (C) 2015 Robert Braeutigam.
 *
 * All rights reserved.
 */

package com.vanillasource.vim.engine;

import java.util.List;

/**
 * Command to be executed from the Vim side. Commands are
 * registered by plugins.
 */
public interface Command {
   VimScript execute(List<String> parameter);
}

