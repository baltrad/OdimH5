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
    private Map<String, List<FTPContainer>> ftps;


    /**
     * 
     */
    public FTPHandler() {
        ftps = OptionsHandler.getOpt().getFtpOptions();
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

        System.out.println("starting to send file: " + file);
        
        if(!ftps.containsKey(radarID)) {
            System.out.println(radarID + ": No radar ID on the list");
            return false;
        }
        
        List<FTPContainer> list = ftps.get(radarID);

        
        for (FTPContainer ftpCont : list) {
            if (!connections.containsKey(ftpCont.getAddress())) {
                connections.put(ftpCont.getAddress(), new FileTransferClient());
            }

            FileTransferClient ftp = connections.get(ftpCont.getAddress());
            try {
                connect(ftp, ftpCont);

                if (ftpCont.getRemoteDir() != null)
                    ftp.changeDirectory(ftpCont.getRemoteDir());

                ftp.setContentType(FTPTransferType.BINARY);

                ftp.uploadFile(file.getPath(), "." + file.getName());

                ftp.rename("." + file.getName(), file.getName());
            } catch (FTPException e) {
                e.printStackTrace();
                return false;
            } catch (IOException e) {
                e.printStackTrace();
                return false;
            }
        }

        return true;
        
    }

}
