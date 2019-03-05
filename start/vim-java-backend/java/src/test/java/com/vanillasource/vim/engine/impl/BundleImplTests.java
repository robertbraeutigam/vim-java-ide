/**
 * Copyright (C) 2015 Robert Braeutigam.
 *
 * All rights reserved.
 */

package com.vanillasource.vim.engine.impl;

import com.vanillasource.vim.engine.*;
import org.testng.annotations.Test;
import org.testng.annotations.AfterMethod;
import static org.mockito.Mockito.*;
import static org.testng.Assert.*;
import java.io.*;
import java.util.ArrayList;
import java.util.jar.*;
import java.net.URL;
import static java.util.Arrays.*;

@Test
public class BundleImplTests {
   private BundleImpl bundle;

   public void testBundleInitializesWithoutExceptionsOnSimpleJar() throws Exception {
      init();
   }

   @SuppressWarnings("unchecked")
   public void testPluginInStartedOnBundleStart() throws Exception {
      init();
      PluginContext context = mock(PluginContext.class);

      bundle.start(new ArrayList<Bundle>(), context);

      verify(context).set(any(ContextKey.class), eq("Test Value"));
   }

   public void testBundleExportsPackageGivenInManifest() throws Exception {
      init("com.something.a", null);

      assertTrue(bundle.isExporting("com.something.a.ClassA"));
   }

   public void testBundleDoesNotExportPackageNotGivenInManifest() throws Exception {
      init("com.something.a", null);

      assertFalse(bundle.isExporting("com.something.b.ClassB"));
   }

   public void testBundleExportsAllPackagesGivenInManifestWithSpaces() throws Exception {
      init("com.something.a com.something.b", null);

      assertTrue(bundle.isExporting("com.something.a.ClassA"));
      assertTrue(bundle.isExporting("com.something.b.ClassB"));
   }

   public void testBundleCanLoadPluginClass() throws Exception {
      init();

      assertNotNull(bundle.loadClass("TestPlugin"));
   }

   public void testBundleCanLoadClassesOnClassPath() throws Exception {
      File dependencyJar = createDependencyFile();
      init(null, dependencyJar.getName());

      assertNotNull(bundle.loadClass("ClassA"));
   }

   @Test(expectedExceptions = ClassNotFoundException.class)
   public void testBundleThrowsClassNotFoundOnNotBundledClass() throws Exception {
      File dependencyJar = createDependencyFile();
      init(null, dependencyJar.getName());

      assertNotNull(bundle.loadClass("ClassB"));
   }

   public void testBundleCanLoadExportedClassesOfOtherBundles() throws Exception {
      init();
      Bundle otherBundle = mock(Bundle.class);
      when(otherBundle.isExporting("some.pkg.ClassB")).thenReturn(true);
      PluginContext context = mock(PluginContext.class);
      bundle.start(asList(otherBundle), context);

      bundle.loadClass("some.pkg.ClassB");

      verify(otherBundle).loadClass("some.pkg.ClassB");
   }

   private void init() throws Exception {
      init(null, null);
   }

   private void init(String exports, String classPath) throws Exception {
      bundle = new BundleImpl(createBundleFile(exports, classPath));
   }

   private URL createBundleFile(String exports, String classPath) throws Exception {
      Manifest manifest = new Manifest();
      manifest.getMainAttributes().put(Attributes.Name.MANIFEST_VERSION, "1.0");
      manifest.getMainAttributes().putValue(Bundle.PLUGIN_CLASS_KEY, "TestPlugin");
      if (exports != null) {
         manifest.getMainAttributes().putValue(Bundle.EXPORTED_PACKAGES_KEY, exports);
      }
      if (classPath != null) {
         manifest.getMainAttributes().put(Attributes.Name.CLASS_PATH, classPath);
      }
      File testPluginClassFile = new File(getClass().getClassLoader().getResource("bundletest1/TestPlugin.class").toURI());
      return createJarFile(manifest, testPluginClassFile);
   }

   private URL createJarFile(Manifest manifest, File... files) throws IOException {
      File jarFile = File.createTempFile("plugin-bundle", ".jar");
      jarFile.deleteOnExit();
      JarOutputStream jarOut = new JarOutputStream(new FileOutputStream(jarFile), manifest);
      for (File file: files) {
         jarOut.putNextEntry(new JarEntry(file.getName()));
         FileInputStream in = new FileInputStream(file);
         int count = 0;
         byte buffer[] = new byte[10240];
         while ((count = in.read(buffer, 0, buffer.length)) >= 0) {
            jarOut.write(buffer, 0, count);
         }
         in.close();
      }
      jarOut.close();
      return jarFile.toURI().toURL();
   }

   private File createDependencyFile() throws Exception {
      File dependencyClass = new File(getClass().getClassLoader().getResource("bundletest1/ClassA.class").toURI());
      URL dependencyJarURL = createJarFile(new Manifest(), dependencyClass);
      return new File(dependencyJarURL.toURI());
   }

   /*
   private File copySomeBDependencyFile() throws Exception {
      File dependencyJar = new File(getClass().getClassLoader().getResource("bundletest1/someb.jar").toURI());
      InputStream in = new FileInputStream(dependencyJar);
      File outFile = File.createTempFile("someb", ".jar");
      outFile.deleteOnExit();
      OutputStream out = new FileOutputStream(outFile);
      int count = 0;
      byte buffer[] = new byte[10240];
      while ((count = in.read(buffer, 0, buffer.length)) >= 0) {
         out.write(buffer, 0, count);
      }
      out.close();
      in.close();
      return outFile;
   }
   */
}

