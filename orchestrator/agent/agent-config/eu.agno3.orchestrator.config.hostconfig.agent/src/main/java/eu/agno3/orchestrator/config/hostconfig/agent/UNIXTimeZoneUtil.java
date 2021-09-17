/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 15.05.2014 by mbechler
 */
package eu.agno3.orchestrator.config.hostconfig.agent;


import org.joda.time.DateTimeZone;


/**
 * @author mbechler
 * 
 */
public final class UNIXTimeZoneUtil {

    private UNIXTimeZoneUtil () {}


    /**
     * @param timezone
     * @return The timezone identifier for the given timezone
     */
    public static String mapTzToUnix ( DateTimeZone timezone ) {
        return timezone.getID();
    }
}
