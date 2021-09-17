/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 23.10.2015 by mbechler
 */
package eu.agno3.fileshare.util;


import java.io.IOException;
import java.net.URLDecoder;
import java.net.URLEncoder;

import org.apache.commons.lang3.StringUtils;


/**
 * Filename encoding utils
 * 
 * This is based on URL encoding, but double encodes spaces and plus signs to prevent issues with the encoding rules for
 * these
 * 
 * @author mbechler
 *
 */
public class FilenameUtil {

    /**
     * 
     */
    private static final String CHARSET = "UTF-8"; //$NON-NLS-1$


    /**
     * @param localName
     * @return an encoded filename or path
     */
    public static String encodeFileName ( String localName ) {
        try {
            return StringUtils.replace(
                URLEncoder.encode(localName, CHARSET),
                "+", //$NON-NLS-1$
                "%20"); //$NON-NLS-1$
        }
        catch ( IOException e ) {
            throw new IllegalStateException(e);
        }
    }


    /**
     * 
     * @param encoded
     * @return the decoded filename
     */
    public static String decodeFileName ( String encoded ) {
        try {
            return URLDecoder.decode(encoded, CHARSET);
        }
        catch ( IOException e ) {
            throw new IllegalStateException(e);
        }
    }

}
