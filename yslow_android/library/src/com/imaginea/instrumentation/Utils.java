/*******************************************************************************
 * Copyright 2014 Asha
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *       http://www.apache.org/licenses/LICENSE-2.0
 * Owner : Asha - initial API and implementation
 * Project Name : Lib_PerformanceMonitoring
 * FileName :Utils
 ******************************************************************************/
package com.imaginea.instrumentation;

import java.io.File;
import java.io.IOException;

/**
 * The Class Utils.
 */
public class Utils {

    /** The Constant INSTRUMENTATION_LIB_PATH. */
    public final static String INSTRUMENTATION_LIB_PATH = File.separator
            + "library" + File.separator + "libs" + File.separator
            + "InstrumentationPackage";

    /** The device density. */
    private static DeviceDensity sDeviceDensity;

    /**
     * Gets the device density.
     * 
     * @return the device density
     */
    public static DeviceDensity getDeviceDensity() {
        return sDeviceDensity;
    }

    /**
     * Sets the device density.
     * 
     * @param deviceDensity
     *            the new device density
     */
    public static void setDeviceDensity(final DeviceDensity deviceDensity) {
        sDeviceDensity = deviceDensity;
    }

    /**
     * The Enum OSType.
     */
    public enum OSType {

        /** The Windows. */
        Windows,
        /** The Mac OS. */
        MacOS,
        /** The Linux. */
        Linux,
        /** The Other. */
        Other
    }

    /**
     * The Enum DeviceDensity.
     */
    public enum DeviceDensity {

        /** The ldpi. */
        LDPI,

        /** The mdpi. */
        MDPI,

        /** The hdpi. */
        HDPI,

    }

    /**
     * Gets the operating system type.
     * 
     * @return the operating system type
     */
    public static OSType getOperatingSystemType() {
        OSType detectedOS = null;
        if (detectedOS == null) {
            final String OS = System.getProperty("os.name", "generic")
                    .toLowerCase();
            if (OS.indexOf("mac") >= 0 || OS.indexOf("darwin") >= 0) {
                detectedOS = OSType.MacOS;
            } else if (OS.indexOf("win") >= 0) {
                detectedOS = OSType.Windows;
            } else if (OS.indexOf("nux") >= 0) {
                detectedOS = OSType.Linux;
            } else {
                detectedOS = OSType.Other;
            }
        }
        return detectedOS;
    }

    /**
     * Setup tesseract.
     */
    public static void SetupTesseract(final String passWord) {
        String[] cmd = null;
        switch (Utils.getOperatingSystemType()) {
            case Windows:
                final String workingDirectory = System.getProperty("user.dir");
                cmd = new String[] { "cmd.exe", "/c", "copy",
                        Utils.getFullPath("Tesseract\\Windows\\win32-x86-64"),
                        workingDirectory + File.separator + "bin" };
                break;
            case Linux:
                cmd = new String[] { "sh",
                        Utils.getFullPath("Tesseract/Linux/tesseract.sh") };
                break;
            case MacOS:
                cmd = new String[] { "sh","echo",passWord, "|", "sudo", "-S",
                        Utils.getFullPath("Tesseract/Mac/tesseract.sh") };
                break;
            default:
                break;
        }

        boolean ret = Utils.execProcessBuilder(cmd);
        if(ret) {
            System.out.println("setup tesseract successful");
        } else {
            System.out.println("Failed to setup tesseract");
        }
    }

    /**
     * Move.
     * 
     * @param srcPath
     *            the src path
     * @param destPath
     *            the dest path
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    public static void move(final String srcPath, final String destPath)
            throws IOException {
        ProcessBuilder localProcessBuilder;
        final OSType os = Utils.getOperatingSystemType();
        if (os == OSType.Windows) {
            localProcessBuilder = new ProcessBuilder(new String[] { "cmd.exe",
                    "/c", "move", srcPath, destPath });
        } else {
            localProcessBuilder = new ProcessBuilder(new String[] { "mv",
                    srcPath, destPath });
        }
        new ProcessWrapper(localProcessBuilder).run();
    }

    /**
     * Gets the java path.
     * 
     * @return the java path
     */
    public static String getJavaPath() {
        final String str = System.getProperty("java.home");
        if (str != null) {
            final File localFile1 = new File(str, "bin");
            File localFile2 = new File(localFile1, "java");
            if (localFile2.exists()) {
                return localFile2.getAbsolutePath();
            }
            localFile2 = new File(localFile1, "java.exe");
            if (localFile2.exists()) {
                return localFile2.getAbsolutePath();
            }
        }
        return "java";

    }

    /**
     * Gets the instrumentation path.
     * 
     * @return the instrumentation path
     */
    public static String getInstrumentationPath() {

        final String workingDirectory = System.getProperty("user.dir");
        String instrumentationDir = "";
        final File f = new File(workingDirectory);
        instrumentationDir = f.getParent();
        return instrumentationDir + INSTRUMENTATION_LIB_PATH + File.separator;
    }

    /**
     * Gets the full path.
     * 
     * @param dir
     *            the dir
     * @return the full path
     */
    public static String getFullPath(final String dir) {
        return getInstrumentationPath() + dir;
    };

    /**
     * Process wrapper exec.
     * 
     * @param cmd
     *            the cmd
     * @return true, if successful
     */
    public static boolean execProcessBuilder(final String[] cmd) {
        final ProcessBuilder localProcessBuilder = new ProcessBuilder(cmd);
        final ProcessWrapper localProcessWrapper = new ProcessWrapper(
                localProcessBuilder);
        try {
            return localProcessWrapper.run();
        } catch (final IOException e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * Gets the aapt path.
     * 
     * @return the aapt path
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    public static String getAaptPath() throws IOException {
        String aapt;
        final File file;
        switch (Utils.getOperatingSystemType()) {
            case Windows:
                aapt = "\\AAPT\\aapt.exe";
                return Utils.getFullPath(aapt);
            case Linux:
                aapt = "/AAPT/aapt";
                final String aaptPathLinux = Utils.getFullPath(aapt);
                file = new File(aaptPathLinux);
                file.setExecutable(true);
                return aaptPathLinux;
            case MacOS:
                aapt = "/AAPT/Mac/aapt";
                final String aaptPathMac = Utils.getFullPath(aapt);
                file = new File(aaptPathMac);
                file.setExecutable(true);
                return aaptPathMac;
            default:
                break;
        }
        throw new IOException("Unsupported platform");
    }

    /**
     * gets minimum sdk version of apk.
     * 
     * @param apk
     *            the apk
     * @return the apk min sdk version
     */
    public static String getApkMinSdkVersion(final String apk) {
        try {
            final Process p = Runtime.getRuntime().exec(
                    new String[] { getAaptPath(), "list", "-a", apk });
            final java.io.BufferedReader standardIn = new java.io.BufferedReader(
                    new java.io.InputStreamReader(p.getInputStream()));
            new java.io.BufferedReader(new java.io.InputStreamReader(
                    p.getErrorStream()));
            String line = "";
            while ((line = standardIn.readLine()) != null) {
                if (line.contains("minSdkVersion")) {
                    return line.substring(line.lastIndexOf("x") + 1);
                }
            }

        } catch (final IOException e) {
            e.printStackTrace();
        }
        return null;
    }

}
