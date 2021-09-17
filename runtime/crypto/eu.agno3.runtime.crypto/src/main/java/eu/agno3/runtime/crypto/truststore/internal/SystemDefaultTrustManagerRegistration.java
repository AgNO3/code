/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 13.10.2014 by mbechler
 */
package eu.agno3.runtime.crypto.truststore.internal;


import java.security.GeneralSecurityException;
import java.security.KeyStore;
import java.util.Dictionary;
import java.util.Hashtable;

import javax.net.ssl.TrustManagerFactory;

import org.apache.log4j.Logger;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.ConfigurationPolicy;
import org.osgi.service.component.annotations.Deactivate;

import eu.agno3.runtime.util.osgi.DsUtil;


/**
 * @author mbechler
 *
 */
@Component (
    immediate = true,
    service = SystemDefaultTrustManagerRegistration.class,
    configurationPid = SystemDefaultTrustManagerRegistration.PID,
    configurationPolicy = ConfigurationPolicy.REQUIRE )
public class SystemDefaultTrustManagerRegistration {

    /**
     * 
     */
    public static final String PID = "truststore.system"; //$NON-NLS-1$

    private static final String JAVA_DEFAULT = "javaDefault"; //$NON-NLS-1$
    private static final String INSTANCE_ID = "instanceId"; //$NON-NLS-1$
    private static final Logger log = Logger.getLogger(SystemDefaultTrustManagerRegistration.class);
    private static final String SUN_JSSE = "SunJSSE"; //$NON-NLS-1$
    private static final String PKIX = "PKIX"; //$NON-NLS-1$

    private ServiceRegistration<TrustManagerFactory> tmfReg;


    @Activate
    protected synchronized void activate ( ComponentContext ctx ) {
        try {
            TrustManagerFactory tmf = TrustManagerFactory.getInstance(PKIX, SUN_JSSE);
            tmf.init((KeyStore) null);

            Dictionary<String, Object> tmfProps = new Hashtable<>();
            tmfProps.put(INSTANCE_ID, JAVA_DEFAULT);
            this.tmfReg = DsUtil.registerSafe(ctx, TrustManagerFactory.class, tmf, tmfProps);
        }
        catch ( GeneralSecurityException e ) {
            log.error("Failed to initialize java default trust store", e); //$NON-NLS-1$
        }
    }


    @Deactivate
    protected synchronized void deactivate ( ComponentContext ctx ) {
        if ( this.tmfReg != null ) {
            DsUtil.unregisterSafe(ctx, this.tmfReg);
            this.tmfReg = null;
        }
    }
}
