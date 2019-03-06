
if exists('g:loaded_tags')
   finish
endif
let g:loaded_tags = 1 

set tags+=.tags;/

function! ExecuteTagsUpdate()
   if exists("b:BuildName") && !empty(b:BuildName)
      call JavaBackendEval('updateTags', {'fileName': expand('%:p')})
   endif
endfunction

augroup autotagsupdate
   autocmd BufWritePost,FileWritePost * call ExecuteTagsUpdate()
augroup END 

