/*******************************************************************************
 * Copyright 2014 Asha
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *       http://www.apache.org/licenses/LICENSE-2.0
 * Owner : Asha - initial API and implementation
 * Project Name : Lib_PerformanceMonitoring
 * FileName :StartProfiling
 ******************************************************************************/
package com.imaginea.profiling;

import java.util.Map;

import com.imaginea.profiling.ADBCommand.ProfilingData;

/**
 * The AndroidProfiler class is used to start the profiling and provide all the
 * API's to get Profiling Data.
 * 
 * API Usage : 
 * 1.First Instantiate a android Profiler class and pass sdk path
 * 2.Call API showAllPackages() to show all the installed packages. 
 * 3. Once user enters the Package Name, call the API
 * startUIPerformanceProfiler(packageNAme, instrumentationFlag) to starts the
 * Android Profiler. 
 * 4. Once done, Call the API stopProfiling(). 
 * 5.To display the results call getProfilingMap().
 * 
 */
public final class AndroidProfiler {

    /** The ADB. */
    private static ADBCommand sAdb;

    /** The s app rater. */
    private static AppRater sAppRater;

    /** The ADB path. */
    private String mADBPath;

    /** The m sdk path. */
    private String mSDKPath;

    /**
     * Instantiates a new android profiler class.
     * 
     * @param sdkPath
     *            the sdk path
     */
    public AndroidProfiler(final String sdkPath, final String passWord) {
        initVariables(sdkPath, passWord);
        sAdb.execClearLogcatCommand();
        sAdb.exceCleanEventsBufferLogs();
    }

    /**
     * Sets the up ADB path.
     * 
     * @param sdkHomePath
     *            the new android sdk path befor start profiling
     */
    private void setAndroidSdkPathBeforStartProfiling(final String sdkHomePath) {
        System.setProperty("ANDROID_SDK_HOME", sdkHomePath);
        mSDKPath = sdkHomePath;
        mADBPath = sdkHomePath + "/platform-tools/adb";
    }

    /**
     * Initialize Variables.
     * 
     * @param sdkPath
     *            the SDK path
     */
    private void initVariables(final String sdkPath, final String passWord) {
        setAndroidSdkPathBeforStartProfiling(sdkPath);
        // Instantiates a new ADB class .
        sAdb = new ADBCommand(mADBPath, passWord);

    }

    /**
     * This method is used to get the list of all the Installed packages into
     * the device using command (adb shell pm list packages).
     * 
     * @return the string
     */

    public String showAllPackages() {
        final String packageList = sAdb.execPackageListCommand();
        return packageList;

    }

    /**
     * Start UI Performance profiler. This method will capture the logcat event
     * logs using (adb shell logcat -b events) to measure profiling Data.And,if
     * Instrumentation required, It will also capture the logcat logs using (adb
     * shell logcat -v time) to measure Fragment Data.
     * 
     * @param packageName
     *            the package name
     * @boolean instrumentationFlag the instrumentation flag ( true if
     *          instrumentation required)
     * @return the int (error If any)
     */
    public int startUIPerformanceProfiler(final String packageName,
            final boolean instrumentationFlag) {
        // Sets the user entered package name to Profiler lib
        int error = 0;
        error = setSelectedPackageToProfiler(packageName);
        if (error != 0) {
            return error;
        }
        boolean isInstrumentationDone = false;
        if (instrumentationFlag) {
            isInstrumentationDone = sAdb.startInstrumentation(packageName,
                    mSDKPath);
            if (isInstrumentationDone) {
                sAdb.captureEventLogs(packageName);
                sAdb.captureLogcat(packageName);
            }
        } else {
            sAdb.captureEventLogs(packageName);
            sAdb.captureLogcat(packageName);
        }
        return error;
    }

    /**
     * Sets the selected package(Input from user as Package name for performing
     * UI Profiling) to profiler. This API will return an error if package is
     * not installed successfully or not found.
     * 
     * @param packageName
     *            the package name
     * @return the int
     */
    private int setSelectedPackageToProfiler(final String packageName) {
        final int success = 0;
        final int error = 1;
        if (isPackageinstalledOnDevice(packageName)) {
            return success;
        } else {
            return error;
        }
    }

    /**
     * Checks if is package entered by user is installed on device.
     * 
     * @param packageName
     *            the package name
     * @return true, if is package installed on device
     */
    private boolean isPackageinstalledOnDevice(final String packageName) {
        return sAdb.isPackageInstalledOnDevice(packageName);
    }

    /**
     * Stop profiling using adb . This method is used to disable all the set
     * properties(overdraws,profiling data, tesseract libs) when profiling is
     * done
     */
    public void stopProfiling() {
        sAppRater = AppRater.getInstance();
        sAdb.stopProfiling();

    }

    /**
     * Checks if the application is launched for the first time after Installing
     * on the device or it's a successive launch.
     * 
     * @return true, if successful
     */
    public boolean isAppLaunchedFirstTime() {
        return sAdb.isAppLaunchForFirstTime();
    }

    /**
     * Gets the profiling map. Key : Activity name, Value : profiling data: (No.
     * of overdraws, Launch Time, activity stack count,GC count(GC_CONC,
     * GC_ALLOC)).
     * 
     * @return the profiling map
     */
    public Map<String, ProfilingData> getProfilingMap() {

        return sAdb.getProfilingItems();
    }

    /**
     * Gets the application score. This method will provide the overall
     * application score on a scale of 100
     * 
     * @return the application score
     */
    public int getApplicationScore() {
        return sAppRater.getApplicationScore();
    }

    /**
     * This method rates the overall application and returns application rating.
     * 
     * @return the application rating
     */
    public String getApplicationRating() {
        return sAppRater.getApplicationRating();
    }

    /**
     * Gets the over draw score for application.
     * 
     * @return the over draw score
     */
    public int getOverDrawScore() {
        return sAppRater.getOverDrawScore();
    }

    /**
     * Gets the garbage collection score for application.
     * 
     * @return the GC score
     */
    public int getGCScore() {
        return sAppRater.getGCCallsScore();
    }

    /**
     * Gets the activity stack score for application
     * 
     * @return the activity stack score
     */
    public int getActivityStackScore() {
        return sAppRater.getActivityStackScore();
    }

    /**
     * Gets the launch time score for application.
     * 
     * @return the launch time score
     */
    public int getLaunchTimeScore() {
        return sAppRater.getLaunchTimeScore();
    }

    /**
     * Gets the activity grades.(This method will return the grade for each
     * individual activity)
     * 
     * @return the activity grades
     */
    public Map<String, String> getActivityGrades() {
        return sAppRater.getsActivitiesGrade();
    }
}
