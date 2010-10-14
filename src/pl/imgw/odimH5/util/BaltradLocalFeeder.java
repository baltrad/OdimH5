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

import org.w3c.dom.Document;

import pl.imgw.odimH5.model.DataProcessorModel;
import pl.imgw.odimH5.model.rainbow.Model;
import pl.imgw.odimH5.model.rainbow.ModelPVOL;

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
    private OptionContainer[] options;
    Path[] watchedPath;
    String path = "";

    HashMap<WatchKey, String> pathMap = new HashMap<WatchKey, String>();
    HashMap<String, Long> fileTimeMap = new HashMap<String, Long>();

    private Model rb;
    private DataProcessorModel proc;
    private String sender, server, radarName;
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
     * @param verbose
     *            verbose mode
     */
    public BaltradLocalFeeder(Document optionsDoc, Model rb,
            DataProcessorModel proc, boolean verbose) {

        sender = OptionsHandler.getElementByName(optionsDoc,
                OptionsHandler.SENDER);
        server = OptionsHandler.getElementByName(optionsDoc,
                OptionsHandler.SERVER);
        radarName = OptionsHandler.getElementByName(optionsDoc,
                OptionsHandler.SERVER);
        this.rb = rb;
        this.proc = proc;
        this.verbose = verbose;

        options = OptionsHandler.getOptions(optionsDoc);
        watchedPath = new Path[options.length];
        WatchKey key[] = new WatchKey[options.length];

        for (int i = 0; i < options.length; i++) {
            watchedPath[i] = Paths.get(options[i].getDir());

            try {
                key[i] = watchedPath[i].register(watchService,
                        StandardWatchEventKind.ENTRY_CREATE,
                        StandardWatchEventKind.ENTRY_MODIFY);
                pathMap.put(key[i], options[i].getDir());
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
    private void convertAndSendFile(String filePath) {

        File file = new File(filePath);
        String fileNameH5 = "";

        if (filePath.endsWith(".vol")) {

            fileNameH5 = file.getName().substring(0,
                    file.getName().indexOf("."))
                    + ".h5";

            int file_len = (int) file.length();
            byte[] file_buf = new byte[file_len];

            try {
                FileInputStream fis = new FileInputStream(file);
                fis.read(file_buf, 0, file_len);
                fis.close();

            } catch (IOException e) {
                e.printStackTrace();
            }

            if (!ModelPVOL.createDescriptor(fileNameH5, file_buf, this.verbose,
                    this.rb))
                return;
        } else if (filePath.endsWith(".h5")) {
            fileNameH5 = file.getName();
        } else {
            System.out.println("plik nie obslugiwany");
            return;
        }

        BaltradFrameHandler bfh = new BaltradFrameHandler(proc
                .getMessageLogger(), server, verbose);

        String a = bfh.createDataHdr(BaltradFrameHandler.MIME_MULTIPART,
                sender, radarName, fileNameH5);

        // System.out.println("BFDataHdr:");
        // System.out.println(a);

        BaltradFrame bf = new BaltradFrame(proc.getMessageLogger(), a,
                fileNameH5, verbose);

        bfh.handleBF(bf);

        // file.delete();
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
                System.out.println("nic");
                long time;
                long timeNow = System.currentTimeMillis();
                Iterator<String> itr = fileTimeMap.keySet().iterator();
                while (itr.hasNext()) {
                    String key = itr.next();
                    time = fileTimeMap.get(key);
                    if ((timeNow - time) > 3000) {
                        fileTimeMap.remove(key);
                        itr = fileTimeMap.keySet().iterator();
                        System.out.println(key + " download finished");
                        convertAndSendFile(key);

                    }
                }
                if (fileTimeMap.isEmpty()) {
                    try {
                        System.out.println("czekam na akcje....");
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
            path = pathMap.get(signalledKey);
            // VERY IMPORTANT! call reset() AFTER pollEvents() to allow the
            // key to be reported again by the watch service
            signalledKey.reset();

            // we'll simply print what has happened; real applications
            // will do something more sensible here
            for (WatchEvent<?> e : list) {
                if (e.kind() == StandardWatchEventKind.ENTRY_CREATE) {
                    Path context = (Path) e.context();
                    fileTimeMap.put(path + "/" + context.toString(), System
                            .currentTimeMillis());
                } else if (e.kind() == StandardWatchEventKind.ENTRY_MODIFY) {
                    Path context = (Path) e.context();
                    fileTimeMap.put(path + "/" + context.toString(), System
                            .currentTimeMillis());
                } else if (e.kind() == StandardWatchEventKind.OVERFLOW) {
                    System.out
                            .println("OVERFLOW: more changes happened than we could retreive");
                }
            }

        }

    }
}