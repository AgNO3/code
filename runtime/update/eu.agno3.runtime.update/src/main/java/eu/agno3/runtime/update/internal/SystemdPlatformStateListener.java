/**
 * © 2017 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: May 10, 2017 by mbechler
 */
package eu.agno3.runtime.update.internal;


import org.apache.log4j.Logger;
import org.osgi.service.component.annotations.Component;

import eu.agno3.runtime.update.PlatformState;
import eu.agno3.runtime.update.PlatformStateListener;


/**
 * @author mbechler
 *
 */
@Component ( service = PlatformStateListener.class )
public class SystemdPlatformStateListener implements PlatformStateListener {

    private static final Logger log = Logger.getLogger(SystemdPlatformStateListener.class);


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.update.PlatformStateListener#stateChanged(eu.agno3.runtime.update.PlatformState)
     */
    @Override
    public void stateChanged ( PlatformState state ) {
        try {
            switch ( state ) {
            case FAILED:
                SDNotify.fail(
                    "eu.agno3.service.Error.GENERIC", //$NON-NLS-1$
                    "Service failed"); //$NON-NLS-1$
                break;
            case RECONFIGURE:
                SDNotify.reloading("Reconfiguring service"); //$NON-NLS-1$
                break;
            case UPDATING:
                SDNotify.reloading("Performing service update"); //$NON-NLS-1$
                break;
            case STARTED:
                SDNotify.ready("Started successfully", 0); //$NON-NLS-1$
                break;
            case WARNING:
                SDNotify.ready("Started with warning", 0); //$NON-NLS-1$
                break;
            case STOPPING:
                SDNotify.stopping("Shutting down"); //$NON-NLS-1$
                break;
            default:
            case BOOTING:
                break;
            }
        }
        catch ( Exception e ) {
            log.warn("Failed to send systemd status notification", e); //$NON-NLS-1$
        }

    }

}
