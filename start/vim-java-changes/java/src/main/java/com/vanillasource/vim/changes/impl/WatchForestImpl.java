/**
 * Copyright (C) 2015 Robert Braeutigam.
 *
 * All rights reserved.
 */

package com.vanillasource.vim.changes.impl;

import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import static java.nio.file.StandardWatchEventKinds.*;
import java.util.List;
import java.util.EnumSet;
import java.util.LinkedList;
import com.vanillasource.vim.changes.ChangeListener;
import com.vanillasource.vim.changes.ChangeEvent;
import static com.vanillasource.vim.changes.ChangeEvent.ChangeType.*;
import org.apache.log4j.Logger;

public class WatchForestImpl implements WatchForest, Runnable {
   private static final Logger logger = Logger.getLogger(WatchForestImpl.class);
   private ChangeListener listener;
   private WatchService watchService;
   private List<WatchEntry> watchEntries = new LinkedList<>();
   private boolean running = true;
   private Thread thread;

   public WatchForestImpl(ChangeListener listener) throws IOException {
      this.listener = listener;
      this.watchService = FileSystems.getDefault().newWatchService();
      thread = new Thread(this);
      thread.setName("Watch Service");
      thread.setDaemon(true);
      thread.start();
   }

   @Override
   public void watch(File file, boolean recursive) throws IOException {
      if (!file.isDirectory()) {
         throw new IllegalArgumentException("can only watch directories, "+file+" was not a directory");
      }
      Path path = file.toPath().toAbsolutePath();
      if (!isWatched(path, recursive)) {
         register(path, recursive);
         watchEntries.add(new WatchEntry(path, recursive));
      }
   }

   @Override
   public void close() throws InterruptedException {
      running = false;
      thread.interrupt();
      thread.join();
   }

   private void register(Path path, boolean recursive) throws IOException {
      Files.walkFileTree(path, EnumSet.noneOf(FileVisitOption.class),
            recursive?Integer.MAX_VALUE:1, new SimpleFileVisitor<Path>() {
         @Override
         public FileVisitResult preVisitDirectory(Path file, BasicFileAttributes attributes) throws IOException {
            register(file);
            return super.visitFile(file, attributes);
         }
      });
   }

   private void register(Path path) throws IOException {
      path.register(watchService, ENTRY_CREATE, ENTRY_DELETE, ENTRY_MODIFY);
   }

   private boolean isWatched(Path path, boolean recursive) {
      for (WatchEntry entry : watchEntries) {
         if (entry.isIncluding(path, recursive)) {
            return true;
         }
      }
      return false;
   }

   @Override
   public void run() {
      while (running) {
         WatchKey key = takeNextKey();
         if (key != null) {
            for (WatchEvent<?> event : key.pollEvents()) {
               processEvent(key, event);
            }
            if (!key.reset()) {
               key.cancel();
            }
         }
      }
   }

   @SuppressWarnings("unchecked")
   private void processEvent(WatchKey key, WatchEvent<?> event) {
      Path contextPath = (Path) key.watchable();
      WatchEvent.Kind<?> kind = event.kind();
      if (kind != OVERFLOW) {
         Path path = contextPath.resolve(((WatchEvent<Path>) event).context()).toAbsolutePath();
         File file = path.toFile();
         if (kind == ENTRY_MODIFY) {
            listener.handleChange(new ChangeEvent(file, Modified));
         } else if (kind == ENTRY_DELETE) {
            listener.handleChange(new ChangeEvent(file, Deleted));
         } else if (kind == ENTRY_CREATE) {
            listener.handleChange(new ChangeEvent(file, Created));
            if (file.isDirectory() && isWatched(path, true)) {
               try {
                  register(path, true);
               } catch (IOException e) {
                  logger.warn("could not register path: "+path+", will not be watched", e);
               }
            }
         }
      } else {
         logger.warn("overflow: "+event);
      }
   }

   private WatchKey takeNextKey() {
      try {
         return watchService.take();
      } catch (InterruptedException e) {
         logger.warn("watcher thread interrupted, exiting", e);
         running = false;
      }
      return null;
   }

   private static class WatchEntry {
      private Path path;
      private boolean recursive;

      public WatchEntry(Path path, boolean recursive) {
         this.path = path;
         this.recursive = recursive;
      }

      public boolean isIncluding(Path other, boolean otherRecursive) {
         return ((!recursive) && (!otherRecursive) && (other.equals(path))) ||
            ((recursive) && (other.startsWith(path)));
      }
   }
}


