/**
 * Copyright (C) 2015 Robert Braeutigam.
 *
 * All rights reserved.
 */

package com.vanillasource.vim.build.util;

import java.util.*;
import java.net.*;
import java.io.*;
import javax.tools.*;
import com.vanillasource.vim.build.FileMessage;
import org.apache.log4j.Logger;

public class JavacUtils {
   private static final Logger logger = Logger.getLogger(JavacUtils.class);
   private static StandardJavaFileManager fileManager;

   public static List<FileMessage> compile(List<String> arguments, List<File> files) {
      final StandardJavaFileManager fileManager = ToolProvider.getSystemJavaCompiler().getStandardFileManager(null,null,null);
      final List<FileMessage> messages = new LinkedList<>();
      JavaCompiler.CompilationTask compileTask = ToolProvider.getSystemJavaCompiler().
         getTask(null,fileManager,new DiagnosticListener<JavaFileObject>() {
            @Override
            public void report(Diagnostic<? extends JavaFileObject> diagnostic) {
               if (diagnostic.getSource() != null) {
                  messages.add(new FileMessage(new File(diagnostic.getSource().getName()), toSeverity(diagnostic.getKind()),
                        diagnostic.getMessage(Locale.getDefault()), diagnostic.getLineNumber(), diagnostic.getColumnNumber()));
               } else {
                  logger.warn("did not add diagnostic message without source: "+diagnostic);
               }
            }
         },arguments,null,fileManager.getJavaFileObjectsFromFiles(files));
      compileTask.call();
      return messages;
   }

   private static FileMessage.Severity toSeverity(Diagnostic.Kind kind) {
      switch (kind) {
         case ERROR:
            return FileMessage.Severity.Error;
         case WARNING:
         case MANDATORY_WARNING:
            return FileMessage.Severity.Information;
         default:
            return FileMessage.Severity.Information;
      }
   }
}




