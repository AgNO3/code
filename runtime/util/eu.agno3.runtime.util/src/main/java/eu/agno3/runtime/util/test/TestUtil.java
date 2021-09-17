/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 12.01.2014 by mbechler
 */
package eu.agno3.runtime.util.test;


import java.util.Arrays;
import java.util.Collection;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.ServiceReference;
import org.osgi.service.component.runtime.ServiceComponentRuntime;
import org.osgi.service.component.runtime.dto.ComponentConfigurationDTO;
import org.osgi.service.component.runtime.dto.ComponentDescriptionDTO;
import org.osgi.service.component.runtime.dto.UnsatisfiedReferenceDTO;


/**
 * @author mbechler
 * 
 */
public final class TestUtil {

    /**
     * 
     */
    private static final String DS_TEST_TIMEOUT = "ds.test.timeout"; //$NON-NLS-1$
    private static final Logger log = Logger.getLogger(TestUtil.class);


    private TestUtil () {}


    /**
     * @return whether currently running under test
     */
    public static boolean isUnderTest () {
        if ( System.getProperty(DS_TEST_TIMEOUT) != null ) {
            return true;
        }

        String cmdLine = System.getProperty("sun.java.command", StringUtils.EMPTY); //$NON-NLS-1$

        if ( cmdLine.contains("org.eclipse.jdt.junit4.runtime") ) { //$NON-NLS-1$
            return true;
        }

        return false;
    }


    /**
     * Wait for the DS component to be activated.
     * 
     * Timeout can be configured through the system property ds.test.timeout.
     * 
     * 
     * @param activateSemaphore
     * @param componentClass
     * @param componentName
     */
    public static void waitForServices ( Semaphore activateSemaphore, Class<?> componentClass, String componentName ) {

        int timeoutSecs = 60;

        String timeoutProp = System.getProperty(DS_TEST_TIMEOUT);

        if ( timeoutProp != null ) {
            try {
                timeoutSecs = Integer.parseInt(timeoutProp);
            }
            catch ( NumberFormatException e ) {
                throw new RuntimeException("Illegal value for ds.test.timeout"); //$NON-NLS-1$
            }
        }

        try {
            if ( !activateSemaphore.tryAcquire(timeoutSecs, TimeUnit.SECONDS) ) {
                if ( componentClass != null ) {
                    debugUnsatisfiedDependencies(componentClass, componentName);
                }
                throw new RuntimeException(String.format("Services did not come up within %d seconds", timeoutSecs)); //$NON-NLS-1$
            }
        }
        catch ( InterruptedException e ) {
            throw new RuntimeException("Interrupted while trying to acquire semaphore"); //$NON-NLS-1$
        }
    }


    /**
     * Wait for the DS component to be activated.
     * 
     * Timeout can be configured through the system property ds.test.timeout.
     * 
     * 
     * @param activateSemaphore
     * @param componentClass
     */
    public static void waitForServices ( Semaphore activateSemaphore, Class<?> componentClass ) {
        log.info("Waiting for service injection on " + //$NON-NLS-1$
                ( componentClass != null ? componentClass.getName() : "<unknown>" )); //$NON-NLS-1$
        waitForServices(activateSemaphore, componentClass, null);
    }


    /**
     * Wait for the DS component to be activated.
     * 
     * Timeout can be configured through the system property ds.test.timeout.
     * 
     * 
     * @param activateSemaphore
     */
    public static void waitForServices ( Semaphore activateSemaphore ) {
        log.info("Waiting for service injection"); //$NON-NLS-1$
        waitForServices(activateSemaphore, null);
    }


    /**
     * @param componentName
     * @param componentClass
     * 
     */
    private static void debugUnsatisfiedDependencies ( Class<?> componentClass, String componentName ) {
        BundleContext ctx = getBundleContext(componentClass);
        ServiceComponentRuntime scrService = getScrService(ctx, componentClass);
        if ( scrService == null ) {
            log.error("Failed to obtain SCR Service"); //$NON-NLS-1$
            return;
        }

        String compName = getComponentName(componentClass, componentName);
        ComponentDescriptionDTO tComp = scrService.getComponentDescriptionDTO(ctx.getBundle(), compName);

        if ( tComp == null ) {
            log.error(String.format("No component named '%s' found: Missing descriptor?", compName)); //$NON-NLS-1$
            return;
        }

        dumpComponentState(scrService, tComp, ctx);

        log.error("Other component states:"); //$NON-NLS-1$
        for ( ComponentDescriptionDTO c : scrService.getComponentDescriptionDTOs() ) {
            if ( !compName.equals(c.name) ) {
                dumpComponentState(scrService, c, ctx);
            }
        }
    }


    private static void dumpComponentState ( ServiceComponentRuntime scrService, ComponentDescriptionDTO c, BundleContext ctx ) {

        Collection<ComponentConfigurationDTO> compCfgs = scrService.getComponentConfigurationDTOs(c);

        for ( ComponentConfigurationDTO compCfg : compCfgs ) {
            if ( compCfg.state == ComponentConfigurationDTO.UNSATISFIED_CONFIGURATION ) {
                log.error(String.format("Component '%s' is unsatisfied, missing configuration", c.name)); //$NON-NLS-1$
            }
            else if ( compCfg.state == ComponentConfigurationDTO.UNSATISFIED_REFERENCE ) {
                log.error(String.format("Component '%s' is unsatisfied, unsatisfied references", c.name)); //$NON-NLS-1$

                for ( UnsatisfiedReferenceDTO unsat : compCfg.unsatisfiedReferences ) {
                    log.error(String.format(
                        "-> Unsatisfied reference %s (filter: %s) services %s", //$NON-NLS-1$
                        unsat.name,
                        unsat.target,
                        Arrays.toString(unsat.targetServices)));
                }
            }
        }

    }


    private static String getComponentName ( Class<?> componentClass, String componentName ) {
        String compName = componentName;
        if ( compName == null ) {
            compName = componentClass.getName();
        }
        return compName;
    }


    private static ServiceComponentRuntime getScrService ( BundleContext ctx, Class<?> component ) {
        if ( ctx == null ) {
            log.warn("No bundle context available"); //$NON-NLS-1$
            return null;
        }

        ServiceReference<ServiceComponentRuntime> scrServiceRef = ctx.getServiceReference(ServiceComponentRuntime.class);

        if ( scrServiceRef == null ) {
            return null;
        }

        ServiceComponentRuntime scrService = ctx.getService(scrServiceRef);

        return scrService;
    }


    private static BundleContext getBundleContext ( Class<?> component ) {
        Bundle bundle = FrameworkUtil.getBundle(component);

        if ( bundle == null ) {
            return null;
        }

        BundleContext ctx = bundle.getBundleContext();

        if ( ctx == null ) {
            return null;
        }
        return ctx;
    }
}
