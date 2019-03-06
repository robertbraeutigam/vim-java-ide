/**
 * Copyright (C) 2015 Robert Braeutigam.
 *
 * All rights reserved.
 */

package com.vanillasource.vim.changes;

import com.vanillasource.vim.engine.PluginContext.ContextKey;

public interface Keys {
   ContextKey<ChangesTracker> CHANGES_TRACKER = ContextKey.createKey();
}
