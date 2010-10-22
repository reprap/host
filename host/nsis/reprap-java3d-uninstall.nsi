; reprap-java3d-uninstall.nsi
;
; Reprap Java3D library uninstall -- included by reprap-host.nsi
;
; Authors: Jonathan Marsden
;
; Date: 2007-12-31
;
  Delete "$PROGRAMFILES\Java\shared\j3dcore.jar"
  Delete "$PROGRAMFILES\Java\shared\j3dutils.jar"
  Delete "$PROGRAMFILES\Java\shared\vecmath.jar"
  Delete "$PROGRAMFILES\Java\shared\LICENSE-Java3D-v1_5_1.txt"
  Delete "$SYSDIR\j3dcore-d3d.dll"
  Delete "$SYSDIR\j3dcore-ogl-cg.dll"
  Delete "$SYSDIR\j3dcore-ogl-chk.dll"
  Delete "$SYSDIR\j3dcore-ogl.dll"
;
;;;  RMDir "$PROGRAMFILES\Java\shared"
; End of reprap-java3d-uninstall.nsi
