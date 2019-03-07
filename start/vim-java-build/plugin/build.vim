
if exists('g:loaded_build')
   finish
endif
let g:loaded_build = 1 

highlight JavaQuickfixErrors ctermbg=red guibg=red

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
      call clearmatches()
      for qf in getqflist()
         if bufnr('%') == qf['bufnr']
            call matchaddpos("JavaQuickfixErrors", [[qf['lnum'], qf['col']]])
         endif
      endfor
   endif
endfunction

augroup autocompile
   autocmd BufWritePost,FileWritePost *.java call ExecuteCompile()
augroup END 

