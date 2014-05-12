/*******************************************************************************
 * Copyright 2014 Asha
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 *Owner : Asha - initial API and implementation
 *Project Name : UIProfiling_Android
 *FileName :Profiling
 ******************************************************************************/
package com.imaginea.adbexplorer;

import java.util.Map;
import java.util.Scanner;

import com.imaginea.profiling.AndroidProfiler;

/**
 * The Class Profiling.
 */
public class Profiling {
    /**
     * Start profiling timer.
     */

    private static class ConsoleInput implements Runnable {

        /** The keep running. */
        volatile static boolean keepRunning = true;

        /*
         * (non-Javadoc)
         * 
         * @see java.lang.Runnable#run()
         */
        @Override
        public void run() {
            while (keepRunning) {
                try {
                    Thread.sleep(1000);
                } catch (final InterruptedException e) {
                }
            }
        }

    }

    /** The s profiling. */
    private static AndroidProfiler sProfiling;

    /** The s in. */
    private static Scanner sIn;

    /** The s package name. */
    private static String sPackageName;

    /**
     * Gets the user console input.
     * 
     * @return the user console input
     */
    private static void getUserConsoleInput() {
        final ConsoleInput input = new ConsoleInput();
        final Thread consoleThread = new Thread(input);
        consoleThread.start();

        final Scanner scanner = new Scanner(System.in);
        while (!"Stop Profiling".equalsIgnoreCase(scanner.nextLine())) {
            ;
        }
        sProfiling.stopProfiling();

        // Print the Profile Data
        printProfilingData();

        // Stop Application
        ConsoleInput.keepRunning = false;
        consoleThread.interrupt(); // cancel current sleep.
        scanner.close();// close the scanner
        sIn.close();
        System.exit(0);

    }

    /**
     * Inits the profiling.
     * 
     * @param sdkpath
     *            the SDK path
     */
    private static void initProfiling(final String sdkHomePath) {
        
        System.out
        .format("-------------------------------------------------------%n");
        System.out.printf("Please Enter your password: %n");
        String password = sIn.nextLine();
        // Instantiate a new class profiling
        sProfiling = new AndroidProfiler(sdkHomePath, password);
        
        System.out
                .format("-------------------------------------------------------%n");
        System.out.printf("List of Packages Installed %n");
        System.out
                .format("-------------------------------------------------------%n");
        final String packageList = sProfiling.showAllPackages();
        System.out.println(packageList);
        System.out
                .format("-------------------------------------------------------%n");

        System.out.println("Please enter a Package Name");
        sPackageName = sIn.nextLine();

    }

    /**
     * Checks if is instrumentation required.
     * 
     * @return true, if successful
     */
    private static boolean IsInstrumentationRequired() {
        /*
         * Note that instrumenting an App replaces the original APK with the
         * instrumented one, so you may lose App state. After you are done
         * testing an App, it is recommended that you restore the original APK
         * by right clicking -> Restore original App.
         */
        System.out
                .format("-------------------------------------------------------%n");
        System.out
                .printf("To get the Fragment Data, the App needs to be “instrumented” first.%n");

        sIn = new Scanner(System.in);
        System.out.println("Do you want to Instrument the App (Y or N)");
        if (sIn.nextLine().equalsIgnoreCase("Y")) {
            return true;
        } else {
            return false;
        }

    }

    /**
     * Start UI performance profiler.
     * 
     * @param packageName
     *            the package name
     * @param isInstrumentationRequired
     *            the is instrumentation required
     * @return the int
     */
    private static int startUIPerformanceProfiler(final String packageName,
            final boolean isInstrumentationRequired) {
        System.out
                .format("-------------------------------------------------------%n");
        System.out.printf("Start profiling %n");
        /*
         * Starting a console thread in background to get the system input when
         * user stops profiling
         */
        new Thread(new Runnable() {
            @Override
            public void run() {
                getUserConsoleInput();

            }
        }).start();

        return sProfiling.startUIPerformanceProfiler(packageName,
                isInstrumentationRequired);
    }

    /**
     * The main method.
     * 
     * @param args
     *            the arguments
     */
    public static void main(final String[] args) {
        int error = 0;
        System.out.format("-----------------------------------------------%n");
        System.out.println("Profiling");
        System.out.format("-----------------------------------------------%n");
        System.out.println("Please enter a android sdk path : ");
        sIn = new Scanner(System.in);
        final String sdkHome = sIn.nextLine();
        // Initialize package Info class and starts initialing paths and
        // Profiler library
        initProfiling(sdkHome);
        // If PAckage is not foun don device start Profiler will return the
        // error
        if (IsInstrumentationRequired()) {
            error = startUIPerformanceProfiler(sPackageName, true);
        } else {
            error = startUIPerformanceProfiler(sPackageName, false);
        }

        if (error != 0) {
            System.out.println("Package not found on device. Try again.");
            System.exit(0);
        }

    }

    /**
     * Prints the profiling data.
     */
    private static void printProfilingData() {

        boolean isAppFirstTime = false;

        final String leftAlignFormat = "| %-35s | %-28d | %-22d | %-20s | %-20s | %-10s  %n";

        System.out
                .format("-------------------------------------------------------%n");
        System.out.printf("Profiling Report %n");
        System.out
                .format("-------------------------------------------------------%n");

        isAppFirstTime = sProfiling.isAppLaunchedFirstTime();
        if (isAppFirstTime) {
            System.out
                    .printf("Note : Application has launched for the first time.");
        } else {
            System.out
                    .printf("Note : This is a successive Application launch.");
        }

        System.out
                .format("%n-------------------------------------------------------%n");
        System.out.printf("Package Name  : " + sPackageName + "%n");
        System.out
                .format("-------------------------------------------------------%n");

        final String format = "| %-35s |  %-10s  | %-10s | %-10s | %-20s| %n %-105s | %-20s| %-20s  %n";

        System.out.format(format, "Activity Name",
                "Launch Time In Milliseconds", "Activity in Back Stack",
                "Number of Overdraws", "GC Calls", "", "GC_FOR_CONCURRENT",
                "GC_FOR_ALLOC");
        System.out
                .format("========================================================================================================================================================+%n");

        final Map<String, com.imaginea.profiling.ADBCommand.ProfilingData> profilingMap = sProfiling
                .getProfilingMap();

        for (final Map.Entry<String, com.imaginea.profiling.ADBCommand.ProfilingData> profilingItem : profilingMap
                .entrySet()) {

            /* Display the Data */
            if (profilingItem.getKey() != null) {
                System.out.format(leftAlignFormat, profilingItem.getKey(),
                        profilingItem.getValue().getLaunchTime(), profilingItem
                                .getValue().getActivityStackCount(),
                        profilingItem.getValue().getOverDraws(), profilingItem
                                .getValue().getGCCalls()[1], profilingItem
                                .getValue().getGCCalls()[0]);

            }
        }

        // Display Grades
        System.out
                .format("-------------------------------------------------------%n");
        System.out
                .printf("%n                  Display Grades                  %n");
        System.out
                .format("-------------------------------------------------------%n");
        /** The m activity grade map. */
        final Map<String, String> sActivitiesGrade = sProfiling
                .getActivityGrades();
        for (final Map.Entry<String, String> entry : sActivitiesGrade
                .entrySet()) {
            System.out
                    .println(entry.getKey() + " :  GRADE " + entry.getValue());
        }

        // Display Grades per rules
        if (profilingMap.size() > 0) {
            System.out
                    .format("-------------------------------------------------------%n");
            System.out
                    .printf("%n                Display Grades Per Rule                  %n");
            System.out
                    .format("-------------------------------------------------------%n");
            System.out.println("Launch Time Performance Score : "
                    + sProfiling.getLaunchTimeScore());

            System.out
                    .format("-------------------------------------------------------%n");
            System.out.println("GC Calls Performance Score : "
                    + sProfiling.getGCScore());

            System.out
                    .format("-------------------------------------------------------%n");
            System.out.println("Activity Stack Score : "
                    + sProfiling.getActivityStackScore());
            System.out
                    .format("-------------------------------------------------------%n");

            System.out.println("Overdraw Score : "
                    + sProfiling.getOverDrawScore());

            // Over All Application Grade
            System.out
                    .format("-------------------------------------------------------%n");
            System.out
                    .printf("%n                  Application Grade                   %n");
            System.out
                    .format("-------------------------------------------------------%n");
            final String rank = sProfiling.getApplicationRating();
            System.out.println("GRADE :" + rank);
            System.out.println("Overall performance score :"
                    + sProfiling.getApplicationScore());
            System.out
                    .format("-------------------------------------------------------%n");
        }

    }

}
