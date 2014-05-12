/*******************************************************************************
 * Copyright 2014 Asha
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *       http://www.apache.org/licenses/LICENSE-2.0
 * Owner : Asha - initial API and implementation
 * Project Name : Lib_PerformanceMonitoring
 * FileName :SignatureInfo
 ******************************************************************************/
package com.imaginea.instrumentation;

/**
 * The Class SignatureInfo.
 */
public class SignatureInfo {

    /** The keystore location. */
    String keystoreLocation;

    /** The keystore password. */
    String keystorePassword;

    /** The key alias. */
    String keyAlias;

    /** The key password. */
    String keyPassword;

    /**
     * Instantiates a new signature info.
     * 
     * @param aKeyStoreLocation
     *            the param string1
     * @param aKeyStorePassword
     *            the param string2
     * @param aKeyAlias
     *            the param string3
     * @param aKeyPassword
     *            the param string4
     */
    public SignatureInfo(final String aKeyStoreLocation, final String aKeyStorePassword,
            final String aKeyAlias, final String aKeyPassword) {
        keystoreLocation = aKeyStoreLocation;
        keystorePassword = aKeyStorePassword;
        keyAlias = aKeyAlias;
        keyPassword = aKeyPassword;
    }
}
