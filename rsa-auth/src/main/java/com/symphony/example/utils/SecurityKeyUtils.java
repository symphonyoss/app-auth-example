/*
 * Copyright 2016-2017 Symphony Application Authentication - Symphony LLC
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.symphony.example.utils;


import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Scanner;

import static org.apache.commons.codec.binary.Base64.decodeBase64;

/**
 * Utility class for creating Java security objects from PEM strings.
 */
public class SecurityKeyUtils {

    private static final String PEM_PUBLIC_START = "-----BEGIN PUBLIC KEY-----";
    private static final String PEM_PUBLIC_END = "-----END PUBLIC KEY-----";

    // PKCS#8 format
    private static final String PEM_PRIVATE_START = "-----BEGIN PRIVATE KEY-----";
    private static final String PEM_PRIVATE_END = "-----END PRIVATE KEY-----";

    // PKCS#1 format
    private static final String PEM_RSA_PRIVATE_START = "-----BEGIN RSA PRIVATE KEY-----";
    private static final String PEM_RSA_PRIVATE_END = "-----END RSA PRIVATE KEY-----";

    /**
     * Create a RSA Private Ket from a PEM String. It supports PKCS#1 and PKCS#8 string formats
     *
     * @param pemPrivateKey RSA private key in PEM format
     * @return private key object
     */
    public static PrivateKey parseRSAPrivateKey(String pemPrivateKey)
            throws GeneralSecurityException {

        if (pemPrivateKey.contains(PEM_RSA_PRIVATE_START)) {
            // OpenSSL / PKCS#1 Base64 PEM encoded file
            pemPrivateKey = pemPrivateKey.replace(PEM_RSA_PRIVATE_START, "");
            pemPrivateKey = pemPrivateKey.replace(PEM_RSA_PRIVATE_END, "");
            return readPkcs1PrivateKey(decodeBase64(pemPrivateKey));
        }

        if (pemPrivateKey.contains(PEM_PRIVATE_START)) {
            // PKCS#8 Base64 PEM encoded file
            pemPrivateKey = pemPrivateKey.replace(PEM_PRIVATE_START, "");
            pemPrivateKey = pemPrivateKey.replace(PEM_PRIVATE_END, "");
            return readPkcs8PrivateKey(decodeBase64(pemPrivateKey));
        }

        throw new GeneralSecurityException("Not valid private key.");
    }

    /**
     * Parse an X509 Cert from a PEM string
     *
     * @param certificateString PEM format
     * @return X.509 certificate object
     */
    public static X509Certificate parseX509Certificate(final String certificateString)
            throws GeneralSecurityException {
        CertificateFactory f = CertificateFactory.getInstance("X.509");
        X509Certificate certificate = (X509Certificate) f
                .generateCertificate(new ByteArrayInputStream(certificateString.getBytes(StandardCharsets.UTF_8)));
        return certificate;

    }


    /**
     * Creates a RSA Public Key from a PEM String
     *
     * @param pemPublicKey public key in PEM format
     * @return a RSA public key
     */
    public static PublicKey parseRSAPublicKey(final String pemPublicKey)
            throws GeneralSecurityException {
        try {
            String publicKeyString = pemPublicKey
                    .replace(PEM_PUBLIC_START, "")
                    .replace(PEM_PUBLIC_END, "")
                    .replaceAll("\\s", "");
            byte[] keyBytes = decodeBase64(publicKeyString.getBytes(StandardCharsets.UTF_8));
            X509EncodedKeySpec spec = new X509EncodedKeySpec(keyBytes);
            KeyFactory keyFactory = KeyFactory.getInstance("RSA");
            return keyFactory.generatePublic(spec);

        } catch (InvalidKeySpecException | NoSuchAlgorithmException e) {
            throw new GeneralSecurityException(e);
        }
    }

    public static String readPemFromFile(String pemFilename) {
        try (Scanner scanner = new Scanner(new File(pemFilename))) {
            String pem = scanner.useDelimiter("\\A").next();
            return pem;
        } catch (FileNotFoundException e) {
            throw new RuntimeException("Error reading private key PEM file [ " + pemFilename + "]", e);
        }
    }

    private static PrivateKey readPkcs8PrivateKey(byte[] pkcs8Bytes) throws GeneralSecurityException {
        KeyFactory keyFactory = KeyFactory.getInstance("RSA", "SunRsaSign");
        PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(pkcs8Bytes);
        try {
            return keyFactory.generatePrivate(keySpec);
        } catch (InvalidKeySpecException e) {
            throw new IllegalArgumentException("Unexpected key format!", e);
        }
    }

    private static PrivateKey readPkcs1PrivateKey(byte[] pkcs1Bytes) throws GeneralSecurityException {
        // We can't use Java internal APIs to parse ASN.1 structures, so we build a PKCS#8 key Java can understand
        int pkcs1Length = pkcs1Bytes.length;
        int totalLength = pkcs1Length + 22;
        byte[] pkcs8Header = new byte[]{
                0x30, (byte) 0x82, (byte) ((totalLength >> 8) & 0xff), (byte) (totalLength & 0xff), // Sequence + total length
                0x2, 0x1, 0x0, // Integer (0)
                0x30, 0xD, 0x6, 0x9, 0x2A, (byte) 0x86, 0x48, (byte) 0x86, (byte) 0xF7, 0xD, 0x1, 0x1, 0x1, 0x5, 0x0, // Sequence: 1.2.840.113549.1.1.1, NULL
                0x4, (byte) 0x82, (byte) ((pkcs1Length >> 8) & 0xff), (byte) (pkcs1Length & 0xff) // Octet string + length
        };
        byte[] pkcs8bytes = join(pkcs8Header, pkcs1Bytes);
        return readPkcs8PrivateKey(pkcs8bytes);
    }

    private static byte[] join(byte[] byteArray1, byte[] byteArray2) {
        byte[] bytes = new byte[byteArray1.length + byteArray2.length];
        System.arraycopy(byteArray1, 0, bytes, 0, byteArray1.length);
        System.arraycopy(byteArray2, 0, bytes, byteArray1.length, byteArray2.length);
        return bytes;
    }

}
