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

    private String address = "";
    private String login = "";
    private StringBuilder password = new StringBuilder("");
    private String remoteDir = "/";
    
    /**
     * 
     */
    public FTPContainer(String address, String login, StringBuilder password, String remoteDir) {
        if (address != null)
            this.address = address;
        if (login != null)
            this.login = login;
        if (password != null)
            this.password = password;
        if (remoteDir != null)
            this.remoteDir = remoteDir;
    }
    
    /**
     * @return the address
     */
    public String getAddress() {
        return address;
    }
    /**
     * @param address the address to set
     */
    public void setAddress(String address) {
        this.address = address;
    }
    /**
     * @return the login, empty string if not set
     */
    public String getLogin() {
        return login;
    }
    /**
     * @param login the login to set
     */
    public void setLogin(String login) {
        this.login = login;
    }
    /**
     * @return the password, empty string if not set
     */
    public String getPassword() {
        return password.toString();
    }
    /**
     * @param password the password to set
     */
    public void setPassword(String password) {
        this.password = new StringBuilder(password);
    }
    /**
     * @return the remoteDir, empty string if not set
     */
    public String getRemoteDir() {
        return remoteDir;
    }
    /**
     * @param remoteDir the remoteDir to set
     */
    public void setRemoteDir(String remoteDir) {
        this.remoteDir = remoteDir;
    }
    
}
