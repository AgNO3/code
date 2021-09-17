/**
 * © 2017 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: Jun 11, 2017 by mbechler
 */
package eu.agno3.runtime.util.osgi;


import java.util.Dictionary;

import org.osgi.framework.ServiceRegistration;
import org.osgi.service.component.ComponentContext;


/**
 * 
 * @author mbechler
 *
 */
public final class DsUtil {

    /**
     * 
     */
    private DsUtil () {}


    /**
     * @param ctx
     */
    public static void waitForSettle ( ComponentContext ctx ) {

    }


    /**
     * 
     * @param ctx
     * @param clazz
     * @param service
     * @param props
     * @return service registration
     */
    public static final <S> ServiceRegistration<S> registerSafe ( ComponentContext ctx, Class<S> clazz, S service, Dictionary<String, ?> props ) {
        return ctx.getBundleContext().registerService(clazz, service, props);
    }


    /**
     * 
     * @param ctx
     * @param reg
     */
    public static final void unregisterSafe ( ComponentContext ctx, ServiceRegistration<?> reg ) {
        if ( reg == null || ctx == null ) {
            return;
        }
        reg.unregister();
    }

}
