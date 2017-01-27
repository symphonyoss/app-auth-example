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

package com.symphony.symphony.client;

import feign.Client;
import feign.okhttp.OkHttpClient;

import javax.net.ssl.*;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.security.*;
import java.security.cert.CertificateException;
import java.util.Arrays;

/**
 * Builds OkHttpClient preconfigured for client certificate authentication.
 *
 * @author Dan Nathanson
 */
public class HttpClientBuilder {
    private SymphonyClientConfiguration symphonyClientConfiguration;

    public HttpClientBuilder(SymphonyClientConfiguration symphonyClientConfiguration) {
        this.symphonyClientConfiguration = symphonyClientConfiguration;
    }

    /**
     * Builds a HTTP Client for Feign which support client certificate authentication.
     */
    public Client buildClient() {
        //
        // Set up KeyManager (holds client cert)
        //
        KeyStore keyStore;
        try {
            keyStore = KeyStore.getInstance("PKCS12");

        } catch (KeyStoreException e) {
            throw new RuntimeException("Couldn't initialize KeyStore for PKCS12 ", e);
        }

        InputStream keyStoreFis;
        try {
            keyStoreFis = new FileInputStream(symphonyClientConfiguration.getKeystoreFilename());
        } catch (FileNotFoundException e) {
            throw new RuntimeException("Couldn't open keystore file: " + symphonyClientConfiguration.getKeystoreFilename(), e);
        }

        try {
            keyStore.load(keyStoreFis, symphonyClientConfiguration.getKeystorePassword().toCharArray());
        } catch (IOException | NoSuchAlgorithmException | CertificateException e) {
            throw new RuntimeException("Couldn't load keystore from file: " + symphonyClientConfiguration.getKeystoreFilename() +
                    ". Is the password correct?  Is it PKCS12 format?", e);
        }
        finally {
            try {
                keyStoreFis.close();
            } catch (IOException e) {
                // Not much we can do
            }
        }

        KeyManagerFactory kmf;
        try {
            kmf = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Couldn't create KeyManagerFactory with algorithm " + KeyManagerFactory.getDefaultAlgorithm(), e);
        }

        try {
            kmf.init(keyStore, symphonyClientConfiguration.getKeystorePassword().toCharArray());
        } catch (KeyStoreException | NoSuchAlgorithmException | UnrecoverableKeyException e) {
            throw new RuntimeException("Couldn't initialize KeyManagerFactory", e);
        }
        KeyManager[] keyManagers = kmf.getKeyManagers();


        //
        // Set up trust manager (for trusted server certs).  Can probably just use the standard cacerts file
        // that comes with JDK unless server is using self signed cert for SSL/TLS.
        //
        KeyStore trustStore;
        try {
            trustStore = KeyStore.getInstance(KeyStore.getDefaultType());
        } catch (KeyStoreException e) {
            throw new RuntimeException("Couldn't initialize trust store with type " + KeyStore.getDefaultType(), e);
        }

        InputStream trustStoreFis;
        try {
            trustStoreFis = new FileInputStream(symphonyClientConfiguration.getTruststoreFilename());
        } catch (FileNotFoundException e) {
            throw new RuntimeException("Couldn't open truststore file: " + symphonyClientConfiguration.getTruststoreFilename(), e);
        }

        try {
            trustStore.load(trustStoreFis, null);
        } catch (IOException | NoSuchAlgorithmException | CertificateException e) {
            throw new RuntimeException("Couldn't load truststore from file: " + symphonyClientConfiguration.getTruststoreFilename(), e);
        }
        finally {
            try {
                trustStoreFis.close();
            } catch (IOException e) {
                // Not much we can do
            }
        }

        TrustManagerFactory trustManagerFactory;
        try {
            trustManagerFactory = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Couldn't create TrustManagerFactory with algorithm " + TrustManagerFactory.getDefaultAlgorithm(), e);
        }

        try {
            trustManagerFactory.init(trustStore);
        } catch (KeyStoreException e) {
            throw new RuntimeException("Couldn't initialize TrustManagerFactory with truststore" , e);
        }

        TrustManager[] trustManagers = trustManagerFactory.getTrustManagers();
        if (trustManagers.length != 1 || !(trustManagers[0] instanceof X509TrustManager)) {
            throw new IllegalStateException("Unexpected default trust managers:" + Arrays.toString(trustManagers));
        }
        X509TrustManager trustManager = (X509TrustManager) trustManagers[0];

        SSLContext sslContext;
        try {
            sslContext = SSLContext.getInstance("TLS");
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Couldn't create SSLContext for TLS", e);
        }

        try {
            sslContext.init(keyManagers, trustManagers, null);
        } catch (KeyManagementException e) {
            throw new RuntimeException("Couldn't initialize SSLContext", e);
        }

        Client okHttpClient = new OkHttpClient(new okhttp3.OkHttpClient.Builder()
                .sslSocketFactory(sslContext.getSocketFactory(), trustManager)
                .build());

        return okHttpClient;
    }

}
