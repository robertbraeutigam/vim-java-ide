/**
 * Copyright (C) 2015 Robert Braeutigam.
 *
 * All rights reserved.
 */

package com.vanillasource.vim.build;

import java.io.File;

public interface BuildService {
   /**
    * Get the build that is responsible for the given file.
    * @return The build that can build the specified file, or null if there is no
    * appropriate build.
    */
   Build getBuild(File file);
}
