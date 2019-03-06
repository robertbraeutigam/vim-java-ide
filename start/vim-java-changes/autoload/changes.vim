if exists('g:autoloaded_changes')
   finish
endif
let g:autoloaded_changes=1

function! changes#changes(listenerId)
   return JavaBackendExec('pollChanges', { 'listenerId': a:listenerId })
endfunction

function! changes#register(listenerId, fileName, recursive)
   return JavaBackendExec('registerPath', { 'listenerId': a:listenerId, 'fileName': a:fileName, 'recursive', a:recursive })
endfunction

