; reprap-java3d.nsi

; Java3D library Files for Reprap -- included by reprap-host.nsi

; Author: Jonathan Marsden

; Date: 2007-12-31

  SetOutPath "$PROGRAMFILES\Java\shared"
  SetOverwrite ifnewer
  File "java3d\j3dcore.jar"
  File "java3d\j3dutils.jar"
  File "java3d\vecmath.jar"
  File "java3d\LICENSE-Java3D-v1_5_1.txt"
  SetOutPath "$SYSDIR"
  File "java3d\j3dcore-d3d.dll"
  File "java3d\j3dcore-ogl-cg.dll"
  File "java3d\j3dcore-ogl-chk.dll"
  File "java3d\j3dcore-ogl.dll"

;
; End of reprap-java3d.nsi
