package com.mingyue;

import java.io.InputStream;
import java.security.KeyStore;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;

/**
 * 加载证书
 */
public class SSLContextFactory {

    public static SSLContext getSslContext() throws Exception {
        char[] passArray = "passwd_123".toCharArray();
        SSLContext sslContext = SSLContext.getInstance("TLSv1.2");
        KeyStore ks = KeyStore.getInstance("JKS");
        //加载 keytool 生成的文件
        InputStream inputStream = SSLContextFactory.class.getResourceAsStream("/local.jks");
        ks.load(inputStream, passArray);
        KeyManagerFactory kmf = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
        kmf.init(ks, passArray);
        sslContext.init(kmf.getKeyManagers(), null, null);
        inputStream.close();
        return sslContext;
    }
}