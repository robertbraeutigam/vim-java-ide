/**
 * Copyright (C) 2015 Robert Braeutigam.
 *
 * All rights reserved.
 */

package com.vanillasource.vim.changes;

import java.util.List;
import java.io.File;
import java.io.IOException;

public interface ChangesTracker {
   /**
    * Register a new file to be watched for the specified listener. If the listener did not yet
    * exists it is registered too. Any previously unknown files or directories registered will be marked as
    * 'created' first and will be returned on the next query.
    */
   void registerPath(String listenerId, File file, boolean recursive) throws IOException;

   /**
    * Poll all the changes that occured since last query. Changes are 
    * determined as exactly as possible, through generating hashes for
    * all files, but if those hashes were not yet generated, pure 
    * modification time may be used. The call consumes all the changes
    * automatically, and those will not be returned on subsequent calls.
    */
   List<ChangeEvent> pollChanges(String listenerId);
}
