/**
 * Copyright (C) 2019 Robert Braeutigam.
 *
 * All rights reserved.
 */

package com.vanillasource.vim.build.context;

import java.io.File;
import java.util.Collection;
import java.util.Map;
import java.util.List;
import java.util.ArrayList;
import java.util.HashMap;
import java.net.URL;
import org.apache.log4j.Logger;
import io.github.classgraph.ClassGraph;
import io.github.classgraph.ClassInfo;
import io.github.classgraph.ScanResult;

public final class ReflectionsCompilationContext implements CompilationContext {
   private static final Logger LOGGER = Logger.getLogger(ReflectionsCompilationContext.class);
   private final Map<String, List<String>> fqns = new HashMap<>();

   @Override
   public void configureUrls(Collection<File> files) {
      try (ScanResult scanResult = new ClassGraph()
            .enableSystemJarsAndModules()
            .blacklistPackages("com.vanillasource", "org.apache", "io.github").scan()) {
         for (ClassInfo info: scanResult.getAllClasses()) {
            fqns.computeIfAbsent(info.getSimpleName(), name -> new ArrayList<>()).add(info.getName());
         }
      }
      try (ScanResult scanResult = new ClassGraph()
            .enableClassInfo().overrideClasspath(files).scan()) {
         for (ClassInfo info: scanResult.getAllClasses()) {
            fqns.computeIfAbsent(info.getSimpleName(), name -> new ArrayList<>()).add(info.getName());
         }
      }
      LOGGER.info("scanned "+fqns.size()+" class simple names");
   }

   @Override
   public List<String> qualifiedNamesForSimpleName(String simpleName) {
      return fqns.computeIfAbsent(simpleName, name -> new ArrayList<>());
   }
}

