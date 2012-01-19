/**
 * (C) 2010 INSTITUT OF METEOROLOGY AND WATER MANAGEMENT
 */
package pl.imgw.odimH5.util;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.text.SimpleDateFormat;
import java.util.Calendar;

/**
 * 
 * /Class description/
 * 
 * 
 * @author <a href="mailto:lukasz.wojtas@imgw.pl">Lukasz Wojtas</a>
 * 
 */
public class LogsHandler {

    public static final String LOG_FILE = "error.log";

    private static String getLogPath() {
        //return new File(Main.getProgPath(), PROGRAM_LOGS_FILE).getPath();
        InitAppUtil init = InitAppUtil.getInstance();
        return init.getLogDir() + File.separator + LOG_FILE;
    }
    
    /**
     * Make a note in log file.
     * 
     * @param className
     *            Name of the class where the error occurred
     * @param text
     *            Output text
     */
    public static void saveProgramLogs(String className, String text) {
        SimpleDateFormat sdf = new SimpleDateFormat("MM/dd HH:mm");
        Calendar cal = Calendar.getInstance();
        saveLine(sdf.format(cal.getTime()) + "; [" + className + "] " + text,
                getLogPath(), cal.getTimeZone().getID());
    }

    /**
     * Make a note in log file.
     * 
     * @param text
     *            Output text
     */
    public static void saveProgramLogs(String text) {
        System.out.println(getLogPath());
        SimpleDateFormat sdf = new SimpleDateFormat("MM/dd HH:mm");
        Calendar cal = Calendar.getInstance();
        saveLine(sdf.format(cal.getTime()) + "; " + text, getLogPath(),
                cal.getTimeZone().getID());
    }

    private static void saveLine(String text, String pathName, String timeZone) {

        File file = new File(pathName);
        if (!file.canRead()) {
            //new File(file.getParent()).mkdirs();
            FileOutputStream fileStream;

            try {
                fileStream = new FileOutputStream(pathName);
                PrintWriter pw = new PrintWriter(fileStream, true);
                pw.println("// Time zone: " + timeZone);
                fileStream.close();
            } catch (FileNotFoundException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
        try {
            RandomAccessFile rf = new RandomAccessFile(pathName, "rw");
            FileChannel fc = rf.getChannel();
            fc.position(fc.size());
            fc.write(ByteBuffer.wrap((text + "\n").getBytes()));
            fc.close();

        } catch (FileNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

}