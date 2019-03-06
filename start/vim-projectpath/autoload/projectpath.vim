if exists('g:autoloaded_projectpath')
   finish
endif
let g:autoloaded_projectpath=1

if !exists('g:projectpath_vcs_markers')
   let g:projectpath_vcs_markers = [ ['Subversion', '.svn' ], ['Git', '.git'], ['Mercurial', '.hg'] ]
endif

if !exists('g:projectpath_build_markers')
   let g:projectpath_build_markers = [ ['Ant', 'build.xml' ], ['Maven', 'pom.xml'], ['Sbt', 'build.sbt'], ['Gradle', 'build.gradle' ] ]
endif

function! projectpath#search(specs)
   let currentLookup='%:p:h'
   let currentPath = expand(currentLookup)
   let currentRoot = []
   let currentFirst = ''
   while len(currentPath) > 1
      for spec in a:specs
         if !empty(glob(currentPath.'/'.spec[1]))
            if len(currentFirst) == 0
               let currentFirst = currentPath
            endif
            let currentRoot = [ spec[0], currentPath ]
         endif
      endfor
      let currentLookup .= ':h'
      let currentPath = expand(currentLookup)
   endwhile
   if len(currentRoot) > 0
      return [ currentRoot[0], currentFirst, currentRoot[1] ]
   endif
   return []
endfunction

function! projectpath#searchForVCS()
   let searchResult = projectpath#search(g:projectpath_vcs_markers)
   if len(searchResult) > 0
      let b:ProjectVCS = searchResult[0]
      let b:ProjectVCSRoot = searchResult[1]
   endif
endfunction

function! projectpath#searchForBuild()
   let searchResult = projectpath#search(g:projectpath_build_markers)
   if len(searchResult) > 0
      let b:ProjectBuild = searchResult[0]
      let b:ProjectBuildCurrent = searchResult[1]
      let b:ProjectBuildRoot = searchResult[2]
   endif
endfunction

