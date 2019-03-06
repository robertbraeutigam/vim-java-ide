/**
 * Copyright (C) 2013 Robert Braeutigam.
 *
 * All rights reserved.
 */

package com.vanillasource.vim.changes.impl;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.AfterMethod;
import java.io.File;
import org.apache.commons.io.FileUtils;
import java.nio.file.Files;
import com.vanillasource.vim.changes.ChangeEvent.ChangeType;
import com.vanillasource.vim.changes.ChangeEvent;

public class FileTestsBase {
   protected File fileBase;

   protected void createFile(String fileName) throws Exception {
      File file = new File(fileBase.getAbsolutePath() + File.separator + fileName);
      file.getParentFile().mkdirs();
      FileUtils.touch(file);
   }

   protected void deleteFile(String fileName) throws Exception {
      File file = new File(fileBase.getAbsolutePath() + File.separator + fileName);
      file.delete();
   }

   protected void touchFile(String fileName) throws Exception {
      File file = new File(fileBase.getAbsolutePath() + File.separator + fileName);
      FileUtils.touch(file);
   }

   protected ChangeEvent changeEvent(String fileName, ChangeType changeType) {
      return new ChangeEvent(file(fileName), changeType);
   }

   protected File file(String fileName) {
      return new File(fileBase.getAbsolutePath()+File.separator+fileName);
   }

   @BeforeMethod
   protected void setUpBaseDir() throws Exception {
      fileBase = Files.createTempDirectory("watcher-test").toFile();
   }

   @AfterMethod
   protected void tearDownBaseDir() throws Exception {
      FileUtils.deleteDirectory(fileBase);
   }
}


