/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 03.12.2014 by mbechler
 */
package eu.agno3.orchestrator.system.img.util;


import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;


/**
 * @author mbechler
 *
 */

public final class SystemImageUtil {

    private static final Logger log = Logger.getLogger(SystemImageUtil.class);

    private static final String AGNO3_RELEASE_FILE_PATH = "/etc/agno3-release"; //$NON-NLS-1$
    private static final Charset CHARSET = Charset.forName("UTF-8"); //$NON-NLS-1$

    private static final String IMAGE_TYPE = "AGNO3_IMAGE_TYPE"; //$NON-NLS-1$
    private static final String VERSION = "AGNO3_VERSION"; //$NON-NLS-1$
    private static final String BUILD = "AGNO3_BUILD"; //$NON-NLS-1$
    private static final String APP_BUILD = "AGNO3_APPBUILD"; //$NON-NLS-1$
    private static final String DISTRIBUTION = "DISTRIBUTION"; //$NON-NLS-1$
    private static final String APPLIANCE_NAME = "AGNO3_APPLIANCE"; //$NON-NLS-1$

    private static Map<String, String> PROPERTIES;


    /**
     * 
     */
    private SystemImageUtil () {}


    /**
     * @return the image properties from /etc/agno3-release
     */
    public static Map<String, String> getImageProperties () {
        if ( PROPERTIES != null ) {
            return PROPERTIES;
        }
        Map<String, String> props = new HashMap<>();
        File f = new File(AGNO3_RELEASE_FILE_PATH);

        try ( FileInputStream fis = new FileInputStream(f);
              InputStreamReader isr = new InputStreamReader(fis, CHARSET);
              BufferedReader br = new BufferedReader(isr) ) {

            String line;

            while ( ( line = br.readLine() ) != null ) {

                int sepIdx = line.indexOf('=');
                if ( sepIdx < 0 ) {
                    continue;
                }

                String[] split = StringUtils.split(line, "=", 2); //$NON-NLS-1$

                if ( split == null || split.length != 2 ) {
                    continue;
                }

                String key = split[ 0 ];
                String val = split[ 1 ];

                if ( val.charAt(0) == '"' && val.charAt(val.length() - 1) == '"' ) {
                    val = val.substring(1, val.length() - 1);
                }

                props.put(key.trim(), val.trim());
            }
        }
        catch ( IOException e ) {
            log.warn("No release property file found at " + AGNO3_RELEASE_FILE_PATH); //$NON-NLS-1$
            log.debug("Failed to get image type", e); //$NON-NLS-1$
        }

        PROPERTIES = props;
        return props;
    }


    /**
     * @return the appliance image type URI
     */
    public static String getLocalImageType () {
        return getImageProperties().get(IMAGE_TYPE);
    }


    /**
     * @return the appliance display name
     */
    public static String getLocalApplianceName () {
        return getImageProperties().get(APPLIANCE_NAME);
    }


    /**
     * @return the appliance version
     */
    public static String getApplianceVersion () {
        return getImageProperties().get(VERSION);
    }


    /**
     * 
     * @return the base distribution codename
     */
    public static String getDistributionCodename () {
        return getImageProperties().get(DISTRIBUTION);
    }


    /**
     * @return the base system build number
     */
    public static String getBaseBuild () {
        return getImageProperties().get(BUILD);
    }


    /**
     * @return the appliance build version
     */
    public static String getApplianceBuild () {
        String appBuild = getImageProperties().get(APP_BUILD);
        if ( !StringUtils.isBlank(appBuild) ) {
            return appBuild;
        }
        return getImageProperties().get(BUILD);
    }

}
