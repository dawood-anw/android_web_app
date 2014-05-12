/*******************************************************************************
 * Copyright 2014 Asha
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *       http://www.apache.org/licenses/LICENSE-2.0
 * Owner : Asha/Saikiran - initial API and implementation
 * Project Name : Lib_PerformanceMonitoring
 * FileName :Aspect
 ******************************************************************************/

package com.imaginea.instrumentation;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Enumeration;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

/**
 * The Class Aspect.
 */
public class Aspect {

    private static final int HONEYCOMB_VERSION = 13;

    /**
     * Adds the import to aspect.
     * 
     * @param PackageName
     *            the package name
     */
    public static void addImportToAspect(final String packageName,
            final String importPackage) {
        try {
            final String data = "\npackage "
                    + packageName
                    + ";\n\n\nimport "
                    + importPackage
                    + ";\n\naspect onCreate"
                    + "\n{ \n\tpointcut captureOnCreate() : (execution(* onCreate(*))&& within("
                    + packageName
                    + "..*"
                    + "));\n\tafter(): captureOnCreate() \n\t{ "
                    + "\n\t\t\tFragmentManager.enableDebugLogging(true);\n\t}\n}";

            final File file = new File(Utils.getInstrumentationPath()
                    + "Aspect.aj");
            if (!file.exists()) {
                file.createNewFile();
            }

            final FileWriter fileWritter = new FileWriter(
                    file.getAbsolutePath(), false);
            final BufferedWriter bufferWritter = new BufferedWriter(fileWritter);
            bufferWritter.write(data);
            bufferWritter.close();
        } catch (final IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Generates the aspectj command line scripts
     * 
     * @param void
     */

    public static void generateAjc() {
        String data = "";
        String ajc_path = "";

        switch (Utils.getOperatingSystemType()) {
            case Windows:
                data = "@echo off\n"
                        + "if \"%JAVA_HOME%\" == \"\" set JAVA_HOME="
                        + System.getProperty("java.home")
                        + "\n"
                        + "if \"%ASPECTJ_HOME%\" == \"\" set ASPECTJ_HOME="
                        + Utils.getInstrumentationPath()
                        + "aspectj1.7\n"
                        + "\"%JAVA_HOME%\\bin\\java\" -classpath \"%ASPECTJ_HOME%\\lib\\aspectjtools.jar;%JAVA_HOME%\\lib\\tools.jar;%CLASSPATH%\" -Xmx256M org.aspectj.tools.ajc.Main %*";
                ajc_path = Utils.getInstrumentationPath()
                        + "aspectj1.7\\ajc.bat";
                break;
            case Linux:
            case MacOS:
                data = "#!/bin/sh\n"
                        + "if [ \"$JAVA_HOME\" = \"\" ] ; then JAVA_HOME="
                        + System.getProperty("java.home")
                        + "\n"
                        + "fi\nif [ \"$ASPECTJ_HOME\" = \"\" ] ; then ASPECTJ_HOME="
                        + Utils.getInstrumentationPath()
                        + "aspectj1.7\n"
                        + "fi\n\n"
                        + "\"$JAVA_HOME/bin/java\" -classpath \"$ASPECTJ_HOME/lib/aspectjtools.jar:$JAVA_HOME/lib/tools.jar:$CLASSPATH\" -Xmx256M org.aspectj.tools.ajc.Main \"$@\"";
                ajc_path = Utils.getInstrumentationPath() + "aspectj1.7/ajc";
                break;
            default:
                break;
        }

        try {
            final File file = new File(ajc_path);
            if (!file.exists()) {
                file.createNewFile();
                file.setExecutable(true);
            }

            final FileWriter fileWritter = new FileWriter(
                    file.getAbsolutePath(), false);
            final BufferedWriter bufferWritter = new BufferedWriter(fileWritter);
            bufferWritter.write(data);
            bufferWritter.close();
        } catch (final IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Inject code2jar.
     * 
     * @param apk
     *            the apk path
     * @param path
     *            the @param path String1
     * @param PackageName
     *            the package name
     * @return true, if successful
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    public static boolean injectCode2jar(final String apk, final String path,
            final String PackageName) throws IOException {
        String[] cmd = null;
        // generate a script to run commad line ajc
        generateAjc();
        // check for fragemnts
        final String importPackage = CheckFragemntJar(apk, new JarFile(
                "profiling_dex2jar.jar"));
        if (importPackage.isEmpty()) {
            return false;
        }
        // adding aspectj to application package
        addImportToAspect(PackageName, importPackage);

        switch (Utils.getOperatingSystemType()) {
            case Windows:
                cmd = new String[] {
                        Utils.getFullPath("aspectj1.7\\ajc.bat"),
                        "-classpath",
                        Utils.getFullPath("aspectj1.7\\lib\\aspectjrt.jar")
                                + ";" + Utils.getFullPath("android.jar") + ";"
                                + Utils.getFullPath("android-support-v4.jar"),
                        "-inpath", "profiling_dex2jar.jar", "-outjar",
                        "out.jar", Utils.getInstrumentationPath() + "Aspect.aj" };
                break;
            case Linux:
            case MacOS:
                cmd = new String[] {
                        "sh",
                        Utils.getFullPath("aspectj1.7/ajc"),
                        "-classpath",
                        Utils.getFullPath("aspectj1.7/lib/aspectjrt.jar") + ":"
                                + Utils.getFullPath("android.jar") + ":"
                                + Utils.getFullPath("android-support-v4.jar"),
                        "-inpath", "profiling_dex2jar.jar", "-outjar",
                        "out.jar", Utils.getInstrumentationPath() + "Aspect.aj" };
                break;
            default:
                break;
        }

        return Utils.execProcessBuilder(cmd);

    }

    /**
     * Check the jar for Fragemnt library.
     * 
     * @param apk
     *            the apk path
     * @param jarFile
     *            the jarFile to check for
     * @return String, the Fragemnt Pacage used.
     */
    public static String CheckFragemntJar(final String apk,
            final JarFile jarFile) {

        final String ret = Utils.getApkMinSdkVersion(apk);
        final int minSdkVersion = Integer.parseInt(ret, 16);
        if (minSdkVersion >= HONEYCOMB_VERSION) {
            return "android.app.FragmentManager";
        } else {
            final Enumeration<JarEntry> entries = jarFile.entries();

            while (entries.hasMoreElements()) {
                final JarEntry entry = entries.nextElement();
                if (entry.getName().contains("android/support/v4/app/")) {
                    return "android.support.v4.app.FragmentManager";
                }
            }
        }

        return "";
    }
}
