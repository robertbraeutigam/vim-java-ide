
if exists('g:loaded_build')
   finish
endif
let g:loaded_build = 1 

function! ExecuteCompile()
   if !exists("b:BuildName")
      let b:BuildName = ''
      call JavaBackendEval('getBuildParameters', {'fileName': expand('%:p') })
   endif
   if !empty(b:BuildName)
      " Don't use make, because it can't be made silent, but simulate quickfix
      " command
      call JavaBackendEval('compile', {'fileName': expand('%:p') })
      do QuickFixCmdPost
   endif
endfunction

augroup autocompile
   autocmd BufWritePost,FileWritePost *.java call ExecuteCompile()
augroup END 

