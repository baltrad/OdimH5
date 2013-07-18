/**
 * (C) 2010 INSTITUTE OF METEOROLOGY AND WATER MANAGEMENT
 */
package pl.imgw.odimH5.util;

import java.io.File;
import java.io.InputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import name.pachler.nio.file.ClosedWatchServiceException;
import name.pachler.nio.file.FileSystems;
import name.pachler.nio.file.Path;
import name.pachler.nio.file.Paths;
import name.pachler.nio.file.StandardWatchEventKind;
import name.pachler.nio.file.WatchEvent;
import name.pachler.nio.file.WatchKey;
import name.pachler.nio.file.WatchService;

import org.w3c.dom.Document;

import pl.imgw.odimH5.model.HDF5Model;
import pl.imgw.odimH5.model.rainbow.HDF2RainbowPVOL;
import pl.imgw.odimH5.model.rainbow.Rainbow2HDFPVOL;
import pl.imgw.odimH5.model.rainbow.RainbowModel;

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
public class LocalFeeder implements Runnable {

    WatchService watchService = FileSystems.getDefault().newWatchService();
    private RadarOptions[] radarOptions;
    private ConvertingTool converter;
    Path[] watchedPath;

    HashMap<WatchKey, RadarOptions> pathMap = new HashMap<WatchKey, RadarOptions>();
    HashMap<String, Long> fileTimeMap = new HashMap<String, Long>();

    private RainbowModel rb;
    private MessageLogger msgl;
    private static int counter = 0;

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
    public LocalFeeder(RainbowModel rb, HDF5Model proc,
            MessageLogger msgl, boolean verbose) {

        this.rb = rb;
        this.verbose = verbose;
        this.msgl = msgl;

        radarOptions = OptionsHandler.getOpt().getRadarOptions();
        
        converter = new ConvertingTool(rb, verbose);

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

    
    private void convertAndSend(String filePath) {
        
        File file = new File(filePath);

        if (filePath.contains("KDP") || filePath.contains("PhiDP")
                || filePath.contains("HV") || filePath.contains("ZDR")) {

            // System.out.println("kasuje: " + filePath.path);

            file.delete();
            return;
        }

        if (!file.canRead())
            return;

        if (file.getName().startsWith(".")) {
            return;
        }

        if (file.getName().endsWith("dBZ.vol")) {
            converter.convertRb5ToHdf5(file);
        } else if (file.getName().endsWith("h5")
                || file.getName().endsWith("hdf")) {
            converter.convertHdf5ToVol(file);
        }
        counter++;
        file.delete();
        if(counter > 50) {
            System.gc();
            counter = 0;
        }
        
    }
    
    /**
     * 
     * @param filePath
     * @deprecated
     */
    private void convertAndSendFileOld(String filePath) throws Exception {

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
            }

            Rainbow2HDFPVOL vol = new Rainbow2HDFPVOL("", file_buf, verbose,
                    rb);
            vol.makeH5();
            radarName = vol.getRadarID();
            toBeSentFileName = vol.getOutputFileName();
            toBeSentFile = new File(toBeSentFileName);
            radarFullName = vol.getRadarName();
        } else if (originalFile.getName().endsWith(".h5")
                || originalFile.getName().endsWith(".hdf")) {

            // no single convertion from hdf to vol handle so far
            HDF2RainbowPVOL hdf = new HDF2RainbowPVOL("", filePath, verbose,
                    rb);
            toBeSentFileName = hdf.getOutputFileName();
//            toBeSentFile = new File(hdf.getOutputFileName());
            radarName = hdf.getRadarName();

            toBeSentFile = originalFile;

        } else {

            System.out.println("Format " + originalFile.getName()
                    + " not supported");
            return;
        }

        FTP_Options[] ftpOptions = OptionsHandler.getOpt().getOldFTPOptions();
        
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
                        } catch (FTPException e) {
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
                        }
                    }
                }
            }

        }

        BaltradOptions baltradOptions = OptionsHandler.getOpt().getBaltradOptions();
        
        if (toBeSentFile != null
                && (toBeSentFile.getName().endsWith("h5") || toBeSentFile
                        .getName().endsWith("hdf"))
                && !baltradOptions.isEmpty()) {

            msgl.showMessage("Sending file " + toBeSentFileName + " to "
                    + baltradOptions.getHostAddress(), verbose);
            InitAppUtil init = InitAppUtil.getInstance();

            // Feed to BALTRAD
            BaltradFeeder baltradFeeder = new BaltradFeeder(
                    baltradOptions.getHostAddress(), init, toBeSentFile);
            baltradFeeder.feedToBaltrad();
            msgl.showMessage(baltradFeeder.getMessage(), verbose);

        }

        if (!baltradOptions.isEmpty()
                && (originalFile.getName().endsWith("h5") || originalFile
                        .getName().endsWith("hdf"))) {

            msgl.showMessage("Sending file " + originalFile + " to "
                    + baltradOptions.getHostAddress(), verbose);
            InitAppUtil init = InitAppUtil.getInstance();

            // Feed to BALTRAD
            BaltradFeeder baltradFeeder = new BaltradFeeder(
                    baltradOptions.getHostAddress(), init, originalFile);
            baltradFeeder.feedToBaltrad();
            msgl.showMessage(baltradFeeder.getMessage(), verbose);

        }

        if (originalFile != null
                && (originalFile.getName().endsWith("h5") || originalFile
                        .getName().endsWith("hdf"))
                && !baltradOptions.isEmpty()) {

            msgl.showMessage("Sending file " + originalFile + " to "
                    + baltradOptions.getHostAddress(), verbose);
            InitAppUtil init = InitAppUtil.getInstance();

            // Feed to BALTRAD
            BaltradFeeder baltradFeeder = new BaltradFeeder(
                    baltradOptions.getHostAddress(), init, originalFile);
            baltradFeeder.feedToBaltrad();
            msgl.showMessage(baltradFeeder.getMessage(), verbose);

        }

        originalFile.delete();
        if (toBeSentFile != null) {
            toBeSentFile.delete();
        }

    }

    private boolean storeFileOnServer(FTP_Options ftpOptions,
            String toBeSentFileName, String sendFileTempName,
            String remoteFolder, String extension) throws IOException,
            UnknownHostException, FTPException {

        UtSocketFactory utSocketFactory = new UtSocketFactory();
        utSocketFactory.setConnectTimeout(5000);

        FileTransferClient ftp = new FileTransferClient();

        System.out.print("Connecting... ");

        ftp.setRemoteHost(ftpOptions.getAddress());

        System.out.print("Connected! ");

        // System.out.println(newFileName + " jako " + sendFileName);

        // After connection attempt, you should check the
        // reply code
        // to verify
        // success.

        ftp.setUserName(ftpOptions.getLogin());
        ftp.setPassword(ftpOptions.getPassword());
        ftp.connect();

        System.out.print("Logged in. ");

        if (ftpOptions.getDir() != null)
            ftp.changeDirectory(ftpOptions.getDir());

        if (!remoteFolder.isEmpty())
            ftp.changeDirectory(remoteFolder);

        System.out.print("Directory changed. ");

        ftp.setContentType(FTPTransferType.BINARY);

        System.out.print("File type set.\n");

        ftp.uploadFile(toBeSentFileName, sendFileTempName);

        // newFileName = sendFileName.replace("tmp", "hdf");

        // String newName = sendFileName.replace("tmp", "hdf");
        // System.out.println("wyslany jako: " + sendFileName);
        // System.out.println("zmieniona nazwa na: " + newName);
        if (toBeSentFileName.endsWith("h5"))
            toBeSentFileName = toBeSentFileName.replace("h5", "hdf");

        ftp.rename(sendFileTempName, toBeSentFileName);

        ftp.disconnect();

        return true;

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
                            convertAndSend(key);
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
                                .println("OVERFLOW: more changes happened than we could retrieve");
                    }
                }
            } catch (Exception e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }

        }

    }

}
