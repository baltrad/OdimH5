/**
 * (C) 2013 INSTITUT OF METEOROLOGY AND WATER MANAGEMENT
 */
package pl.imgw.odimH5.util;

/**
 *
 *  /Class description/
 *
 *
 * @author <a href="mailto:lukasz.wojtas@imgw.pl">Lukasz Wojtas</a>
 * 
 */
public class FTPContainer {

    public static final int NO_SUBFOLDERS = 0;
    public static final int RADAR_NAME_SUBFOLDERS = 1;
    public static final int RADAR_NAME_DATE_SUBFOLDERS = 2;
    
    private String address = "";
    private String login = "";
    private StringBuilder password = new StringBuilder("");
    private String remoteDir = "/";
    private boolean subfolder = true;
    private int subfolders = 0;
    
    
    /**
     * 
     *
    public FTPContainer(String address, String login, StringBuilder password, String remoteDir, boolean subfolder) {
        if (address != null)
            this.address = address;
        if (login != null)
            this.login = login;
        if (password != null)
            this.password = password;
        if (remoteDir != null)
            this.remoteDir = remoteDir;
        this.subfolder = subfolder;
    }
    */
    
    /**
     * 
     * @param address
     *            remote ftp server address
     * @param login
     *            ftp server login
     * @param password
     *            ftp server password
     * @param remoteDir
     *            remote directory, use empty string if not needed
     * @param subfolders
     *            choose if you want ftp sender to create subfolders for
     *            outcoming data
     */
    public FTPContainer(String address, String login, StringBuilder password, String remoteDir, int subfolders) {
        if (address != null)
            this.address = address;
        if (login != null)
            this.login = login;
        if (password != null)
            this.password = password;
        if (remoteDir != null)            
            this.remoteDir = remoteDir;
        this.subfolders = subfolders;
    }
    
    /**
     * @return the address
     */
    public String getAddress() {
        return address;
    }

    /**
     * @return the login, empty string if not set
     */
    public String getLogin() {
        return login;
    }

    /**
     * @return the password, empty string if not set
     */
    public String getPassword() {
        return password.toString();
    }

    /**
     * @return the remoteDir without slash (/) at the end, empty string if not set
     */
    public String getRemoteDir() {
        if(remoteDir.endsWith("/"))
            return remoteDir.substring(0, remoteDir.length() - 1);
        return remoteDir;
    }


    public int getSubfolders() {
        return subfolders;
    }
    
    /**
     * @return the subfolders
     */
    public boolean isSubfolders() {
        return subfolder;
    }

}
