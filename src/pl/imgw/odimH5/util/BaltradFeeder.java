/**
 * (C) 2010 INSTITUT OF METEOROLOGY AND WATER MANAGEMENT
 */
package pl.imgw.odimH5.util;

import java.io.File;
import java.util.Calendar;
import java.util.TimeZone;
import java.util.Vector;

import org.w3c.dom.Document;

import pl.imgw.odimH5.model.DataProcessorModel;
import pl.imgw.odimH5.model.rainbow.Model;

/**
 * 
 * /Class description/
 * 
 * 
 * @author <a href="mailto:lukasz.wojtas@imgw.pl">Lukasz Wojtas</a>
 * 
 */
public class BaltradFeeder extends Thread {

    private final static String RVOL = "RVOL";
    private final static String H5 = "H5";
    private String fileFormat;
    private OptionContainer[] options;
//    private int startingTime;
    private int repetitionTime;
    private int counter;
    private Model rb;
    private DataProcessorModel proc;
    private String sender, address;
    private boolean verbose;

    /**
     * 
     * Works as a thread, wakes up each time new volume file is expected. It
     * downloads new file and converts it to HDF5 if needed.
     * 
     * @param optionsDoc
     *            XML document containing working options
     * @param rb
     *            Rainbow model class instance
     * @param proc
     *            Data processor model class instance
     * @param fileFormat
     *            format of files to be downloaded (RVOL, H5)
     * @param verbose
     *            verbose mode
     */

    public BaltradFeeder(Document optionsDoc, Model rb,
            DataProcessorModel proc, String fileFormat, boolean verbose) {

        options = OptionsHandler.getOptions(optionsDoc);
//        startingTime = OptionsHandler.getTime(optionsDoc, "start_time");
        repetitionTime = OptionsHandler.getTime(optionsDoc, "repetition_time");
        sender = OptionsHandler.getElementByName(optionsDoc, "sender");
        address = OptionsHandler.getElementByName(optionsDoc, "server");

        this.counter = options.length;
        this.rb = rb;
        this.proc = proc;
        this.fileFormat = fileFormat;
        this.verbose = verbose;
    }

    @Override
    public void run() {
        try {

            Calendar startDate = Calendar.getInstance(TimeZone
                    .getTimeZone("GMT"));
            Calendar endDate = Calendar
                    .getInstance(TimeZone.getTimeZone("GMT"));

            int minutes = startDate.get(Calendar.MINUTE);

            endDate.set(Calendar.MINUTE, (minutes / repetitionTime)
                    * repetitionTime );
            startDate.set(Calendar.MINUTE, (minutes / repetitionTime - 1)
                    * repetitionTime );

            while (true) {

                Vector<String> fileName = null;

                System.out.println("start: " + startDate.getTime());
                System.out.println("end: " + endDate.getTime());

                Calendar calNow = Calendar.getInstance(TimeZone
                        .getTimeZone("GMT"));

                System.out.println("now: " + calNow.getTime());

                if (calNow.getTime().after(endDate.getTime())) {

                    for (int i = 0; i < counter; i++) {

                        if (fileFormat.matches(RVOL)) {
                            fileName = FTPHandler.getVolFiles(options[i]
                                    .getAddress(), options[i].getLogin(),
                                    options[i].getPassword(), options[i]
                                            .getRemoteDir(), startDate,
                                    endDate, rb, proc, verbose);
                        } else if (fileFormat.matches(H5)) {
                            fileName = FTPHandler.getH5Files(options[i]
                                    .getAddress(), options[i].getLogin(),
                                    options[i].getPassword(), options[i]
                                            .getRemoteDir(), startDate,
                                    endDate, rb, proc);
                        }
                        
//                        boolean ad = false;
                        
                        if (!fileName.isEmpty()) {
                            for (int j = 0; j < fileName.size(); j++) {

                                BaltradFrameHandler bfh = new BaltradFrameHandler(
                                        proc.getMessageLogger(), address);

                                String a = bfh.createBFDataHdr(
                                        BaltradFrameHandler.BF_MIME_MULTIPART,
                                        sender, options[i].getRadarName(),
                                        fileName.get(j));

//                                System.out.println("BFDataHdr:");
//                                System.out.println(a);

                                BaltradFrame bf = new BaltradFrame(proc
                                        .getMessageLogger(), a, fileName.get(j));

                                bfh.handleBF(bf);

                                File file = new File(fileName.get(j));
                                file.delete();
                            }
                        }

                    }
                    startDate.setTime(endDate.getTime());
                    endDate.add(Calendar.MINUTE, repetitionTime);

                } else {
                    try {
                        sleep(60000);

                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();

        }

    }

}
