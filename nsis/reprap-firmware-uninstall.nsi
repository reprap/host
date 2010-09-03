; reprap-firmware-uninstall.nsi
;
; Reprap firmware uninstall -- included by reprap-host.nsi
;
; Authors: Bruce Wattendorf and Jonathan Marsden
;
; Date: 2008-01-25
;
  Delete "$INSTDIR\firmware\extruder0.hex"
  Delete "$INSTDIR\firmware\extruder1.hex"
  Delete "$INSTDIR\firmware\stepmotorx.hex"
  Delete "$INSTDIR\firmware\stepmotory.hex"
  Delete "$INSTDIR\firmware\stepmotorz.hex"
;
  RMDir "$INSTDIR\firmware"
;
; End of reprap-firmware-uninstall.nsi
