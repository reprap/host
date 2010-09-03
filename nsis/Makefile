RELEASE=`date -u +%Y%m%d`
NOW=`date -u +'%d %B %Y'`

RXTXFILES=rxtx/RXTXcomm.jar rxtx/rxtxParallel.dll rxtx/rxtxSerial.dll
RXTXZIP=rxtx-2.1-7-bins-r2.zip
RXTXURL=ftp://ftp.qbang.org/pub/rxtx
RXTXSRCZIP=rxtx-2.1-7.zip

JAVA3DZIP=java3d-1_5_1-windows-i586.zip
JAVA3DURL=http://download.java.net/media/java3d/builds/release/1.5.1

JAVAURL=http://javadl.sun.com/webapps/download/AutoDL?BundleId=12798
JAVAFILE=jre-6u3-windows-i586-p-s.exe

.phony: clean all jar readme rxtx java3d firmware stls rxtxsource installer

all: jar readme rxtx java3d firmware rxtxsource ${JAVAFILE} installer

installer:
	makensis reprap-host.nsi

jar:
	(cd .. ; ant jar )

readme:
	sed -e "s/RELEASE/$(RELEASE)/g" -e "s/DATE/$(NOW)/g" ../README >README.txt

rxtx: rxtxsource
	( cd rxtx ; [ ! -f ${RXTXZIP} ] && wget ${RXTXURL}/${RXTXZIP} ; true )
	( cd rxtx ; unzip -jo ${RXTXZIP} */RXTXcomm.jar )
	( cd rxtx ; unzip -jo ${RXTXZIP} */*/*/rxtxParallel.dll )
	( cd rxtx ; unzip -jo ${RXTXZIP} */*/*/rxtxSerial.dll )

rxtxsource:
	# working subdirectory
	mkdir -p rxtx
	( cd rxtx ; [ ! -f ${RXTXSRCZIP} ] && wget ${RXTXURL}/${RXTXSRCZIP} ; true )

java3d:
	mkdir -p java3d
	( cd java3d ; [ ! -f ${JAVA3DZIP} ] && wget ${JAVA3DURL}/${JAVA3DZIP} ; true )
	( cd java3d ; unzip -jo ${JAVA3DZIP} )
	( cd java3d ; unzip -jo j3d-jre.zip )

${JAVAFILE}:
	wget -O ${JAVAFILE} ${JAVAURL}

firmware:
	mkdir -p firmware
	( cd ../../firmware ; make )
	cp -p ../../firmware/devices/*/*.hex firmware/

clean:
	-[ -d rxtx ] && rm -rf rxtx
	-[ -d java3d ] && rm -rf java3d
	-[ -d firmware ] && rm -rf firmware
	rm README.txt ${JAVAFILE}
