/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package pl.imgw.odimH5.net.util;

import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.HttpResponse;

import java.io.IOException;

/**
 *
 * @author szewczenko
 */
public interface IHttpClientUtil {
    
    public HttpResponse post(HttpUriRequest request) throws IOException, 
            Exception;
    
}
