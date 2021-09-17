/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 21.02.2015 by mbechler
 */
package eu.agno3.runtime.jmx.internal;


import java.lang.management.ManagementFactory;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Set;

import javax.management.InstanceNotFoundException;
import javax.management.JMException;
import javax.management.MBeanRegistrationException;
import javax.management.MBeanServer;
import javax.management.MalformedObjectNameException;
import javax.management.NotCompliantMBeanException;
import javax.management.ObjectName;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.ConfigurationPolicy;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;

import eu.agno3.runtime.jmx.MBean;
import eu.agno3.runtime.jmx.MBeanHolder;
import eu.agno3.runtime.util.osgi.DsUtil;


/**
 * @author mbechler
 *
 */
@Component ( immediate = true, configurationPid = "mbean", configurationPolicy = ConfigurationPolicy.REQUIRE )
public class MBeanServerRegistrationImpl {

    /**
     * 
     */
    private static final Logger log = Logger.getLogger(MBeanServerRegistrationImpl.class);
    private MBeanServer mbeanServer;

    private Set<ObjectName> registeredMBeans = new HashSet<>();
    private ServiceRegistration<MBeanServer> registration;
    private BundleContext bundleContext;
    private Queue<ServiceReference<MBean>> deferredMBeans = new LinkedList<>();
    private Queue<MBeanHolder> deferredMBeanHolders = new LinkedList<>();


    @Activate
    protected synchronized void activate ( ComponentContext ctx ) {
        this.bundleContext = ctx.getBundleContext();
        try {
            this.mbeanServer = ManagementFactory.getPlatformMBeanServer();
            this.registration = DsUtil.registerSafe(ctx, MBeanServer.class, this.mbeanServer, new Hashtable<>());

            for ( ServiceReference<MBean> ref : this.deferredMBeans ) {
                this.bindMBean(ref);
            }
            this.deferredMBeans.clear();

            for ( MBeanHolder ref : this.deferredMBeanHolders ) {
                try {
                    this.bindMBean(ref.getMBean(), new ObjectName(ref.getObjectName()));
                }
                catch ( JMException e ) {
                    log.error("Failed to bind mbean " + ref.getObjectName(), e); //$NON-NLS-1$
                }
            }
            this.deferredMBeanHolders.clear();
        }
        catch ( Exception e ) {
            log.error("Failed to setup mbean server", e); //$NON-NLS-1$
        }
    }


    @Deactivate
    protected synchronized void deactivate ( ComponentContext ctx ) {

        if ( this.registration != null ) {
            DsUtil.unregisterSafe(ctx, this.registration);
            this.registration = null;
        }

        for ( ObjectName on : this.registeredMBeans ) {
            try {
                this.mbeanServer.unregisterMBean(on);
            }
            catch (
                MBeanRegistrationException |
                InstanceNotFoundException e ) {
                log.warn("Failed to unregister remaining mbean", e); //$NON-NLS-1$
            }
        }
        this.registeredMBeans.clear();
        this.deferredMBeanHolders.clear();

        this.bundleContext = null;
    }


    @Reference ( service = MBean.class, cardinality = ReferenceCardinality.MULTIPLE, policy = ReferencePolicy.DYNAMIC )
    protected synchronized void bindMBean ( ServiceReference<MBean> reg ) {

        String objectNameProp = (String) reg.getProperty("objectName"); //$NON-NLS-1$
        if ( StringUtils.isBlank(objectNameProp) ) {
            log.warn("Not registering MBean, objectName is empty"); //$NON-NLS-1$
            return;
        }

        if ( this.bundleContext == null ) {
            this.deferredMBeans.add(reg);
            return;
        }

        Object o = this.bundleContext.getService(reg);
        if ( o == null ) {
            log.warn("Not registering MBean, object is NULL"); //$NON-NLS-1$
            return;
        }

        try {
            this.bindMBean(o, new ObjectName(objectNameProp));
        }
        catch ( Exception e ) {
            log.warn("Failed to register MBean " + objectNameProp, e); //$NON-NLS-1$
        }
    }


    protected synchronized void unbindMBean ( ServiceReference<MBean> reg ) {
        String objectNameProp = (String) reg.getProperty("objectName"); //$NON-NLS-1$
        if ( StringUtils.isBlank(objectNameProp) ) {
            log.warn("Not registering MBean, objectName is empty"); //$NON-NLS-1$
            return;
        }

        this.deferredMBeans.remove(reg);

        try {
            this.unbindMBean(new ObjectName(objectNameProp));
        }
        catch (
            MBeanRegistrationException |
            InstanceNotFoundException |
            MalformedObjectNameException e ) {
            log.warn("Failed to unregister MBean " + objectNameProp, e); //$NON-NLS-1$
        }

    }


    @Reference ( cardinality = ReferenceCardinality.MULTIPLE, policy = ReferencePolicy.DYNAMIC )
    protected synchronized void bindMBeanHolder ( MBeanHolder reg ) {
        if ( StringUtils.isBlank(reg.getObjectName()) ) {
            log.warn("Not registering MBean, objectName is empty"); //$NON-NLS-1$
            return;
        }

        if ( this.bundleContext == null ) {
            this.deferredMBeanHolders.add(reg);
            return;
        }

        try {
            this.bindMBean(reg.getMBean(), new ObjectName(reg.getObjectName()));
        }
        catch ( Exception e ) {
            log.warn("Failed to register MBean " + reg.getObjectName(), e); //$NON-NLS-1$
        }
    }


    protected synchronized void unbindMBeanHolder ( MBeanHolder reg ) {
        if ( StringUtils.isBlank(reg.getObjectName()) ) {
            log.warn("Not registering MBean, objectName is empty"); //$NON-NLS-1$
            return;
        }

        this.deferredMBeanHolders.remove(reg);

        try {
            this.unbindMBean(new ObjectName(reg.getObjectName()));
        }
        catch (
            MBeanRegistrationException |
            InstanceNotFoundException |
            MalformedObjectNameException e ) {
            log.warn("Failed to unregister MBean " + reg.getObjectName(), e); //$NON-NLS-1$
        }
    }


    protected synchronized void bindMBean ( Object obj, ObjectName name ) throws JMException {

        if ( this.registeredMBeans.contains(name) ) {
            return;
        }

        if ( log.isDebugEnabled() ) {
            log.debug("Registering MBean " + name); //$NON-NLS-1$
        }

        this.registeredMBeans.add(name);
        try {
            this.mbeanServer.registerMBean(obj, name);
        }
        catch (
            MBeanRegistrationException |
            NotCompliantMBeanException e ) {
            this.registeredMBeans.remove(name);
            throw e;
        }
    }


    protected synchronized void unbindMBean ( ObjectName name ) throws MBeanRegistrationException, InstanceNotFoundException {

        if ( log.isDebugEnabled() ) {
            log.debug("Unregistering MBean " + name); //$NON-NLS-1$
        }

        if ( !this.registeredMBeans.contains(name) ) {
            return;
        }

        try {
            this.mbeanServer.unregisterMBean(name);
        }
        finally {
            this.registeredMBeans.remove(name);
        }
    }
}
