/**
 * Copyright (C) 2013 Robert Braeutigam.
 *
 * All rights reserved.
 */

package com.vanillasource.vim.changes.impl;

import java.io.File;

public interface HashListener {
   void handleHash(File file, String hash);
}


