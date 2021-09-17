/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 25.03.2015 by mbechler
 */
package eu.agno3.fileshare.webgui.admin.quota;


import javax.enterprise.context.ApplicationScoped;
import javax.inject.Named;

import eu.agno3.runtime.util.format.ByteSizeFormatter;


/**
 * @author mbechler
 *
 */
@ApplicationScoped
@Named ( "app_fs_adm_quotaFormatter" )
public class QuotaFormatter {

    /**
     * 
     * @param obj
     * @return a formatted byte size ( 1000 base )
     */
    public static String format ( Object obj ) {
        return ByteSizeFormatter.formatByteSizeSI(obj);
    }


    /**
     * 
     * @param size
     * @return the unit exponent
     */
    public static int getBaseExponent ( Long size ) {

        if ( size == null ) {
            return 0;
        }

        int exp = 0;
        if ( size > 0 ) {
            exp = (int) ( Math.log(size) / Math.log(1000) );
        }

        return exp;
    }
}
