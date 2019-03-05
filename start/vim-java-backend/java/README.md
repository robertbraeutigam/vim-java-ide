Vim Java Backend Engine
=======================

This Java project is designed to enable running Java-based commands from Vim. Because
running single Java programs is too slow (JVM startup time), this engine
is designed to run in the background and accept commands through a very simple socket-based
protocol. This also enables background, long running or asynchronous jobs.

The Engine itself does not provide any functionality but it can be extended through a
very simple Plugin mechanism.

## Writing Plugins

Plugins should be normal Java projects that produce a Jar file, potentially with some dependencies delivered
independently. The Plugin Jar file has to have some additional *Manifest* entries:

 * `Class-Path`: The list of jar files the plugin needs and were delivered together with the plugin.
 * `Vim-Plugin-Class`: The fully qualified classname that implements the `Plugin` interface.
 * `Vim-Exported-Packages`: The list of packages other plugins are allowed to see from this plugin.

Each plugin is loaded in its own classloader, which by default has access to the Vim Backend
Engine jar, and log4j for Logging. Also the plugin classloader has access to any jar files in the
same directory as the plugin jar file and explicitly defined `Class-Path` *Manifest* entry.

The plugin also automatically has access to any packages of other plugins, which are
exported through the entry `Vim-Exported-Packages`.

## Maven

The following pom.xml fragment is suitable for producing a Plugin for this engine:

```xml
   ...
   <build>
      <plugins>
         ...
         <plugin>
            <groupId>org.apache.maven.plugins</groupId>
            <artifactId>maven-jar-plugin</artifactId>
            <version>2.5</version>
            <configuration>
               <archive>
                  <manifest>
                     <addClasspath>true</addClasspath>
                  </manifest>
                  <manifestEntries>
                     <Vim-Plugin-Class>com.package.my.impl.MyPlugin</Vim-Plugin-Class>
                     <Vim-Exported-Packages>com.package.my.api</Vim-Exported-Packages>
                  </manifestEntries>
               </archive>
            </configuration>
         </plugin>
      </plugins>
   </build>

   <dependencies>
      <dependency>
         <groupId>com.vanillasource.vim</groupId>
         <artifactId>vim-java-backend-engine</artifactId>
         <version>1.0.0</version>
         <scope>provided</scope>
      </dependency>
      ...
   </dependencies>
```

Please note, that the `vim-java-backend-engine` artifact is in the *provided* scope. Also, other Plugins you might
use should also be in the *provided* scope.

## Bundling the Plugin

If the plugin is ready, it can be delivered together with its dependencies via a normal vim plugin distribution. For example:

```
  + plugin
  |- myawsomeplugin.vim
  + autoload
  |- myawsomeplugin.vim
  + java-plugin
  |- myawesomeplugin-1.0.0.jar
  |- somelib1-2.3.4.jar
  `- someotherlib-1.1.1.jar
```

## Logging

The backend engine comes with a log4j (1.2.x) library that is available to all plugins. All logging done will be
written to `~/.vim-java-backend.log`.


