/**
 * © 2016 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: Sep 17, 2016 by mbechler
 */
package eu.agno3.orchestrator.server.webgui.config.logger;


import javax.enterprise.context.ApplicationScoped;
import javax.inject.Named;

import eu.agno3.orchestrator.config.logger.IPLogAnonymizationType;
import eu.agno3.orchestrator.config.logger.LoggerConfiguration;
import eu.agno3.orchestrator.config.logger.LoggerConfigurationImpl;
import eu.agno3.orchestrator.config.model.base.exceptions.ModelServiceException;
import eu.agno3.orchestrator.config.web.i18n.WebConfigurationMessages;
import eu.agno3.orchestrator.gui.connector.ws.GuiWebServiceException;
import eu.agno3.orchestrator.server.webgui.config.AbstractConfigObjectBean;
import eu.agno3.orchestrator.server.webgui.config.ConfigContext;


/**
 * @author mbechler
 *
 */
@ApplicationScoped
@Named ( "loggerConfigBean" )
public class LoggerConfigBean extends AbstractConfigObjectBean<LoggerConfiguration, LoggerConfigurationImpl> {

    /**
     * 
     * @return the server types
     */
    public IPLogAnonymizationType[] getIpAnonymizationTypes () {
        return IPLogAnonymizationType.values();
    }


    /**
     * 
     * @param val
     * @return the translated server type
     */
    public String translateIpAnonymizationType ( Object val ) {
        return translateEnumValue(IPLogAnonymizationType.class, val);
    }


    public String translateIpAnonymizationDescription ( Object val ) {
        return translateEnumDescription(IPLogAnonymizationType.class, val);
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.server.webgui.config.AbstractConfigObjectBean#getMessageBase()
     */
    @Override
    protected String getMessageBase () {
        return WebConfigurationMessages.BASE_PACKAGE;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.server.webgui.config.AbstractConfigObjectBean#getObjectType()
     */
    @Override
    protected Class<LoggerConfiguration> getObjectType () {
        return LoggerConfiguration.class;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.server.webgui.config.AbstractConfigObjectBean#getInstanceType()
     */
    @Override
    protected Class<LoggerConfigurationImpl> getInstanceType () {
        return LoggerConfigurationImpl.class;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.server.webgui.config.AbstractConfigObjectBean#cloneInternal(eu.agno3.orchestrator.server.webgui.config.ConfigContext,
     *      eu.agno3.orchestrator.config.model.realm.AbstractConfigurationObject,
     *      eu.agno3.orchestrator.config.model.realm.ConfigurationObject,
     *      eu.agno3.orchestrator.config.model.realm.ConfigurationObject)
     */
    @Override
    protected void cloneInternal ( ConfigContext<?, ?> ctx, LoggerConfigurationImpl cloned, LoggerConfiguration local, LoggerConfiguration defaults )
            throws ModelServiceException, GuiWebServiceException {

    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.server.webgui.config.AbstractConfigObjectBean#labelForInternal(eu.agno3.orchestrator.config.model.realm.ConfigurationObject)
     */
    @Override
    protected String labelForInternal ( LoggerConfiguration obj ) {
        return null;
    }

}
