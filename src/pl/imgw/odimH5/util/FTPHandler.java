/**
 * (C) 2013 INSTITUT OF METEOROLOGY AND WATER MANAGEMENT
 */
package pl.imgw.odimH5.util;

import java.io.File;
import java.io.IOException;
import java.net.SocketException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import com.enterprisedt.net.ftp.FTPException;
import com.enterprisedt.net.ftp.FTPTransferType;
import com.enterprisedt.net.ftp.FileTransferClient;

/**
 * 
 * /Class description/
 * 
 * 
 * @author <a href="mailto:lukasz.wojtas@imgw.pl">Lukasz Wojtas</a>
 * 
 */
public class FTPHandler {

    private Map<String, FileTransferClient> connections = new HashMap<String, FileTransferClient>();
    private Map<String, List<FTPContainer>> ftpContainers;

    /**
     * 
     * Map key is a radar ID, and value is a list of FTP properties, where this radar should be sent
     * 
     * @param ftps
     */
    public FTPHandler(Map<String, List<FTPContainer>> ftps) throws IllegalArgumentException {
        if(ftps.isEmpty())
            throw new IllegalArgumentException("List cannot be empty");
        this.ftpContainers = ftps;
    }

    private void connect(FileTransferClient ftp, FTPContainer ftpCont)
            throws FTPException, IOException {
        // UtSocketFactory utSocketFactory = new UtSocketFactory();
        // utSocketFactory.setConnectTimeout(5000);

        if (ftp.isConnected())
            return;

        ftp.setRemoteHost(ftpCont.getAddress());
        ftp.setUserName(ftpCont.getLogin());
        ftp.setPassword(ftpCont.getPassword());
        ftp.connect();
    }

    /**
     * 
     * @param file
     * @param radarID
     * @throws FTPException
     * @throws IOException
     */
    public boolean sendFile(File file, String radarID) {

        if(!ftpContainers.containsKey(radarID))
            return false;
        
        List<FTPContainer> list = ftpContainers.get(radarID);

        
        for (FTPContainer ftpCont : list) {
            if (!connections.containsKey(ftpCont.getAddress())) {
                connections.put(ftpCont.getAddress(), new FileTransferClient());
            }

            FileTransferClient ftp = connections.get(ftpCont.getAddress());
            try {
                connect(ftp, ftpCont);

                ftp.changeDirectory(ftpCont.getRemoteDir());

                ftp.setContentType(FTPTransferType.BINARY);

                ftp.uploadFile(file.getPath(), "." + file.getName());

                ftp.rename("." + file.getName(), file.getName());
            } catch (FTPException e) {
                return false;
            } catch (IOException e) {
                return false;
            }

        }

        return true;
        
    }

}
