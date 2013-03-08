/**
 * (C) 2013 INSTITUT OF METEOROLOGY AND WATER MANAGEMENT
 */
package pl.imgw.odimH5.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.SocketException;

import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;

/**
 * 
 * /Class description/
 * 
 * 
 * @author <a href="mailto:lukasz.wojtas@imgw.pl">Lukasz Wojtas</a>
 * 
 */
public class FTPApacheHandler {

    FTPClient ftp = new FTPClient();
    String login;
    StringBuilder pass;
    String address;

    /**
     * @param address
     * @param login
     * @param pass
     * @throws IOException
     * @throws SocketException
     */
    public FTPApacheHandler(String address, String login, StringBuilder pass)
            throws SocketException, IOException {
        this.login = login;
        this.pass = pass;
        this.address = address;
        connect();
    }

    private void connect() throws SocketException, IOException {
        ftp.connect(address);
        ftp.setFileType(FTP.BINARY_FILE_TYPE, FTP.BINARY_FILE_TYPE);
        ftp.setFileTransferMode(FTP.BINARY_FILE_TYPE);
    }
    
    /**
     * @return
     * @throws IOException 
     */
    public boolean login() throws IOException {
        if(!ftp.isConnected()) {
            connect();
        }
        return ftp.login(login, pass.toString());
        
    }

    /**
     * @param remoteFolder
     * @param file
     * @return
     * @throws IOException 
     */
    public boolean sendFile(String remoteFolder, File file) throws IOException {
        if(!ftp.changeWorkingDirectory(remoteFolder)) {
            return false;
        }
        String tmp = file.getName() + ".tmp";
        FileInputStream fis = new FileInputStream(file);
        if(!ftp.storeFile(tmp, fis)) {
            fis.close();
            return false;
        }
        fis.close();
        return ftp.rename(tmp, file.getName());
        
       
    }
    
    public void disconnect() throws IOException {
        ftp.disconnect();
    }
    

}
