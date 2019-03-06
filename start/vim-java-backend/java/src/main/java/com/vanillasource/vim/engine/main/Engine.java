/**
 * Copyright (C) 2015 Robert Braeutigam.
 *
 * All rights reserved.
 */

package com.vanillasource.vim.engine.main;

import com.vanillasource.vim.engine.Command;
import com.vanillasource.vim.engine.PluginContext;
import com.vanillasource.vim.engine.Plugin;
import com.vanillasource.vim.engine.script.Message;
import com.vanillasource.vim.engine.VimScript;
import com.eclipsesource.json.Json;
import com.eclipsesource.json.JsonValue;
import java.lang.reflect.Method;
import java.util.ServiceLoader;
import java.util.List;
import java.util.LinkedList;
import java.util.Map;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.stream.Collectors;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.TimeUnit;
import static java.util.Arrays.asList;
import java.io.Reader;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.BufferedWriter;
import java.io.IOException;
import org.apache.log4j.Logger;
import java.net.ServerSocket;
import java.net.InetAddress;
import java.net.Socket;

public final class Engine implements PluginContext {
   private static final Logger LOGGER = Logger.getLogger(Engine.class);
   private final Map<ContextKey, Object> objects = new HashMap<>();
   private final ServiceLoader<Plugin> plugins = ServiceLoader.load(Plugin.class);
   private final Map<String, Command> commands = new HashMap<>();
   private final ServerSocket serverSocket;
   private boolean running = true;

   public Engine() throws IOException {
      this.serverSocket = new ServerSocket(7766, 10);
      LOGGER.info("vim java backend accepting commands...");
   }

   public void run() throws IOException {
      try {
         startPlugins();
         while (running) {
            executeCommands();
         }
      } finally {
         stopListeningForCommands();
      }
   }

   @Override
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

   @Override
   public <V> void set(ContextKey<V> key, V value) {
      synchronized (objects) {
         objects.put(key, value);
         objects.notifyAll();
      }
   }

   @Override
   public void registerCommand(String commandName, Command command) {
      synchronized (commands) {
         commands.put(commandName, command);
      }
   }

   private void startPlugins() {
      ForkJoinPool pool = new ForkJoinPool();
      plugins.forEach(plugin -> {
         LOGGER.info("loading "+plugin.getClass());
         plugin.startPlugin(Engine.this);
      });
      pool.shutdown();
      try {
         if (!pool.awaitTermination(2, TimeUnit.SECONDS)) {
            throw new RuntimeException("could not start bundles, initialization took more than 2 second");
         }
      } catch (InterruptedException e) {
         throw new RuntimeException("could not start bundles, wait interrupted", e);
      }
   }

   private void stopListeningForCommands() throws IOException {
      serverSocket.close();
      LOGGER.info("vim java backend stopped listening for commands.");
   }

   private void executeCommands() {
      try {
         Socket socket = serverSocket.accept();
         try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            String inputLine = reader.readLine();
            LOGGER.info("received "+inputLine);
            JsonValue value = Json.parse(inputLine);
            long requestId = value.asArray().get(0).asLong();
            String[] parts = value.asArray().get(1).asString().split(",");
            String command = parts[0];
            Map<String, String> parameters = asList(parts).subList(1, parts.length).stream()
               .map(s -> s.split(":"))
               .collect(Collectors.toMap(p -> p[0], p -> p[1]));
            LOGGER.info("executing command  "+command+" with "+parameters);
            VimScript response = executeCommand(command, parameters);
            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
            writer.write("["+requestId+", \""+response.toScript()+"\"]");
            writer.close();
            reader.close();
         } finally {
            socket.close();
         }
      } catch (Exception e) {
         LOGGER.error("error while processing request", e);
      }
   }

   private VimScript executeCommand(String commandString, Map<String, String> parameters) {
      Command command = commands.get(commandString);
      if ("exit".equals(commandString)) {
         LOGGER.info("exiting...");
         running = false;
         return new Message(Message.Severity.Info, "Java backend exiting...");
      } else if (command != null) {
         return command.execute(parameters);
      } else {
         LOGGER.warn("did not find command: "+commandString);
         return new Message(Message.Severity.Error, "Command not found '"+commandString+"'.");
      }
   }
}

