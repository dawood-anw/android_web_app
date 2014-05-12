/*******************************************************************************
 * Copyright 2014 Asha
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *       http://www.apache.org/licenses/LICENSE-2.0
 * Owner : Asha - initial API and implementation
 * Project Name : Lib_PerformanceMonitoring
 * FileName :SignedJarBuilder
 ******************************************************************************/
package com.imaginea.instrumentation;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.security.DigestOutputStream;
import java.security.GeneralSecurityException;
import java.security.KeyStore;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.Signature;
import java.security.SignatureException;
import java.security.cert.X509Certificate;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.jar.Attributes;
import java.util.jar.JarEntry;
import java.util.jar.JarOutputStream;
import java.util.jar.Manifest;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import sun.misc.BASE64Encoder;
import sun.security.pkcs.ContentInfo;
import sun.security.pkcs.PKCS7;
import sun.security.pkcs.SignerInfo;
import sun.security.x509.AlgorithmId;
import sun.security.x509.X500Name;

/**
 * The Class SignedJarBuilder.
 */
public class SignedJarBuilder {

    /**
     * The Class SignatureOutputStream.
     */
    private static class SignatureOutputStream extends FilterOutputStream {

        /** The m signature. */
        private final Signature mSignature;

        /** The m count. */
        private int mCount = 0;

        /**
         * Instantiates a new signature output stream.
         * 
         * @param aOutputStream
         *            the param output stream
         * @param aSignature
         *            the param signature
         */
        public SignatureOutputStream(final OutputStream aOutputStream,
                final Signature aSignature) {
            super(aOutputStream);
            mSignature = aSignature;
        }

        /**
         * Size.
         * 
         * @return the int
         */
        public int size() {
            return mCount;
        }

        /*
         * (non-Javadoc)
         * 
         * @see java.io.FilterOutputStream#write(byte[], int, int)
         */
        @Override
        public void write(final byte[] array, final int aData1,
                final int aData2) throws IOException {
            try {
                mSignature.update(array, aData1, aData2);
            } catch (final SignatureException localSignatureException) {
                throw new IOException("SignatureException: "
                        + localSignatureException);
            }
            super.write(array, aData1, aData2);
            mCount += aData2;
        }

        /*
         * (non-Javadoc)
         * 
         * @see java.io.FilterOutputStream#write(int)
         */
        @Override
        public void write(final int data) throws IOException {
            try {
                mSignature.update((byte) data);
            } catch (final SignatureException localSignatureException) {
                throw new IOException("SignatureException: "
                        + localSignatureException);
            }
            super.write(data);
            mCount += 1;
        }
    }

    /** The m output jar. */
    private final JarOutputStream mOutputJar;

    /** The m manifest. */
    private final Manifest mManifest;

    /** The m base64 encoder. */
    private final BASE64Encoder mBase64Encoder;

    /** The m message digest. */
    private final MessageDigest mMessageDigest;

    /** The m buffer. */
    private final byte[] mBuffer = new byte[4096];

    /** The m key entries. */
    private final KeyStore.PrivateKeyEntry[] mKeyEntries;

    /** The m aliases. */
    private final String[] mAliases;

    /**
     * Instantiates a new signed jar builder.
     * 
     * @param outputFile
     *            the param file output stream
     * @param keyEntries
     *            the param array of private key entry
     * @param aAliases
     *            the param array of string
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     * @throws NoSuchAlgorithmException
     *             the no such algorithm exception
     */
    public SignedJarBuilder(final FileOutputStream outputFile,
            final KeyStore.PrivateKeyEntry[] keyEntries,
            final String[] aAliases) throws IOException,
            NoSuchAlgorithmException {
        mOutputJar = new JarOutputStream(outputFile);
        mOutputJar.setLevel(9);
        mKeyEntries = keyEntries;
        mAliases = aAliases;
        mManifest = new Manifest();
        final Attributes localAttributes = mManifest.getMainAttributes();
        localAttributes.putValue("Manifest-Version", "1.0");
        localAttributes.putValue("Created-By", "1.0 (Android)");
        mBase64Encoder = new BASE64Encoder();
        mMessageDigest = MessageDigest.getInstance("SHA1");
    }

    /**
     * Close.
     * 
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     * @throws GeneralSecurityException
     *             the general security exception
     */
    public void close() throws IOException, GeneralSecurityException {
        if (mManifest != null) {
            mOutputJar.putNextEntry(new JarEntry("META-INF/MANIFEST.MF"));
            mManifest.write(mOutputJar);
            for (int i = 0; i < mKeyEntries.length; i++) {
                final PrivateKey localPrivateKey = mKeyEntries[i]
                        .getPrivateKey();
                final Signature localSignature = Signature
                        .getInstance("SHA1with"
                                + localPrivateKey.getAlgorithm());
                localSignature.initSign(localPrivateKey);
                mOutputJar.putNextEntry(new JarEntry("META-INF/" + mAliases[i]
                        + ".SF"));
                writeSignatureFile(new SignatureOutputStream(mOutputJar,
                        localSignature));
                mOutputJar.putNextEntry(new JarEntry("META-INF/" + mAliases[i]
                        + "." + localPrivateKey.getAlgorithm()));
                writeSignatureBlock(localSignature,
                        (X509Certificate) mKeyEntries[i].getCertificate(),
                        localPrivateKey);
            }
        }
        mOutputJar.close();
    }

    /**
     * Write entry.
     * 
     * @param inputStream
     *            the param input stream
     * @param jarEntry
     *            the param jar entry
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    private void writeEntry(final InputStream inputStream,
            final JarEntry jarEntry) throws IOException {
        mOutputJar.putNextEntry(jarEntry);
        int i;
        while ((i = inputStream.read(mBuffer)) != -1) {
            mOutputJar.write(mBuffer, 0, i);
            if (mMessageDigest != null) {
                mMessageDigest.update(mBuffer, 0, i);
            }
        }
        mOutputJar.closeEntry();
        if (mManifest != null) {
            Attributes localAttributes = mManifest.getAttributes(jarEntry
                    .getName());
            if (localAttributes == null) {
                localAttributes = new Attributes();
                mManifest.getEntries().put(jarEntry.getName(),
                        localAttributes);
            }
            localAttributes.putValue("SHA1-Digest",
                    mBase64Encoder.encode(mMessageDigest.digest()));
        }
    }

    /**
     * Write file.
     * 
     * @param paramFile
     *            the param file
     * @param paramString
     *            the param string
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    public void writeFile(final File paramFile, final String paramString)
            throws IOException {
        final FileInputStream localFileInputStream = new FileInputStream(
                paramFile);
        try {
            final JarEntry localJarEntry = new JarEntry(paramString);
            localJarEntry.setTime(paramFile.lastModified());
            writeEntry(localFileInputStream, localJarEntry);
        } finally {
            localFileInputStream.close();
        }
    }

    /**
     * Write signature block.
     * 
     * @param paramSignature
     *            the param signature
     * @param paramX509Certificate
     *            the param x509 certificate
     * @param paramPrivateKey
     *            the param private key
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     * @throws GeneralSecurityException
     *             the general security exception
     */
    private void writeSignatureBlock(final Signature paramSignature,
            final X509Certificate paramX509Certificate,
            final PrivateKey paramPrivateKey) throws IOException,
            GeneralSecurityException {
        final SignerInfo localSignerInfo = new SignerInfo(new X500Name(
                paramX509Certificate.getIssuerX500Principal().getName()),
                paramX509Certificate.getSerialNumber(),
                AlgorithmId.get("SHA1"), AlgorithmId.get(paramPrivateKey
                        .getAlgorithm()), paramSignature.sign());
        final PKCS7 localPKCS7 = new PKCS7(
                new AlgorithmId[] { AlgorithmId.get("SHA1") }, new ContentInfo(
                        ContentInfo.DATA_OID, null),
                new X509Certificate[] { paramX509Certificate },
                new SignerInfo[] { localSignerInfo });
        localPKCS7.encodeSignedData(mOutputJar);
    }

    /**
     * Write signature file.
     * 
     * @param paramSignatureOutputStream
     *            the param signature output stream
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     * @throws GeneralSecurityException
     *             the general security exception
     */
    @SuppressWarnings("rawtypes")
    private void writeSignatureFile(
            final SignatureOutputStream paramSignatureOutputStream)
            throws IOException, GeneralSecurityException {
        final Manifest localManifest = new Manifest();
        final Attributes localAttributes = localManifest.getMainAttributes();
        localAttributes.putValue("Signature-Version", "1.0");
        localAttributes.putValue("Created-By", "1.0 (Android + Imaginea)");
        final BASE64Encoder localBASE64Encoder = new BASE64Encoder();
        final MessageDigest localMessageDigest = MessageDigest
                .getInstance("SHA1");
        final PrintStream localPrintStream = new PrintStream(
                new DigestOutputStream(new ByteArrayOutputStream(),
                        localMessageDigest), true, "UTF-8");
        mManifest.write(localPrintStream);
        localPrintStream.flush();
        localAttributes.putValue("SHA1-Digest-Manifest",
                localBASE64Encoder.encode(localMessageDigest.digest()));
        final Map localMap = mManifest.getEntries();
        final Iterator localIterator1 = localMap.entrySet().iterator();
        while (localIterator1.hasNext()) {
            final Map.Entry localEntry = (Map.Entry) localIterator1.next();
            localPrintStream.print("Name: " + (String) localEntry.getKey()
                    + "\r\n");
            final Iterator localIterator2 = ((Attributes) localEntry.getValue())
                    .entrySet().iterator();
            while (localIterator2.hasNext()) {
                final Entry localObject = (Map.Entry) localIterator2.next();
                localPrintStream.print(localObject.getKey() + ": "
                        + localObject.getValue() + "\r\n");
            }
            localPrintStream.print("\r\n");
            localPrintStream.flush();
            final Object localObject = new Attributes();
            ((Attributes) localObject).putValue("SHA1-Digest",
                    localBASE64Encoder.encode(localMessageDigest.digest()));
            localManifest.getEntries().put((String) localEntry.getKey(),
                    (Attributes) localObject);
        }
        localManifest.write(paramSignatureOutputStream);
        if (paramSignatureOutputStream.size() % 1024 == 0) {
            paramSignatureOutputStream.write(13);
            paramSignatureOutputStream.write(10);
        }
    }

    /**
     * Write zip.
     * 
     * @param paramInputStream
     *            the param input stream
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    public void writeZip(final InputStream paramInputStream) throws IOException {
        final ZipInputStream localZipInputStream = new ZipInputStream(
                paramInputStream);
        try {
            ZipEntry localZipEntry;
            while ((localZipEntry = localZipInputStream.getNextEntry()) != null) {
                final String str = localZipEntry.getName();
                if (!localZipEntry.isDirectory()
                        && !str.startsWith("META-INF/")) {
                    JarEntry localJarEntry;
                    if (localZipEntry.getMethod() == 0) {
                        localJarEntry = new JarEntry(localZipEntry);
                    } else {
                        localJarEntry = new JarEntry(str);
                    }
                    writeEntry(localZipInputStream, localJarEntry);
                    localZipInputStream.closeEntry();
                }
            }
        } finally {
            localZipInputStream.close();
        }
    }
}
