if &cp || v:version < 800 || (exists('g:loaded_java_backend') && g:loaded_java_backend)
   finish
endif
let g:loaded_java_backend=1

if !exists("g:java_command")
   if empty($JAVA_HOME)
      let g:java_command = "java"
   else
      let g:java_command = $JAVA_HOME."/bin/java"
   endif
endif

if !exists("g:mvn_command")
   let g:mvn_command = "mvn"
endif

highlight ResponseSuccess ctermbg=green ctermfg=white
highlight ResponseError ctermbg=red ctermfg=white
highlight ResponseInfo ctermfg=white

let s:script_path = expand('<sfile>:p:h')

function! JavaBackendExec(command, parameters)
   " Assemble input
   let input = s:encodeInput(a:command, a:parameters)
   " If channel is not establish, then establish, optionally start the engine
   if !exists('s:backend_channel') || ch_status(s:backend_channel) != "open"
      " Establish channel
      let s:backend_channel = ch_open("localhost:7766", {"mode":"json", "waittime":"100"})
      " If can not be established, compile and start backend
      if (ch_status(s:backend_channel) != "open")
         call s:startBackend()
         " Try again
         let s:backend_channel = ch_open("localhost:7766", {"mode":"json", "waittime":"100"})
         " If still not available, abort
         if (ch_status(s:backend_channel) != "open")
            echohl Error
            echon "Could not start vim backend engine!"
            echohl None
            return
         endif
      else
      endif
   endif
   return ch_evalexpr(s:backend_channel, input)
endfunction

function! JavaBackendExit()
   return JavaBackendExec("exit", {})
endfunction

function! s:startBackend()
   " First find all the directories for java plugins
   let pluginDirs = []
   for potentialPluginDir in split(join(split(&runtimepath,','),'/java '), ' ')
      if filereadable(potentialPluginDir."/pom.xml")
         call insert(pluginDirs, potentialPluginDir)
      endif
   endfor
   " Compile all java plugins
   for pluginDir in pluginDirs
      call system(g:mvn_command.' -f '.pluginDir.'/pom.xml install -DskipTests')
      if v:shell_error != 0
         echohl Error
         echo "Could compile java plugin at ".pluginDir."\n"
         echohl None
         return
      endif
   endfor
   " Start server
   let classpath = ""
   for pluginDir in pluginDirs
      let classpath = classpath . pluginDir . "/target/*:"
   endfor
   call job_start([ g:java_command, '-classpath', classpath, 'com.vanillasource.vim.engine.main.Main' ], {"stoponexit": ""})
   sleep 3
endfunction

function! s:encodeInput(command, parameters)
   let input = a:command
   for [key, value] in items(a:parameters)
      let input = input . "," . key . ":" . value
   endfor
   return input
endfunction

