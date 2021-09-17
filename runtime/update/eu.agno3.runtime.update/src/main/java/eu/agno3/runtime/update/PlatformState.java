/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 17.12.2014 by mbechler
 */
package eu.agno3.runtime.update;


/**
 * @author mbechler
 *
 */
public enum PlatformState {

    /**
     * Runtime currently booting
     */
    BOOTING,

    /**
     * Runtime has started
     */
    STARTED,

    /**
     * Runtime is currently updating
     */
    UPDATING,

    /**
     * Runtime is current reconfiguring
     */
    RECONFIGURE,

    /**
     * Runtime is shutting down
     */
    STOPPING,

    /**
     * Application is started but not fully functional
     */
    WARNING,

    /**
     * A failure occured, i.e. an essential component was not activated
     */
    FAILED

}
