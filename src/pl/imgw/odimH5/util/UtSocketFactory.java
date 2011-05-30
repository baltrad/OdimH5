/**
 * (C) 2011 INSTITUT OF METEOROLOGY AND WATER MANAGEMENT
 */
package pl.imgw.odimH5.util;

import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;

import org.apache.commons.net.DefaultSocketFactory;

/**
 * 
 * new socketfactory helps to avoid long waiting for response from unavailable server
 * 
 * 
 * @author B.R.
 * 
 */

public class UtSocketFactory extends DefaultSocketFactory {
    private int connectTimeout = 0;

    /**
             * 
             */
    public UtSocketFactory() {
        super();
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.apache.commons.net.SocketFactory#createSocket(java.lang.String,
     * int)
     */
    public Socket createSocket(String host, int port)
            throws UnknownHostException, IOException {
        Socket s = new java.net.Socket();
        java.net.InetSocketAddress addr = new java.net.InetSocketAddress(host,
                port);
        s.connect(addr, connectTimeout);
        return s;
    }

    /**
     * @return
     */
    public int getConnectTimeout() {
        return connectTimeout;
    }

    /**
     * @param i
     */
    public void setConnectTimeout(int i) {
        connectTimeout = i;
    }
}
