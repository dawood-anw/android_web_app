/*******************************************************************************
 * Copyright 2014 Asha
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *       http://www.apache.org/licenses/LICENSE-2.0
 * Owner : Asha - initial API and implementation
 * Project Name : Lib_PerformanceMonitoring
 * FileName :ProcessWrapper
 ******************************************************************************/
package com.imaginea.instrumentation;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 * The Class ProcessWrapper.
 */
public class ProcessWrapper {

    /** The builder. */
    ProcessBuilder mBuilder;

    /**
     * Instantiates a new process wrapper.
     * 
     * @param aBuilder
     *            the @param process builder
     */
    public ProcessWrapper(final ProcessBuilder aBuilder) {
        mBuilder = aBuilder;
    }

    /**
     * Run.
     * 
     * @return true, if successful
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    public boolean run() throws IOException {
        BufferedReader localBufferedReader = null;
        try {
            mBuilder.redirectErrorStream(true);
            final Process process = mBuilder.start();
            localBufferedReader = new BufferedReader(new InputStreamReader(
                    process.getInputStream()));
            while (localBufferedReader.readLine() != null) {
                // System.out.println("ProcessWrapper " + str2);
            }
            process.waitFor();
            return true;
        } catch (final InterruptedException localInterruptedException) {
            System.out.println("ProcessWrapper Interrupted executing command "
                    + localInterruptedException);
            return false;
        } finally {
            if (localBufferedReader != null) {
                localBufferedReader.close();
            }
        }
    }
}
