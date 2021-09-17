/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 08.01.2015 by mbechler
 */
package eu.agno3.fileshare.service.internal;


import javax.validation.ValidatorFactory;

import org.apache.log4j.Logger;
import org.osgi.service.component.ComponentConstants;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import eu.agno3.fileshare.service.ConfigurationProvider;
import eu.agno3.fileshare.service.api.internal.DefaultServiceContext;
import eu.agno3.runtime.db.orm.EntityTransactionContext;
import eu.agno3.runtime.db.orm.EntityTransactionService;
import eu.agno3.runtime.eventlog.EventLogger;


/**
 * @author mbechler
 *
 */
@Component ( service = DefaultServiceContext.class )
public class DefaultServiceContextImpl implements DefaultServiceContext {

    private static final Logger log = Logger.getLogger(DefaultServiceContextImpl.class);

    private ValidatorFactory validatorFactory;
    private EventLogger eventLogger;
    private ConfigurationProvider configurationProvider;
    private EntityTransactionService fileshareETS;


    @Activate
    protected synchronized void activate ( ComponentContext ctx ) {
        // make sure the entity manager factory is initialized
        try ( EntityTransactionContext c = this.fileshareETS.start() ) {}
        catch ( Exception e ) {
            log.error("Failed to create entity manager", e); //$NON-NLS-1$
            ctx.disableComponent((String) ctx.getProperties().get(ComponentConstants.COMPONENT_NAME));
        }
    }


    @Reference ( service = EntityTransactionService.class, target = "(persistenceUnit=fileshare)" )
    protected synchronized void bindEntityTransactionService ( EntityTransactionService ets ) {
        this.fileshareETS = ets;
    }


    protected synchronized void unbindEntityTransactionService ( EntityTransactionService ets ) {
        if ( this.fileshareETS == ets ) {
            this.fileshareETS = null;
        }
    }


    @Reference
    protected synchronized void setValidatorFactory ( ValidatorFactory vf ) {
        this.validatorFactory = vf;
    }


    protected synchronized void unsetValidatorFactory ( ValidatorFactory vf ) {
        if ( this.validatorFactory == vf ) {
            this.validatorFactory = null;
        }
    }


    @Reference
    protected synchronized void setConfigurationProvider ( ConfigurationProvider cp ) {
        this.configurationProvider = cp;
    }


    protected synchronized void unsetConfigurationProvider ( ConfigurationProvider cp ) {
        if ( this.configurationProvider == cp ) {
            this.configurationProvider = null;
        }
    }


    @Reference
    protected synchronized void setEventLogger ( EventLogger evl ) {
        this.eventLogger = evl;
    }


    protected synchronized void unsetEventLogger ( EventLogger evl ) {
        if ( this.eventLogger == evl ) {
            this.eventLogger = null;
        }
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.fileshare.service.api.internal.DefaultServiceContext#getFileshareEntityTS()
     */
    @Override
    public EntityTransactionService getFileshareEntityTS () {
        return this.fileshareETS;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.fileshare.service.api.internal.DefaultServiceContext#getValidatorFactory()
     */
    @Override
    public ValidatorFactory getValidatorFactory () {
        return this.validatorFactory;
    }


    /**
     * @return the configurationProvider
     */
    @Override
    public ConfigurationProvider getConfigurationProvider () {
        return this.configurationProvider;
    }


    /**
     * @return the eventLogger
     */
    @Override
    public EventLogger getEventLogger () {
        return this.eventLogger;
    }
}
