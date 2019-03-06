/**
 * Copyright (C) 2015 Robert Braeutigam.
 *
 * All rights reserved.
 */

package com.vanillasource.vim.engine;

/**
 * The initialization context of the java-vim-backend Engine.
 * Plugins may use this context to register any objects they wish
 * to share with other plugins. Keys may not be registered more than
 * once.
 */
public interface PluginContext {
   /**
    * Get a value for a given key in the initialization context. This
    * operation may block until the given key becomes available.
    */
   <V> V get(ContextKey<V> key);

   /**
    * Set a value to a key in the context.
    */
   <V> void set(ContextKey<V> key, V value);

   /**
    * Install a command that can be invoked from Vim. All public methods
    * of the given object will be exposed.
    * @param commandName The name by which to invoke this command from Vim.
    */
   void registerCommand(String commandName, Command command);

   public static final class ContextKey<V> {
      private ContextKey() {
      }

      public static <V> ContextKey<V> createKey() {
         return new ContextKey<>();
      }
   }
}

