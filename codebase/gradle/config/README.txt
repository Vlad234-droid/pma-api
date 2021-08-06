This folder contains config files for codestyle plugins.

1. Add PMD
    a) Install PMD plugin to IDE (for example pmd (Eclipse) or PMDPlugin (Idea))
    b) Go to plugin configuration and set up new rule set to pmd.xml
       If you use PMDPlugin and have no results try to change plugin option to java of other version
    c) Run PMD check for whatever files you need. Notice that no need to run PMD for test files.
       If you use PMDPlugin select folder to check and select from popup menu run pmd custom rule set

2. Add Checkstyle
    a) Install Checkstyle plugin to IDE (for example Eclipse-CS or CheckStyle-IDEA)
    b) Go to plugin configuration and set up new rule set to checkstyle.xml.
       Also rule set property is required "checkstyle.dir" with full path to this folder.
       Alternatively you can replace this property directly in checkstyle.xml 
    c) Run Checkstyle check whenever you need. Notice that test files are ignored by suppression.xml.
       If you use CheckStyle-IDEA test files can be ignored via plugin config.

1/2. If IDE is Idea codestyle plugins can be replaced with QAPlug plugin
    a) Install QAPlug, QAPlug-Checkstyle and QAPlug-PMD plugins
    b) Go to plugin configuration and create new profile; clear all checks and import configurations from checkstyle.xml and pmd.xml
    c) Go to plugin checkstyle configuration:
        1) Set suppression filter to suppression.xml
        2) Enable suppression comment filter
        3) Enable suppression with nearby comment filter and set comment format to CHECKSTYLE\:IGNORE LINE
    d) Select folder and choose [analyze > analyze code] to run check

3. Formatter recommendations
    a) Set do not use tab character and count 4 spaces for one tab
    a) Set line length to 140 (used for wrapping)
    c) Set do not use * in imports (can be achieved via setting big number to use *)
    d) Set imports order which can be found in checkstyle.xml
    In most other cases default settings are acceptable

4. Suppression recommendations
    pmd: use comment //NOPMD at the end of line which cause error
    checkstyle: use comments //CHECKSTYLE:ON and //CHECKSTYLE:OFF or //CHECKSTYLE:IGNORE LINE