#!/bin/sh

#	creating symlinks for leptonica and teeseract dynamic libs

if [ ! -L /usr/local/lib/liblept.a ]; then
   ln -sf `pwd`/InstrumentationPackage/Tesseract/Linux/liblept.a /usr/local/lib/liblept.a
   ln -sf `pwd`/InstrumentationPackage/Tesseract/Linux/liblept.la /usr/local/lib/liblept.la
   ln -sf `pwd`/InstrumentationPackage/Tesseract/Linux/leptonica/liblept.so.3.0.0 /usr/local/lib/liblept.so.3.0.0
fi

if [ ! -L /usr/local/lib/libtesseract.a ]; then
   ln -sf `pwd`/InstrumentationPackage/Tesseract/Linux/libtesseract.a /usr/local/lib/libtesseract.a
   ln -sf `pwd`/InstrumentationPackage/Tesseract/Linux/libtesseract.la /usr/local/lib/libtesseract.la
   ln -sf `pwd`/InstrumentationPackage/Tesseract/Linux/libtesseract.so.3.0.0 /usr/local/lib/libtesseract.so.3.0.0
    
      # copying eng.traineddata to required path
	if [ ! -f /usr/local/share/tessdata/eng.traineddata ];then
   		mkdir -p /usr/local/share/tessdata/
   		cp `pwd`/InstrumentationPackage/tessdata/eng.traineddata /usr/local/share/tessdata/
	fi
fi   

#libpng

if [ ! -L /usr/local/lib/libpng.a ]; then
   ln -sf `pwd`/InstrumentationPackage/Tesseract/Linux/libpng/libpng.a /usr/local/lib/libpng.a
   ln -sf `pwd`/InstrumentationPackage/Tesseract/Linux/libpng/libpng12.a /usr/local/lib/libpng12.a
   ln -sf `pwd`/InstrumentationPackage/Tesseract/Linux/libpng/libpng.so /usr/local/lib/libpng.so
   ln -sf `pwd`/InstrumentationPackage/Tesseract/Linux/libpng/libpng12.so /usr/local/lib/libpng12.so
fi

#libtiff
if [ ! -L /usr/local/lib/libtiff.a ]; then
   ln -sf `pwd`/InstrumentationPackage/Tesseract/Linux/libtiff/libtiff.a /usr/local/lib/libtiff.a
   ln -sf `pwd`/InstrumentationPackage/Tesseract/Linux/libtiff/libtiff.la /usr/local/lib/libtiff.la
   ln -sf `pwd`/InstrumentationPackage/Tesseract/Linux/libtiff/libtiff.so /usr/local/lib/libtiff.so
   ln -sf `pwd`/InstrumentationPackage/Tesseract/Linux/libtiff/libtiffxx.a /usr/local/lib/libtiffxx.a
   ln -sf `pwd`/InstrumentationPackage/Tesseract/Linux/libtiff/libtiffxx.la /usr/local/lib/libtiffxx.la
   ln -sf `pwd`/InstrumentationPackage/Tesseract/Linux/libtiff/libtiffxx.so /usr/local/lib/libtiffxx.so    	
fi    

#libjpeg
if [ ! -L /usr/local/lib/libjpeg.a ]; then
   ln -sf `pwd`/InstrumentationPackage/Tesseract/Linux/jpeg/libjpeg.a /usr/local/lib/libjpeg.a
   ln -sf `pwd`/InstrumentationPackage/Tesseract/Linux/jpeg/libjpeg.so /usr/local/lib/libjpeg.so
   ln -sf `pwd`/InstrumentationPackage/Tesseract/Linux/jpeg/libjpeg.so.8 /usr/local/lib/libjpeg.so.8
fi         
