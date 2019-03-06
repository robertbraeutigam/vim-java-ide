/**
 * Copyright (C) 2015 Robert Braeutigam.
 *
 * All rights reserved.
 */

package com.vanillasource.vim.build;

import java.io.File;

public class FileMessage {
   private File file;
   private Severity severity;
   private String message;
   private long row;
   private long column;

   public FileMessage(File file, Severity severity, String message, long row, long column) {
      this.file = file;
      this.severity = severity;
      this.message = message;
      this.row = row;
      this.column = column;
   }

   public File getFile() {
      return file;
   }

   public Severity getSeverity() {
      return severity;
   }

   public String getMessage() {
      return message;
   }

   public long getRow() {
      return row;
   }

   public long getColumn() {
      return column;
   }

   public enum Severity {
      Error("E"), Warning("W"), Information("I");

      private String vimIndicator;

      private Severity(String vimIndicator) {
         this.vimIndicator = vimIndicator;
      }

      public String getVimIndicator() {
         return vimIndicator;
      }
   }
}

