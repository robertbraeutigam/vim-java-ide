/**
 * Copyright (C) 2015 Robert Braeutigam.
 *
 * All rights reserved.
 */

package com.vanillasource.vim.engine.impl;

import com.vanillasource.vim.engine.ContextKey;
import com.vanillasource.vim.engine.Command;
import com.vanillasource.vim.engine.PluginContext;
import com.vanillasource.vim.engine.Response;
import com.vanillasource.vim.engine.response.MessageResponse;
import java.lang.reflect.Method;
import java.util.List;
import java.util.LinkedList;
import java.util.Map;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.TimeUnit;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.BufferedWriter;
import org.apache.log4j.Logger;
import java.net.ServerSocket;
import java.net.InetAddress;
import java.net.Socket;

public class EngineImpl implements Engine {
   private static final Logger logger = Logger.getLogger(Engine.class);
   private List<Bundle> bundles;
   private Map<ContextKey, Object> objects = new HashMap<>();
   private List<Command> commands = new ArrayList<>();
   private ServerSocket serverSocket;
   private boolean running = true;

   public EngineImpl(List<Bundle> bundles) {
      this.bundles = bundles;
      startListeningForCommands();
   }

   @Override
   public void run() {
      try {
         startBundles();
         while (running) {
            executeCommands();
         }
      } finally {
         stopListeningForCommands();
      }
   }

   private void startBundles() {
      List<BundleContext> bundleContexts = createBundleContexts();
      ForkJoinPool pool = new ForkJoinPool();
      startAllBundles(pool, bundleContexts);
      pool.shutdown();
      try {
         if (!pool.awaitTermination(2, TimeUnit.SECONDS)) {
            throw new RuntimeException("could not start bundles, initialization took more than 2 second");
         }
      } catch (InterruptedException e) {
         throw new RuntimeException("could not start bundles, wait interrupted", e);
      }
   }

   private List<BundleContext> createBundleContexts() {
      List<BundleContext> bundleContexts = new ArrayList<>();
      for (Bundle bundle : bundles) {
         BundleContext bundleContext = new BundleContext(bundle);
         bundleContexts.add(bundleContext);
      }
      return bundleContexts;
   }

   private void startAllBundles(ForkJoinPool pool, List<BundleContext> bundleContexts) {
      for (final BundleContext bundleContext : bundleContexts) {
         pool.execute(new Runnable() {
            @Override
            public void run() {
               try {
                  bundleContext.bundle.start(bundles, bundleContext);
               } catch (Throwable t) {
                  logger.warn("error initializing bundle", t);
               }
            }
         });
      }
   }

   private void startListeningForCommands() {
      try {
         serverSocket = new ServerSocket(7766, 10, InetAddress.getLocalHost());
         logger.info("vim java backend accepting commands...");
      } catch (Exception e) {
         throw new RuntimeException("could not establish socket", e);
      }
   }

   private void stopListeningForCommands() {
      try {
         serverSocket.close();
      } catch (Exception e) {
         throw new RuntimeException("could not close socket", e);
      }
   }

   private void executeCommands() {
      try {
         Socket socket = serverSocket.accept();
         try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            String command = reader.readLine();
            List<String> parameters = new LinkedList<>();
            String line;
            while ( !"".equals(line = reader.readLine()) ) {
               parameters.add(line);
            }
            logger.info("executing command: "+command+", parameters: "+parameters);
            Response response = executeCommand(command, parameters);
            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
            writer.write(response.toScript());
            writer.close();
            reader.close();
         } finally {
            socket.close();
         }
      } catch (Exception e) {
         logger.error("error while processing request", e);
      }
   }

   private Response executeCommand(String commandString, List<String> parameters) {
      if ("exit".equals(commandString)) {
         logger.info("exiting...");
         running = false;
         return new MessageResponse(MessageResponse.Severity.Info, "Java backend exiting...");
      } else {
         if (commandString.indexOf('.') <= 0) {
            logger.warn("command can not be executed, does not seem to be a canonical class name");
            return new MessageResponse(MessageResponse.Severity.Error, "Command was not correctly specified '"+commandString+"'.");
         } else {
            String commandClassName = commandString.substring(0, commandString.lastIndexOf('.'));
            String methodName = commandString.substring(commandString.lastIndexOf('.')+1);
            for (Command command : commands) {
               if (command.getClass().getName().equals(commandClassName)) {
                  return executeCommand(command, methodName, parameters);
               }
            }
            logger.warn("did not find command: "+commandClassName);
            return new MessageResponse(MessageResponse.Severity.Error, "Command not found '"+commandClassName+"'.");
         }
      }
   }

   private Response executeCommand(Command command, String methodName, List<String> parameters) {
      for (Method method : command.getClass().getMethods()) {
         if (method.getName().equals(methodName)) {
            if (!Response.class.isAssignableFrom(method.getReturnType())) {
               logger.warn("method '"+methodName+"' in command: "+command+" does not return a response type");
               return new MessageResponse(MessageResponse.Severity.Error, "Specified command method '"+methodName+"' does not return a response type, can not execute.");
            }
            return executeCommand(command, method, parameters);
         }
      }
      logger.warn("did not find command method '"+methodName+"' in command: "+command);
      return new MessageResponse(MessageResponse.Severity.Error, "Method '"+methodName+"' not found in command '"+command.getClass().getName()+".");
   }

   private Response executeCommand(Command command, Method method, List<String> parameters) {
      if (parameters.size() != method.getParameterTypes().length) {
         logger.warn("wrong number of parameters for method: "+method+", parameters were: "+parameters);
         return new MessageResponse(MessageResponse.Severity.Error, "Wrong number of parameters for command '"+command.getClass().getName()+"', method: '"+method.getName()+"', needed: "+method.getParameterTypes().length+" vs. specified: "+parameters.size());
      }
      List<Object> parameterObjects = new ArrayList<>();
      Class<?>[] parameterClasses = method.getParameterTypes();
      for (int i=0; i<parameters.size(); i++) {
         parameterObjects.add(getParameterObject(parameterClasses[i], parameters.get(i)));
      }
      try {
         Response response = (Response) method.invoke(command, parameterObjects.toArray());
         if (response == null) {
            return new MessageResponse(MessageResponse.Severity.Info, "Call returned 'null' for command '"+command.getClass().getName()+"', method: '"+method.getName()+"'.");
         }
         return response;
      } catch (Exception e) {
         logger.warn("there was an exception while executing method: "+method, e);
         return new MessageResponse(MessageResponse.Severity.Error, "Call returned "+e.getClass().getName()+" with message '"+e.getMessage()+"', command '"+command.getClass().getName()+"', method: '"+method.getName()+"'.");
      }
   }

   private Object getParameterObject(Class<?> parameterClass, String parameterString) {
      String parameterName = parameterString.substring(0, parameterString.indexOf(" "));
      String parameterValue = parameterString.substring(parameterString.indexOf(" ")+1);
      if (String.class.equals(parameterClass)) {
         return parameterValue;
      } else if (boolean.class.equals(parameterClass) || Boolean.class.equals(parameterClass)) {
         try {
            return Boolean.valueOf(parameterValue);
         } catch (RuntimeException e) {
            return Integer.valueOf(parameterValue) != 0;
         }
      } else if (int.class.equals(parameterClass) || Integer.class.equals(parameterClass)) {
         return Integer.valueOf(parameterValue);
      } else if (long.class.equals(parameterClass) || Long.class.equals(parameterClass)) {
         return Long.valueOf(parameterValue);
      }
      logger.warn("unknown parameter type: "+parameterClass+", will return null");
      return null;
   }

   public class BundleContext implements PluginContext {
      private Bundle bundle;

      public BundleContext(Bundle bundle) {
         this.bundle = bundle;
      }

      @SuppressWarnings("unchecked")
      public <V> V get(ContextKey<V> key) {
         try {
            synchronized (objects) {
               while (!objects.containsKey(key)) {
                  objects.wait();
               }
               return (V) objects.get(key);
            }
         } catch (InterruptedException e) {
            throw new RuntimeException("wait interrupted for bundle, its probably not initialized correctly now", e);
         }
      }

      public <V> void set(ContextKey<V> key, V value) {
         synchronized (objects) {
            objects.put(key, value);
            objects.notifyAll();
         }
      }

      public void registerCommand(Command command) {
         synchronized (commands) {
            logger.info("registering command: "+command);
            commands.add(command);
         }
      }
   }
}


