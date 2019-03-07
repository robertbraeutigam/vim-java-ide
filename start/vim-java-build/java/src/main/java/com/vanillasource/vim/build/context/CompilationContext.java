/**
 * Copyright (C) 2019 Robert Braeutigam.
 *
 * All rights reserved.
 */

package com.vanillasource.vim.build.context;

import java.io.File;
import java.util.Collection;
import java.util.List;

public interface CompilationContext {
   void configureUrls(Collection<File> files);

   List<String> qualifiedNamesForSimpleName(String simpleName);
}

