/**
 * (C) 2013 INSTITUT OF METEOROLOGY AND WATER MANAGEMENT
 */
package pl.imgw.odimH5.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.SocketException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentLinkedQueue;

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

    private ConcurrentLinkedQueue<FTPClient> connections = new ConcurrentLinkedQueue<FTPClient>();
    private Map<String, List<FTPContainer>> ftps;


    public FTPApacheHandler() {
        ftps = OptionsHandler.getOpt().getFtpOptions();
    }
    
    public boolean sendFile(File file, String radarID) {

        if (!ftps.containsKey(radarID)) {
            System.out.println(radarID + ": No radar ID on the list");
            return false;
        }

        List<FTPContainer> list = ftps.get(radarID);

        for (FTPContainer ftpCont : list) {

            for (int i = 0; i < 3; i++)
                if (send(file, radarID, ftpCont))
                    break;

        }

        return true;
    }

    /**
     * @param file
     * @param radarID
     * @param ftpCont
     */
    private boolean send(File file, String radarID, FTPContainer ftpCont) {
        
        FTPClient ftp = connections.poll();
        if(ftp == null)
            ftp = new FTPClient();

        try {
            ftp.connect(ftpCont.getAddress());
            ftp.setFileType(FTP.BINARY_FILE_TYPE, FTP.BINARY_FILE_TYPE);
            ftp.setFileTransferMode(FTP.BINARY_FILE_TYPE);
            ftp.login(ftpCont.getLogin(), ftpCont.getPassword());
            
            if (!ftpCont.getRemoteDir().isEmpty()) {
                if (!ftp.changeWorkingDirectory(ftpCont.getRemoteDir())) {
                    if (ftp.makeDirectory(ftpCont.getRemoteDir())) {
                        ftp.changeWorkingDirectory(ftpCont.getRemoteDir());
                    } else {
                        System.out.println(radarID
                                + ": cannot change remote directory");
                        return false;
                    }
                }
            }
            if (!ftp.changeWorkingDirectory(radarID)) {
                if(ftp.makeDirectory(radarID)) {
                    ftp.changeWorkingDirectory(radarID);
                } else {
                    System.out.println(radarID
                            + ": cannot create radar directory");
                    return false;
                }
            } 
            
            FileInputStream fis = new FileInputStream(file);
            
            ftp.appendFile("." + file.getName(), fis);
            
            if (ftp.rename("." + file.getName(), file.getName())) {
                System.out.println(radarID + ": sending file: "
                        + file.getName() + " to " + ftpCont.getAddress()
                        + " completed.");
                return true;
            } else {
                System.out.println(radarID + ": sending file: "
                        + file.getName() + " to " + ftpCont.getAddress()
                        + " failed!");
                return false;
            }
            
            
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } finally {
            try {
                ftp.disconnect();
            } catch (IOException e) {
            }
            connections.offer(ftp);
            
        }
        return false;
    }
    

}
