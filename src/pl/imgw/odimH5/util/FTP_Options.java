/**
 * (C) 2010 INSTITUT OF METEOROLOGY AND WATER MANAGEMENT
 */
package pl.imgw.odimH5.util;

/**
 * 
 * /Class description/
 * 
 * 
 * @author <a href="mailto:lukasz.wojtas@imgw.pl">Lukasz Wojtas</a>
 * 
 */
public class FTP_Options {
    
    private String[] radars = null;
    private String address = "";
    private String login = "";
    private String password = "";
    private String dir = "";

    
    public String[] getRadars() {
        return radars;
    }

    public void setRadars(String[] radars) {
        this.radars = radars;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getLogin() {
        return login;
    }

    public void setLogin(String login) {
        this.login = login;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getDir() {
        return dir;
    }

    public void setDir(String dir) {
        this.dir = dir;
    }

    /**
     * 
     * Returns true if any of its fields is empty
     * 
     * @return
     */
    public boolean isEmpty() {
        if(radars == null)
            return true;
        
        if(address == null || address.isEmpty())
            return true;
        
        if(login == null || login.isEmpty())
            return true;
        
        if(password == null || password.isEmpty())
            return true;
        
//        if(dir == null || dir.isEmpty())
//            return true;
        
        return false;
        
    }

}
