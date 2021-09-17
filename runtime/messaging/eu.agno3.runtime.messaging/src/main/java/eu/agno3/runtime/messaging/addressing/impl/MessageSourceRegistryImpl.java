/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 03.06.2014 by mbechler
 */
package eu.agno3.runtime.messaging.addressing.impl;


import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;

import eu.agno3.runtime.messaging.addressing.MessageSource;
import eu.agno3.runtime.messaging.addressing.MessageSourceRegistration;
import eu.agno3.runtime.messaging.addressing.MessageSourceRegistry;


/**
 * @author mbechler
 * 
 */
@Component ( service = MessageSourceRegistry.class )
public class MessageSourceRegistryImpl implements MessageSourceRegistry {

    private static final String TYPE = "type"; //$NON-NLS-1$

    private static final Logger log = Logger.getLogger(MessageSourceRegistryImpl.class);

    private Map<String, ServiceReference<MessageSourceRegistration>> types = new HashMap<>();
    private BundleContext bundleContext;


    @Activate
    protected void activate ( ComponentContext ctx ) {
        this.bundleContext = ctx.getBundleContext();
    }


    @Deactivate
    protected void decativate ( ComponentContext ctx ) {
        this.bundleContext = null;
    }


    @Reference ( service = MessageSourceRegistration.class, cardinality = ReferenceCardinality.AT_LEAST_ONE, policy = ReferencePolicy.DYNAMIC )
    protected synchronized void bindMessageSource ( ServiceReference<MessageSourceRegistration> ref ) {
        String typeProp = (String) ref.getProperty(TYPE);
        if ( typeProp != null ) {
            if ( log.isDebugEnabled() ) {
                log.debug("Registering MessageSource type " + typeProp); //$NON-NLS-1$
            }
            this.types.put(typeProp, ref);
        }
        else {
            log.warn("MessageSourceRegistration without a type " + ref); //$NON-NLS-1$
        }
    }


    protected synchronized void unbindMessageSource ( ServiceReference<MessageSourceRegistration> ref ) {
        String typeProp = (String) ref.getProperty(TYPE);
        if ( typeProp != null ) {
            ServiceReference<MessageSourceRegistration> cur = this.types.get(typeProp);
            if ( cur.equals(ref) ) {
                this.types.remove(typeProp);
            }
        }
    }


    /**
     * {@inheritDoc}
     * 
     * @see eu.agno3.runtime.messaging.addressing.MessageSourceRegistry#getMessageSource(java.lang.String)
     */
    @Override
    public MessageSource getMessageSource ( String spec ) {

        int colonPos = spec.indexOf(':');
        if ( colonPos < 0 ) {
            throw new IllegalArgumentException("Not a valid message source specification: " + spec); //$NON-NLS-1$
        }

        String type = spec.substring(0, colonPos);

        if ( log.isTraceEnabled() ) {
            log.trace("Found message source type " + type); //$NON-NLS-1$
        }

        try {
            Class<? extends MessageSource> msgSourceClass = this.getMessageSourceClass(type);
            if ( msgSourceClass != null ) {
                if ( log.isTraceEnabled() ) {
                    log.trace("Found message source class " + msgSourceClass.getName()); //$NON-NLS-1$
                }
                MessageSource s = msgSourceClass.newInstance();
                s.parse(spec);
                return s;
            }

            if ( log.isDebugEnabled() ) {
                log.debug("Failed to determine message source class for type " + type); //$NON-NLS-1$
            }
        }
        catch (
            InstantiationException |
            IllegalAccessException e ) {
            log.warn("Failed to create message source object for type " + type, e); //$NON-NLS-1$
        }
        return null;
    }


    /**
     * @param type
     * @return
     */
    @SuppressWarnings ( "unchecked" )
    private Class<? extends MessageSource> getMessageSourceClass ( String type ) {

        ServiceReference<MessageSourceRegistration> ref = this.types.get(type);

        if ( ref == null ) {
            log.debug("Type not (yet) registered " + type); //$NON-NLS-1$
            return null;
        }

        MessageSourceRegistration service = this.bundleContext.getService(ref);

        if ( service == null ) {
            if ( log.isDebugEnabled() ) {
                log.debug("Failed to obtain MessageSource class via reference " + ref); //$NON-NLS-1$
            }
            return null;
        }

        return (Class<? extends MessageSource>) service.getClass();
    }

}
