/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package pl.imgw.odimH5.net.util;

import org.apache.http.HttpVersion;
import org.apache.http.client.HttpClient;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpProtocolParams;
import org.apache.http.params.HttpParams;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;

import java.security.SecureRandom;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;

import java.io.IOException;

/**
 *
 * @author szewczenko
 */
public class HttpClientUtil implements IHttpClientUtil {
    
    /** Maximum number of connections */
    private static final int MAX_TOTAL_CONNS = 200;
    /** Maximum number of connections per route */
    private static final int MAX_PER_ROUTE_CONNS = 20;
    
    private HttpClient client;

    public HttpClientUtil(int connTimeout, int soTimeout) {
        SchemeRegistry schemeRegistry = new SchemeRegistry();
        registerHttpScheme(schemeRegistry);
        registerHttpsScheme(schemeRegistry);
        ThreadSafeClientConnManager connMgr = new ThreadSafeClientConnManager(
                schemeRegistry);
        connMgr.setMaxTotal(MAX_TOTAL_CONNS);
        connMgr.setDefaultMaxPerRoute(MAX_PER_ROUTE_CONNS);
        client = new DefaultHttpClient(connMgr);
        HttpParams httpParams = client.getParams();
        HttpConnectionParams.setConnectionTimeout(httpParams, connTimeout);
        HttpConnectionParams.setSoTimeout(httpParams, soTimeout);
        HttpProtocolParams.setVersion(httpParams, HttpVersion.HTTP_1_1);
    }
    
    
    public HttpResponse post(HttpUriRequest request) throws IOException, 
            Exception {
        HttpResponse response = null;
        try {
            response = client.execute(request);
        } catch (IOException e) {
            throw e;
        } catch (Exception e) {
            throw e;
        }
        return response;
    }
    
    
    /**
     * Registers HTTP scheme.
     * 
     * @param schemeReg Scheme registry
     */
    private void registerHttpScheme(SchemeRegistry schemeReg) {
        Scheme http = new Scheme("http", 80, new PlainSocketFactory());
        schemeReg.register(http);
    }
    /**
     * Registers HTTPS scheme.
     * 
     * @param schemeReg Scheme registry
     */
    private void registerHttpsScheme(SchemeRegistry schemeReg) {
        try {
            SSLContext sslContext = SSLContext.getInstance("SSL");
            sslContext.init(
                null,
                new TrustManager[] {
                    new EasyX509TrustManager()
                },
                new SecureRandom()
            );
            Scheme https = new Scheme("https", 443, new SSLSocketFactory(
                sslContext, SSLSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER));
            schemeReg.register(https);
        } catch (Exception e) {
            throw new RuntimeException("Failed to register https scheme", e);
        }
    } 
    
}
