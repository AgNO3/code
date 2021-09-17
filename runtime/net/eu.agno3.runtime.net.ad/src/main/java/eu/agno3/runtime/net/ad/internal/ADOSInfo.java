/**
 * © 2017 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: Oct 30, 2017 by mbechler
 */
package eu.agno3.runtime.net.ad.internal;

import java.util.Dictionary;

import eu.agno3.runtime.util.config.ConfigUtil;

/**
 * @author mbechler
 *
 */
public class ADOSInfo {

    /**
     * Default OS name
     */
    public static final String DEFAULT_NAME = "AgNO3 Orchestrator host"; //$NON-NLS-1$

    /**
     * Default OS version
     * 
     * there is some magic going on based on this attribute
     * for example etype support seems to depend on this value
     * (while there is an attribute that actually specifies that)
     * This is the value from a win7 machine
     */
    public static final String DEFAULT_VERSION = "6.1 (7601)"; //$NON-NLS-1$

    /**
     * Default OS service pack
     */
    public static final String DEFAULT_SERVICE_PACK = "-"; //$NON-NLS-1$

    private final String servicePack;
    private final String version;
    private final String name;


    /**
     * 
     */
    public ADOSInfo () {
        this(DEFAULT_NAME, DEFAULT_VERSION, DEFAULT_SERVICE_PACK);
    }


    /**
     * @param name
     * @param version
     * @param servicePack
     */
    public ADOSInfo ( String name, String version, String servicePack ) {
        this.name = name;
        this.version = version;
        this.servicePack = servicePack;
    }


    /**
     * @return os name
     */
    public String getOSName () {
        return this.name;
    }


    /**
     * @return os version
     */
    public String getOSVersion () {
        return this.version;
    }


    /**
     * @return os service pack
     */
    public String getOSServicePack () {
        return this.servicePack;
    }


    /**
     * @param props
     * @return
     */
    protected static ADOSInfo fromProperties ( Dictionary<String, Object> props ) {
        String osName = ConfigUtil.parseString(
            props,
            "osName", //$NON-NLS-1$
            DEFAULT_NAME);
    
        String osVersion = ConfigUtil.parseString(
            props,
            "osVer", //$NON-NLS-1$
            DEFAULT_VERSION);
    
        String osServicePack = ConfigUtil.parseString(
            props,
            "osServicePack", //$NON-NLS-1$
            DEFAULT_SERVICE_PACK);
    
        return new ADOSInfo(osName, osVersion, osServicePack);
    }
}
