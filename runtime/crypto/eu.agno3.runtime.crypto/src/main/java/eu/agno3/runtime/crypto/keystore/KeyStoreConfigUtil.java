/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 05.11.2014 by mbechler
 */
package eu.agno3.runtime.crypto.keystore;


import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.Properties;

import org.apache.commons.lang3.StringUtils;

import eu.agno3.runtime.crypto.CryptoException;


/**
 * @author mbechler
 *
 */
public final class KeyStoreConfigUtil {

    private static final String PKCS11_LIB_FILE = "libpkcs11.so"; //$NON-NLS-1$
    private static final String PROPERTIES_FILE = "java.properties"; //$NON-NLS-1$
    private static final String TYPE_FILE = "type"; //$NON-NLS-1$
    private static final String PIN_FILE = "pin.txt"; //$NON-NLS-1$

    private static final Charset CONFIG_CHARSET = Charset.forName("UTF-8"); //$NON-NLS-1$


    /**
     * 
     */
    private KeyStoreConfigUtil () {}


    /**
     * @param keyStoreDir
     * @return the keystore properties
     * @throws CryptoException
     */
    public static Properties getKeystoreProperties ( File keyStoreDir ) throws CryptoException {
        File propertiesFile = new File(keyStoreDir, KeyStoreConfigUtil.PROPERTIES_FILE);
        Properties props = new Properties();
        try ( FileInputStream fis = new FileInputStream(propertiesFile);
              InputStreamReader fr = new InputStreamReader(fis, CONFIG_CHARSET) ) {
            props.load(fr);
        }
        catch ( IOException e ) {
            throw new CryptoException("Failed to load properties", e); //$NON-NLS-1$
        }
        return props;
    }


    /**
     * @param keyStoreDir
     * @return the PKCS11 library path
     * @throws CryptoException
     */
    public static String getPKCS11Lib ( File keyStoreDir ) throws CryptoException {
        File pkcs11LibFile = new File(keyStoreDir, KeyStoreConfigUtil.PKCS11_LIB_FILE);

        if ( !pkcs11LibFile.canRead() ) {
            throw new CryptoException("Failed to locate PKCS11 library"); //$NON-NLS-1$
        }

        return pkcs11LibFile.toString();

    }


    /**
     * @param keyStoreDir
     * @return the keystore pin
     * @throws CryptoException
     */
    public static String getPIN ( File keyStoreDir ) throws CryptoException {
        File pinFile = new File(keyStoreDir, KeyStoreConfigUtil.PIN_FILE);
        try ( FileInputStream fis = new FileInputStream(pinFile);
              InputStreamReader fr = new InputStreamReader(fis, CONFIG_CHARSET);
              BufferedReader br = new BufferedReader(fr) ) {
            String line = br.readLine();
            if ( line == null ) {
                throw new CryptoException("Illegal PIN file format"); //$NON-NLS-1$
            }
            return line.trim();
        }
        catch ( IOException e ) {
            throw new CryptoException("Failed to read PIN", e); //$NON-NLS-1$
        }
    }


    /**
     * @param keyStoreDir
     * @return the keystore type
     * @throws CryptoException
     */
    public static String getKeyStoreType ( File keyStoreDir ) throws CryptoException {
        File typeFile = new File(keyStoreDir, KeyStoreConfigUtil.TYPE_FILE);
        try ( FileInputStream fis = new FileInputStream(typeFile);
              InputStreamReader fr = new InputStreamReader(fis, CONFIG_CHARSET);
              BufferedReader br = new BufferedReader(fr) ) {
            String line = br.readLine();
            if ( line == null ) {
                throw new CryptoException("Illegal type file format"); //$NON-NLS-1$
            }
            return line.trim();
        }
        catch ( IOException e ) {
            throw new CryptoException("Failed to read type", e); //$NON-NLS-1$
        }
    }


    /**
     * @param props
     * @return the token extra config
     */
    public static String getExtraConfig ( Properties props ) {
        String initArgsProp = props.getProperty("extraConfig"); //$NON-NLS-1$
        if ( !StringUtils.isBlank(initArgsProp) ) {
            return initArgsProp.trim();
        }

        return null;
    }


    /**
     * @param props
     * @return the token initialization arguments
     */
    public static String getInitArgs ( Properties props ) {
        String initArgsProp = props.getProperty("initArgs"); //$NON-NLS-1$
        if ( !StringUtils.isBlank(initArgsProp) ) {
            return initArgsProp.trim();
        }

        return null;
    }


    /**
     * @param props
     * @return the slot index
     */
    public static int getSlotIndex ( Properties props ) {
        String slotIndexProp = props.getProperty("slotIndex"); //$NON-NLS-1$
        if ( !StringUtils.isBlank(slotIndexProp) ) {
            return Integer.parseInt(slotIndexProp);
        }

        return 0;
    }


    /**
     * @param props
     * @return the slot id
     */
    public static String getSlotId ( Properties props ) {
        String slotIdProp = props.getProperty("slotId"); //$NON-NLS-1$
        if ( !StringUtils.isBlank(slotIdProp) ) {
            return slotIdProp.trim();
        }

        return null;
    }

}
