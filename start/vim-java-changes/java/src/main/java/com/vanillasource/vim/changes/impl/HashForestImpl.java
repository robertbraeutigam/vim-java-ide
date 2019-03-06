/**
 * Copyright (C) 2013 Robert Braeutigam.
 *
 * All rights reserved.
 */

package com.vanillasource.vim.changes.impl;

import com.vanillasource.vim.changes.ChangeEvent;
import java.io.File;
import java.io.IOException;
import java.io.FileInputStream;
import java.util.Set;
import java.util.Iterator;
import java.util.HashSet;
import java.util.EnumSet;
import java.nio.file.*;
import org.apache.commons.codec.digest.DigestUtils;
import java.nio.file.attribute.BasicFileAttributes;
import static com.vanillasource.vim.changes.ChangeEvent.ChangeType.*;
import org.apache.log4j.Logger;

public class HashForestImpl implements HashForest, Runnable {
   private static final Logger logger = Logger.getLogger(HashForestImpl.class);
   private HashListener listener;
   private Set<File> jobs = new HashSet<>();
   private Thread hashingThread;
   private boolean running = true;

   public HashForestImpl(HashListener listener) {
      this.listener = listener;
      hashingThread = new Thread(this);
      hashingThread.setName("Hashing Thread");
      hashingThread.setDaemon(true);
      hashingThread.start();
   }

   @Override
   public void handleChange(ChangeEvent event) {
      if ((event.getChangeType() == Modified) && (event.getFile().isFile())) {
         addJob(event.getFile());
      } else if ((event.getChangeType() == Created) && ((event.getFile().isFile()))) {
         addJob(event.getFile());
      } else if ((event.getChangeType() == Created) && ((event.getFile().isDirectory()))) {
         try {
            recursiveAddJob(event.getFile(), true);
         } catch (IOException e) {
            logger.warn("could not hash all created files for "+event, e);
         }
      }
   }

   private void addJob(File file) {
      synchronized (jobs) {
         jobs.add(file);
         jobs.notifyAll();
      }
   }

   @Override
   public void registerPath(File file, boolean recursive) throws IOException {
      recursiveAddJob(file, recursive);
   }

   private void recursiveAddJob(File file, boolean recursive) throws IOException {
      Files.walkFileTree(file.toPath(), EnumSet.noneOf(FileVisitOption.class),
            recursive?Integer.MAX_VALUE:1, new SimpleFileVisitor<Path>() {
         public FileVisitResult visitFile(Path path, BasicFileAttributes attrs) throws IOException {
            addJob(path.toFile());
            return super.visitFile(path, attrs);
         }
      });
   }

   @Override
   @SuppressWarnings("unchecked")
   public void run() {
      try {
         while (running) {
            File job = null;
            synchronized (jobs) {
               if (jobs.isEmpty()) {
                  jobs.wait();
               }
               if (!jobs.isEmpty()) {
                  Iterator<File> jobsIterator = jobs.iterator();
                  job = jobsIterator.next();
                  jobsIterator.remove();
               }
            }
            if (job != null) {
               process(job);
            }
         }
      } catch (InterruptedException e) {
         logger.warn("hashing interrupted", e);
      }
   }

   private void process(File job) {
      try {
         if (job.isFile()) {
            logger.debug("hashing file: "+job);
            String md5sum = DigestUtils.md5Hex(new FileInputStream(job));
            listener.handleHash(job, md5sum);
         }
      } catch (IOException e) {
         logger.warn("could not md5sum file: "+job, e);
      }
   }

   @Override
   public void close() {
      running = false;
      synchronized (jobs) {
         jobs.notifyAll();
      }
      try {
         hashingThread.join();
      } catch (InterruptedException e) {
         logger.warn("closing hash forest interrupted", e);
      }
   }
}


