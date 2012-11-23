/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package pl.imgw.odimH5.net.util;

import javax.net.ssl.X509TrustManager;
import java.security.cert.X509Certificate;
import java.security.cert.CertificateException;

/**
 *
 * @author szewczenko
 */
public class EasyX509TrustManager implements X509TrustManager {
    @Override
    public void checkClientTrusted( X509Certificate[] chain, String authType)
            throws CertificateException {
        // Oh, I am easy!
    }
    @Override
    public void checkServerTrusted( X509Certificate[] chain, String authType)
            throws CertificateException {
        // Oh, I am easy!
    }
    @Override
    public X509Certificate[] getAcceptedIssuers() {
        return null;
    }
}
