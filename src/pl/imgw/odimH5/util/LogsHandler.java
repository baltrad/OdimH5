/**
 * (C) 2010 INSTITUT OF METEOROLOGY AND WATER MANAGEMENT
 */
package pl.imgw.odimH5.util;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.text.SimpleDateFormat;
import java.util.Calendar;

import pl.imgw.odimH5.AplicationConstans;

/**
 * 
 * /Class description/
 * 
 * 
 * @author <a href="mailto:lukasz.wojtas@imgw.pl">Lukasz Wojtas</a>
 * 
 */
public class LogsHandler {

    private static final SimpleDateFormat sdf = new SimpleDateFormat("[MM/dd HH:mm:ss]");
    private static final String RECENT_FILES = "recent.log";
    private static final String RECENT_TMP_FILES = "recent.tmp";
    public static final String LOG_FILE = "error.log";

    private static String getLogPath() {
        //return new File(Main.getProgPath(), PROGRAM_LOGS_FILE).getPath();
        InitAppUtil init = InitAppUtil.getInstance();
//        return init.getLogDir() + File.separator + LOG_FILE;
        return new File(AplicationConstans.LOG, LOG_FILE).getPath();
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

    /**
     * Save recent file name to log file. Keep only 10 newest lines in the log
     * file.
     * 
     * @param recantFile
     *            name of the file
     */
    public static void saveRecentFile(String recantFile) {
        saveRecentFile(recantFile, "localhost");
    }

    /**
     * 
     * Save recent file name to log file. Keep only 10 most recent lines in the log
     * file.
     * 
     * @param recantFile
     *            name of the file
     * @param remoteHost
     *            separate log file for this host will be created
     */
    public static void saveRecentFile(String recantFile, String remoteHost) {

        Calendar cal = Calendar.getInstance();
        
        String line = sdf.format(cal.getTime());
        line += ": " + recantFile;
        
        if(!remoteHost.isEmpty()) {
            line += " file stored in " + remoteHost;
        }
        
        File tmp = new File(AplicationConstans.LOG, remoteHost + "_"
                + RECENT_TMP_FILES);
        File old = new File(AplicationConstans.LOG, remoteHost + "_"
                + RECENT_FILES);
        
        try {
            old.createNewFile();

            BufferedReader br = new BufferedReader(new FileReader(old));
            BufferedWriter bw = new BufferedWriter(new FileWriter(tmp));
            
            bw.write(String.format("%s%n", line));
//            String l;
            
            for (int i = 0; i < 10; i++) {
                line = br.readLine();
                if (line == null)
                    break;
                bw.write(String.format("%s%n", line));
            }

            br.close();
            bw.close();
            
            if (old.delete()) {
                tmp.renameTo(old);
            }
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
    
}