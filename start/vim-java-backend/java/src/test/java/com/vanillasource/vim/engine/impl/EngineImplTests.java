/**
 * Copyright (C) 2015 Robert Braeutigam.
 *
 * All rights reserved.
 */

package com.vanillasource.vim.engine.impl;

import org.testng.annotations.Test;
import org.testng.annotations.AfterMethod;
import static org.mockito.Mockito.*;
import static org.testng.Assert.*;
import static java.util.Arrays.asList;
import java.util.List;
import java.net.*;
import java.io.*;
import com.vanillasource.vim.engine.PluginContext;
import com.vanillasource.vim.engine.ContextKey;
import com.vanillasource.vim.engine.Command;
import com.vanillasource.vim.engine.Response;
import com.vanillasource.vim.engine.response.*;

@Test
public class EngineImplTests {
   private EngineImpl engine;
   private Thread engineThread;
   private boolean running;
   private Exception engineException;

   public void testEngineIsStoppedAferExitCommand() throws Exception {
      start();

      exit();
   }

   public void testEngineAnswersWithErrorOnCommandNotFound() throws Exception {
      start();

      String result = execute("some.command");

      assertTrue(result.contains("Command not found"));
   }

   @SuppressWarnings("unchecked")
   public void testEngineInitializesAllBundles() throws Exception {
      Bundle bundle1 = mock(Bundle.class);
      Bundle bundle2 = mock(Bundle.class);

      start(bundle1, bundle2);
      exit();

      verify(bundle1).start(eq(asList(bundle1, bundle2)), any(PluginContext.class));
      verify(bundle2).start(eq(asList(bundle1, bundle2)), any(PluginContext.class));
   }

   public void testBundlesCanGetEachothersPublishedObjects() throws Exception {
      final ContextKey<String> key1 = new ContextKey<String>() {};
      final ContextKey<String> key2 = new ContextKey<String>() {};
      final ContextKey<String> key3 = new ContextKey<String>() {};
      AbstractBundle bundle1 = new AbstractBundle() {
         @Override
         public void start(List<Bundle> bundles, PluginContext context) {
            context.set(key1, "Value1");
            context.get(key2);
            context.set(key3, "Value3");
            super.start(bundles, context);
         }
      };
      AbstractBundle bundle2 = new AbstractBundle() {
         @Override
         public void start(List<Bundle> bundles, PluginContext context) {
            context.get(key1);
            context.set(key2, "Value2");
            context.get(key3);
            super.start(bundles, context);
         }
      };

      start(bundle1, bundle2);
      exit();

      assertTrue(bundle1.isInitialized());
      assertTrue(bundle2.isInitialized());
   }

   public void testMissingDependenciesCauseExceptionInRun() throws Exception {
      final ContextKey<String> key1 = new ContextKey<String>() {};
      AbstractBundle bundle1 = new AbstractBundle() {
         @Override
         public void start(List<Bundle> bundles, PluginContext context) {
            context.get(key1);
            super.start(bundles, context);
         }
      };

      start(bundle1);

      engineThread.join(3000);
      engine = null;
      assertFalse(running);
      assertNotNull(engineException);
   }

   public void testCommandCanBeInvoked() throws Exception {
      start(new CommandBundle());

      String reply = execute("com.vanillasource.vim.engine.impl.EngineImplTests$TestCommand.echo",
            "message This is the message");

      assertTrue(reply.contains("This is the message"));
   }

   private String execute(String... lines) throws Exception {
      Socket socket = new Socket(InetAddress.getLocalHost(), 7766);
      OutputStreamWriter writer = new OutputStreamWriter(socket.getOutputStream());
      for (String line: lines) {
         writer.write(line+"\n");
      }
      writer.write("\n");
      writer.flush();
      BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
      StringBuilder builder = new StringBuilder();
      String line;
      while ( (line = reader.readLine()) != null) {
         builder.append(line);
      }
      socket.close();
      return builder.toString();
   }

   private void exit() throws Exception {
      execute("exit");
      engineThread.join(3000);
      engine = null;
      engineThread = null;
      assertFalse(running);
   }

   private void start(Bundle... bundles) {
      running = true;
      engineException = null;
      engine = new EngineImpl(asList(bundles));
      engineThread = new Thread(new Runnable() {
         @Override
         public void run() {
            try {
               engine.run();
            } catch (Exception e) {
               engineException = e;
            }
            running = false;
         }
      });
      engineThread.setDaemon(true);
      engineThread.start();
   }

   @AfterMethod
   protected void tearDown() throws Exception {
      if (engine != null) {
         exit();
      }
   }

   private static class AbstractBundle implements Bundle {
      private boolean initialized = false;

      @Override
      public Class<?> loadClass(String className) throws ClassNotFoundException {
         return null;
      }

      @Override
      public boolean isExporting(String className) {
         return false;
      }

      @Override
      public void start(List<Bundle> bundles, PluginContext context) {
         initialized = true;
      }

      public boolean isInitialized() {
         return initialized;
      }
   }

   private static class CommandBundle extends AbstractBundle {
      @Override
      public void start(List<Bundle> bundles, PluginContext context) {
         context.registerCommand(new TestCommand());
         super.start(bundles, context);
      }
   }

   private static class TestCommand implements Command {
      public Response echo(String message) {
         return new MessageResponse(MessageResponse.Severity.Info, message);
      }
   }
}


