/**
 * (C) 2013 INSTITUT OF METEOROLOGY AND WATER MANAGEMENT
 */
package pl.imgw.odimH5.util;

import java.io.File;
import java.io.IOException;
import java.net.SocketException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.concurrent.ConcurrentLinkedQueue;

import org.apache.commons.io.FileUtils;
import org.apache.commons.net.ftp.FTPClient;

import com.enterprisedt.net.ftp.FTPConnectionClosedException;
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

    private SimpleDateFormat folderName = new SimpleDateFormat("yyyy-MM-dd");
    private SimpleDateFormat fileName = new SimpleDateFormat("yyyyMMdd");
    

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

        if(ftp.isConnected()) {
            ftp.disconnect();
        }
        
        ftp.setRemoteHost(ftpCont.getAddress());
        ftp.setUserName(ftpCont.getLogin());
        ftp.setPassword(ftpCont.getPassword());
        ftp.setTimeout(2000);
        ftp.connect();
        
    }

    public boolean sendFile(File file, String radarID) {

        if (!ftps.containsKey(radarID)) {
            System.out.println(radarID + ": No radar ID on the list");
            return false;
        }

        List<FTPContainer> list = ftps.get(radarID);

        for (FTPContainer ftpCont : list) {

            for (int i = 0; i < 3; i++)
                if (send(file, radarID, ftpCont)) {
                    LogsHandler.saveRecentFile(radarID + " " + file.getName(),
                            ftpCont.getAddress());
                    break;
                }
        }

        return true;
    }

    /**
     * @param file
     * @param radarID
     * @param ftpCont
     * @return
     */
    private boolean send(File file, String radarID, FTPContainer ftpCont) {
        
        if (ftpCont.getAddress().contains("localhost")) {
            File output = new File(new File(ftpCont.getRemoteDir(), radarID),
                    file.getName());
            output.getParentFile().mkdirs();
            try {
                FileUtils.copyFile(file, output);
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }

            if (output.exists() && output.isFile()) {

                System.out.println(radarID + ": " + output.getPath()
                        + " copied.");
                return true;
            } else
                return false;
        }
        
        FileTransferClient ftp = connections.get(ftpCont.getAddress());
        if (ftp == null) {
            ftp = new FileTransferClient();
            connections.put(ftpCont.getAddress(), ftp);
        }
        
        try {
            
            ftp.setRemoteHost(ftpCont.getAddress());
            ftp.setUserName(ftpCont.getLogin());
            ftp.setPassword(ftpCont.getPassword());
            ftp.setTimeout(10000);
            ftp.connect();

//            String subfolder;
//            if(ftpCont.isSubfolders()) {
//                subfolder = radarID; 
//            } else {
//                subfolder = "";
//            }
            
            String remote = getRemoteFolder(radarID, file.getName(),
                    ftpCont.getRemoteDir(), ftpCont.getSubfolders());
            
//            System.out.println("remote: " + remote);
            
            if (!cd(ftp, remote)) {
                System.out.println(radarID + ": sending file " + file.getName()
                        + " to " + ftpCont.getAddress()
                        + " FAILED: cannot change remote directory");
                return false;
            }
            
            ftp.setContentType(FTPTransferType.BINARY);

            ftp.uploadFile(file.getPath(), "." + file.getName());

            ftp.rename("." + file.getName(), file.getName());
            
            System.out.println(radarID + ": sending file: "
                    + file.getName() + " to " + ftpCont.getAddress()
                    + " completed.");
            
        }catch (FTPConnectionClosedException e) {
             
        }catch (FTPException e) {
            System.out.println(radarID + ": sending file " + file.getName()
                    + " to " + ftpCont.getAddress() + " FAILED: "
                    + e.getMessage());
            return false;
        } catch (IOException e) {
            System.out.println(radarID + ": sending file " + file.getName()
                    + " to " + ftpCont.getAddress() + " FAILED: "
                    + e.getMessage());
            return false;
        } finally {

            try {
                ftp.disconnect();
            } catch (Exception e) {
            }
        }

        return true;
    }

    /**
     * @param ftp
     * @param remoteDir
     * @param radarID
     */
    private boolean cd(FileTransferClient ftp, String remoteDir) {
        
        try {
            ftp.changeDirectory(remoteDir);
        } catch (FTPException e) {
            try {
                ftp.createDirectory(remoteDir);
                ftp.changeDirectory(remoteDir);
            } catch (FTPException e1) {
                return false;
            } catch (IOException e1) {
                return false;
            }
        } catch (IOException e) {
            return false;
        }
        
//        try {
//            if(!radarID.isEmpty())
//                ftp.changeDirectory(radarID);
//        } catch (FTPException e) {
//            try {
//                ftp.createDirectory(radarID);
//                ftp.changeDirectory(radarID);
//            } catch (FTPException e1) {
//                return false;
//            } catch (IOException e1) {
//                return false;
//            }
//        } catch (IOException e) {
//            return false;
//        }
        
        return true;
    }
    
    private String getRemoteFolder(String radarid, String file,
            String remoteDir, int subfolders) {

        switch (subfolders) {
        case FTPContainer.NO_SUBFOLDERS:
            return remoteDir;

        case FTPContainer.RADAR_NAME_SUBFOLDERS:
            return remoteDir + "/" + radarid;

        case FTPContainer.RADAR_NAME_DATE_SUBFOLDERS:
            Date date = null;
            
            if(file.contains("_")) {
                file = file.split("_")[file.split("_").length - 1].substring(0,  8);
            } else {
                file = file.substring(0, 8);
            }

//            System.out.println("file: " + file);
            
            try {
                date = fileName.parse(file);
            } catch (ParseException e) {
                date = new Date();
            }
            return remoteDir + "/" + radarid + "/" + folderName.format(date);

        }

        return "";
    }
    
    /**
     * 
     * @param file
     * @param radarID
     * @throws FTPException
     * @throws IOException
     *
    public boolean sendFile(File file, String radarID) {

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

                if (ftpCont.getRemoteDir() != null) {
                    try {
                        ftp.changeDirectory(ftpCont.getRemoteDir());
                    } catch (FTPException e) {
                        ftp.createDirectory(ftpCont.getRemoteDir());
                        ftp.changeDirectory(ftpCont.getRemoteDir());
                    }
                }

                ftp.createDirectory(radarID);
                ftp.changeDirectory(radarID);
                
                ftp.setContentType(FTPTransferType.BINARY);

                ftp.uploadFile(file.getPath(), "." + file.getName());

                ftp.rename("." + file.getName(), file.getName());
                
                ftp.disconnect();
            }catch (FTPConnectionClosedException e) {
                 
            }catch (FTPException e) {
                e.printStackTrace();
                return false;
            } catch (IOException e) {
                e.printStackTrace();
                return false;
            } 
        }

        return true;
        
    }
*/
}
