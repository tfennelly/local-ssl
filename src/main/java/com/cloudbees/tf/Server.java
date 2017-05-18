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

import org.apache.http.HttpException;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.impl.bootstrap.HttpServer;
import org.apache.http.impl.bootstrap.ServerBootstrap;
import org.apache.http.protocol.HttpContext;
import org.apache.http.protocol.HttpRequestHandler;
import org.apache.http.ssl.SSLContexts;

import javax.net.ssl.KeyManager;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import java.io.IOException;
import java.net.Inet4Address;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;

/**
 * @author <a href="mailto:tom.fennelly@gmail.com">tom.fennelly@gmail.com</a>
 */
public class Server {
    
    private HttpServer server;

    private Server(HttpServer server) {
        this.server = server;
    }

    public static Server start() throws Exception {
        HttpServer server = ServerBootstrap.bootstrap()
                .setLocalAddress(Inet4Address.getByName("localhost"))
                .setSslContext(createServerSSLContext())
                .setSslSetupHandler(socket -> socket.setNeedClientAuth(true))
                .registerHandler("*", new HttpRequestHandler() {
                    @Override
                    public void handle(HttpRequest httpRequest, HttpResponse httpResponse, HttpContext httpContext) throws HttpException, IOException {
                        httpResponse.setStatusCode(HttpStatus.SC_OK);
                    }
                })
                .create();
        server.start();
        
        return new Server(server);
    }
    
    public String getUrl() {
        return server.getInetAddress().getHostName() + ":" + server.getLocalPort();
    }
    
    public void stop() {
        server.stop();
    }

    /**
     * Create an SSLContext for the server using the server's JKS. This instructs the server to
     * present its certificate when clients connect over HTTPS.
     */
    private static SSLContext createServerSSLContext() throws CertificateException, NoSuchAlgorithmException, KeyStoreException,
            IOException, UnrecoverableKeyException, KeyManagementException {

        // See genkeys.sh file
        KeyStore serverKeyStore = SSLUtil.getStore("./server_keystore.jks", "123123".toCharArray());
        KeyManager[] serverKeyManagers = SSLUtil.getKeyManagers(serverKeyStore, "123123".toCharArray());
        TrustManager[] serverTrustManagers = SSLUtil.getTrustManagers(serverKeyStore);

        SSLContext sslContext = SSLContexts.custom().useProtocol("TLS").build();
        sslContext.init(serverKeyManagers, serverTrustManagers, new SecureRandom());

        return sslContext;
    }
}
