if exists('g:loaded_projectpath')
   finish
endif
let g:loaded_projectpath=1

augroup projectpath
   autocmd!
   autocmd BufRead *
            \ call projectpath#searchForVCS() |
            \ call projectpath#searchForBuild()
augroup END

