/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package pl.imgw.odimH5.util;

import pl.imgw.odimH5.net.RequestFactory;
import pl.imgw.odimH5.net.DefaultRequestFactory;
import pl.imgw.odimH5.net.Authenticator;
import pl.imgw.odimH5.net.KeyczarAuthenticator;
import pl.imgw.odimH5.net.util.IHttpClientUtil;
import pl.imgw.odimH5.net.util.HttpClientUtil;

import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.HttpResponse;

import java.net.URI;

import java.io.File;
import java.io.InputStream;
import java.io.FileInputStream;

/**
 * Feeds data file to BALTRAD.
 * @author szewczenko
 */
public class BaltradFeeder {

    private String serverAddress;
    private InitAppUtil init;
    private File f;
    
    private int status;
    private String msg;
    
    public BaltradFeeder(String serverAddress, InitAppUtil init, File f) {
        this.serverAddress = serverAddress;
        this.init = init;
        this.f = f;
    }
    
    public void feedToBaltrad() {
        RequestFactory requestFactory = new DefaultRequestFactory(
                    URI.create(serverAddress));
        IHttpClientUtil httpClient = new HttpClientUtil(
                init.getConnTimeout(), init.getSoTimeout());
        Authenticator auth = new KeyczarAuthenticator(init.getKeystoreDir());
        try {
            InputStream is = null;
            try {
                is = new FileInputStream(f);
                HttpUriRequest request = requestFactory.createPostFileRequest(
                    init.getHostName(), init.getHostAddress(),
                    new FileInputStream(f));
                auth.addCredentials(request, init.getHostName());
                HttpResponse response = httpClient.post(request);
                // Server status code 
                status = response.getStatusLine().getStatusCode();
            } finally {
                is.close();
            }
        } catch (Exception e) {
            // Internal error
            status = 1; 
        }
    }
    
    public String getMessage() {
        switch (status) {
            case 200: msg = "OK. File " + f.getName() + " sent to " +
                    serverAddress;
                break;
            case 401: msg = "Failed to send file " + f.getName() + " to " +
                    serverAddress + ": Failed to authenticate message.";
                break;
            case 404: msg = "Failed to send file " + f.getName() + " to " +
                    serverAddress + ": Generic server error.";
                break;
            case 409: msg = "Failed to send file " + f.getName() + " to " +
                    serverAddress + ": Duplicate entry error.";
                break;
            case 500: msg = "Failed to send file " + f.getName() + " to " +
                    serverAddress + ": Internal server error.";
                break;
            case 1: msg = "Failed to send file " + f.getName() + " to " +
                    serverAddress + ": I/O exception.";
                break;
            default: msg = "Failed to send file " + f.getName() + " to " +
                    serverAddress + ": Generic error.";
                break;
        }
        return msg;
    }
    
}
