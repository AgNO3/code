/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 26.06.2014 by mbechler
 */
package eu.agno3.fileshare.orch.common.config;


import org.eclipse.jdt.annotation.NonNull;
import org.joda.time.Duration;
import org.osgi.service.component.annotations.Component;

import eu.agno3.fileshare.orch.common.i18n.FileshareConfigurationMessages;
import eu.agno3.orchestrator.config.model.descriptors.AbstractObjectTypeDescriptor;
import eu.agno3.orchestrator.config.model.descriptors.ObjectTypeDescriptor;
import eu.agno3.orchestrator.config.web.WebEndpointConfigObjectTypeDescriptor;


/**
 * @author mbechler
 * 
 */
@Component ( service = ObjectTypeDescriptor.class )
public class FileshareWebConfigObjectTypeDescriptor extends AbstractObjectTypeDescriptor<FileshareWebConfig, FileshareWebConfigImpl> {

    /**
     * 
     */
    public FileshareWebConfigObjectTypeDescriptor () {
        super(FileshareWebConfig.class, FileshareWebConfigImpl.class, FileshareConfigurationMessages.BASE_PACKAGE);
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.config.model.descriptors.AbstractObjectTypeDescriptor#getParentTypeName()
     */
    @Override
    public String getParentTypeName () {
        return FileshareConfigurationObjectTypeDescriptor.TYPE_NAME;
    }


    /**
     * {@inheritDoc}
     * 
     * @see eu.agno3.orchestrator.config.model.descriptors.ConcreteObjectTypeDescriptor#newInstance()
     */
    @Override
    public @NonNull FileshareWebConfig newInstance () {
        return emptyInstance();
    }


    /**
     * {@inheritDoc}
     * 
     * @see eu.agno3.orchestrator.config.model.descriptors.AbstractObjectTypeDescriptor#getGlobalDefaults()
     */
    @Override
    public @NonNull FileshareWebConfig getGlobalDefaults () {
        FileshareWebConfigMutable fwc = new FileshareWebConfigImpl();
        fwc.setEnableWebDAV(true);
        fwc.setWebDAVAllowSetModificationTime(false);
        fwc.setIntentTimeout(Duration.standardHours(6));
        fwc.setSessionIncompleteExpireDuration(Duration.standardHours(2));
        fwc.setPerSessionIncompleteSizeLimitEnabled(false);
        fwc.setPerSessionIncompleteSizeLimit(1000L * 1000 * 1000);
        fwc.setUserIncompleteExpireDuration(Duration.standardHours(4));
        fwc.setPerUserIncompleteSizeLimitEnabled(false);
        fwc.setPerUserIncompleteSizeLimit(10000L * 1000 * 1000);
        fwc.setDefaultUploadChunkSize(2 * 1024 * 1024L);
        fwc.setMaximumUploadChunkSize(16 * 1024 * 1024L);
        fwc.setOptimalUploadChunkCount(32);
        return fwc;
    }


    /**
     * @return empty instance
     */
    public static @NonNull FileshareWebConfigMutable emptyInstance () {
        FileshareWebConfigMutable fwc = new FileshareWebConfigImpl();
        fwc.setWebEndpointConfig(WebEndpointConfigObjectTypeDescriptor.emptyInstance());
        return fwc;
    }

}
