/**
 * Copyright (C) 2015 Robert Braeutigam.
 *
 * All rights reserved.
 */

package com.vanillasource.vim.changes.impl;

import org.testng.annotations.Test;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.AfterMethod;
import static org.testng.Assert.*;
import static org.mockito.Mockito.*;
import static com.vanillasource.vim.changes.ChangeEvent.ChangeType.*;

@Test
public class HashForestImplTests extends FileTestsBase {
   private HashForestImpl hashForest;
   private HashListener listener;

   public void testStartsAndStopsProperly() {
   }

   public void testHashesModifiedFile() throws Exception {
      createFile("a");

      hashForest.handleChange(changeEvent("a", Modified));
      hashForest.close();

      verify(listener).handleHash(file("a"), "d41d8cd98f00b204e9800998ecf8427e");
   }

   public void testHashesCreatedFile() throws Exception {
      createFile("a");

      hashForest.handleChange(changeEvent("a", Modified));
      hashForest.close();

      verify(listener).handleHash(file("a"), "d41d8cd98f00b204e9800998ecf8427e");
   }

   public void testDoesNotHashNonexistentFile() throws Exception {
      hashForest.handleChange(changeEvent("a", Modified));
      hashForest.close();

      verifyNoMoreInteractions(listener);
   }

   public void testDoesNotHashDeletedFile() throws Exception {
      createFile("a");
      hashForest.handleChange(changeEvent("a", Deleted));
      hashForest.close();

      verifyNoMoreInteractions(listener);
   }

   public void testCreatedDirectoryHashesRecursively() throws Exception {
      createFile("a/b");

      hashForest.handleChange(changeEvent("a", Created));
      hashForest.close();

      verify(listener).handleHash(file("a/b"), "d41d8cd98f00b204e9800998ecf8427e");
   }

   @BeforeMethod
   protected void setUp() {
      listener = mock(HashListener.class);
      hashForest = new HashForestImpl(listener);
   }

   @AfterMethod
   protected void tearDown() {
      hashForest.close();
   }
}

