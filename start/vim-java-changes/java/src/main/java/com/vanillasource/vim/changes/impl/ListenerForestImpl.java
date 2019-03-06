/**
 * Copyright (C) 2015 Robert Braeutigam.
 *
 * All rights reserved.
 */

package com.vanillasource.vim.changes.impl;

import com.vanillasource.vim.changes.ChangeListener;
import com.vanillasource.vim.changes.ChangeEvent;
import com.vanillasource.vim.changes.ChangeEvent.ChangeType;
import static com.vanillasource.vim.changes.ChangeEvent.ChangeType.*;
import java.io.File;
import java.util.List;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Map;
import java.util.HashMap;
import java.io.IOException;
import org.apache.log4j.Logger;

/**
 * Represents a filesystem forest of a single listener. At any given point
 * either a change event or a hash event can be delivered. Hash events
 * always follow the corresponding change event, after an arbitrary time
 * has elapsed (and the hashes could be generated for all potential files).
 * It is the responsibility of this class to keep track of a listener's
 * current view of its tracked files, and maintain a list of changes.
 */
public class ListenerForestImpl implements ChangeListener, HashListener {
   private static final Logger logger = Logger.getLogger(ListenerForestImpl.class);
   private Map<File, StateEntry> states = new HashMap<>();

   /**
    * Register a file or directory to be watched. If the file or directory
    * is not yet known, it will be introduced as 'created'.
    */
   public void registerPath(File file, boolean recursive) throws IOException {
      if (!file.isDirectory()) {
         throw new IOException("trying to register "+file+", but it is not a directory");
      }
      recursiveCreateState(file, recursive);
   }

   public void recursiveCreateState(File file, boolean recursive) throws IOException {
      StateEntry fileEntry = states.get(file);
      if (fileEntry == null) {
         StateEntry parentEntry = states.get(file.getParentFile());
         if (parentEntry != null) {
            parentEntry.addChild(file);
         }
         fileEntry = new StateEntry(file, recursive);
         states.put(file, fileEntry);
      } else {
         fileEntry.updateState(Created);
      }
      if (file.isDirectory()) {
         for (File child : file.listFiles()) {
            if (child.isDirectory() && recursive && (!child.getName().equals(".")) && (!child.getName().equals(".."))) {
               fileEntry.addChild(child);
               recursiveCreateState(child, true);
            }
            if (child.isFile()) {
               fileEntry.addChild(child);
               states.put(child, new StateEntry(child, false));
            }
         }
      }
   }

   @Override
   public void handleChange(ChangeEvent change) {
      if (change.getChangeType() == Modified) {
         StateEntry entry = states.get(change.getFile());
         if (entry != null) {
            entry.updateState(change.getChangeType());
         }
      } else if (change.getChangeType() == Deleted) {
         StateEntry entry = states.get(change.getFile());
         recursiveUpdateState(entry, Deleted);
      } else if (change.getChangeType() == Created) {
         if (isTracked(change.getFile())) {
            try {
               recursiveCreateState(change.getFile(), true);
            } catch (IOException e) {
               logger.error("could not recursively track for change: "+change, e);
            }
         }
      }
   }

   private void recursiveUpdateState(StateEntry entry, ChangeType changeType) {
      if (entry != null) {
         entry.updateState(changeType);
         for (File child : entry.getChildren()) {
            StateEntry childEntry = states.get(child);
            recursiveUpdateState(childEntry, changeType);
         }
      }
   }

   private boolean isTracked(File file) {
      StateEntry parentEntry = states.get(file.getParentFile());
      if (parentEntry == null) {
         return false;
      }
      return (file.isFile()) || ((file.isDirectory()) && (parentEntry.isRecursive()));
   }

   @Override
   public void handleHash(File file, String hash) {
      StateEntry fileEntry = states.get(file);
      if (fileEntry != null) {
         fileEntry.updateHash(hash);
      }
   }

   /**
    * Poll all the currently known changes to the tracked files for this listener.
    * After this method call, all changes will be cleared and listener is considered
    * up to date.
    */
   public List<ChangeEvent> pollChanges() {
      List<ChangeEvent> events = new LinkedList<ChangeEvent>();
      for (StateEntry entry : new ArrayList<>(states.values())) {
         if (entry.getChangeType() != null) {
            events.add(new ChangeEvent(entry.getFile(), entry.getChangeType()));
            if (entry.getChangeType() == Deleted) {
               states.remove(entry.getFile());
            } else {
               entry.resetChange();
            }
         }
      }
      return events;
   }

   public static class StateEntry {
      private File file;
      private boolean recursive;
      private List<File> children;
      private ChangeType changeType;
      private String lastKnownHash;
      private String currentHash;

      public StateEntry(File file, boolean recursive) {
         this.file = file;
         this.recursive = recursive;
         this.children = new LinkedList<File>();
         this.changeType = ChangeType.Created;
      }

      public void addChild(File child) {
         children.add(child);
      }

      public File getFile() {
         return file;
      }

      public boolean isRecursive() {
         return recursive;
      }

      public List<File> getChildren() {
         return children;
      }

      public ChangeType getChangeType() {
         return changeType;
      }

      public void updateHash(String hash) {
         currentHash = hash;
         if ((changeType == Modified) && (currentHash.equals(lastKnownHash))) {
            changeType = null;
         }
      }

      public void updateState(ChangeType changeType) {
         if (this.changeType == null) {
            this.changeType = changeType;
         } else if (this.changeType == Deleted && changeType != Deleted) {
            this.changeType = Modified;
         } else if (this.changeType == Modified && changeType == Deleted) {
            this.changeType = Deleted;
         } else if (this.changeType == Created && changeType == Deleted) {
            this.changeType = Deleted;
         }
      }

      public void resetChange() {
         changeType = null;
         lastKnownHash = currentHash;
      }
   }
}

