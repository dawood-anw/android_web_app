/*******************************************************************************
 * Copyright 2014 Asha
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *       http://www.apache.org/licenses/LICENSE-2.0
 * Owner : Asha - initial API and implementation
 * Project Name : Lib_PerformanceMonitoring
 * FileName :ADBCommand
 ******************************************************************************/
package com.imaginea.profiling;

import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;
import java.util.TimeZone;
import java.util.Timer;
import java.util.TimerTask;

import javax.swing.filechooser.FileSystemView;

import com.imaginea.instrumentation.Utils;

/**
 * The Class ADBCommand. This Class is used to execute all the ADB command
 * Required for Performance Monitoring, stores the output and provides the
 * Performance Data
 */
public class ADBCommand {

    /** The Constant INSTR_PACKAGE. */
    private final String INSTR_PACKAGE = "/InstrumentationPackage";

    /** The portrait. */
    private final int PORTRAIT = 0;

    /** The m activity timer. */
    Timer mActivityTimer;

    /** The m fragment timer. */
    Timer mFragmentTimer;

    /** The previous activity name. */
    String mPreviousActivityName = null;

    /** The previous fragment name. */
    String mPreviousFragmentName = null;

    /** The landscape. */
    private final int LANDSCAPE = 1;

    /** The overdraw delay. */
    private final int OVERDRAW_DELAY = 300;

    /** The overdraw occur time. */
    private final int OVERDRAW_OCCUR_TIME = 1000; // 1 sec

    /** The Constant ADB_PATH. */
    private String mADBPath;

    /** The m package name. */
    private String mPackageName;

    /** The is app first time launch. */
    private boolean isAppFirstTimeLaunch = false;

    /** The runtime. */
    private final Runtime mRuntime;

    /** The m fragment start time. */
    private long mFragmentStartTime = 0;

    /** The m fragment resumed time. */
    private long mFragmentResumedTime = 0;

    /** The m activity name. */
    private String mActivityName = null;

    /** The m activity name. */
    private String mFragmentName = null;

    /** The dir instrumentation. */
    private File mDirInstrumentation;

    /** The activities data. */
    private final Map<String, Long> mActivitiesData = new HashMap<String, Long>();

    /** The gc data. */
    private final Map<String, int[]> mGCData = new HashMap<String, int[]>();

    /** The over draw data. */
    private final Map<String, String> mOverDrawData = new HashMap<String, String>();

    /** The profiling items. */
    public final HashMap<String, ProfilingData> profilingItems = new HashMap<String, ProfilingData>();

    /**
     * Gets the profiling items.This method is used to provide activity
     * data,overdraws data,GC data,Activity Stack Count stored while Performance
     * Monitoring
     * 
     * 
     * @return the profiling items
     */
    public HashMap<String, ProfilingData> getProfilingItems() {
        return profilingItems;
    }

    /**
     * Instantiates a new ADB command.
     * 
     * @param adbPath
     *            the ADB path
     */
    public ADBCommand(final String adbPath, final String passWord) {
        mADBPath = adbPath;
        mRuntime = Runtime.getRuntime();
        if (canRunProgram()) {
            /* enable overdraws */
            final String[] ENABLE_OVERDRAW = new String[] { mADBPath, "shell",
                    "setprop", "debug.hwui.overdraw", "count" };
            exec(ENABLE_OVERDRAW);
            // this will setup the tesseract path
            Utils.SetupTesseract(passWord);
        } else {
            System.exit(0);
        }

    }

    /**
     * Can run program.
     * 
     * @return true, if successful
     */
    private boolean canRunProgram() {
        if (connectedDevices() <= 0) {
            System.out
                    .println("Java ADB Explorer - Device Error : No device connected");
            return false;
        } else if (connectedDevices() > 1) {
            System.out
                    .println("Java ADB Explorer - Device Error : Multiple devices connected");
            return false;
        }
        return true;
    }

    /**
     * Capture event logs : This method is used to capture event logs using (adb
     * shell logcat -b events) to measure profiling Data.
     * 
     * @param packageName
     *            the package name
     */
    public void captureEventLogs(final String packageName) {
        mPackageName = packageName;

        // Clear all the logcat before start capturing the event logs
        new Thread(new Runnable() {
            @Override
            public void run() {
                exceCleanEventsBufferLogs();
                final Process p = customExec(new String[] { mADBPath, "shell",
                        "logcat", "-b", "events" });
                filterEventCalls(p, packageName);
            }
        }).start();

    }

    /**
     * Capture logcat.This method is used to capture logcat logs using (adb
     * shell logcat -v time) to measure Fragment Data
     * 
     * @param packageName
     *            the package name
     */
    public void captureLogcat(final String packageName) {
        // Clear all the logcat before start capturing the logs
        new Thread(new Runnable() {
            @Override
            public void run() {
                execClearLogcatCommand();
                final Process p = customExec(new String[] { mADBPath, "shell",
                        "logcat", "-v", "time" });
                filterLogcatCalls(p);
            }
        }).start();

    }

    /**
     * Filter logcat calls.
     * 
     * @param p
     *            the p
     */
    private void filterLogcatCalls(final Process p) {
        try {
            final java.io.BufferedReader standardIn = new java.io.BufferedReader(
                    new java.io.InputStreamReader(p.getInputStream()));
            new java.io.BufferedReader(new java.io.InputStreamReader(
                    p.getErrorStream()));
            String line = "";
            while ((line = standardIn.readLine()) != null) {
                if (line.contains("FragmentManager")) {
                    filterLogcatFragmentCalls(line);
                }
            }

        } catch (final IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Filter logcat fragment calls.
     * 
     * @param eventsLogs
     *            the events logs
     */
    private void filterLogcatFragmentCalls(final String eventsLogs) {
        if (eventsLogs.contains("moveto ACTIVITY_CREATED")) {
            /* filter fragment name */
            filterFragmentName(eventsLogs, ":", "{");
            /* filter fragment start time */
            mFragmentStartTime = filterTimeStamp(eventsLogs.substring(0, 18));
        } else if (eventsLogs.contains("moveto RESUMED")) {
            mFragmentTimer = new Timer();
            mFragmentTimer.schedule(new TimerTask() {
                @Override
                public void run() {
                    mPreviousFragmentName = mFragmentName;
                    captureScreenShot();
                    getOverdraws(false, isDeviceModelNexus());
                }
            }, OVERDRAW_DELAY, OVERDRAW_OCCUR_TIME);
            /* filter fragment resumed time */
            mFragmentResumedTime = filterTimeStamp(eventsLogs.substring(0, 18));
            final long launchTime = mFragmentResumedTime - mFragmentStartTime;
            storeFragmentLaunchTime(launchTime);
        } else if (eventsLogs.contains("add:")) {
            filterFragmentName(eventsLogs, ":", "{");
            execGCCommand(false);
        } else if (eventsLogs.contains(" movefrom RESUMED:")) {
            filterFragmentName(eventsLogs, ":", "{");
            if (mFragmentName.equalsIgnoreCase(mPreviousFragmentName)) {
                mFragmentTimer.cancel();
            }
        } else if (eventsLogs.contains("remove:")) {
            filterFragmentName(eventsLogs, ":", "{");
            execGCCommand(false);
        }
    }

    /**
     * This Method is used to Capture the screen shot.
     */
    private synchronized void captureScreenShot() {
        try {
            final Process pr = customExec(new String[] { mADBPath, "shell",
                    "screencap", "-p", "/mnt/sdcard/screen.png" });
            // Wait for process to finish
            pr.waitFor();
            pr.destroy();
        } catch (final InterruptedException e) {
            e.printStackTrace();
        }

    }

    /**
     * Capture orientation. This method captures whether screen orientation is
     * Landscape or Portrait and returns the output
     * 
     * @return the int value
     */
    private int captureOrientation() {
        String filterOutput = null;
        int screenOrientation = 0;
        int port = 0;
        int land = 0;
        final Process p = customExec(new String[] { mADBPath, "shell",
                "dumpsys", "activity", "activities" });
        final String retoutput = processOutput(p);
        for (final String v : retoutput.split("\n")) {
            if (v.contains("config")) {
                filterOutput += v + "\n";
            }
        }
        // further split with "config" to find the orientation
        for (final String v : filterOutput.split("config")) {
            if (v.contains("port")) {
                port++;
            } else {
                land++;
            }
        }
        if (port > land) {
            screenOrientation = PORTRAIT;
        } else {
            screenOrientation = LANDSCAPE;
        }
        return screenOrientation;
    }

    /**
     * Gets the overdraws.
     * 
     * @param isActivity
     *            the is activity
     * @param isBlackPatchEnable
     *            the is black patch enable
     * @return the overdraws
     */
    private synchronized void getOverdraws(final boolean isActivity,
            final boolean isBlackPatchEnable) {
        final int mode = captureOrientation();
        final File dir = FileSystemView.getFileSystemView().getHomeDirectory();
        try {
            final Process pr = customExec(new String[] { mADBPath, "pull",
                    "/mnt/sdcard/screen.png", dir.getAbsolutePath() });
            // Wait for process to finish
            pr.waitFor();
            pr.destroy();
        } catch (final InterruptedException e) {
            e.printStackTrace();
        }
        final String noOfOverDraw = OverdrawScanner.getNoOfOverdraws(
                dir.getAbsolutePath(), mode, isBlackPatchEnable);
        String viewName = null;
        if (isActivity) {
            viewName = mActivityName;
        } else {
            viewName = mFragmentName;
        }
        if (viewName != null) {
            if (!mOverDrawData.containsKey(viewName.toString())) {
                if (noOfOverDraw != null) {
                    mOverDrawData.put(viewName, noOfOverDraw.trim());
                }
            }
        }
        // Delete all the captured stored files once overdraw is calculated.
        exec(new String[] { mADBPath, "shell", "rm", "/mnt/sdcard/screen.png" });

    }

    /**
     * Store fragment launch time.
     * 
     * @param timeInmillis
     *            the time in millis
     */
    private void storeFragmentLaunchTime(final long timeInmillis) {
        if (!mActivitiesData.containsKey(mFragmentName)) {
            mActivitiesData.put(mFragmentName, timeInmillis);
        }
    }

    /**
     * Filter time stamp.
     * 
     * @param time
     *            the time
     * @return the long
     */
    private long filterTimeStamp(final String time) {
        final SimpleDateFormat sdf = new SimpleDateFormat("MM-dd HH:mm:ss.SSS");
        sdf.setTimeZone(TimeZone.getTimeZone("GMT"));
        try {
            return sdf.parse(time).getTime();
        } catch (final ParseException e) {
            e.printStackTrace();
        }
        return 0;

    }

    /**
     * Filter fragment name.
     * 
     * @param line
     *            the line
     * @param start
     *            the start
     * @param end
     *            the end
     */
    private void filterFragmentName(final String line, final String start,
            final String end) {
        final int startIndex = line.lastIndexOf(start);
        final int endIndex = line.indexOf(end);
        if (startIndex >= 0) {
            mFragmentName = line.substring(startIndex + 1, endIndex)
                    .replaceAll("\\s", "");
        }
    }

    /**
     * Connected devices.
     * 
     * @return the int
     */
    private int connectedDevices() {
        final String[] listADBDevices = getDevices();
        if (listADBDevices == null) {
            return 0;
        } else {
            return listADBDevices.length;
        }
    }

    /**
     * Custom exec.
     * 
     * @param cmdArray
     *            the cmd array
     * @return the process
     */
    private Process customExec(final String[] cmdArray) {
        Process p = null;
        try {
            p = mRuntime.exec(cmdArray);
        } catch (final IOException e) {
            e.printStackTrace();
        }
        return p;
    }

    /**
     * Execute activity stack command. This method is used to find the number of
     * activities for selected package present in activity back stack using adb
     * shell dumpsys activity activities and in the resulting logs we will find
     * a activity stack
     * 
     * @param packageName
     *            the package name
     * @return the string
     */
    private String exceActivityStackCommand(final String packageName) {
        String retout = "";
        final Process p = customExec(new String[] { mADBPath, "shell",
                "dumpsys", "activity", "activities" });
        final String retoutput = processOutput(p);
        p.destroy();
        // filter the recent activity stack output
        for (final String v : retoutput.split("\n")) {
            if (v.contains(packageName)) {
                if (v.contains("Run #")) {
                    retout += v + "\n";
                }
            }
        }
        return retout;

    }

    /**
     * Execute clean events buffer logs. : This method is used to clear the
     * logcat buffer logs using command (adb logcat -c -b events )
     */
    public void exceCleanEventsBufferLogs() {
        exec(new String[] { mADBPath, "logcat", "-c", "-b", "events" });
    }

    /**
     * Execute commands.
     * 
     * @param cmdArray
     *            the cmd array
     */
    private void exec(final String[] cmdArray) {
        try {
            mRuntime.exec(cmdArray);
        } catch (final IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Execute clear Logcat command. : This method is used to clear the logcat
     * information using command (adb logcat -c)
     */
    public void execClearLogcatCommand() {
        try {
            mRuntime.exec(new String[] { mADBPath, "logcat", "-c" });
        } catch (final java.io.IOException e) {
            System.out
                    .println("SDk path is not valid. Please enter a android sdk path again");
            mADBPath = new Scanner(System.in).nextLine()
                    + "/platform-tools/adb";
            execClearLogcatCommand();
        }
    }

    /**
     * Execute disconnect. This method is used to disconnect the adb using
     * command (adb disconnect <devicename> )
     */
    public void execDisconnect() {
        final String deviceName = getDeviceName();
        // execute disconnect command
        exec(new String[] { mADBPath, "disconnect", deviceName });
    }

    /**
     * Exec gc command.
     * 
     * @param isActivity
     *            the is activity
     */
    private synchronized void execGCCommand(final boolean isActivity) {
        String filterOutput = "";
        try {
            final Process p = customExec(new String[] { mADBPath, "logcat",
                    "-d" });
            p.waitFor();
            final String retoutput = processOutput(p);
            p.destroy();
            // filter GC logs
            for (final String v : retoutput.split("\n")) {
                if (v.contains("GC_FOR_ALLOC") || v.contains("GC_CONCURRENT")) {
                    filterOutput += v + "\n";
                }
            }
            // clean the logs
            execClearLogcatCommand();
            calculateGCCalls(filterOutput, isActivity);
        } catch (final InterruptedException e) {
            e.printStackTrace();
        }

    }

    /**
     * Execute package list command. This method is used to get the list of all
     * the packages install in the device using command (adb shell pm list
     * packages) and in the resulting logs we will find a list of packages
     * 
     * @return the string
     */
    public String execPackageListCommand() {
        String retoutput = null;
        final Process p = customExec(new String[] { mADBPath, "shell", "pm",
                "list", "packages" });
        retoutput = processOutput(p);
        return retoutput;
    }

    /**
     * Filter activity name.
     * 
     * @param line
     *            the line
     * @param start
     *            the start
     * @param end
     *            the end
     */
    private void filterActivityName(final String line, final String start,
            final String end) {
        final int startIndex = line.lastIndexOf(start);
        final int endIndex = line.lastIndexOf(end);
        if (startIndex >= 0) {
            mActivityName = line.substring(startIndex + 1, endIndex);
        }
    }

    /**
     * Filter event calls.
     * 
     * @param p
     *            the p
     * @param packageName
     *            the package name
     */
    private void filterEventCalls(final Process p, final String packageName) {
        // clear buffer logs first
        exec(new String[] { mADBPath, "logcat", "-c", "-b", "events" });
        try {
            final java.io.BufferedReader standardIn = new java.io.BufferedReader(
                    new java.io.InputStreamReader(p.getInputStream()));
            new java.io.BufferedReader(new java.io.InputStreamReader(
                    p.getErrorStream()));
            String line = "";
            while ((line = standardIn.readLine()) != null) {
                if (line.contains(packageName)) {
                    calculateEventCalls(line, packageName);
                }
            }

        } catch (final IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Filter launch data.
     * 
     * @param log
     *            the log
     */
    private void filterLaunchInfo(final String log) {
        final int startIndex = log.lastIndexOf(".");
        String launchData = null;
        final int endIndex = log.lastIndexOf(",");
        if (startIndex >= 0) {
            launchData = log.substring(startIndex + 1, endIndex);
        }
        if (launchData != null) {
            final String[] launchInfo = launchData.split(",");
            // activity name
            mActivityName = launchInfo[0];
            // calculate launch Time
            long timeInmillis = Long.parseLong(launchInfo[1]);
            if (mActivitiesData.containsKey(mActivityName)) {
                final long time = mActivitiesData.get(mActivityName);
                timeInmillis = (time + timeInmillis) / 2;
            }
            mActivitiesData.put(mActivityName, timeInmillis);
        }
    }

    /**
     * Gets the device name.
     * 
     * @return the device name
     */
    private String getDeviceName() {
        String retoutput = "";
        String deviceName = null;
        final Process p = customExec(new String[] { mADBPath, "devices" });
        retoutput = processOutput(p);
        final String[] lines = retoutput.split("\\n");
        final String[] matcher = lines[1].split("device");
        deviceName = matcher[0].trim();

        return deviceName;
    }

    /**
     * Gets the devices.
     * 
     * @return the devices
     */
    private String[] getDevices() {
        try {
            final Process p = customExec(new String[] { mADBPath, "devices" });
            final java.io.DataInputStream in = new java.io.DataInputStream(
                    p.getInputStream());

            final byte[] buf = new byte[1024];
            final int len = in.read(buf);

            final String[] ligne = new String(buf, 0, len).split("\n");
            int deviceArrayLength = 0;
            // For Windows extra "/r" is appended into adb device output .
            // Ignoring extra "/r" line
            for (final String element : ligne) {
                if (element.equals("\r")) {
                    deviceArrayLength = ligne.length - 1;
                } else {
                    deviceArrayLength = ligne.length;
                }
            }
            // We don't take the first line "List of devices attached"
            final String[] retour = new String[deviceArrayLength - 1];

            for (int i = 1; i < deviceArrayLength; i++) {
                retour[i - 1] = ligne[i].split("\t")[0];

            }

            return retour;
        } catch (final java.io.IOException e) {

        }

        return null;
    }

    /**
     * Checks if the application is launched for the first time after Install or
     * it's a successive launch.
     * 
     * @return true, if is app launched for the first time
     */
    public boolean isAppLaunchForFirstTime() {
        return isAppFirstTimeLaunch;
    }

    /**
     * Process output.
     * 
     * @param p
     *            the p
     * @return the string
     */
    private String processOutput(final Process p) {
        String retout = "";
        try {
            final java.io.BufferedReader standardIn = new java.io.BufferedReader(
                    new java.io.InputStreamReader(p.getInputStream()));
            final java.io.BufferedReader errorIn = new java.io.BufferedReader(
                    new java.io.InputStreamReader(p.getErrorStream()));
            String line = "";
            while ((line = standardIn.readLine()) != null) {
                retout += line + "\n";
            }
            while ((line = errorIn.readLine()) != null) {
                retout += line + "\n";
            }
        } catch (final java.io.IOException e) {
            e.printStackTrace();
        }

        return retout;
    }

    /**
     * Checks if the package entered by user is Installed on device.
     * 
     * @param packageName
     *            the package name
     * @return true, if is package Installed on device
     */
    public boolean isPackageInstalledOnDevice(final String packageName) {
        final Process p = customExec(new String[] { mADBPath, "shell",
                "pm path ", packageName });
        final String retoutput = processOutput(p);
        if (retoutput.length() > 1) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * Start instrumentation.
     * 
     * @param packageName
     *            the package name
     * @param sdkPath
     *            the sdk path
     * @return true, if successful
     */
    public boolean startInstrumentation(final String packageName,
            final String sdkPath) {
        System.out.println("Instrumenting... \n");

        final Process p = customExec(new String[] { mADBPath, "shell", "pm",
                "list", "packages", "-f" });
        final String retoutput = filterApkPath(p, packageName);

        final int startIndex = retoutput.indexOf(":");
        final int endIndex = retoutput.indexOf("=");

        final String apkPath = retoutput.substring(startIndex + 1, endIndex);

        // Filter Apk name
        final int apkStartIndex = apkPath.lastIndexOf("/");
        final String APKName = apkPath.substring(apkStartIndex + 1);

        // Pull the apk and copy to home Directory
        final File dir = FileSystemView.getFileSystemView().getHomeDirectory();

        // Create Instrumentation Input Directory
        mDirInstrumentation = new File(dir + INSTR_PACKAGE);
        mDirInstrumentation.mkdir();

        // Create output directory to Unpack APK (OutputDir)
        final File dirInstrumenationOut = new File(dir + INSTR_PACKAGE + "/out");
        dirInstrumenationOut.mkdir();

        // Copy APK to instrumentation directory for instrumentation
        try {
            final Process pr = customExec(new String[] { mADBPath, "pull",
                    apkPath, mDirInstrumentation.getAbsolutePath() });
            // Wait for process to finish
            pr.waitFor();
            pr.destroy();
        } catch (final InterruptedException e) {
            e.printStackTrace();
        }

        // Start Instrumentation
        final Instrumentation instrumenatation = new Instrumentation();

        if (instrumenatation.startInstrumentation(mDirInstrumentation,
                dirInstrumenationOut, APKName, sdkPath, packageName)) {
            // UnInstall Existing APK and Install the instrument APK
            try {
                final Process pr = customExec(new String[] { mADBPath,
                        "uninstall", packageName });
                // Wait for process to finish
                pr.waitFor();
                pr.destroy();
                final Process prInstall = customExec(new String[] { mADBPath,
                        "install", dirInstrumenationOut + "/Profiling.apk" });
                // Wait for process to finish
                prInstall.waitFor();
                prInstall.destroy();
            } catch (final InterruptedException e) {
                e.printStackTrace();
            }
            System.out
                    .println("The app is instrumented and re-installed on the device \nNote : remember to reinstall the original after you are done testing! \n");
        } else {
            System.out
                    .println("App cannot be Instrumented,no Fragments are Available. \n");
        }

        System.out
                .format("-------------------------------------------------------%n");
        System.out.printf("Start profiling %n");
        return true;

    }

    /**
     * Filter apk path.
     * 
     * @param p
     *            the p
     * @param packageName
     *            the package name
     * @return the string
     */
    private String filterApkPath(final Process p, final String packageName) {
        String retout = null;
        try {
            final java.io.BufferedReader standardIn = new java.io.BufferedReader(
                    new java.io.InputStreamReader(p.getInputStream()));
            new java.io.BufferedReader(new java.io.InputStreamReader(
                    p.getErrorStream()));
            String line = "";
            while ((line = standardIn.readLine()) != null) {
                if (line.contains(packageName)) {
                    retout = line;
                    break;

                }
            }

        } catch (final IOException e) {
            e.printStackTrace();
        }
        return retout;
    }

    /**
     * Calculate event calls.
     * 
     * @param eventsLogs
     *            the events logs
     * @param packageName
     *            the package name
     */
    private void calculateEventCalls(final String eventsLogs,
            final String packageName) {
        final String line = eventsLogs;
        if (line.contains("am_proc_start")) {
            isAppFirstTimeLaunch = true;
        } else if (line.contains("am_on_resume_called"))
        // In Samsung event logs are different it
        // appends [] example
        // [0,com.android.email.activity.setup.AccountSetupType]
        // in Other device logs are :
        // 0,com.android.email.activity.setup.AccountSetupType
        {
            if (line.contains("]")) {
                filterActivityName(line, ".", "]");
            } else {
                final int startIndex = line.lastIndexOf(".");
                mActivityName = line.substring(startIndex + 1);
            }
            execGCCommand(true);
        } else if (line.contains("am_pause_activity")) {
            filterActivityName(line, ".", "]");
            if (mActivityName.equalsIgnoreCase(mPreviousActivityName)) {
                mActivityTimer.cancel();
            }
            execGCCommand(true);
        } else if (line.contains("menu_item_selected")) {
            execGCCommand(true);
        } else if (line.contains("am_finish_activity")) {
            filterActivityName(line, ".", ",");
            execGCCommand(true);
        } else if (line.contains("activity_launch_time")) {
            mActivityTimer = new Timer();
            mActivityTimer.schedule(new TimerTask() {
                @Override
                public void run() {
                    mPreviousActivityName = mActivityName;
                    captureScreenShot();
                    getOverdraws(true, isDeviceModelNexus());
                }
            }, OVERDRAW_DELAY, OVERDRAW_OCCUR_TIME);
            filterLaunchInfo(line);
        }

    }

    /**
     * Calculate GC calls.
     * 
     * @param GCLogs
     *            the gC logs
     * @param isActivity
     *            the is activity
     */
    private void calculateGCCalls(final String GCLogs, final boolean isActivity) {
        int noOfGCForAlloc = 0;
        int noOfGCForConcurrent = 0;
        for (final String line : GCLogs.split("\\n")) {
            if (line.contains("GC_FOR_ALLOC") || line.contains("GC_FOR_MALLOC")) {
                noOfGCForAlloc++;
            } else if (GCLogs.contains("GC_CONCURRENT")) {
                noOfGCForConcurrent++;
            }
        }
        // When key(activity name) is already exists sum up the total value and
        // store
        String viewName = null;
        if (isActivity) {
            viewName = mActivityName;
        } else {
            viewName = mFragmentName;
        }
        // When key(activity name) is already exists sum up the total value and
        // store
        if (mGCData.containsKey(viewName.toString())) {
            final int gcValuse[] = mGCData.get(viewName);
            noOfGCForConcurrent = gcValuse[1] + noOfGCForConcurrent;
            noOfGCForAlloc = gcValuse[0] + noOfGCForAlloc;
        }
        mGCData.put(viewName.toString(), new int[] { noOfGCForAlloc,
                noOfGCForConcurrent });
        // reset the data
        noOfGCForAlloc = 0;
        noOfGCForConcurrent = 0;
    }

    /**
     * Stop profiling. This function will consolidate all the profiling data and
     * prepare the new map and also perform the grading
     */
    public void stopProfiling() {
        // Consolidate all the Profiling data and sets the profiling map
        setProfilingData();
        // Gets the App rater instance
        final AppRater appRater = AppRater.getInstance();
        for (final Map.Entry<String, ProfilingData> profileData : profilingItems
                .entrySet()) {
            // Calculate the grades
            appRater.calculateGrades(profileData.getKey(),
                    profileData.getValue().mLaunchTime,
                    profileData.getValue().mActivityStackCount,
                    profileData.getValue().mNoOfOverdraws,
                    profileData.getValue().mGCCalls[1]
                            + profileData.getValue().mGCCalls[0]);
        }

        // Disable overdraws
        final String[] DISABLE_OVERDRAW = new String[] { mADBPath, "shell",
                "setprop", "debug.hwui.overdraw", "false" };
        exec(DISABLE_OVERDRAW);
        // Remove the tesseract softlinks
        removeTesseractPaths();
        // Remove the Instrumentation directory and generated Jars from working
        // directory
        removeInstrumentationFolder();
    }

    /**
     * Removes the tesseract paths.
     */
    private void removeTesseractPaths() {
        String[] cmd = null;
        // removing the installed tesseract setup
        switch (Utils.getOperatingSystemType()) {
            case Windows:
                break;
            case Linux:
                cmd = new String[] {
                        "sh",
                        Utils.getFullPath("Tesseract/Linux/tesseract_remove.sh") };
                exec(cmd);
                break;
            case MacOS:
                cmd = new String[] { "sh",
                        Utils.getFullPath("Tesseract/Mac/tesseract_remove.sh") };
                exec(cmd);
                break;
            default:
                break;
        }

    }

    /**
     * Sets the profiling data.
     */
    public void setProfilingData() {

        String activityName = null;
        long launchTime = 0;
        String noOfOverDraws = null;
        final String activityStackLog = exceActivityStackCommand(mPackageName);

        for (final Map.Entry<String, Long> entry : mActivitiesData.entrySet()) {
            int numberOfActivitiesInStack = 0;

            /* Activity name */
            activityName = entry.getKey().toString().trim();

            /* Gets the launch time */
            launchTime = mActivitiesData.get(activityName);

            /* Gets the GC data using activity name as key */
            if (mGCData.get(activityName) == null) {
                mGCData.put(activityName, new int[] { 0, 0 });

            }

            /* Gets the overdraw data using activity name key */
            noOfOverDraws = mOverDrawData.get(activityName);
            if (noOfOverDraws == null || noOfOverDraws.isEmpty()) {
                noOfOverDraws = "Not Available";
            }

            /*
             * Gets the activity stack to find the number of activities present
             * in the stack
             */
            if (activityStackLog.length() > 1) {
                for (String v : activityStackLog.split("Run #")) {
                    final int newStartIndex = v.lastIndexOf(".");
                    v = v.substring(newStartIndex + 1);
                    if (v.contains(activityName)) {
                        numberOfActivitiesInStack++;
                    }
                }
            }

            profilingItems.put(activityName, new ProfilingData(launchTime,
                    mGCData.get(activityName), numberOfActivitiesInStack,
                    noOfOverDraws));

        }

    }

    /**
     * Checks if is device model nexus.
     * 
     * @return true, if is device model nexus
     */
    private boolean isDeviceModelNexus() {
        final Process p = customExec(new String[] { mADBPath, "shell",
                "getprop", "ro.product.model" });
        final String retoutput = processOutput(p);
        if (retoutput.contains("Nexus 5") || retoutput.contains("Nexus 4")) {
            setDeviceDensity();
            return true;
        } else {
            return false;
        }
    }

    /**
     * Sets the device density.
     */
    private void setDeviceDensity() {
        final Process p = customExec(new String[] { mADBPath, "shell",
                "getprop", "ro.sf.lcd_density" });
        String retoutput = processOutput(p);
        retoutput = retoutput.trim();
        if (retoutput.equals("320")) {
            Utils.setDeviceDensity(Utils.DeviceDensity.MDPI);
        } else if (retoutput.equals("480")) {
            Utils.setDeviceDensity(Utils.DeviceDensity.HDPI);
        } else {
            Utils.setDeviceDensity(Utils.DeviceDensity.LDPI);
        }
    }

    /**
     * Removes the instrumentation folder.
     */
    private void removeInstrumentationFolder() {
        removeDirectory(mDirInstrumentation);
        final String workingDirectory = System.getProperty("user.dir");
        final File f = new File(workingDirectory + "/out.jar");
        final File f1 = new File(workingDirectory + "/profiling_dex2jar.jar");
        f1.delete();
        f.delete();

    }

    /**
     * Removes the directory.
     * 
     * @param dir
     *            the directory
     */
    private void removeDirectory(final File dir) {
        if (dir != null) {
            for (final File file : dir.listFiles()) {
                if (file.isDirectory()) {
                    removeDirectory(file);
                }
                file.delete();
            }
            dir.delete();
        }
    }

    /**
     * The Class ProfilingData.
     */
    public class ProfilingData {

        /** The m launch time. */
        private final long mLaunchTime;

        /** The m gc calls. */
        private final int[] mGCCalls;

        /** The m activity stack count. */
        private final int mActivityStackCount;

        /** The m no of overdraws. */
        private final String mNoOfOverdraws;

        /**
         * Instantiates a new profiling data.
         * 
         * @param launchTime
         *            the launch time
         * @param gcCalls
         *            the gc calls
         * @param activityStackCount
         *            the activity stack count
         * @param noOfOverdraws
         *            the no of overdraws
         */
        ProfilingData(final long launchTime, final int[] gcCalls,
                final int activityStackCount, final String noOfOverdraws) {
            mLaunchTime = launchTime;
            mGCCalls = gcCalls;
            mActivityStackCount = activityStackCount;
            mNoOfOverdraws = noOfOverdraws;
        }

        /**
         * Gets the launch time.
         * 
         * @return the launch time
         */
        public long getLaunchTime() {
            return mLaunchTime;
        }

        /**
         * Gets the GC calls.
         * 
         * @return the GC calls
         */
        public int[] getGCCalls() {
            return mGCCalls;
        }

        /**
         * Gets the activity stack count.
         * 
         * @return the activity stack count
         */
        public int getActivityStackCount() {
            return mActivityStackCount;
        }

        /**
         * Gets the over draws.
         * 
         * @return the over draws
         */
        public String getOverDraws() {
            return mNoOfOverdraws;
        }
    }
}
