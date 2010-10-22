#!/bin/bash
#
# setup-reprap-host-dev-environment.sh -- installs RepRap host software development stuff
#
# Copyright (C) 2007 Jonathan Marsden
#
# This program is free software; you can redistribute it and/or
# modify it under the terms of the GNU General Public Licence
# as published by the Free Software Foundation, version 2.
#

# Main variables 
REPRAPDIR=${1:-~/projects/reprap}	# Subdir under which to install everything
DOWNLOADDIR=${REPRAPDIR}/download	# Edit  if you downloaded Java3D/RXTX libs elsewhere
WGET="wget -c"				# Used to get libs if you don't seem to have them
UNZIP="unzip -q"			# Unzip libs quietly

# Subversion variables
SVNURL="https://reprap.svn.sourceforge.net/svnroot/reprap/trunk/reprap"
SVN=svn				# svn command name -- should not need to be changed
SVNREVISION=""			# Set this to "-r 600" to check out revision 600, for example

# Java3D lib variables
JAVA3DURL="http://download.java.net/media/java3d/builds/release/1.5.0/java3d-1_5_0-linux-i586.bin"
JAVA3DFILE=${JAVA3DURL##*/}	# Just the filename

# RXTX Comms lib variables
RXTXURL="ftp://ftp.qbang.org/pub/rxtx/rxtx-2.1-7-bins-r2.zip"
RXTXFILE=${RXTXURL##*/}		# Just the filename

##### DO NOT CHANGE ANYTHING BELOW HERE WITHOUT GOOD CAUSE #####

# Check we have wget
WGETPATH=`echo "$WGET" |cut -d " " -f1`
if [ ! -x "`which $WGETPATH`" ] 
then
    echo "$0: ERROR: No $WGETPATH program found"
    [ -x "`which dpkg`" ] && grep -sq ubuntu-standard <(dpkg -l) && \
	echo -e "Try\n\n  sudo apt-get install wget\n\nto fix this."
    exit 1
fi

# Check we have unzip
UNZIPPATH=`echo "$UNZIP" |cut -d " " -f1`
if [ ! -x "`which $UNZIPPATH`" ] 
then
    echo "$0: ERROR: No $UNZIPPATH program found"
    [ -x "`which dpkg`" ] && grep -sq ubuntu-standard <(dpkg -l) && \
	echo -e "Try\n\n  sudo apt-get install unzip\n\nto fix this."
    exit 1
fi

# Check we have svn
SVNPATH=`echo "$SVN" |cut -d " " -f1`
if [ ! -x "`which $SVNPATH`" ] 
then
    echo "$0: ERROR: No $SVNPATH program found"
    [ -x "`which dpkg`" ] && grep -sq ubuntu-standard <(dpkg -l) && \
	echo -e "Try\n\n  sudo apt-get install subversion\n\nto fix this."
    exit 1
fi

# Check we have ant
if [ ! -x "`which ant`" ] 
then
    echo "$0: ERROR: No ant program found in your PATH"
    [ -x "`which dpkg`" ] && grep -sq ubuntu-standard <(dpkg -l) && \
	echo -e "Try\n\n  sudo apt-get install ant\n\nto fix this."
    exit 1
fi


# Check our Java environment a little
if [ ! -x "`which java`" ] 
then
    echo "$0: ERROR: No java interpreter found in your PATH"
    [ -x "`which dpkg`" ] && grep -sq ubuntu-standard <(dpkg -l) && \
	echo -e "Try\n\n  sudo apt-get install sun-java5-jdk\n\nto fix this."
    exit 1
fi

if [ ! -x "`which javac`" ] 
then
    echo "$0: ERROR: No javac compiler found in your PATH"
    [ -x "`which dpkg`" ] && grep -sq ubuntu-standard <(dpkg -l) && \
	echo -e "Try\n\n  sudo apt-get install sun-java5-jdk\n\nto fix this."
    exit 1
fi

# Convert to absolute paths
[ ${REPRAPDIR:0:1} = "/" ] || REPRAPDIR="`pwd`/$REPRAPDIR"
[ ${DOWNLOADDIR:0:1} = "/" ] || DOWNLOADDIR="`pwd`/$DOWNLOADDIR"

# Set up working areas
mkdir -p "$REPRAPDIR" "$DOWNLOADDIR"
cd "$REPRAPDIR"

# Check out the host software into a subdir called Reprap
$SVN co $SVNREVISION $SVNURL/host Reprap

# Determine Java JRE location for external libs

cat <<EOF >home.java
class home {
  public static void main(String[] args) {
    System.out.println(System.getProperty("java.home"));
  }
}
EOF

javac home.java
JREDIR=`java home`

JREDIR=${JREDIR%%:*} # Pick first dir in the returned list
JREDIR=${JREDIR%%/lib/ext}

if [ -d ${JREDIR}/lib/ext ]
then
  echo ""
  echo "Your java.home property seems to be:"
  echo $JREDIR
  echo ""
else
  echo "$0: Error: BAD JRE DIR: $JREDIR"
  exit 1
fi

# Install Java3D libs
[ -e $DOWNLOADDIR/$JAVA3DFILE ] || \
    ( cd $DOWNLOADDIR ; echo "Downloading Java3D" ; $WGET $JAVA3DURL )
[ -e $DOWNLOADDIR/$JAVA3DFILE ] || \
    echo "$0: Unable to download $JAVA3DFILE"

pushd "$JREDIR"
echo "$0  needs to use sudo to install Java 3D libraries, so"
echo "please enter your password if prompted for it"
sleep 2
sudo $UNZIP -o ${DOWNLOADDIR}/${JAVA3DFILE}
popd

# Install comms libs
pushd "$DOWNLOADDIR"
[ -e $DOWNLOADDIR/$RXTXFILE ] || ( cd $DOWNLOADDIR ; $WGET $RXTXURL )
[ -e $DOWNLOADDIR/$RXTXFILE ] || echo "$0: Unable to download $RXTXFILE"
RXTXVER=${RXTXFILE%%.zip}
$UNZIP -o -j $RXTXFILE $RXTXVER/RXTXcomm.jar || echo "$0: Failed to unzip RXTXcomm.jar"
$UNZIP -o -j $RXTXFILE $RXTXVER/Linux/i686-unknown-linux-gnu/librxtx*.so
sudo cp -vp RXTXcomm.jar $JREDIR/lib/ext/
sudo cp -vp librxtx*.so $JREDIR/lib/i386/
rm -rf ./$RXTXVER
popd

### Now do the strange fixups we hope to eliminate one day...

# FIXUP #1: Copy strange java3d STL loader file to where Java will find it
sudo cp -p Reprap/lib/j3d-org-java3d-all.jar $JREDIR/lib/ext

### Compile the Reprap host software into a .jar file
cd Reprap
ant clean jar && echo -e "\n\nReprap host software compiled successfully.\n\n"

### Final words of wisdom
echo "Please note: to run the Reprap host software, type:"
echo ""
echo "  cd $REPRAPDIR/Reprap"
echo "  ant run"
