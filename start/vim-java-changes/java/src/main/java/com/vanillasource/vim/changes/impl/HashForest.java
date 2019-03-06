/**
 * Copyright (C) 2013 Robert Braeutigam.
 *
 * All rights reserved.
 */

package com.vanillasource.vim.changes.impl;

import java.io.File;
import java.io.IOException;
import com.vanillasource.vim.changes.ChangeListener;

/**
 * A service that asynchronously calculates hashes of the
 * changes received, and notifies any listeners when ready.
 */
public interface HashForest extends ChangeListener, AutoCloseable {
   void registerPath(File file, boolean recursive) throws IOException;
}


