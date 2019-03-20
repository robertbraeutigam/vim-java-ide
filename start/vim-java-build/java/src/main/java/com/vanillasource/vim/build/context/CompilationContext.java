/**
 * Copyright (C) 2019 Robert Braeutigam.
 *
 * All rights reserved.
 */

package com.vanillasource.vim.build.context;

import java.io.File;
import java.util.Collection;
import java.util.Set;

public interface CompilationContext {
   void configureUrls(Collection<File> files);

   Set<String> qualifiedNamesForSimpleName(String simpleName);
}

