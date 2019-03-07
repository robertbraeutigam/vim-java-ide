
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

function! QuickfixSuggestImport()
   let quickfix = {'text':'N/A'}
   for qf in getqflist()
      if bufnr('%') == qf['bufnr'] && line('.') == qf['lnum'] && stridx(qf['text'], "cannot find symbol")>=0
         let quickfix = qf
      endif
   endfor
   if quickfix['text'] != 'N/A'
      let matches = matchlist(substitute(quickfix['text'], '\n\+$', '', ''), 'class \([A-Z]\w*\)')
      if !empty(matches)
         let suggestions = split(JavaBackendExec("suggestImport", {'simpleName': matches[1]}), ',')
         if empty(suggestions)
            echo "No imports suggested for ".matches[1]
         elseif len(suggestions) == 1
            call JavaAddImport(suggestions[0])
         elseif len(suggestions) > 1
            call quickmenu#current(11)
            call quickmenu#header('Select import')
            call quickmenu#reset()
            for suggestion in suggestions
               call quickmenu#append(matches[1], 'call JavaAddImport("'.suggestion.'")', suggestion)
            endfor
            call quickmenu#bottom(11)
         endif
      endif
   endif
endfunction

function! JavaAddImport(fullClassName)
   let importLine = "import " . a:fullClassName . ";"
   " Split before we jump
   split

   let hasImport = JavaImpGotoLast()
   let importLoc = line('.')

   let hasPackage = JavaImpGotoPackage()
   if (hasPackage == 1)
      let pkgLoc = line('.')
      let pkgPat = '^\s*package\s\+\(\%(\w\+\.\)*\w\+\)\s*;.*$'
      let pkg = substitute(getline(pkgLoc), pkgPat, '\1', '')

      " Check to see if the class is in this package, we won't
      " need an import.
      if (hasImport == 0)
         " Add an extra blank line after the package before
         " the import
         let importLoc = pkgLoc + 1
      endif
   elseif (hasImport == 0)
      let importLoc = 0
   endif

   exec 'call append(importLoc, importLine)'

   " go back to the old location
   close

   " save to reset quickfixlist
   exec 'w'
endfun

function! JavaImpGotoLast()
    " First search for the className in an import statement
    normal G$
    let flags = "w"
    let pattern = '^\s*import\s\s*.*;'
    let importFound = 0
    while search(pattern, flags) > 0
        let importFound = 1
        let flags = "W"
    endwhile
    return importFound
endfun

" Go to the package declaration
function! JavaImpGotoPackage()
    " First search for the className in an import statement
    normal G$
    let flags = "w"
    let pattern = '^\s*package\s\s*.*;'
    if (search(pattern, flags) == 0)
        return 0
    else
        return 1
    endif
endfun

augroup autocompile
   autocmd BufWritePost,FileWritePost *.java call ExecuteCompile()
augroup END 

