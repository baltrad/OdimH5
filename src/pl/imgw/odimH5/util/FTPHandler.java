/**
 * (C) 2010 INSTITUT OF METEOROLOGY AND WATER MANAGEMENT
 */
package pl.imgw.odimH5.util;

import java.io.File;
import java.io.FileOutputStream;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;
import java.util.Vector;

import ncsa.hdf.object.FileFormat;
import ncsa.hdf.object.h5.H5File;

import org.apache.commons.io.output.ByteArrayOutputStream;
import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;

import pl.imgw.odimH5.model.DataProcessorModel;
import pl.imgw.odimH5.model.rainbow.Model;
import pl.imgw.odimH5.model.rainbow.ModelPVOLH5;

/**
 * 
 * 
 * /Class description/
 * 
 * 
 * @author <a href="mailto:lukasz.wojtas@imgw.pl">Lukasz Wojtas</a>
 * 
 */
public class FTPHandler {

    /**
     * This method downloads volume files from ftp server and converts them to
     * HDF format. It looks for files created in specified period of time.
     * 
     * @param server
     *            address
     * @param username
     * @param password
     * @param folder
     *            full path to the folder containing files on remote machine
     * @param start
     *            begining of the period
     * @param end
     *            end of the period
     * @param rb
     *            Rainbow model class instance
     * @param proc
     *            Data processor model class instance
     * @param verbose
     *            verbose mode
     * @return String vector of files name
     */

    public static Vector<String> getVolFiles(String server, String username,
            String password, String folder, Calendar start, Calendar end,
            Model rb, DataProcessorModel proc, boolean verbose) {

        Vector<String> fileName = new Vector<String>();

        try {
            // Connect and logon to FTP Server
            FTPClient ftp = new FTPClient();
            ftp.connect(server);
            ftp.login(username, password);
            ftp.setFileType(FTP.BINARY_FILE_TYPE);
            System.out.println("Connected to " + server + ".");
            System.out.print(ftp.getReplyString());

            // List the files in the directory
            ftp.changeWorkingDirectory(folder);
            FTPFile[] files = ftp.listFiles();
            System.out.println("Number of files in dir: " + files.length);
            Calendar calGMT;

            for (int i = 0; i < files.length; i++) {
                calGMT = files[i].getTimestamp();
                calGMT.setTimeZone(TimeZone.getTimeZone("GMT"));
                Date fileDate = calGMT.getTime();

                if (fileDate.compareTo(start.getTime()) >= 0
                        && fileDate.compareTo(end.getTime()) <= 0) {

                    // Download a file from the FTP Server
                    System.out.print(files[i].getTimestamp().getTime());
                    System.out.println("\t" + files[i].getName());

                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    baos.flush();
                    ftp.retrieveFile(files[i].getName(), baos);
                    fileName.add(ModelPVOLH5.createDescriptor(files[i]
                            .getName(), baos.toByteArray(), verbose, rb, proc));
                    baos.close();
                }
            }

            // Logout from the FTP Server and disconnect
            ftp.logout();
            ftp.disconnect();

        } catch (Exception e) {
            e.printStackTrace();
        }
        return fileName;
    }

    /**
     * 
     * This method downloads HDF5 files from ftp server. It looks for files
     * created in specified period of time.
     * 
     * @param server
     *            address
     * @param username
     * @param password
     * @param folder
     *            full path to the folder containing files on remote machine
     * @param start
     *            begining of the period
     * @param end
     *            end of the period
     * @param rb
     *            Rainbow model class instance
     * @param proc
     *            Data processor model class instance
     * @param verbose
     *            verbose mode
     * @return String vector of files name
     * 
     */
    public static Vector<String> getH5Files(String server, String username,
            String password, String folder, Calendar start, Calendar end,
            Model rb, DataProcessorModel proc) {

        Vector<String> fileName = new Vector<String>();

        try {
            // Connect and logon to FTP Server
            FTPClient ftp = new FTPClient();
            ftp.connect(server);
            ftp.login(username, password);
            ftp.setFileType(FTP.BINARY_FILE_TYPE);
            System.out.println("Connected to " + server + ".");
            System.out.print(ftp.getReplyString());

            // List the files in the directory
            ftp.changeWorkingDirectory(folder);
            FTPFile[] files = ftp.listFiles();
            System.out.println("Number of files in dir: " + files.length);
            Calendar calGMT;

            for (int i = 0; i < files.length; i++) {
                calGMT = files[i].getTimestamp();
                calGMT.setTimeZone(TimeZone.getTimeZone("GMT"));
                Date fileDate = calGMT.getTime();

                if (fileDate.compareTo(start.getTime()) >= 0
                        && fileDate.compareTo(end.getTime()) <= 0) {

                    // Download a file from the FTP Server
                    System.out.print(files[i].getTimestamp().getTime());
                    System.out.println("\t" + files[i].getName());

                    File file = new File(files[i].getName());
                    FileOutputStream fos = new FileOutputStream(file);
                    ftp.retrieveFile(files[i].getName(), fos);
                    // checking if downloaded file if valid hdf5 file
                    if (verifyHDF5File(file)) {
                        fileName.add(file.getName());
                    }
                    fos.close();
                    file.setLastModified(fileDate.getTime());
                }
            }

            // Logout from the FTP Server and disconnect
            ftp.logout();
            ftp.disconnect();

        } catch (Exception e) {
            e.printStackTrace();
        }
        return fileName;
    }

    /**
     * Method used to verify whether current file is valid HDF5 file
     * 
     * @param fileName
     *            Input file name
     * @return True if current file is HDF5 file
     */
    private static boolean verifyHDF5File(File file) {
        boolean res;
        try {
            FileFormat fileFormat = FileFormat
                    .getFileFormat(FileFormat.FILE_TYPE_HDF5);
            H5File inputFile = (H5File) fileFormat.createInstance(file
                    .getName(), FileFormat.READ);
            inputFile.open();
            res = true;
        } catch (Exception e) {
            System.out.println(file.getName() + " is not valid hdf5 file.");
            res = false;
        }
        return res;
    }

}
