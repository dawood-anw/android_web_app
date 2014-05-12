/*******************************************************************************
 * Copyright 2014 Asha
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *       http://www.apache.org/licenses/LICENSE-2.0
 * Owner : Asha - initial API and implementation
 * Project Name : Lib_PerformanceMonitoring
 * FileName :InstrumentationException
 ******************************************************************************/
package com.imaginea.instrumentation;

/**
 * The Class InstrumentationException.
 */
public class InstrumentationException extends Exception {

    /** The Constant serialVersionUID. */
    private static final long serialVersionUID = 1L;

    /**
     * Instantiates a new instrumentation exception.
     * 
     * @param aException
     *            the param exception
     */
    public InstrumentationException(final Exception aException) {
        super(aException);
    }

    /**
     * Instantiates a new instrumentation exception.
     * 
     * @param aString
     *            the @param string
     */
    public InstrumentationException(final String aString) {
        super(aString);
    }

    /**
     * Instantiates a new instrumentation exception.
     * 
     * @param aString1
     *            the @param string1
     * @param aString2
     *            the @param string2
     */
    public InstrumentationException(final String aString1,
            final String aString2) {
        super(aString2);
    }
}
