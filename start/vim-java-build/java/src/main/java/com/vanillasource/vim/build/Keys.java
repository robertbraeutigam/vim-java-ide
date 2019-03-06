/**
 * Copyright (C) 2015 Robert Braeutigam.
 *
 * All rights reserved.
 */

package com.vanillasource.vim.build;

import com.vanillasource.vim.engine.PluginContext.ContextKey;

public interface Keys {
   ContextKey<BuildService> BUILD_SERVICE = ContextKey.createKey();
}
