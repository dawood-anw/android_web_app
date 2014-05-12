/*******************************************************************************
 * Copyright 2014 Asha
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *       http://www.apache.org/licenses/LICENSE-2.0
 * Owner : Asha - initial API and implementation
 * Project Name : Lib_PerformanceMonitoring
 * FileName :Apktool
 ******************************************************************************/
package com.imaginea.instrumentation;

import java.io.File;
import java.io.IOException;

/**
 * The Class Apktool.
 */
public class Apktool {

    /**
     * Gets the apktool path.
     * 
     * @return the apktool path
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    private static String getApktoolPath() throws IOException {
        final String str = Utils.getFullPath("apktool.jar");
        return new File(str).getAbsolutePath();
    }

    /**
     * Repackage.
     * 
     * @param srcPath
     *            the @param string1
     * @param destPath
     *            the @param string2
     * @return true, if successful
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    public static boolean repackage(final String srcPath, final String destPath)
            throws IOException {
        // System.out.println("repackage the app ");
        final String str = Utils.getAaptPath();
        return Utils.execProcessBuilder(new String[] { Utils.getJavaPath(),
                "-jar", getApktoolPath(), "b", "-f", "-a", str, srcPath,
                destPath });

    }

    /**
     * Unpack apk.
     * 
     * @param srcPath
     *            the @param string1
     * @param destPath
     *            the @param string2
     * @return true, if successful
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    public static boolean unpackApk(final String srcPath, final String destPath)
            throws IOException {
        // System.out.println("Unpacking the app ");
        return Utils.execProcessBuilder(new String[] { Utils.getJavaPath(),
                "-jar", getApktoolPath(), "d", "-f", "-s", "-r", srcPath,
                destPath });

    }
}
