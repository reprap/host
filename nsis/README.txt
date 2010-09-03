
Reprap Host Software README

Date: 03 November 2008
Author: Jonathan Marsden

Version: 20081103

This is an attempt at a quick package of the Reprap host software.  

It should run on Linux, Windows, and Macintosh.  

CHANGES:

Several bugs have been fixed since the last release, though plenty
remain, as usual!  

Also, the software is now run so it can find the libraries it needs at
a fixed location (/usr/share/java in Linux, %programfiles%\Java\shared
in Windows).  This change is made in preparation for more automated
installation, also means the zip file needs to be unpacked and then
its contents moved to a fixed location:

  /usr/lib/reprap/        # For Linux

or

  C:\Program Files\Reprap # For Windows  

So that (for example) you will have a file /usr/lib/reprap/reprap.jar
or C:\Program Files\Reprap\Reprap.jar

If you need to install it somewhere else, edit the reprap-host or
reprap-host.bat file appropriately.

DOCUMENTATION:

See http://www.reprap.org for project details.

See http://www.reprap.org/bin/view/Main/DriverSoftware for the closest
thing we have to a user manual for this host software at this stage.

Please use http://forums.reprap.org to ask about this package.  The
Software forum is probably the right place to ask.

NOTES:

(1) You need a working Java 1.5.0 or later JRE to run this.
(2) You need to manually install the Java3D libraries.
(3) You need to manually install the rxtx libraries ( http://www.rxtx.org )
(4) You need to manually install the .jar file from j3d.org, which
    you can download from

    http://reprap.svn.sourceforge.net/viewvc/*checkout*/reprap/trunk/reprap/host/lib/j3d-org-java3d-all.jar

    until we have a better idea of where this can be obtained!    

KNOWN BUGS:

(1) This software still uses too much memory, but is a lot better than
    it used to be.

(2) Installing the libraries by hand is a bit awkward.

(3) Probably many other bugs, less serious than (1) above!  See
    http://sourceforge.net/tracker/?group_id=159590 for more.

(4) This package isn't a nice friendly installer.  One is planned...!
