/**
 * © 2016 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: Oct 14, 2016 by mbechler
 */
package eu.agno3.orchestrator.jobs.agent.licensing.internal;


import java.util.Optional;

import org.eclipse.jdt.annotation.NonNull;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import eu.agno3.orchestrator.agent.msg.addressing.AgentMessageSource;
import eu.agno3.orchestrator.config.model.realm.license.LicenseInfo;
import eu.agno3.orchestrator.config.model.realm.license.LicenseInfoRequest;
import eu.agno3.orchestrator.config.model.realm.license.LicenseInfoResponse;
import eu.agno3.runtime.messaging.MessagingException;
import eu.agno3.runtime.messaging.addressing.MessageSource;
import eu.agno3.runtime.messaging.listener.MessageProcessingException;
import eu.agno3.runtime.messaging.listener.RequestEndpoint;
import eu.agno3.runtime.messaging.xml.DefaultXmlErrorResponseMessage;
import eu.agno3.runtime.update.License;
import eu.agno3.runtime.update.LicensingService;


/**
 * @author mbechler
 *
 */
@Component ( service = RequestEndpoint.class, property = "msgType=eu.agno3.orchestrator.config.model.realm.license.LicenseInfoRequest" )
public class LicenseInfoRequestEndpoint implements RequestEndpoint<LicenseInfoRequest, LicenseInfoResponse, DefaultXmlErrorResponseMessage> {

    private LicensingService licensingService;
    private Optional<@NonNull AgentMessageSource> messageSource = Optional.empty();


    @Reference
    protected synchronized void setMessageSource ( @NonNull MessageSource ms ) {
        this.messageSource = Optional.of((AgentMessageSource) ms);
    }


    protected synchronized void unsetMessageSource ( MessageSource ms ) {
        if ( this.messageSource.equals(ms) ) {
            this.messageSource = Optional.empty();
        }
    }


    @Reference
    protected synchronized void setLicensingService ( LicensingService ls ) {
        this.licensingService = ls;
    }


    protected synchronized void unsetLicensingService ( LicensingService ls ) {
        if ( this.licensingService == ls ) {
            this.licensingService = null;
        }
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.messaging.listener.RequestEndpoint#onReceive(eu.agno3.runtime.messaging.msg.RequestMessage)
     */
    @Override
    public LicenseInfoResponse onReceive ( @NonNull LicenseInfoRequest msg ) throws MessageProcessingException, MessagingException {
        LicenseInfoResponse r = new LicenseInfoResponse(this.messageSource.get(), msg);
        License license = this.licensingService.getLicense();
        if ( license != null ) {
            r.setInfo(LicenseInfo.fromLicense(license));
        }
        else {
            r.setInfo(dummyLicenseInfo());
        }
        return r;
    }


    /**
     * @return
     */
    private LicenseInfo dummyLicenseInfo () {
        LicenseInfo li = new LicenseInfo();
        li.setExpirationDate(this.licensingService.getExpirationDate());
        return li;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.messaging.listener.RequestEndpoint#getMessageType()
     */
    @Override
    public Class<LicenseInfoRequest> getMessageType () {
        return LicenseInfoRequest.class;
    }

}
