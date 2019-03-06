/**
 * Copyright (C) 2015 Robert Braeutigam.
 *
 * All rights reserved.
 */

package com.vanillasource.vim.changes;

import java.io.File;

public class ChangeEvent {
   private File file;
   private ChangeType changeType;

   public ChangeEvent(File file, ChangeType changeType) {
      this.file = file;
      this.changeType = changeType;
   }

   public File getFile() {
      return file;
   }

   public ChangeType getChangeType() {
      return changeType;
   }

   @Override
   public String toString() {
      return file+" ("+changeType+")";
   }

   @Override
   public int hashCode() {
      return file.hashCode() + 13^changeType.hashCode();
   }

   @Override
   public boolean equals(Object o) {
      if ((o == null) || (!(o instanceof ChangeEvent))) {
         return false;
      }
      ChangeEvent other = (ChangeEvent) o;
      return other.file.equals(file) && other.changeType.equals(changeType);
   }

   public static enum ChangeType {
      Created, Modified, Deleted;
   }
}

