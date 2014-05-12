/*******************************************************************************
 * Copyright 2014 Asha
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *       http://www.apache.org/licenses/LICENSE-2.0
 * Owner : Asha - initial API and implementation
 * Project Name : Lib_PerformanceMonitoring
 * FileName :Instrumentation
 ******************************************************************************/
package com.imaginea.profiling;

import java.io.File;
import java.io.IOException;

import com.imaginea.instrumentation.Apktool;
import com.imaginea.instrumentation.Aspect;
import com.imaginea.instrumentation.JarSigner;
import com.imaginea.instrumentation.SignatureInfo;
import com.imaginea.instrumentation.Utils;
import com.imaginea.instrumentation.Dex2Jar;

/**
 * The Class Instrumentation.
 */
public class Instrumentation {

    /** The instrumentation apk. */
    private final String INSTRUMENTATION_APK = "/Profiling.apk";
    private final String ASPECT_DEX2JAR = "/out.jar";
    private final String PROFILING_KEYSTORE = "/InstrumentationPackage/profiling.keystore";

    /**
     * Convert dex2jar.
     * 
     * @param outputDir
     *            the output directory
     */
    public void convertDex2jar(final String inputDir, final String apkname) {
        // Dex to jar converter
        try {
            Dex2Jar.convertDex2jar(inputDir + "/" + apkname);
        } catch (final IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * insert code2jar.
     * 
     * @param outputDir
     *            the output directory
     */
    public boolean injectCode2jar(final String inptuDir, final String apkName, final String outputDir, final String PackageName) {
        boolean ret = false;
        try {
             ret = Aspect.injectCode2jar(inptuDir+ "/" + apkName, outputDir + "/classes.dex", PackageName);
        } catch (final IOException e) {
            e.printStackTrace();
        }
        return ret;
    }

    /**
     * Jar2 dex converter.
     * 
     * @param workingDirectory
     *            the working directory
     * @param outputDir
     *            the output directory
     */
    public void jar2DexConverter(final String workingDirectory,
            final File outputDir) {
        // convert Dex to jar
        try {
            Dex2Jar.convertjar2dex(workingDirectory + ASPECT_DEX2JAR);
            // Now copy generated dex file to Instrumentation Package
            Utils.move(workingDirectory + "/classes.dex",
                    outputDir.getAbsolutePath());
        } catch (final IOException e) {
            e.printStackTrace();
        }

    }

    /**
     * Repackage apk.
     * 
     * @param outputDir
     *            the output directory
     */
    public void repackageApk(final String outputDir) {
        /* Repackage the APK */
        try {
            Apktool.repackage(outputDir, outputDir + INSTRUMENTATION_APK);
        } catch (final IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Start instrumentation.
     * 
     * @param inputDir
     *            the input directory
     * @param outputDir
     *            the output directory
     * @param apkName
     *            the apk name
     */
    public boolean startInstrumentation(final File inputDir,
            final File outputDir, final String apkName, final String sdkPath,
            final String PackageName) {
        // Unpack the APK File
        unPackApk(inputDir.getAbsolutePath(), outputDir.getAbsolutePath(),
                apkName);
        // Dex to jar converter
        convertDex2jar(inputDir.getAbsolutePath(), apkName);

        // Apply aspectJ and enable fragment log
        if(!injectCode2jar(inputDir.getAbsolutePath(), apkName, outputDir.getAbsolutePath(), PackageName))
            return false;

        // Gets the WorkingDir
        final String workingDirectory = System.getProperty("user.dir");
        // JAR to Dex converter
        jar2DexConverter(workingDirectory, outputDir);

        /* Repackage the APK */
        repackageApk(outputDir.getAbsolutePath());

        /* Sign the APK file */
        final String keystoreLocation = workingDirectory + PROFILING_KEYSTORE;
        final SignatureInfo paramSignatureInfo = new SignatureInfo(
                keystoreLocation, "pramati123", "Imaginea", "pramati123");
        try {
            JarSigner.signUsingJDKSigner(outputDir.getAbsolutePath()
                    + INSTRUMENTATION_APK, paramSignatureInfo, sdkPath);
        } catch (final IOException e) {
            e.printStackTrace();
        }

        return true;

    }

    /**
     * Unpack apk.
     * 
     * @param inputDir
     *            the input directory
     * @param outputDir
     *            the output directory
     * @param apkName
     *            the apk name
     */
    public void unPackApk(final String inputDir, final String outputDir,
            final String apkName) {
        // Unpack the APK File
        try {
            Apktool.unpackApk(inputDir + "/" + apkName, outputDir);
        } catch (final IOException e) {
            e.printStackTrace();
        }
    }
}
