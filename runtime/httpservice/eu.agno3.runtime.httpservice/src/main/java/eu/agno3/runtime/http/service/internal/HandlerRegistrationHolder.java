/**
 * © 2017 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: Jun 12, 2017 by mbechler
 */
package eu.agno3.runtime.http.service.internal;


import java.util.concurrent.atomic.AtomicReference;

import org.osgi.framework.ServiceRegistration;

import eu.agno3.runtime.http.service.ActiveHandler;


/**
 * @author mbechler
 *
 */
public class HandlerRegistrationHolder {

    private final AtomicReference<ServiceRegistration<ActiveHandler>> activeHandler = new AtomicReference<>();


    /**
     * Get and clear active handler registration
     * 
     * @return registration
     */
    public ServiceRegistration<ActiveHandler> fetch () {
        return this.activeHandler.getAndSet(null);
    }


    /**
     * @param reg
     */
    public void set ( ServiceRegistration<ActiveHandler> reg ) {
        this.activeHandler.set(reg);
    }

}
