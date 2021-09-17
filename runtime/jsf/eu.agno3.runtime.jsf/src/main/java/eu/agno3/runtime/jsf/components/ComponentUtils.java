/**
 * © 2016 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: Sep 30, 2016 by mbechler
 */
package eu.agno3.runtime.jsf.components;


import javax.enterprise.context.ApplicationScoped;
import javax.inject.Named;

import org.apache.commons.lang3.StringUtils;


/**
 * @author mbechler
 *
 */
@Named ( "componentUtils" )
@ApplicationScoped
public class ComponentUtils {

    /**
     * @param disabled
     * @return ui-state-disabled if disabled
     */
    public static String disabledIf ( boolean disabled ) {
        return ifTrue(disabled, "ui-state-disabled"); //$NON-NLS-1$
    }


    /**
     * 
     * @param b
     * @param val
     * @return val if b is true, empty otherwise
     */
    public static String ifTrue ( boolean b, String val ) {
        if ( b ) {
            return val;
        }
        return StringUtils.EMPTY;
    }
}
