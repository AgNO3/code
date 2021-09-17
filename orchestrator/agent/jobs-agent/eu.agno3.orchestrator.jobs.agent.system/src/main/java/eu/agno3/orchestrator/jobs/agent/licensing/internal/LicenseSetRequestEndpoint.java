/**
 * © 2016 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: Oct 14, 2016 by mbechler
 */
package eu.agno3.orchestrator.jobs.agent.licensing.internal;


import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import org.apache.commons.codec.binary.Base64;
import org.apache.log4j.Logger;
import org.eclipse.jdt.annotation.NonNull;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import eu.agno3.orchestrator.agent.msg.addressing.AgentMessageSource;
import eu.agno3.orchestrator.config.model.realm.StructuralObjectReference;
import eu.agno3.orchestrator.config.model.realm.license.LicenseInfo;
import eu.agno3.orchestrator.config.model.realm.license.LicenseInfoResponse;
import eu.agno3.orchestrator.config.model.realm.license.LicenseSetRequest;
import eu.agno3.orchestrator.jobs.agent.service.BaseServiceManager;
import eu.agno3.orchestrator.jobs.agent.service.ServiceManagementException;
import eu.agno3.orchestrator.jobs.agent.service.ServiceManager;
import eu.agno3.orchestrator.jobs.agent.system.ConfigRepository;
import eu.agno3.orchestrator.jobs.agent.system.ConfigRepositoryException;
import eu.agno3.orchestrator.system.file.util.FileSecurityUtils;
import eu.agno3.orchestrator.system.file.util.FileTemporaryUtils;
import eu.agno3.orchestrator.system.file.util.FileUtil;
import eu.agno3.runtime.messaging.MessagingException;
import eu.agno3.runtime.messaging.addressing.MessageSource;
import eu.agno3.runtime.messaging.listener.MessageProcessingException;
import eu.agno3.runtime.messaging.listener.RequestEndpoint;
import eu.agno3.runtime.messaging.xml.DefaultXmlErrorResponseMessage;
import eu.agno3.runtime.update.License;
import eu.agno3.runtime.update.LicensingException;
import eu.agno3.runtime.update.LicensingService;


/**
 * @author mbechler
 *
 */
@Component ( service = RequestEndpoint.class, property = "msgType=eu.agno3.orchestrator.config.model.realm.license.LicenseSetRequest" )
public class LicenseSetRequestEndpoint implements RequestEndpoint<LicenseSetRequest, LicenseInfoResponse, DefaultXmlErrorResponseMessage> {

    /**
     * 
     */
    private static final Logger log = Logger.getLogger(LicenseSetRequestEndpoint.class);
    private static final String LICENSE_PID = "license"; //$NON-NLS-1$
    private LicensingService licensingService;
    private ServiceManager serviceManager;
    private ConfigRepository configRepo;
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
    protected synchronized void setServiceManager ( ServiceManager sm ) {
        this.serviceManager = sm;
    }


    protected synchronized void unsetServiceManager ( ServiceManager sm ) {
        if ( this.serviceManager == sm ) {
            this.serviceManager = null;
        }
    }


    @Reference
    protected synchronized void setConfigRepository ( ConfigRepository cr ) {
        this.configRepo = cr;
    }


    protected synchronized void unsetConfigRepository ( ConfigRepository cr ) {
        if ( this.configRepo == cr ) {
            this.configRepo = null;
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
    public LicenseInfoResponse onReceive ( @NonNull LicenseSetRequest msg ) throws MessageProcessingException, MessagingException {

        try {
            LicenseInfo newLicense = msg.getLicense();
            Path licPath = this.licensingService.getLicensePath();
            if ( newLicense == null ) {
                log.info("Removing license"); //$NON-NLS-1$
                return removeLicense(msg, licPath);
            }

            this.licensingService.checkValid(newLicense.getData(), getLocalServices());
            replaceLicense(newLicense, licPath);

            // trigger license reloads
            triggerServiceReloads();

            LicenseInfoResponse r = new LicenseInfoResponse(this.messageSource.get(), msg);
            r.setInfo(checkLicenseEnabled(newLicense));
            return r;
        }
        catch ( LicensingException e ) {
            log.warn("Failed to set license", e); //$NON-NLS-1$
            throw new MessageProcessingException(new DefaultXmlErrorResponseMessage(e, this.messageSource.get(), msg));
        }
    }


    /**
     * @param newLicense
     * @param licPath
     * @throws LicensingException
     */
    void replaceLicense ( LicenseInfo newLicense, Path licPath ) throws LicensingException {
        try {
            Path tmpFile = FileTemporaryUtils.createRelatedTemporaryFile(licPath);
            try {
                Files.write(tmpFile, Base64.encodeBase64(newLicense.getData()), StandardOpenOption.WRITE, StandardOpenOption.TRUNCATE_EXISTING);
                Files.setPosixFilePermissions(tmpFile, FileSecurityUtils.getWorldReadableFilePermissions());
                FileUtil.safeMove(tmpFile, licPath, true);
            }
            finally {
                Files.deleteIfExists(tmpFile);
            }
        }
        catch ( IOException e ) {
            throw new LicensingException("Failed to write license file " + licPath, e); //$NON-NLS-1$
        }
    }


    /**
     * @param msg
     * @param licPath
     * @return
     * @throws LicensingException
     */
    LicenseInfoResponse removeLicense ( LicenseSetRequest msg, Path licPath ) throws LicensingException {
        try {
            Files.delete(licPath);
            triggerServiceReloads();
        }
        catch ( IOException e ) {
            throw new LicensingException("Failed to remove license", e); //$NON-NLS-1$
        }
        return new LicenseInfoResponse(this.messageSource.get(), msg);
    }


    /**
     * @throws LicensingException
     */
    void triggerServiceReloads () throws LicensingException {
        try {
            for ( StructuralObjectReference service : this.configRepo.getServiceReferences() ) {
                BaseServiceManager sm = this.serviceManager.getServiceManager(service, BaseServiceManager.class);
                sm.forceReloadAll(LICENSE_PID);
            }
        }
        catch (
            ConfigRepositoryException |
            ServiceManagementException e ) {
            throw new LicensingException("Failed to get services", e); //$NON-NLS-1$
        }
    }


    /**
     * @return
     * @throws LicensingException
     */
    private Set<String> getLocalServices () throws LicensingException {
        try {
            Set<String> serviceTypes = new HashSet<>();
            for ( StructuralObjectReference service : this.configRepo.getServiceReferences() ) {
                serviceTypes.add(service.getLocalType());
            }
            return serviceTypes;
        }
        catch ( ConfigRepositoryException e ) {
            throw new LicensingException("Failed to enumerate services", e); //$NON-NLS-1$
        }
    }


    /**
     * @param newLicense
     * @return
     * @throws LicensingException
     */
    private LicenseInfo checkLicenseEnabled ( LicenseInfo newLicense ) throws LicensingException {
        synchronized ( this.licensingService ) {
            License localLicense = this.licensingService.refreshLicense();
            if ( localLicense == null || !localLicense.getLicenseId().equals(newLicense.getLicenseId()) ) {
                throw new LicensingException("Failed to set new license"); //$NON-NLS-1$
            }
            return LicenseInfo.fromLicense(localLicense);
        }
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.messaging.listener.RequestEndpoint#getMessageType()
     */
    @Override
    public Class<LicenseSetRequest> getMessageType () {
        return LicenseSetRequest.class;
    }

}
