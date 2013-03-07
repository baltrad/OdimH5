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

    private String address;
    private String login;
    private StringBuilder password;
    private String remoteDir;
    
    /**
     * 
     */
    public FTPContainer(String address, String login, StringBuilder password, String remoteDir) {
        this.address = address;
        this.login = login;
        this.password = password;
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
     * @return the login
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
     * @return the password
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
     * @return the remoteDir
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
