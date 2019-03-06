/**
 * Copyright (C) 2015 Robert Braeutigam.
 *
 * All rights reserved.
 */

package com.vanillasource.vim.engine.script;

import com.vanillasource.vim.engine.VimScript;

/**
 * Displays messages on the vim side as response.
 */
public class Message implements VimScript {
   private Severity severity;
   private String message;

   public Message(Severity severity, String message) {
      this.severity = severity;
      this.message = message;
   }

   @Override
   public String toScript() {
      return "echohl "+severity.getVimHighlight()+"\necho \""+message+"\"\nechohl None";
   }

   public enum Severity {
      Error("ResponseError"), Success("ResponseSuccess"), Info("ResponseInfo");

      private String vimHighlight;

      private Severity(String vimHighlight) {
         this.vimHighlight = vimHighlight;
      }

      public String getVimHighlight() {
         return vimHighlight;
      }
   }
}

