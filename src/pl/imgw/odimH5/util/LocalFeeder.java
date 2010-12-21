/**
 * (C) 2010 INSTITUT OF METEOROLOGY AND WATER MANAGEMENT
 */
package pl.imgw.odimH5.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.SocketException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import name.pachler.nio.file.ClosedWatchServiceException;
import name.pachler.nio.file.FileSystems;
import name.pachler.nio.file.Path;
import name.pachler.nio.file.Paths;
import name.pachler.nio.file.StandardWatchEventKind;
import name.pachler.nio.file.WatchEvent;
import name.pachler.nio.file.WatchKey;
import name.pachler.nio.file.WatchService;

import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPReply;
import org.apache.commons.net.io.CopyStreamException;
import org.w3c.dom.Document;

import pl.imgw.odimH5.model.HDF5Model;
import pl.imgw.odimH5.model.rainbow.HDF5PVOL;
import pl.imgw.odimH5.model.rainbow.RainbowModel;
import pl.imgw.odimH5.model.rainbow.RainbowPVOL;
import eu.baltrad.frame.model.BaltradFrame;
import eu.baltrad.frame.model.BaltradFrameHandler;

/**
 * 
 * /Class description/
 * 
 * 
 * @author <a href="mailto:lukasz.wojtas@imgw.pl">Lukasz Wojtas</a>
 * 
 */
public class LocalFeeder extends Thread {

    WatchService watchService = FileSystems.getDefault().newWatchService();
    private RadarOptions[] radarOptions;
    private FTP_Options[] ftpOptions;
    private BaltradOptions baltradOptions;
    Path[] watchedPath;

    RadarOpt radarOpt = new RadarOpt();

    HashMap<WatchKey, RadarOptions> pathMap = new HashMap<WatchKey, RadarOptions>();
    HashMap<RadarOpt, Long> fileTimeMap = new HashMap<RadarOpt, Long>();

    private RainbowModel rb;
    private MessageLogger msgl;

    private boolean verbose;

    /**
     * 
     * Works as a thread, wakes up each time new volume file has been
     * downloaded. All options (path, radar name, server address, sender name)
     * are stored in options.xml. It converts the file to HDF5 if needed and
     * sends it to Baltrad server.
     * 
     * @param optionsDoc
     *            XML document containing working options
     * @param rb
     *            Rainbow model class instance
     * @param proc
     *            Data processor model class instance
     * @param msgl
     * @param verbose
     *            verbose mode
     */
    public LocalFeeder(Document optionsDoc, RainbowModel rb, HDF5Model proc,
            MessageLogger msgl, boolean verbose) {

        this.rb = rb;
        this.verbose = verbose;
        this.msgl = msgl;

        radarOptions = OptionsHandler.getRadarOptions(optionsDoc);
        ftpOptions = OptionsHandler.getFTPOptions(optionsDoc);
        baltradOptions = OptionsHandler.getBaltrad(optionsDoc);

        watchedPath = new Path[radarOptions.length];
        WatchKey key[] = new WatchKey[radarOptions.length];

        for (int i = 0; i < radarOptions.length; i++) {

            if (radarOptions[i].isEmpty())
                continue;

            watchedPath[i] = Paths.get(radarOptions[i].getDir());

            try {
                key[i] = watchedPath[i].register(watchService,
                        StandardWatchEventKind.ENTRY_CREATE,
                        StandardWatchEventKind.ENTRY_MODIFY);
                pathMap.put(key[i], radarOptions[i]);
            } catch (UnsupportedOperationException uox) {
                System.err.println("file watching not supported!");
                // handle this error here
            } catch (IOException iox) {
                System.err.println("I/O errors");
                // handle this error here
            }

        }
    }

    /**
     * 
     * @param filePath
     */
    private void convertAndSendFile(RadarOpt filePath) {

        if (filePath.path.contains("KDP") || filePath.path.contains("PhiDP")
                || filePath.path.contains("HV")
                || filePath.path.contains("ZDR")) {

//            System.out.println("kasuje: " + filePath.path);

            new File(filePath.path).delete();

            return;

        }

        File originalFile = new File(filePath.path);

        if (originalFile.getName().startsWith(".")) {
            return;
        }

        File newFile = null;
        String newFileName = "";
        String radarName = "";
        boolean sentOk = false;

        if (originalFile.getName().endsWith(".vol")) {

            int file_len = (int) originalFile.length();
            byte[] file_buf = new byte[file_len];

            try {
                FileInputStream fis = new FileInputStream(originalFile);
                fis.read(file_buf, 0, file_len);
                fis.close();

            } catch (IOException e) {
                e.printStackTrace();
            }

            RainbowPVOL vol = new RainbowPVOL("", file_buf, verbose, rb,
                    radarOptions);
            vol.makeH5();
            radarName = vol.getRadarName();
            newFileName = vol.getOutputFileName();
            newFile = new File(newFileName);
//            System.out.println("nowy plik: " + newFileName);
        } else if (originalFile.getName().endsWith(".h5")
                || originalFile.getName().endsWith(".hdf")) {

            HDF5PVOL hdf = new HDF5PVOL("", filePath.path, verbose, rb,
                    radarOptions);
            radarName = hdf.getRadarName();
            newFileName = hdf.getOutputFileName();
            newFile = new File(newFileName);

        } else {

            System.out.println("Format not supported");
            return;
        }

        if (newFile != null && !baltradOptions.isEmpty()) {

            BaltradFrameHandler bfh = new BaltradFrameHandler(baltradOptions
                    .getServer());

            String a = bfh.createDataHdr(BaltradFrameHandler.MIME_MULTIPART,
                    baltradOptions.getSender(), radarName, newFileName);

            // System.out.println("BFDataHdr:");
            // System.out.println(a);

            BaltradFrame bf = new BaltradFrame(a, newFileName);

            if (bfh.handleBF(bf) == 1) {

                msgl.showMessage(radarName + ": file " + newFileName
                        + " sent to BALTRAD", true);

            } else {
                System.out.println(radarName
                        + " failed to send file to BALTRAD");
            }
        }

        if (newFile != null && ftpOptions != null) {

            for (int i = 0; i < ftpOptions.length; i++) {
                if (ftpOptions[i].isEmpty())
                    continue;

                for (int j = 0; j < ftpOptions[i].getRadars().length; j++) {
                    
                    

                    if (radarName.matches(ftpOptions[i].getRadars()[j])) {

                        // sending file to FTP

                        String sendFileName = "";
                        String remoteFolder = "";
                        if (newFileName.endsWith("vol")) {
                            sendFileName = newFileName.replace("vol", "tmp");
                            remoteFolder = radarName;
                        } else
                            sendFileName = filePath.prefix
                                    + originalFile.getName().substring(0, 14)
                                    + ".tmp";

                        try {

                            sentOk = storeFileOnServer(ftpOptions[i],
                                    newFileName, sendFileName, remoteFolder);
                            msgl.showMessage(radarName + ": " + "file "
                                    + newFileName + " sent to "
                                    + ftpOptions[i].getAddress(), sentOk);

                        } catch (CopyStreamException e) {

                            LogsHandler.saveProgramLogs(e.getMessage());
                            msgl.showMessage(radarName + " " + newFileName
                                    + " cannot be sent to "
                                    + ftpOptions[i].getAddress()
//                                    + " sending file again...", verbose);
                            , true);
/*
                            try {
                                sentOk = storeFileOnServer(ftpOptions[i],
                                        newFileName, sendFileName, remoteFolder);
                                msgl.showMessage(radarName + ": " + "file "
                                        + newFileName + " sent to "
                                        + ftpOptions[i].getAddress(), sentOk);
                            } catch (SocketException e1) {
                                // TODO Auto-generated catch block
                                e1.printStackTrace();
                            } catch (IOException e1) {
                                // TODO Auto-generated catch block
                                e1.printStackTrace();
                            }
*/
                        } catch (IOException e) {
                            
                            LogsHandler.saveProgramLogs(e.getMessage());

                            msgl.showMessage(radarName + " " + newFileName
                                    + " cannot be sent to "
                                    + ftpOptions[i].getAddress()
//                                    + " sending file again...", verbose);
                            , true);
                            /*
                            try {
                                sentOk = storeFileOnServer(ftpOptions[i],
                                        newFileName, sendFileName, remoteFolder);
                                msgl.showMessage(radarName + ": " + "file "
                                        + newFileName + " sent to "
                                        + ftpOptions[i].getAddress(), sentOk);
                            } catch (SocketException e1) {
                                // TODO Auto-generated catch block
                                e1.printStackTrace();
                            } catch (IOException e1) {
                                // TODO Auto-generated catch block
                                e1.printStackTrace();
                            }
*/
                        }
                    }
                }
            }

        }

        originalFile.delete();
        if (newFile != null)
            newFile.delete();

    }

    private boolean storeFileOnServer(FTP_Options ftpOptions,
            String newFileName, String sendFileName, String remoteFolder)
            throws SocketException, IOException {

        FTPClient ftp = new FTPClient();

        int reply;
        ftp.connect(ftpOptions.getAddress());

        // After connection attempt, you should check the
        // reply code
        // to verify
        // success.
        reply = ftp.getReplyCode();

        if (!FTPReply.isPositiveCompletion(reply)) {
            ftp.disconnect();
            System.err.println("FTP server refused connection.");
            return false;
        }

        ftp.login(ftpOptions.getLogin(), ftpOptions.getPassword());

        ftp.changeWorkingDirectory(ftpOptions.getDir());

        if (!remoteFolder.isEmpty())
            ftp.changeWorkingDirectory(remoteFolder);

        ftp.setFileType(FTPClient.BINARY_FILE_TYPE);

        // ftp
        // .setFileTransferMode(FTPClient.PASSIVE_REMOTE_DATA_CONNECTION_MODE);

        // transfer files
        FileInputStream fis = null;
        fis = new FileInputStream(newFileName);

        ftp.storeFile(sendFileName, fis);

        fis.close();

        boolean ok = false;
        
        ok = ftp.rename(sendFileName, newFileName);
        
        ftp.disconnect();
        
        return ok;

    }

    @Override
    public void run() {

        while (true) {

            // hm.get(spotedKey);
            // System.out.println("poczatek " + System.currentTimeMillis());

            // take() will block until a file has been created/deleted
            WatchKey signalledKey;
            try {
                signalledKey = watchService.poll(2,
                        java.util.concurrent.TimeUnit.SECONDS);
            } catch (InterruptedException ix) {
                // we'll ignore being interrupted
                System.out.println("watch service interrupted.");
                continue;
            } catch (ClosedWatchServiceException cwse) {
                // other thread closed watch service
                System.out.println("watch service closed, terminating.");
                break;
            }

            if (signalledKey == null) {
                int a = 0;
                long time;
                long timeNow = System.currentTimeMillis();
                Iterator<RadarOpt> itr = fileTimeMap.keySet().iterator();
                while (itr.hasNext()) {
                    RadarOpt key = itr.next();
                    time = fileTimeMap.get(key);
                    if ((timeNow - time) > 3000) {
                        fileTimeMap.remove(key);
                        itr = fileTimeMap.keySet().iterator();
                        msgl.showMessage(key.path + " download finished",
                                verbose);
                        convertAndSendFile(key);
                    }
                }
                if (fileTimeMap.isEmpty()) {
                    try {
                        // System.out.println("Waiting....");
                        signalledKey = watchService.take();
                    } catch (ClosedWatchServiceException e1) {
                        // TODO Auto-generated catch block
                        e1.printStackTrace();
                    } catch (InterruptedException e1) {
                        // TODO Auto-generated catch block
                        e1.printStackTrace();
                    }
                } else

                    continue;

            }

            try {
                List<WatchEvent<?>> list = signalledKey.pollEvents();

                // VERY IMPORTANT! call reset() AFTER pollEvents() to allow the
                // key to be reported again by the watch service
                signalledKey.reset();

                for (WatchEvent<?> e : list) {
                    String radarName = "";
                    if (e.kind() == StandardWatchEventKind.ENTRY_CREATE) {
                        Path context = (Path) e.context();

                        radarOpt = new RadarOpt();
                        radarOpt.path = pathMap.get(signalledKey).getDir();
                        radarName = pathMap.get(signalledKey).getRadarName();
                        radarOpt.prefix = pathMap.get(signalledKey)
                                .getFileName();
                        if (!radarOpt.path.endsWith("/"))
                            radarOpt.path += "/";

                        radarOpt.path += context.toString();

                        msgl.showMessage(radarName + " new file: "
                                + radarOpt.path, verbose);

                        fileTimeMap.put(radarOpt, System.currentTimeMillis());
                    } else if (e.kind() == StandardWatchEventKind.ENTRY_MODIFY) {
                        Path context = (Path) e.context();

                        radarOpt = new RadarOpt();
                        radarOpt.path = pathMap.get(signalledKey).getDir();
                        radarName = pathMap.get(signalledKey).getRadarName();
                        radarOpt.prefix = pathMap.get(signalledKey)
                                .getFileName();
                        if (!radarOpt.path.endsWith("/"))
                            radarOpt.path += "/";

                        radarOpt.path += context.toString();
                        fileTimeMap.put(radarOpt, System.currentTimeMillis());

                    } else if (e.kind() == StandardWatchEventKind.OVERFLOW) {
                        System.out
                                .println("OVERFLOW: more changes happened than we could retreive");
                    }
                }
            } catch (Exception e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }

        }

    }

    class RadarOpt {
        private String path = "";
        private String prefix = "";

    }

}