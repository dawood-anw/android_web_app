#!/bin/sh

#	check if symlinks are created and delete


LINK=$(readlink "/usr/local/lib/liblept.so.3.0.0")
VAR_PATH="/InstrumentationPackage/Tesseract/Linux/liblept.so.3.0.0"
ABS_PATH=`pwd`$VAR_PATH

if [ "$LINK" == "$ABS_PATH" ]; then
	rm /usr/local/lib/liblept.a
	rm /usr/local/lib/liblept.la
	rm /usr/local/lib/liblept.so.3.0.0
fi; 

LINK=$(readlink "/usr/local/lib/libtesseract.so.3.0.3")
VAR_PATH="/InstrumentationPackage/Tesseract/Linux/libtesseract.so.3.0.3"
ABS_PATH=`pwd`$VAR_PATH

if [ "$LINK" == "$ABS_PATH" ]; then
	rm /usr/local/lib/libtesseract.so.3.0.3
	rm /usr/local/lib/libtesseract.la
	rm /usr/local/lib/libtesseract.a
	
	rm -rf /usr/local/share/tessData/
fi;  

#libpng
LINK=$(readlink "/usr/local/lib/libpng.a")
VAR_PATH="/InstrumentationPackage/Tesseract/Linux/libpng/libpng.a"
ABS_PATH=`pwd`$VAR_PATH

if [ "$LINK" == "$ABS_PATH" ]; then
	rm /usr/local/lib/libpng.a
	rm /usr/local/lib/libpng.so
	rm /usr/local/lib/libpng12.a
	rm /usr/local/lib/libpng12.so
fi;  

#libtiff
LINK=$(readlink "/usr/local/lib/libtiff.a")
VAR_PATH="/InstrumentationPackage/Tesseract/Linux/libtiff/libtiff.a"
ABS_PATH=`pwd`$VAR_PATH

if [ "$LINK" == "$ABS_PATH" ]; then
	rm /usr/local/lib/libtiff.a
	rm /usr/local/lib/libtiff.la
	rm /usr/local/lib/libtiff.so
	rm /usr/local/lib/libtiffxx.a
	rm /usr/local/lib/libtiffxx.la
	rm /usr/local/lib/libtiffxx.so
fi; 

#jpeg
LINK=$(readlink "/usr/local/lib/libjpeg.8.dylib")
VAR_PATH="/InstrumentationPackage/Tesseract/Linux/jpeg/libjpeg.a"
ABS_PATH=`pwd`$VAR_PATH

if [ "$LINK" == "$ABS_PATH" ]; then
	rm /usr/local/lib/libjpeg.a
	rm /usr/local/lib/libjpeg.so
	rm /usr/local/lib/libjpeg.so.8
fi; 
