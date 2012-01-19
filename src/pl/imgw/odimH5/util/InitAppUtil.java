/*
 * OdimH5 :: Converter software for OPERA Data Information Model
 * Remote Sensing Department, Institute of Meteorology and Water Management
 * Maciej Szewczykowski, 2009-2012
 *
 * maciej.szewczykowski@imgw.pl
 */
package pl.imgw.odimH5.util;

import java.util.Properties;
import java.io.InputStream;
import java.io.IOException;

/**
 *
 * @author szewczenko
 */
public class InitAppUtil {
    
    private static final String PROPS_FILE_NAME = "odim.properties";
    private static final String APP_VERSION_KEY = "app.version";
    private static final String HOST_NAME_KEY = "host.name";
    private static final String HOST_ADDR_KEY = "host.address";
    private static final String KEYSTORE_DIR_KEY = "keystore.dir";
    private static final String CONF_DIR_KEY = "conf.dir";
    private static final String LOG_DIR_KEY = "log.dir";
    private static final String CONN_TIMEOUT_KEY = "conn.timeout";
    private static final String SO_TIMEOUT_KEY = "so.timeout";
    
    private static InitAppUtil instance = null;
    private static Properties props = null;;
    
    protected InitAppUtil() {}
    
    public static InitAppUtil getInstance() {
        try {
            if (instance == null) {
                instance = new InitAppUtil();
                InputStream is = instance.getClass().getResourceAsStream(PROPS_FILE_NAME);
                props = new Properties();
                props.load(is);
            }
        } catch (IOException e) {
            System.out.println("Failed to initialize application: " + e.getMessage());
        }
        return instance;
    }
    
    public String getAppVersion() {
        return props.getProperty(APP_VERSION_KEY);
    }
    public String getHostName() {
        return props.getProperty(HOST_NAME_KEY);
    }
    public String getHostAddress() {
        return props.getProperty(HOST_ADDR_KEY);
    }
    public String getKeystoreDir() {
        return props.getProperty(KEYSTORE_DIR_KEY);
    }
    public String getConfDir() {
        return props.getProperty(CONF_DIR_KEY);
    }
    public String getLogDir() {
        return props.getProperty(LOG_DIR_KEY);
    }
    public int getConnTimeout() {
        return Integer.parseInt(props.getProperty(CONN_TIMEOUT_KEY));
    }
    public int getSoTimeout() {
        return Integer.parseInt(props.getProperty(SO_TIMEOUT_KEY));
    }
}
