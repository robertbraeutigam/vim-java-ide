/**
 * Copyright (C) 2015 Robert Braeutigam.
 *
 * All rights reserved.
 */

package com.vanillasource.vim.build;

import java.io.File;

public interface BuildFactory {
   /**
    * @return True iff this factory's build implementation is responsible for building the given file.
    */
   boolean isResponsibleFor(File file);

   /**
    * Get a build instance for the given file. This instance may be cached by the factory.
    */
   Build getBuild(File file);
}

