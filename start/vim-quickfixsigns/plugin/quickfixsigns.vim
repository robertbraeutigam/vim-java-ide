
if exists('g:loaded_quickfixsigns')
   finish
endif
let g:loaded_quickfixsigns = 1 
let g:IssueId = 1

sign define Dummy
sign define MessageE text=EE texthl=DiffText
sign define MessageW text=WW texthl=DiffText
sign define MessageI text=II texthl=DiffAdd

function! UpdateQuickfixSigns()
   sign unplace *
   exec "sign place 9999 line=1 name=Dummy buffer=".bufnr('')
   for qfitem in getqflist()
      let g:IssueId = g:IssueId + 1
      exec "sign place ".g:IssueId." line=".qfitem['lnum']." name=Message".qfitem['type']." buffer=".qfitem['bufnr']
   endfor
endfunction

augroup quickfixsigncommands
   autocmd QuickFixCmdPost * call UpdateQuickfixSigns()
   autocmd BufEnter * exec "sign place 9999 line=1 name=Dummy buffer=".bufnr('')
augroup END 

