/**
 * Copyright (C) 2015 Robert Braeutigam.
 *
 * All rights reserved.
 */

package com.vanillasource.vim.changes.impl;

import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;
import java.io.File;
import java.io.IOException;
import com.vanillasource.vim.changes.ChangesTracker;
import com.vanillasource.vim.changes.ChangeEvent;
import com.vanillasource.vim.changes.ChangeListener;

public class WatchServiceChangesTrackerImpl implements ChangesTracker, ChangeListener, HashListener {
   private WatchForest watchForest;
   private HashForest hashForest;
   private Map<String, ListenerForestImpl> listenerForests = new HashMap<>();

   public WatchServiceChangesTrackerImpl() throws IOException {
      watchForest = new WatchForestImpl(this);
      hashForest = new HashForestImpl(this);
   }

   @Override
   public synchronized void registerPath(String listenerId, File file, boolean recursive) throws IOException {
      ListenerForestImpl listenerForest = listenerForests.get(listenerId);
      if (listenerForest == null) {
         listenerForest = new ListenerForestImpl();
         listenerForests.put(listenerId, listenerForest);
      }
      listenerForest.registerPath(file, recursive);
      hashForest.registerPath(file, recursive);
      watchForest.watch(file, recursive);
   }

   @Override
   public synchronized List<ChangeEvent> pollChanges(String listenerId) {
      if (!listenerForests.containsKey(listenerId)) {
         throw new IllegalArgumentException("listener id '"+listenerId+"' is not known");
      }
      return listenerForests.get(listenerId).pollChanges();
   }

   @Override
   public synchronized void handleChange(ChangeEvent change) {
      for (ListenerForestImpl listenerForest : listenerForests.values()) {
         listenerForest.handleChange(change);
         hashForest.handleChange(change);
      }
   }

   @Override
   public synchronized void handleHash(File file, String hash) {
      for (ListenerForestImpl listenerForest : listenerForests.values()) {
         listenerForest.handleHash(file, hash);
      }
   }
}

