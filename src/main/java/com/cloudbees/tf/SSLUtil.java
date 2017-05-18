/*
 * The MIT License
 *
 * Copyright (c) 2017, CloudBees, Inc.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package com.cloudbees.tf;

import javax.net.ssl.KeyManager;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;

/**
 * @author <a href="mailto:tom.fennelly@gmail.com">tom.fennelly@gmail.com</a>
 */
public class SSLUtil {

    /**
     * KeyStores provide credentials, TrustStores verify credentials.
     * <p>
     * Server KeyStores stores the server's private keys, and certificates for corresponding public
     * keys. Used here for HTTPS connections over localhost.
     * <p>
     * Client TrustStores store servers' certificates.
     */
    public static KeyStore getStore(final String storeFileName, final char[] password) throws
            KeyStoreException, IOException, CertificateException, NoSuchAlgorithmException {
        final KeyStore store = KeyStore.getInstance("jks");
        InputStream inputStream = new FileInputStream(storeFileName);
        try {
            store.load(inputStream, password);
        } finally {
            inputStream.close();
        }

        return store;
    }

    /**
     * KeyManagers decide which authentication credentials (e.g. certs) should be sent to the remote
     * host for authentication during the SSL handshake.
     * <p>
     * Server KeyManagers use their private keys during the key exchange algorithm and send
     * certificates corresponding to their public keys to the clients. The certificate comes from
     * the KeyStore.
     */
    public static KeyManager[] getKeyManagers(KeyStore store, final char[] password) throws
            NoSuchAlgorithmException, UnrecoverableKeyException, KeyStoreException {
        KeyManagerFactory keyManagerFactory = KeyManagerFactory.getInstance(
                KeyManagerFactory.getDefaultAlgorithm());
        keyManagerFactory.init(store, password);

        return keyManagerFactory.getKeyManagers();
    }

    /**
     * TrustManagers determine if the remote connection should be trusted or not.
     * <p>
     * Clients will use certificates stored in their TrustStores to verify identities of servers.
     * Servers will use certificates stored in their TrustStores to verify identities of clients.
     */
    public static TrustManager[] getTrustManagers(KeyStore store) throws NoSuchAlgorithmException,
            KeyStoreException {
        TrustManagerFactory trustManagerFactory =
                TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
        trustManagerFactory.init(store);

        return trustManagerFactory.getTrustManagers();
    }    
}
