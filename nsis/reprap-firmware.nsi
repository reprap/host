; reprap-firmware.nsi
;
; Reprap firmware install -- included by reprap-host.nsi
;
; Authors: Bruce Wattendorf and Jonathan Marsden
;
; Date: 2008-01-25
;
  SetOutPath "$INSTDIR\firmware"
  SetOverwrite ifnewer
  File "firmware\extruder0.hex"
  File "firmware\extruder1.hex"
  File "firmware\stepmotorx.hex"
  File "firmware\stepmotory.hex"
  File "firmware\stepmotorz.hex"
;
; End of reprap-firmware.nsi
