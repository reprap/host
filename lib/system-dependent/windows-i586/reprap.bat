rem reprap-host -- runs Reprap Java host code with an appropriate classpath

rem Amount of RAM to allow Java VM to use
set REPRAP_RAM_SIZE=1024M

rem reprap.jar file and stl file
set REPRAP_DIR=%ProgramFiles%\Reprap

rem Java3D and j3d.org libraries
rem set JAVA_LIBRARY_DIR=%ProgramFiles%\Reprap


rem cd so we can find the reprap-wv.stl file.  Can we avoid this??
IF NOT EXIST reprap-wv.stl cd "%REPRAP_DIR%"
java -cp ".\reprap.jar;.\RXTXcomm.jar;.\j3dcore.jar;.\j3d-org-java3d-all.jar;.\j3dutils.jar;.\swing-layout-1.0.3.jar;.\vecmath.jar;." -Xmx%REPRAP_RAM_SIZE% org/reprap/Main
if ERRORLEVEL 1 pause

