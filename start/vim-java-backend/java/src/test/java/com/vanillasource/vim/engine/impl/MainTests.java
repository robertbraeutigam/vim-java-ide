/**
 * Copyright (C) 2015 Robert Braeutigam.
 *
 * All rights reserved.
 */

package com.vanillasource.vim.engine.impl;

import org.testng.annotations.Test;
import org.testng.annotations.BeforeMethod;
import static org.mockito.Mockito.*;
import static org.testng.Assert.*;
import java.util.List;
import java.util.ArrayList;
import java.io.File;
import java.net.URL;

@Test
public class MainTests {
   private Engine engine;
   private List<Bundle> engineBundles;
   private Main main;

   public void testEngineIsStarted() throws Exception {
      run();

      verify(engine).run();
   }

   public void testNoScanDirectoryResultsInNoBundles() throws Exception {
      run();

      assertTrue(engineBundles.isEmpty());
   }

   public void testJarWithoutPluginEntryAreNotReadAsBundles() throws Exception {
      run("test1");

      assertTrue(engineBundles.isEmpty());
   }

   public void testJarWithPluginEntryGetsLoaded() throws  Exception {
      run("test2");

      assertEquals(engineBundles.size(), 1);
   }

   private void run(String... paths) throws Exception {
      List<String> absolutePaths = new ArrayList<String>();
      String rootPath = new File(getClass().getClassLoader().getResource("rootmarker").toURI()).getParent().toString();
      for (String path: paths) {
         absolutePaths.add(rootPath + File.separator + path);
      }
      main.run(absolutePaths.toArray(new String[] {}));
   }

   @BeforeMethod
   protected void setUp() {
      engine = mock(Engine.class);
      main = new Main() {
         @Override
         Engine newEngineInstance(List<Bundle> bundles) {
            engineBundles = bundles;
            return engine;
         }

         @Override
         Bundle newBundleInstance(URL jarFile) {
            return mock(Bundle.class);
         }
      };
   }
}


