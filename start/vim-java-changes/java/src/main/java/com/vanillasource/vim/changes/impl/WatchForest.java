/**
 * Copyright (C) 2015 Robert Braeutigam.
 *
 * All rights reserved.
 */

package com.vanillasource.vim.changes.impl;

import java.io.File;
import java.io.IOException;

/**
 * Watch registered files on the filesystem.
 */
public interface WatchForest extends AutoCloseable {
   void watch(File path, boolean recursive) throws IOException;
}

