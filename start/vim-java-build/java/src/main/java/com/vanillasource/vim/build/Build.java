/**
 * Copyright (C) 2015 Robert Braeutigam.
 *
 * All rights reserved.
 */

package com.vanillasource.vim.build;

import java.util.List;
import java.io.File;
import java.io.IOException;

public interface Build {
   String getName();

   File getBuildDirectory();

   File getTopDirectory();

   List<FileMessage> compile(File file) throws IOException;
}
