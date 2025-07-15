This directory contains full Quarkdown snippets to be tested *manually*, about CSS-related fixed issues and edge-cases.

1. `cd` to this directory (`cd quarkdown-html/src/test/resources/issues`)
2. Compile sources, for example to PDF:
   - Parallel:
     `ls *.qd | xargs -P 4 -I {} quarkdown c "{}" --pdf`
   - Sequential (slower, use this in case of problems with the parallel one):  
     `for f in *.qd; do quarkdown c "$f" --pdf; done`