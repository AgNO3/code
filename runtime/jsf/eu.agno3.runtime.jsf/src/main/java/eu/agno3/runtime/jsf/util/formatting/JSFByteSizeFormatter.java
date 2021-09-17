/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 25.02.2015 by mbechler
 */
package eu.agno3.runtime.jsf.util.formatting;


import javax.enterprise.context.ApplicationScoped;
import javax.inject.Named;

import eu.agno3.runtime.util.format.ByteSizeFormatter;


/**
 * @author mbechler
 *
 */
@ApplicationScoped
@Named ( "byteSizeFormatter" )
public class JSFByteSizeFormatter {

    /**
     * 
     * @param obj
     *            number of bytes
     * @return formatted result including unit (1024 based)
     */
    public static String formatByteSize ( Object obj ) {
        return ByteSizeFormatter.formatByteSize(obj);
    }


    /**
     * 
     * @param obj
     *            number of bytes
     * @return formatted result including unit (1000 based)
     */
    public static String formatByteSizeSI ( Object obj ) {
        return ByteSizeFormatter.formatByteSizeSI(obj);
    }
}
