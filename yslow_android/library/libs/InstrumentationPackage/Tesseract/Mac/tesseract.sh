#!/bin/sh

#	creating symlinks for leptonica dynamic libs

# tesseract

if [ ! -L /usr/local/lib/libtesseract.3.dylib ]; then
   ln -sf `pwd`/InstrumentationPackage/Tesseract/Mac/libtesseract.dylib /usr/local/lib/libtesseract.dylib
    
      # copying eng.traineddata to required path
	if [ ! -f /usr/local/Cellar/tesseract/3.02.02/share/tessdata/eng.traineddata ];then
   		mkdir -p /usr/local/Cellar/tesseract/3.02.02/share/tessdata/
   		cp `pwd`/InstrumentationPackage/Tesseract/tessdata/eng.traineddata /usr/local/Cellar/tesseract/3.02.02/share/tessdata/
	fi
fi

if [ ! -L /usr/local/lib/liblept.3.dylib ]; then
   ln -sf `pwd`/InstrumentationPackage/Tesseract/Mac/liblept.3.dylib /usr/local/lib/liblept.3.dylib
fi

#libpng

if [ ! -L /usr/local/lib/libpng15.15.dylib ]; then
   ln -sf `pwd`/InstrumentationPackage/Tesseract/Mac/libpng15.15.dylib /usr/local/lib/libpng15.15.dylib
fi

#libtiff
if [ ! -L /usr/local/lib/libtiff.5.dylib ]; then
   ln -sf `pwd`/InstrumentationPackage/Tesseract/Mac/libtiff.5.dylib /usr/local/lib/libtiff.5.dylib     	
fi    

#libjpeg
if [ ! -L /usr/local/lib/libjpeg.8.dylib ]; then
   ln -sf `pwd`/InstrumentationPackage/Tesseract/Mac/libjpeg.8.dylib /usr/local/lib/libjpeg.8.dylib
fi      
