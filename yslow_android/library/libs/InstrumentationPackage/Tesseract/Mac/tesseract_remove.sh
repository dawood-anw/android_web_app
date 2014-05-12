#!/bin/sh

#	check if symlinks are created and delete


#tesseract
LINK=$(readlink "/usr/local/lib/libtesseract.3.dylib")
VAR_PATH="/InstrumentationPackage/Tesseract/Mac/libtesseract.3.dylib"
ABS_PATH=`pwd`$VAR_PATH

if [ "$LINK" == "$ABS_PATH" ]; then
	rm /usr/local/lib/libtesseract.dylib	
	rm -rf /usr/local/Cellar/tesseract/
fi;


#leptonica
LINK=$(readlink "/usr/local/lib/liblept.3.dylib")
VAR_PATH="/InstrumentationPackage/Tesseract/Mac/liblept.3.dylib"
ABS_PATH=`pwd`$VAR_PATH

if [ "$LINK" == "$ABS_PATH" ]; then
	rm /usr/local/lib/liblept.3.dylib
fi; 


#libpng
LINK=$(readlink "/usr/local/lib/libpng15.15.dylib")
VAR_PATH="/InstrumentationPackage/Tesseract/Mac/libpng15.15.dylib"
ABS_PATH=`pwd`$VAR_PATH

if [ "$LINK" == "$ABS_PATH" ]; then
	rm /usr/local/lib/libpng15.15.dylib
fi;  

#libtiff
LINK=$(readlink "/usr/local/lib/libtiff.5.dylib")
VAR_PATH="/InstrumentationPackage/Tesseract/Mac/libtiff.5.dylib"
ABS_PATH=`pwd`$VAR_PATH

if [ "$LINK" == "$ABS_PATH" ]; then
	rm /usr/local/lib/libtiff.5.dylib
fi; 

#jpeg
LINK=$(readlink "/usr/local/lib/libjpeg.8.dylib")
VAR_PATH="/InstrumentationPackage/Tesseract/Mac/libjpeg.8.dylib"
ABS_PATH=`pwd`$VAR_PATH

if [ "$LINK" == "$ABS_PATH" ]; then
	rm /usr/local/lib/libjpeg.8.dylib
fi; 
