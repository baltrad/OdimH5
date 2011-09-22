/**
 * (C) 2010 INSTITUT OF METEOROLOGY AND WATER MANAGEMENT
 */
package pl.imgw.odimH5.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
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

import pl.imgw.odimH5.Main;
import pl.imgw.odimH5.model.HDF5Model;
import pl.imgw.odimH5.model.rainbow.HDF2RainbowPVOL;
import pl.imgw.odimH5.model.rainbow.RainbowModel;
import pl.imgw.odimH5.model.rainbow.Rainbow2HDFPVOL;
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

    HashMap<WatchKey, RadarOptions> pathMap = new HashMap<WatchKey, RadarOptions>();
    HashMap<String, Long> fileTimeMap = new HashMap<String, Long>();

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
    private void convertAndSendFile(String filePath) throws Exception {

        if (filePath.contains("KDP") || filePath.contains("PhiDP")
                || filePath.contains("HV") || filePath.contains("ZDR")) {

            // System.out.println("kasuje: " + filePath.path);

            new File(filePath).delete();

            return;

        }

        File originalFile = new File(filePath);

        if (!originalFile.canRead())
            return;

        if (originalFile.getName().startsWith(".")) {
            return;
        }

        File toBeSentFile = null;
        String toBeSentFileName = "";
        String radarName = "";
        String radarFullName = "";
        String extension = "";
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

            Rainbow2HDFPVOL vol = new Rainbow2HDFPVOL("", file_buf, verbose,
                    rb, radarOptions);
            vol.makeH5();
            radarName = vol.getRadarName();
            toBeSentFileName = vol.getOutputFileName();
            toBeSentFile = new File(toBeSentFileName);
            radarFullName = vol.getRadarFullName();
        } else if (originalFile.getName().endsWith(".h5")
                || originalFile.getName().endsWith(".hdf")) {

            HDF2RainbowPVOL hdf = new HDF2RainbowPVOL("", filePath, verbose,
                    rb, radarOptions);
            radarName = hdf.getRadarName();
            toBeSentFileName = hdf.getOutputFileName();
            toBeSentFile = new File(toBeSentFileName);

        } else {

            System.out.println("Format of " + originalFile.getName()
                    + "not supported");
            return;
        }

        // System.out.println("nowy plik: " + newFileName);

        if (toBeSentFile != null && ftpOptions != null) {

            for (int i = 0; i < ftpOptions.length; i++) {
                if (ftpOptions[i].isEmpty())
                    continue;

                for (int j = 0; j < ftpOptions[i].getRadars().length; j++) {

                    if (radarName.matches(ftpOptions[i].getRadars()[j])) {

                        // sending file to FTP

                        String toBeSentFileTempName = "";
                        String remoteFolder = "";
                        if (toBeSentFileName.endsWith("vol")) {
                            toBeSentFileTempName = toBeSentFileName.replace(
                                    "vol", "tmp");
                            remoteFolder = radarName;
                            extension = "vol";
                        } else {
                            toBeSentFileTempName = toBeSentFileName.replace(
                                    "h5", "tmp");
                            extension = "hdf";
                            // sendFileName = newFileName.replace("hdf", "tmp");
                            // System.out.println("wyslac jako: " +
                            // sendFileName);
                        }

                        try {

                            sentOk = storeFileOnServer(ftpOptions[i],
                                    toBeSentFileName, toBeSentFileTempName,
                                    remoteFolder, extension);
                            msgl.showMessage(
                                    radarName
                                            + ": "
                                            + "file "
                                            + toBeSentFileName.substring(0,
                                                    toBeSentFileName
                                                            .indexOf(".") + 1)
                                            + extension + " sent to "
                                            + ftpOptions[i].getAddress(),
                                    sentOk);

                        } catch (CopyStreamException e) {

                            LogsHandler.saveProgramLogs(e.getMessage());
                            msgl.showMessage(
                                    radarName
                                            + ": "
                                            + toBeSentFileName.substring(0,
                                                    toBeSentFileName
                                                            .indexOf(".") + 1)
                                            + extension + " cannot be sent to "
                                            + ftpOptions[i].getAddress()
                                    // + " sending file again...", verbose);
                                    , true);
                            /*
                             * try { sentOk = storeFileOnServer(ftpOptions[i],
                             * newFileName, sendFileName, remoteFolder);
                             * msgl.showMessage(radarName + ": " + "file " +
                             * newFileName + " sent to " +
                             * ftpOptions[i].getAddress(), sentOk); } catch
                             * (SocketException e1) { // TODO Auto-generated
                             * catch block e1.printStackTrace(); } catch
                             * (IOException e1) { // TODO Auto-generated catch
                             * block e1.printStackTrace(); }
                             */
                        } catch (IOException e) {

                            LogsHandler.saveProgramLogs(e.getMessage());

                            msgl.showMessage(
                                    radarName
                                            + ": "
                                            + toBeSentFileName.substring(0,
                                                    toBeSentFileName
                                                            .indexOf(".") + 1)
                                            + extension + " cannot be sent to "
                                            + ftpOptions[i].getAddress()
                                    // + " sending file again...", verbose);
                                    , true);
                            /*
                             * try { sentOk = storeFileOnServer(ftpOptions[i],
                             * newFileName, sendFileName, remoteFolder);
                             * msgl.showMessage(radarName + ": " + "file " +
                             * newFileName + " sent to " +
                             * ftpOptions[i].getAddress(), sentOk); } catch
                             * (SocketException e1) { // TODO Auto-generated
                             * catch block e1.printStackTrace(); } catch
                             * (IOException e1) { // TODO Auto-generated catch
                             * block e1.printStackTrace(); }
                             */
                        }
                    }
                }
            }

        }
        if (toBeSentFile != null
                && (toBeSentFile.getName().endsWith("h5") || toBeSentFile
                        .getName().endsWith("hdf"))
                && !baltradOptions.isEmpty()) {

            // System.out.println("sender: " + baltradOptions.getSender());
            // System.out.println("server: " + baltradOptions.getServer());

            BaltradFrameHandler bfh = new BaltradFrameHandler( Main.SCHEME,
                    baltradOptions.getHostAddress(), baltradOptions.getPort(), Main.APP_CTX,
                    Main.ENTRY_ADDRESS, Main.SO_TIMEOUT, Main.CONN_TIMEOUT );

            String a = bfh
                    .createDataHdr(BaltradFrameHandler.MIME_MULTIPART,
                            baltradOptions.getSender(), radarFullName,
                            toBeSentFileName);

            // System.out.print("BFDataHdr: ");
            // System.out.println(a);

            BaltradFrame bf = new BaltradFrame( Main.ADDR_SEPARATOR + Main.APP_CTX +
                    Main.ADDR_SEPARATOR + Main.ENTRY_ADDRESS, a, toBeSentFile );

            if (bfh.handleBF(bf) == 0) {

                msgl.showMessage(radarName + ": file " + toBeSentFileName
                        + " sent to BALTRAD", true);

            } else {
                msgl.showMessage(radarName + " failed to send file to BALTRAD",
                        true);
            }
        }

        originalFile.delete();
        if (toBeSentFile != null)
            toBeSentFile.delete();

    }

    private boolean storeFileOnServer(FTP_Options ftpOptions,
            String toBeSentFileName, String sendFileTempName,
            String remoteFolder, String extension) throws IOException,
            UnknownHostException {

        UtSocketFactory utSocketFactory = new
        UtSocketFactory();
        utSocketFactory.setConnectTimeout(5000);

        FTPClient ftp = new FTPClient();
        ftp.setSocketFactory(utSocketFactory);
       
        
        int reply;

        ftp.connect(ftpOptions.getAddress());

        // System.out.println(newFileName + " jako " + sendFileName);

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
        fis = new FileInputStream(toBeSentFileName);

        ftp.storeFile(sendFileTempName, fis);

        fis.close();

        boolean ok = false;

        // newFileName = sendFileName.replace("tmp", "hdf");

        // String newName = sendFileName.replace("tmp", "hdf");
        // System.out.println("wyslany jako: " + sendFileName);
        // System.out.println("zmieniona nazwa na: " + newName);
        if (toBeSentFileName.endsWith("h5"))
            toBeSentFileName = toBeSentFileName.replace("h5", "hdf");

        ok = ftp.rename(sendFileTempName, toBeSentFileName);

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
                // int a = 0;
                long time;
                long timeNow = System.currentTimeMillis();
                Iterator<String> itr = fileTimeMap.keySet().iterator();
                while (itr.hasNext()) {
                    String key = itr.next();
                    time = fileTimeMap.get(key);
                    if ((timeNow - time) > 1000) {
                        fileTimeMap.remove(key);
                        itr = fileTimeMap.keySet().iterator();
                        msgl.showMessage(key + " download finished", true);
                        try {
                            convertAndSendFile(key);
                        } catch (Exception e) {
                            // TODO Auto-generated catch block
                            e.printStackTrace();
                            LogsHandler.saveProgramLogs("convertAndSendFile",
                                    e.getLocalizedMessage());
                        }
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

                        if (context.toString().startsWith(".")) {
                            continue;
                        }

                        String path = "";
                        path = pathMap.get(signalledKey).getDir();
                        radarName = pathMap.get(signalledKey).getRadarName();

                        if (!path.endsWith("/"))
                            path += "/";

                        path += context.toString();

                        msgl.showMessage(radarName + ": new file: " + path,
                                true);

                        fileTimeMap.put(path, System.currentTimeMillis());
                    } else if (e.kind() == StandardWatchEventKind.ENTRY_MODIFY) {
                        Path context = (Path) e.context();

                        if (context.toString().startsWith(".")) {
                            continue;
                        }

                        String path = "";
                        path = pathMap.get(signalledKey).getDir();
                        radarName = pathMap.get(signalledKey).getRadarName();

                        if (!path.endsWith("/"))
                            path += "/";

                        path += context.toString();
                        fileTimeMap.put(path, System.currentTimeMillis());

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

}