; Reprap Host Installer script created by the HM NIS Edit Script Wizard (then hand-modified by hand)

; Date: 2007-12-31

; Authors: Bruce Wattendorf and Jonathan Marsden

; HM NIS Edit Wizard helper defines
!define PRODUCT_NAME "Reprap Host"
!define PRODUCT_VERSION "0.8.4-20080125"
!define PRODUCT_PUBLISHER "RepRap Research Foundation (RRRF)"
!define PRODUCT_WEB_SITE "http://www.reprap.org"
!define PRODUCT_UNINST_KEY "Software\Microsoft\Windows\CurrentVersion\Uninstall\${PRODUCT_NAME}"
!define PRODUCT_UNINST_ROOT_KEY "HKLM"

; File locations on build host
!define HOST_DIR ".."
!define RXTX_DIR "rxtx"

; MUI 1.67 compatible ------
!include "MUI.nsh"

; MUI Settings
!define MUI_ABORTWARNING
!define MUI_ICON "${NSISDIR}\Contrib\Graphics\Icons\modern-install.ico"
!define MUI_UNICON "${NSISDIR}\Contrib\Graphics\Icons\modern-uninstall.ico"

Name "${PRODUCT_NAME} ${PRODUCT_VERSION}"
OutFile "reprap-installer-${PRODUCT_VERSION}.exe"
InstallDir "$PROGRAMFILES\Reprap"
ShowInstDetails show
ShowUnInstDetails show

; Welcome page
!insertmacro MUI_PAGE_WELCOME
; License page
!insertmacro MUI_PAGE_LICENSE "${HOST_DIR}\LICENSE"
; Directory page
!insertmacro MUI_PAGE_DIRECTORY
; Instfiles page
!insertmacro MUI_PAGE_INSTFILES
; Finish page
!insertmacro MUI_PAGE_FINISH

; Uninstaller pages
!insertmacro MUI_UNPAGE_INSTFILES

; Language files
!insertmacro MUI_LANGUAGE "English"

; MUI end ------

Section "MainSection" SEC01
  SetOutPath "$INSTDIR"
  SetOverwrite ifnewer
  File "${HOST_DIR}\jar\reprap.jar"
  File "${HOST_DIR}\README"
  File "${HOST_DIR}\LICENSE"
  File "${HOST_DIR}\reprap-host.bat"
  File "${HOST_DIR}\lib\reprap-wv.stl"

  SetShellVarContext all
  CreateShortCut "$DESKTOP\${PRODUCT_NAME}.lnk" "$INSTDIR\reprap-host.bat"

; Uncomment reprap-stls.nsi here  if desired -- omitted during testing.  JM 20071231
; Note: Corresponding !include in uninstall section should match this one.
; !include "reprap-stls.nsi"

; Uncomment this if bundling firmware.  Omitted for initial testing.  JM 2007-12-31.
; Note: Corresponding !include in uninstall section should match this one.
; !include "reprap-firmware.nsi"

; Install RXTX Libraries
  SetOutPath "$PROGRAMFILES\Java\shared"
  File "${HOST_DIR}\lib\j3d-org-java3d-all.jar"
  File "${RXTX_DIR}\RXTXcomm.jar"
  SetOutPath "$SYSDIR"
  File "${RXTX_DIR}\rxtxParallel.dll"
  File "${RXTX_DIR}\rxtxSerial.dll"

; Check that Java is installed, install Java 6u3 if not.
  Call JVM

;  Call Java3D  ;; NO LONGER USED.

; Install Java3D libraries by hand into our chosen location
!include "reprap-java3d.nsi"

  SetAutoClose false
SectionEnd

Section -AdditionalIcons
  SetOutPath $INSTDIR
  SetShellVarContext all
  WriteIniStr "$INSTDIR\${PRODUCT_NAME}.url" "InternetShortcut" "URL" "${PRODUCT_WEB_SITE}"
  CreateDirectory "$SMPROGRAMS\Reprap"
  CreateShortCut "$SMPROGRAMS\Reprap\Reprap-Host.lnk" "$INSTDIR\reprap-host.bat"
  CreateShortCut "$SMPROGRAMS\Reprap\Website.lnk" "$INSTDIR\${PRODUCT_NAME}.url"
  CreateShortCut "$SMPROGRAMS\Reprap\Uninstall.lnk" "$INSTDIR\uninst.exe"
SectionEnd

Section -Post
  WriteUninstaller "$INSTDIR\uninst.exe"
  WriteRegStr ${PRODUCT_UNINST_ROOT_KEY} "${PRODUCT_UNINST_KEY}" "DisplayName" "$(^Name)"
  WriteRegStr ${PRODUCT_UNINST_ROOT_KEY} "${PRODUCT_UNINST_KEY}" "UninstallString" "$INSTDIR\uninst.exe"
  WriteRegStr ${PRODUCT_UNINST_ROOT_KEY} "${PRODUCT_UNINST_KEY}" "DisplayVersion" "${PRODUCT_VERSION}"
  WriteRegStr ${PRODUCT_UNINST_ROOT_KEY} "${PRODUCT_UNINST_KEY}" "URLInfoAbout" "${PRODUCT_WEB_SITE}"
  WriteRegStr ${PRODUCT_UNINST_ROOT_KEY} "${PRODUCT_UNINST_KEY}" "Publisher" "${PRODUCT_PUBLISHER}"
SectionEnd

; This is the install function for Java 6 Update 3 jre
; Note that Reprap Host will run with any Sun Java JRE >= 5 -- can we test for that?  ** FIXME **
Function JVM
  SetOutPath "$PROGRAMFILES\Temp"
  IfFileExists $PROGRAMFILES\Java\jre1.6.0_03\README.txt endJVM
    MessageBox MB_OK "Your system does not appear to have Java 6 Update 3 JRE installed.$\n$\nPress Ok to install it."
    File "jre-6u3-windows-i586-p-s.exe"
    ExecWait "$INSTDIR\Temp\jre-6u3-windows-i586-p-s.exe"
  endJVM:
FunctionEnd

;; ; Java3D Library installation
;; Function Java3D
;;   SetOutPath "$PROGRAMFILES\Temp"
;;   IfFileExists $PROGRAMFILES\Java\Java3D\1.5.1\README.html endJava3D
;;     MessageBox MB_OK "Your system does not appear to have Java 3D installed.$\n$\nPress Ok to install it."
;;     File "java3d-1_5_1-windows-i586.exe"
;;     ExecWait "$INSTDIR\Temp\java3d-1_5_1-windows-i586.exe"
;;   endJava3D:
;; FunctionEnd

Function un.onUninstSuccess
  HideWindow
  MessageBox MB_ICONINFORMATION|MB_OK "$(^Name) was successfully removed from your computer."
FunctionEnd

Function un.onInit
  MessageBox MB_ICONQUESTION|MB_YESNO|MB_DEFBUTTON2 "Are you sure you want to completely remove $(^Name) and all of its components?" IDYES +2
  Abort
FunctionEnd

Section Uninstall
  Delete "$INSTDIR\${PRODUCT_NAME}.url"
  Delete "$INSTDIR\uninst.exe"
  Delete "$INSTDIR\reprap-wv.stl"
  Delete "$INSTDIR\reprap-host.bat"
  Delete "$INSTDIR\LICENSE"
  Delete "$INSTDIR\README"
  Delete "$INSTDIR\Reprap.jar"

; Uncomment this if bundling STL files.  Omitted for initial testing.  JM 2007-12-31.
;!include "reprap-stls-uninstall.nsi"

; Uncomment this if bundling firmware.  Omitted for initial testing.  JM 2007-12-31.
;!include "reprap-firmware-uninstall.nsi"

!include "reprap-java3d-uninstall.nsi"

; Uninstall RXTX Libraries and j3d.org lib
  Delete "$PROGRAMFILES\Java\shared\j3d-org-java3d-all.jar"
  Delete "$PROGRAMFILES\Java\shared\RXTXcomm.jar"
  RMDir "$PROGRAMFILES\Java\shared"
  Delete "$SYSDIR\rxtxParallel.dll"
  Delete "$SYSDIR\rxtxSerial.dll"

  SetShellVarContext all
  Delete "$SMPROGRAMS\Reprap\Reprap-Host.lnk"
  Delete "$SMPROGRAMS\Reprap\Uninstall.lnk"
  Delete "$SMPROGRAMS\Reprap\Website.lnk"
  Delete "$DESKTOP\${PRODUCT_NAME}.lnk"

  RMDir "$SMPROGRAMS\Reprap"
  RMDir "$INSTDIR"

  DeleteRegKey ${PRODUCT_UNINST_ROOT_KEY} "${PRODUCT_UNINST_KEY}"
  SetAutoClose true
SectionEnd
