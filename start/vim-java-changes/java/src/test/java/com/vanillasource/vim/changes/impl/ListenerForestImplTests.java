/**
 * Copyright (C) 2013 Robert Braeutigam.
 *
 * All rights reserved.
 */

package com.vanillasource.vim.changes.impl;

import org.testng.annotations.Test;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.AfterMethod;
import static org.mockito.Mockito.*;
import static org.testng.Assert.*;
import java.util.List;
import java.io.File;
import com.vanillasource.vim.changes.ChangeEvent;
import static com.vanillasource.vim.changes.ChangeEvent.ChangeType.*;
import com.vanillasource.vim.changes.ChangeEvent.ChangeType;

@Test
public class ListenerForestImplTests extends FileTestsBase {
   private ListenerForestImpl listenerForest;

   public void testIfNothingIsRegisteredThereIsNoChange() {
      assertTrue(listenerForest.pollChanges().isEmpty());
   }

   public void testNewlyRegisteredNonrecursiveDirectoryShowsAllFilesAsChanged() throws Exception {
      createFile("a");
      createFile("b");

      listenerForest.registerPath(fileBase, false);
      List<ChangeEvent> changes = listenerForest.pollChanges();

      assertEquals(changes.size(), 3);
      assertTrue(changes.contains(changeEvent("a", Created)));
      assertTrue(changes.contains(changeEvent("b", Created)));
   }

   public void testRegisteredNonrecursiveDirectoryShowsAllFilesUpToDateOnSecondQuery() throws Exception {
      createFile("a");
      createFile("b");
      listenerForest.registerPath(fileBase, false);

      listenerForest.pollChanges();
      List<ChangeEvent> changes = listenerForest.pollChanges();

      assertTrue(changes.isEmpty());
   }

   public void testNewlyRegisteredNonrecursiveDirectoryDoesNotShowSubdirectories() throws Exception {
      createFile("a");
      createFile("b/c");
      listenerForest.registerPath(fileBase, false);

      List<ChangeEvent> changes = listenerForest.pollChanges();

      assertEquals(changes.size(), 2);
      assertTrue(changes.contains(changeEvent("a", Created)));
      assertFalse(changes.contains(changeEvent("b", Created)));
   }

   public void testNewlyRegisteredRecursiveDirectoryShowsAllFilesInAnyDepthAsChanged() throws Exception {
      createFile("a");
      createFile("b/c");

      listenerForest.registerPath(fileBase, true);
      List<ChangeEvent> changes = listenerForest.pollChanges();

      assertEquals(changes.size(), 4);
      assertTrue(changes.contains(changeEvent("a", Created)));
      assertTrue(changes.contains(changeEvent("b/c", Created)));
   }

   public void testCreatedFileAndThenModifiedIsReturnedAsCreated() throws Exception {
      createFile("a");
      listenerForest.registerPath(fileBase, false);

      listenerForest.handleChange(changeEvent("a", Modified));

      List<ChangeEvent> changes = listenerForest.pollChanges();
      assertTrue(changes.contains(changeEvent("a", Created)));
   }

   public void testModifiedFileIsReturnedAsModified() throws Exception {
      createFile("a");
      listenerForest.registerPath(fileBase, false);
      listenerForest.pollChanges();

      listenerForest.handleChange(changeEvent("a", Modified));

      List<ChangeEvent> changes = listenerForest.pollChanges();
      assertTrue(changes.contains(changeEvent("a", Modified)));
   }

   public void testModifiedUntrackedFileResultsInNoChanges() throws Exception {
      listenerForest.handleChange(changeEvent("a", Modified));

      assertTrue(listenerForest.pollChanges().isEmpty());
   }

   public void testDeleteFileResultsInDeleteEvent() throws Exception {
      createFile("a");
      listenerForest.registerPath(fileBase, false);

      listenerForest.handleChange(changeEvent("a", Deleted));

      List<ChangeEvent> changes = listenerForest.pollChanges();
      assertTrue(changes.contains(changeEvent("a", Deleted)));
   }

   public void testDeleteDirectoryDeliversChangeToAllKnownFiles() throws Exception {
      createFile("a/b");
      createFile("a/c");
      listenerForest.registerPath(fileBase, true);

      listenerForest.handleChange(changeEvent("a", Deleted));

      List<ChangeEvent> changes = listenerForest.pollChanges();
      assertTrue(changes.contains(changeEvent("a/b", Deleted)));
      assertTrue(changes.contains(changeEvent("a/c", Deleted)));
   }

   public void testDeletedFilesDoNotShowUpAfterTheirDeleteEvent() throws Exception {
      createFile("a");
      listenerForest.registerPath(fileBase, false);

      listenerForest.handleChange(changeEvent("a", Deleted));
      listenerForest.pollChanges();
      listenerForest.handleChange(changeEvent("a", Modified));

      assertTrue(listenerForest.pollChanges().isEmpty());
   }

   public void testSeparatelyRegisteredSubtreesDeleteTogether() throws Exception {
      createFile("a/b/c/d");
      listenerForest.registerPath(fileBase, false);
      listenerForest.registerPath(file("a/b"), false);
      listenerForest.registerPath(file("a/b/c"), false);
      listenerForest.pollChanges();

      listenerForest.handleChange(changeEvent("a/b", Deleted));

      List<ChangeEvent> changes = listenerForest.pollChanges();
      assertTrue(changes.contains(changeEvent("a/b", Deleted)));
      assertTrue(changes.contains(changeEvent("a/b/c", Deleted)));
      assertTrue(changes.contains(changeEvent("a/b/c/d", Deleted)));
   }

   public void testDeliversCreatedFileEvent() throws Exception {
      listenerForest.registerPath(fileBase, false);
      createFile("a");

      listenerForest.handleChange(changeEvent("a", Created));

      List<ChangeEvent> changes = listenerForest.pollChanges();
      assertTrue(changes.contains(changeEvent("a", Created)));
   }

   public void testCreatedDirectoryNotDeliveredIfNotRecursive() throws Exception {
      listenerForest.registerPath(fileBase, false);
      listenerForest.pollChanges();
      createFile("a/b");

      listenerForest.handleChange(changeEvent("a", Created));
      listenerForest.handleChange(changeEvent("a/b", Created));

      assertTrue(listenerForest.pollChanges().isEmpty());
   }

   public void testCreatedDirectoryAndAllFilesDeliveredIfRecursive() throws Exception {
      listenerForest.registerPath(fileBase, true);
      listenerForest.pollChanges();
      createFile("a/b");

      listenerForest.handleChange(changeEvent("a", Created));

      List<ChangeEvent> changes = listenerForest.pollChanges();
      assertTrue(changes.contains(changeEvent("a", Created)));
      assertTrue(changes.contains(changeEvent("a/b", Created)));
   }

   public void testDeletedThenCreatedIsDeliveredAsModified() throws Exception {
      createFile("a");
      listenerForest.registerPath(fileBase, true);
      listenerForest.pollChanges();

      listenerForest.handleChange(changeEvent("a", Deleted));
      listenerForest.handleChange(changeEvent("a", Created));

      List<ChangeEvent> changes = listenerForest.pollChanges();
      assertTrue(changes.contains(changeEvent("a", Modified)));
   }

   public void testCreatedAndThenDeletedShowsAsDeleted() throws Exception {
      listenerForest.registerPath(fileBase, true);
      listenerForest.pollChanges();

      createFile("a");
      listenerForest.handleChange(changeEvent("a", Created));
      listenerForest.handleChange(changeEvent("a", Deleted));

      List<ChangeEvent> changes = listenerForest.pollChanges();
      assertTrue(changes.contains(changeEvent("a", Deleted)));
   }

   public void testModifiedWithSameHashDoesNotShowUpAsChange() throws Exception {
      createFile("a");
      listenerForest.registerPath(fileBase, true);
      listenerForest.handleHash(file("a"), "123");
      listenerForest.pollChanges();

      listenerForest.handleChange(changeEvent("a", Modified));
      listenerForest.handleHash(file("a"), "123");

      assertTrue(listenerForest.pollChanges().isEmpty());
   }

   @BeforeMethod
   protected void setUp() {
      listenerForest = new ListenerForestImpl();
   }
}


