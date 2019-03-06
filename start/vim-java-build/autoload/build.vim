if exists('g:autoloaded_build')
   finish
endif
let g:autoloaded_build=1

function! build#compile(fileName)
   return JavaBackendExec('com.vanillasource.vim.build.impl.BuildCommand.compile', [ [ 'fileName', a:fileName ] ])
endfunction


