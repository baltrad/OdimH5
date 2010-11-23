/**
 * (C) 2010 INSTITUT OF METEOROLOGY AND WATER MANAGEMENT
 */
package pl.imgw.odimH5.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
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
import org.w3c.dom.Document;

import eu.baltrad.frame.model.BaltradFrame;
import eu.baltrad.frame.model.BaltradFrameHandler;

import pl.imgw.odimH5.controller.DataProcessorController;
import pl.imgw.odimH5.model.DataProcessorModel;
import pl.imgw.odimH5.model.rainbow.Model;
import pl.imgw.odimH5.model.rainbow.ModelPVOL;
import pl.imgw.odimH5.model.rainbow531.Model531;
import pl.imgw.odimH5.model.rainbow531.Model531PVOL;

/**
 * 
 * /Class description/
 * 
 * 
 * @author <a href="mailto:lukasz.wojtas@imgw.pl">Lukasz Wojtas</a>
 * 
 */
public class BaltradLocalFeeder extends Thread {

    WatchService watchService = FileSystems.getDefault().newWatchService();
    private RadarOptions[] radarOptions;
    private FTP_Options[] ftpOptions;
    private BaltradOptions baltradOptions;
    Path[] watchedPath;

    RadarOpt radarOpt = new RadarOpt();

    HashMap<WatchKey, RadarOptions> pathMap = new HashMap<WatchKey, RadarOptions>();
    HashMap<RadarOpt, Long> fileTimeMap = new HashMap<RadarOpt, Long>();

    private Model531 rb531;
    private Model rb;
    private DataProcessorModel proc;
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
    public BaltradLocalFeeder(Document optionsDoc, Model rb, Model531 rb531,
            DataProcessorModel proc, MessageLogger msgl, boolean verbose) {

        this.rb = rb;
        this.rb531 = rb531;
        this.proc = proc;
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

            return;

        }

        File originalFile = new File(filePath.path);
        File h5file = null;
        String fileNameH5 = "";

        if (filePath.path.endsWith(".vol")) {

            int file_len = (int) originalFile.length();
            byte[] file_buf = new byte[file_len];

            try {
                FileInputStream fis = new FileInputStream(originalFile);
                fis.read(file_buf, 0, file_len);
                fis.close();

            } catch (IOException e) {
                e.printStackTrace();
            }

            fileNameH5 = "hdf5/" + filePath.radarName;
            h5file = new File(fileNameH5);
            if (!h5file.exists()) {
                h5file.mkdir();
            }

            if (filePath.platform
                    .equals(DataProcessorController.RAINBOW531_PLATFORM)) {

                fileNameH5 = Model531PVOL.createDescriptor(h5file
                        .getAbsolutePath()
                        + "/" + originalFile.getName() + ".h5", file_buf,
                        this.verbose, this.rb531, radarOptions);
            } else if (filePath.platform
                    .equals(DataProcessorController.RAINBOW_PLATFORM)) {

                fileNameH5 = ModelPVOL.createDescriptor(h5file
                        .getAbsolutePath()
                        + "/" + originalFile.getName() + ".h5", file_buf,
                        this.verbose, this.rb, radarOptions);
            } else {
                System.out.println("Platform not supported.");
                return;
            }

            h5file = new File(fileNameH5);
            if (fileNameH5 == null)
                return;
        } else if (filePath.path.endsWith(".h5")
                || filePath.path.endsWith(".hdf")) {
            fileNameH5 = originalFile.getName();
        } else {
            System.out.println("Format not supported");
            return;
        }

        if (!baltradOptions.isEmpty()) {

            BaltradFrameHandler bfh = new BaltradFrameHandler(baltradOptions
                    .getServer());

            String a = bfh.createDataHdr(BaltradFrameHandler.MIME_MULTIPART,
                    baltradOptions.getSender(), filePath.radarName, fileNameH5);

            // System.out.println("BFDataHdr:");
            // System.out.println(a);

            BaltradFrame bf = new BaltradFrame(a, fileNameH5);

            if (bfh.handleBF(bf) == 1) {

                msgl.showMessage(filePath.radarName + " file sent to BALTRAD",
                        true);

            } else {
                System.out.println(filePath.radarName
                        + " failed to send file to BALTRAD");
            }
        }

        if (ftpOptions != null) {

            for (int i = 0; i < ftpOptions.length; i++) {
                if (ftpOptions[i].isEmpty())
                    continue;

                // sending file to FTP
                FTPClient ftp = new FTPClient();
                FileInputStream fis = null;

                try {
                    int reply;
                    ftp.connect(ftpOptions[i].getAddress());

                    // After connection attempt, you should check the reply code
                    // to verify
                    // success.
                    reply = ftp.getReplyCode();

                    if (!FTPReply.isPositiveCompletion(reply)) {
                        ftp.disconnect();
                        System.err.println("FTP server refused connection.");
                        continue;
                    }

                    ftp.login(ftpOptions[i].getLogin(), ftpOptions[i]
                            .getPassword());
                    ftp.changeWorkingDirectory(ftpOptions[i].getDir());

                    ftp.setFileType(FTPClient.BINARY_FILE_TYPE);
                    ftp
                            .setFileTransferMode(FTPClient.PASSIVE_REMOTE_DATA_CONNECTION_MODE);

                    String sendFileName = filePath.prefix
                            + originalFile.getName().substring(0, 14) + ".tmp";

                    // transfer files
                    fis = new FileInputStream(fileNameH5);

                    ftp.storeFile(sendFileName, fis);
                    fis.close();

                    boolean sentOK = ftp.rename(sendFileName, sendFileName
                            .replace("tmp", "hdf"));

                    ftp.logout();

                    msgl.showMessage(filePath.radarName + " file sent to "
                            + ftpOptions[i].getAddress(), sentOK);

                } catch (IOException e) {

                    e.printStackTrace();
                }

            }

        }

        // System.out.print("original file: " + originalFile.getAbsolutePath() +
        // " ");
        // System.out.print(originalFile.delete() + "\n");
        // System.out.print("h5 file: " + h5file.getAbsolutePath() + " ");
        // System.out.print(h5file.delete() + "\n");
        // file = new File(fileNameH5);
        // file.delete();

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
                continue;
            } catch (ClosedWatchServiceException cwse) {
                // other thread closed watch service
                System.out.println("watch service closed, terminating.");
                break;
            }

            if (signalledKey == null) {
                long time;
                long timeNow = System.currentTimeMillis();
                Iterator<RadarOpt> itr = fileTimeMap.keySet().iterator();
                while (itr.hasNext()) {
                    RadarOpt key = itr.next();

                    time = fileTimeMap.get(key);
                    if ((timeNow - time) > 3000) {
                        fileTimeMap.remove(key);
                        itr = fileTimeMap.keySet().iterator();
                        msgl.showMessage(key.radarName + " download finished",
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
            List<WatchEvent<?>> list = signalledKey.pollEvents();

            // VERY IMPORTANT! call reset() AFTER pollEvents() to allow the
            // key to be reported again by the watch service
            signalledKey.reset();

            // we'll simply print what has happened; real applications
            // will do something more sensible here
            for (WatchEvent<?> e : list) {
                if (e.kind() == StandardWatchEventKind.ENTRY_CREATE) {
                    Path context = (Path) e.context();

                    radarOpt = new RadarOpt();
                    radarOpt.path = pathMap.get(signalledKey).getDir();
                    radarOpt.radarName = pathMap.get(signalledKey)
                            .getRadarName();
                    radarOpt.prefix = pathMap.get(signalledKey).getFileName();
                    radarOpt.platform = pathMap.get(signalledKey).getPlatform();
                    if (!radarOpt.path.endsWith("/"))
                        radarOpt.path += "/";

                    radarOpt.path += context.toString();

                    msgl.showMessage(radarOpt.radarName + " new file: "
                            + radarOpt.path, verbose);

                    fileTimeMap.put(radarOpt, System.currentTimeMillis());
                } else if (e.kind() == StandardWatchEventKind.ENTRY_MODIFY) {
                    Path context = (Path) e.context();

                    radarOpt = new RadarOpt();
                    radarOpt.path = pathMap.get(signalledKey).getDir();
                    radarOpt.radarName = pathMap.get(signalledKey)
                            .getRadarName();
                    radarOpt.prefix = pathMap.get(signalledKey).getFileName();
                    if (!radarOpt.path.endsWith("/"))
                        radarOpt.path += "/";

                    radarOpt.path += context.toString();
                    fileTimeMap.put(radarOpt, System.currentTimeMillis());

                } else if (e.kind() == StandardWatchEventKind.OVERFLOW) {
                    System.out
                            .println("OVERFLOW: more changes happened than we could retreive");
                }
            }

        }

    }

    class RadarOpt {
        private String path = "";
        private String radarName = "";
        private String prefix = "";
        private String platform = "";

    }

}