/**
 * © 2013 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 11.06.2013 by mbechler
 */
package eu.agno3.runtime.console.osgi;


import java.util.HashMap;
import java.util.Map;

import org.osgi.framework.Bundle;


/**
 * Proper enum for osgi bundle states
 * 
 * @author mbechler
 * 
 */
@SuppressWarnings ( "javadoc" )
public enum BundleState {
    UNINSTALLED(Bundle.UNINSTALLED),
    INSTALLED(Bundle.INSTALLED),
    RESOLVED(Bundle.RESOLVED),
    STARTING(Bundle.STARTING),
    STOPPING(Bundle.STOPPING),
    ACTIVE(Bundle.ACTIVE);

    private int stateCode;


    BundleState ( int stateCode ) {
        this.stateCode = stateCode;
    }


    /**
     * @return the stateCode
     */
    public int getStateCode () {
        return this.stateCode;
    }

    private static Map<Integer, BundleState> STATE_MAP = new HashMap<>();

    static {
        STATE_MAP.put(Bundle.UNINSTALLED, UNINSTALLED);
        STATE_MAP.put(Bundle.INSTALLED, INSTALLED);
        STATE_MAP.put(Bundle.RESOLVED, RESOLVED);
        STATE_MAP.put(Bundle.STARTING, STARTING);
        STATE_MAP.put(Bundle.STOPPING, STOPPING);
        STATE_MAP.put(Bundle.ACTIVE, ACTIVE);
    }


    /**
     * 
     * @param code
     * @return the BundleState enum value for a osgi state code
     */
    public static BundleState fromStateCode ( int code ) {

        BundleState s = STATE_MAP.get(code);

        if ( s == null ) {
            throw new IllegalArgumentException(String.format("Unknown BundleState %d", code)); //$NON-NLS-1$
        }

        return s;
    }
}