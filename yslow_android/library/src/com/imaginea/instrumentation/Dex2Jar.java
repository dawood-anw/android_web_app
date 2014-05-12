/*******************************************************************************
 * Copyright 2014 Asha
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *       http://www.apache.org/licenses/LICENSE-2.0
 * Owner : Asha - initial API and implementation
 * Project Name : Lib_PerformanceMonitoring
 * FileName :dex2jar
 ******************************************************************************/
package com.imaginea.instrumentation;

import java.io.IOException;

/**
 * The Class dex2jar.
 */
public class Dex2Jar {

    /**
     * Convert dex2jar.
     * 
     * @param apkName
     *            the @param string1
     * @return true, if successful
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    public static boolean convertDex2jar(final String apkName)
            throws IOException {
        // System.out.println("convert dex 2 jar ");
        String[] cmd = null;
        switch (Utils.getOperatingSystemType()) {
            case Windows:
                cmd = new String[] {
                        Utils.getFullPath("dex2jar\\") + "d2j-dex2jar.bat",
                        apkName };
                break;
            case Linux:
            case MacOS:
                cmd = new String[] { "sh",
                        Utils.getFullPath("dex2jar/") + "./d2j-dex2jar.sh",
                        "-f", "-o", "profiling_dex2jar.jar", apkName };
                break;
            default:
                break;
        }
        return Utils.execProcessBuilder(cmd);

    }

    /**
     * Convertjar2dex.
     * 
     * @param jarName
     *            the @param string1
     * @return true, if successful
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    public static boolean convertjar2dex(final String jarName)
            throws IOException {
        // System.out.println("convert JAR 2 DEX ");
        String[] cmd = null;
        switch (Utils.getOperatingSystemType()) {
            case Windows:
                cmd = new String[] {
                        Utils.getFullPath("dex2jar\\") + "d2j-jar2dex.bat",
                        jarName };
                break;
            case Linux:
            case MacOS:
                cmd = new String[] { "sh",
                        Utils.getFullPath("dex2jar/") + "./d2j-jar2dex.sh",
                        "-f", "-o", "classes.dex", jarName };
                break;
            default:
                break;
        }
        return Utils.execProcessBuilder(cmd);
    }

}
