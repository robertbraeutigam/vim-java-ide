/**
 * Copyright (C) 2015 Robert Braeutigam.
 *
 * All rights reserved.
 */

package com.vanillasource.vim.changes.impl;

import org.testng.annotations.Test;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.AfterMethod;
import static org.mockito.Mockito.*;
import static org.testng.Assert.*;
import com.vanillasource.vim.changes.*;
import static com.vanillasource.vim.changes.ChangeEvent.ChangeType.*;
import com.vanillasource.vim.changes.ChangeEvent.ChangeType;
import java.util.List;
import java.util.LinkedList;
import java.io.File;
import org.apache.commons.io.FileUtils;

@Test
public class WatchForestImplTests extends FileTestsBase {
   private WatchForestImpl watcher;
   private List<ChangeEvent> events;

   public void testWatcherStartsAndStops() {
   }

   public void testCreatedFileEventIsDeliveredInWatchedDirectory() throws Exception {
      watcher.watch(fileBase, false);

      createFile("a");
      
      assertEvent("a", Created);
   }

   public void testDeletedFileEventIsDeliveredInWatchedDirectory() throws Exception {
      createFile("a");
      watcher.watch(fileBase, false);

      deleteFile("a");
      
      assertEvent("a", Deleted);
   }

   public void testModifiedFileEventIsDeliveredInWatchedDirectory() throws Exception {
      createFile("a");
      watcher.watch(fileBase, false);

      touchFile("a");
      
      assertEvent("a", Modified);
   }

   public void testSubdirectoriesAreNotWatchedIfNotRecursive() throws Exception {
      createFile("a/b");
      watcher.watch(fileBase, false);

      touchFile("a/b");
      
      assertNoEvents();
   }

   public void testSubdirectoriesAreWatchedIfRecursive() throws Exception {
      createFile("a/b");
      watcher.watch(fileBase, true);

      touchFile("a/b");
      
      assertEvent("a/b", Modified);
   }

   public void testNewSubdirectoriesAreAutomaticallyWatchedIfRecursive() throws Exception {
      watcher.watch(fileBase, true);

      createFile("a/b");
      Thread.sleep(50); // If its too quick, it will be missed
      createFile("a/c");

      assertEvent("a/c", Created);
   }

   public void testTwiceRegisteringDoesNotDeliverEventTwice() throws Exception {
      watcher.watch(fileBase, false);
      watcher.watch(fileBase, false);

      createFile("a");
      
      assertEvent("a", Created);
      assertEquals(events.size(), 1);
   }

   private void assertNoEvents() throws InterruptedException {
      Thread.sleep(100);
      assertTrue(events.isEmpty(), "events were not empty: "+events);
   }

   private void assertEvent(String fileName, ChangeType changeType) throws InterruptedException {
      ChangeEvent event = changeEvent(fileName, changeType);
      long endTime = System.currentTimeMillis() + 100;
      synchronized (events) {
         while (!events.contains(event)) {
            long currentTime = System.currentTimeMillis();
            if (currentTime >= endTime) {
               fail("could not find change "+event+", changes were: "+events);
            }
            events.wait(endTime - currentTime + 10);
         }
      }
   }

   @BeforeMethod
   protected void setUp() throws Exception {
      events = new LinkedList<ChangeEvent>();
      watcher = new WatchForestImpl(new ChangeListener() {
         @Override
         public void handleChange(ChangeEvent event) {
            synchronized (events) {
               events.add(event);
               events.notifyAll();
            }
         }
      });
   }

   @AfterMethod
   protected void tearDown() throws Exception {
      watcher.close();
   }
}
