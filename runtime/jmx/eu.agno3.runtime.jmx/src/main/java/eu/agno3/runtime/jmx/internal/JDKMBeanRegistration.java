/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 22.02.2015 by mbechler
 */
package eu.agno3.runtime.jmx.internal;


import java.lang.management.ManagementFactory;
import java.lang.management.PlatformManagedObject;

import javax.management.JMException;

import org.apache.log4j.Logger;


/**
 * @author mbechler
 *
 */
public final class JDKMBeanRegistration {

    private static final Logger log = Logger.getLogger(JDKMBeanRegistration.class);


    /**
     * 
     */
    private JDKMBeanRegistration () {}


    /**
     * 
     * @param reg
     */
    public static void registerJDKMbeans ( MBeanServerRegistrationImpl reg ) {
        for ( Class<? extends PlatformManagedObject> clz : ManagementFactory.getPlatformManagementInterfaces() ) {
            for ( PlatformManagedObject platformManagedObject : ManagementFactory.getPlatformMXBeans(clz) ) {
                tryRegister(reg, platformManagedObject);
            }
        }
    }


    /**
     * @param reg
     * @param classLoadingMxbeanName
     * @param classLoadingMXBean
     */
    private static void tryRegister ( MBeanServerRegistrationImpl reg, PlatformManagedObject mbean ) {
        try {
            reg.bindMBean(mbean, mbean.getObjectName());
        }
        catch ( JMException e ) {
            log.warn("Failed to register mbean " + mbean.getObjectName(), e); //$NON-NLS-1$
        }
    }

}
