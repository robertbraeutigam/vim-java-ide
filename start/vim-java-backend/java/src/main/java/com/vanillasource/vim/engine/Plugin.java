/**
 * Copyright (C) 2015 Robert Braeutigam.
 *
 * All rights reserved.
 */

package com.vanillasource.vim.engine;

/**
 * A plugin for the vim-java-backend, a lightweight 
 * static plugin system. Implement this interface to create
 * an entry point for your bundle. As soon as the plugin
 * engine is started, all plugins will be notified of a start event,
 * and when the engine is stopped all plugins are stopped as well.
 */
public interface Plugin {
   /**
    * Called by the Engine to notify a plugin to start.
    * The plugin should at this point get all the relevant services
    * from other plugins if needed. The context will not be valid
    * after this call. All dependencies will be dynamically mapped
    * depending on which classes this method call tries to use.
    * The order in which plugins will be initialized depends on these
    * dependencies automatically, so it is guaranteed that all needed
    * dependencies are initialized before they are returned from context.
    */
   void startPlugin(PluginContext context);

   /**
    * Called by the Engine to stop this plugin and release all resources.
    */
   void stopPlugin(PluginContext context);
}


