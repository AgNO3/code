/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 23.01.2014 by mbechler
 */
package eu.agno3.runtime.util.log;


import org.apache.commons.lang3.StringUtils;


/**
 * @author mbechler
 *
 */
/**
 * @author mbechler
 * 
 */
public final class LogFormatter {

    /**
     * 
     */
    private static final String ELEMENT_SEPARATOR = ", "; //$NON-NLS-1$


    /**
     * 
     */
    private LogFormatter () {}


    /**
     * @param iterable
     * @return a joined string of all elements separated by ,
     */
    public static String format ( Iterable<?> iterable ) {
        return StringUtils.join(iterable, ELEMENT_SEPARATOR);
    }


    /**
     * @param items
     * @return a joined string of all elements separated by ,
     */
    public static String format ( Object[] items ) {
        return StringUtils.join(items, ELEMENT_SEPARATOR);
    }

}
