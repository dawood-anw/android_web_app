/*******************************************************************************
 * Copyright 2014 Asha
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *       http://www.apache.org/licenses/LICENSE-2.0
 * Owner : Asha - initial API and implementation
 * Project Name : Lib_PerformanceMonitoring
 * FileName :Jarsigner
 ******************************************************************************/
package com.imaginea.instrumentation;

import java.io.IOException;

/**
 * The Class Jarsigner.
 */
public class JarSigner {

    /**
     * Sign using jdk signer.
     * 
     * @param apkPath
     *            the @param string
     * @param paramSignatureInfo
     *            the @param signature info
     * @return true, if successful
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    public static boolean signUsingJDKSigner(final String apkPath,
            final SignatureInfo paramSignatureInfo, final String sdkPath)
            throws IOException {
        final String instrumentationDir = Utils.getInstrumentationPath();
        final String keyStorePath = instrumentationDir + "profiling.keystore";
        ProcessBuilder localProcessBuilder = new ProcessBuilder(new String[] {
                "jarsigner", "-sigalg", "MD5withRSA", "-digestalg", "SHA1",
                "-keystore", keyStorePath, "-storepass", "pramati123", apkPath,
                "Imaginea" });
        new ProcessWrapper(localProcessBuilder).run();
        String keystoreLocation = null;
        String keystorePassword = null;
        String keyPassword = null;
        String keyAlias = null;
        if (paramSignatureInfo != null) {
            keystoreLocation = paramSignatureInfo.keystoreLocation;
            keystorePassword = paramSignatureInfo.keystorePassword;
            keyPassword = paramSignatureInfo.keyPassword;
            keyAlias = paramSignatureInfo.keyAlias;
            if (keystoreLocation == null || keystoreLocation.trim().isEmpty()) {
                System.out
                        .println("JarSigner User enabled private key, but specified empty path for key store");
                return false;
            }
        } else {
            System.out.println("JarSigner signature info is empty ");
            return false;
        }
        if (keyPassword == null) {
            localProcessBuilder = new ProcessBuilder(new String[] {
                    "jarsigner", "-sigalg", "MD5withRSA", "-digestalg", "SHA1",
                    "-keystore", keystoreLocation, "-storepass",
                    keystorePassword, apkPath, keyAlias });
        } else {
            localProcessBuilder = new ProcessBuilder(new String[] {
                    "jarsigner", "-sigalg", "MD5withRSA", "-digestalg", "SHA1",
                    "-keystore", keystoreLocation, "-storepass",
                    keystorePassword, "-keypass", keyPassword, apkPath,
                    keyAlias });
        }

        final ProcessWrapper localProcessWrapper = new ProcessWrapper(
                localProcessBuilder);
        localProcessWrapper.run();

        // System.out.println("JarSigner Signed the APK. Now trying to align");
        final String srcDir = zipAlign(apkPath, sdkPath);
        Utils.move(srcDir, apkPath);
        return true;
    }

    /**
     * Zip align.
     * 
     * @param apkPath
     *            the param string
     * @return the string
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    private static String zipAlign(final String apkPath, final String sdkPath)
            throws IOException {
        final String srcDir = apkPath.substring(0, apkPath.lastIndexOf('.'))
                + "-aligned" + ".apk";
        String sdkDir = sdkPath;
        if (!sdkDir.endsWith("/") && !sdkDir.endsWith("\\")) {
            sdkDir = sdkDir + "/";
        }
        final String zipAlignDirPath = sdkPath + "/tools/zipalign";

        final ProcessBuilder localProcessBuilder = new ProcessBuilder(
                new String[] { zipAlignDirPath, "-v", "-f", "4", apkPath,
                        srcDir });
        final ProcessWrapper localProcessWrapper = new ProcessWrapper(
                localProcessBuilder);
        localProcessWrapper.run();
        // System.out.println("JarSigner Zip aligned the APK");
        return srcDir;
    }
}
