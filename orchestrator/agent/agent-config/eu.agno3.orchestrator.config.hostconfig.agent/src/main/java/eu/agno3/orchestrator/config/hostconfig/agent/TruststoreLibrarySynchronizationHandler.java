/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 29.07.2015 by mbechler
 */
package eu.agno3.orchestrator.config.hostconfig.agent;


import java.io.ByteArrayInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateEncodingException;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.codec.binary.Hex;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import eu.agno3.orchestrator.agent.crypto.truststore.TruststoreManager;
import eu.agno3.orchestrator.agent.crypto.truststore.TruststoreManagerException;
import eu.agno3.orchestrator.agent.crypto.truststore.TruststoresManager;
import eu.agno3.orchestrator.config.crypto.truststore.TruststoreResourceLibraryDescriptor;
import eu.agno3.orchestrator.config.crypto.truststore.TruststoreUtil;
import eu.agno3.orchestrator.config.hostconfig.desc.HostConfigServiceTypeDescriptor;
import eu.agno3.orchestrator.config.model.realm.StructuralObjectReference;
import eu.agno3.orchestrator.config.model.realm.resourcelibrary.ResourceLibraryEntry;
import eu.agno3.orchestrator.config.model.realm.resourcelibrary.ResourceLibraryException;
import eu.agno3.orchestrator.jobs.agent.service.BaseServiceManager;
import eu.agno3.orchestrator.jobs.agent.service.RuntimeServiceManager;
import eu.agno3.orchestrator.jobs.agent.service.ServiceManagementException;
import eu.agno3.orchestrator.jobs.agent.service.ServiceManager;
import eu.agno3.orchestrator.jobs.agent.system.ConfigRepository;
import eu.agno3.orchestrator.jobs.agent.system.ConfigRepositoryException;
import eu.agno3.orchestrator.system.cfgfiles.ResourceLibrarySynchronizationHandler;


/**
 * @author mbechler
 *
 */
@Component ( service = ResourceLibrarySynchronizationHandler.class )
public class TruststoreLibrarySynchronizationHandler implements ResourceLibrarySynchronizationHandler {

    private static final Logger log = Logger.getLogger(TruststoreLibrarySynchronizationHandler.class);

    private TruststoresManager truststoresManager;
    private ServiceManager serviceManager;
    private ConfigRepository configRepo;


    @Reference
    protected synchronized void setTruststoresManager ( TruststoresManager tsm ) {
        this.truststoresManager = tsm;
    }


    protected synchronized void unsetTruststoresManager ( TruststoresManager tsm ) {
        if ( this.truststoresManager == tsm ) {
            this.truststoresManager = null;
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
    protected synchronized void setConfigRepo ( ConfigRepository cr ) {
        this.configRepo = cr;
    }


    protected synchronized void unsetConfigRepo ( ConfigRepository cr ) {
        if ( this.configRepo == cr ) {
            this.configRepo = null;
        }
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.system.cfgfiles.ResourceLibrarySynchronizationHandler#getType()
     */
    @Override
    public String getType () {
        return TruststoreResourceLibraryDescriptor.RESOURCE_LIBRARY_TYPE;
    }


    /**
     * 
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.system.cfgfiles.ResourceLibrarySynchronizationHandler#list(eu.agno3.orchestrator.config.model.realm.StructuralObjectReference,
     *      java.lang.String)
     */
    @Override
    public List<ResourceLibraryEntry> list ( StructuralObjectReference serviceTarget, String hint ) throws ResourceLibraryException {

        if ( !HostConfigServiceTypeDescriptor.HOSTCONFIG_SERVICE_TYPE.equals(serviceTarget.getLocalType()) ) {
            throw new ResourceLibraryException("Target is not hostconfig"); //$NON-NLS-1$
        }

        if ( StringUtils.isEmpty(hint) ) {
            throw new ResourceLibraryException("Cannot list without a hint"); //$NON-NLS-1$
        }

        try {

            if ( !this.truststoresManager.hasTrustStore(hint) ) {
                if ( log.isDebugEnabled() ) {
                    log.debug("Truststore does not exist " + hint); //$NON-NLS-1$
                }
                return Collections.EMPTY_LIST;
            }

            TruststoreManager trustStoreManager = this.truststoresManager.getTrustStoreManager(hint);

            List<ResourceLibraryEntry> res = new ArrayList<>();
            for ( X509Certificate x509Certificate : trustStoreManager.listCertificates() ) {
                ResourceLibraryEntry entry = new ResourceLibraryEntry();
                entry.setContent(x509Certificate.getEncoded());
                entry.setPath(TruststoreUtil.makeCertificatePath(x509Certificate));
                byte[] certHash = certHash(x509Certificate);
                entry.setHash(Hex.encodeHexString(certHash));
                if ( log.isDebugEnabled() ) {
                    log.debug(String.format("Certificate at path %s: %s", entry.getPath(), Hex.encodeHexString(certHash))); //$NON-NLS-1$
                }
                res.add(entry);
            }

            if ( res.isEmpty() ) {
                log.debug("No certificates found"); //$NON-NLS-1$
            }
            return res;
        }
        catch (
            TruststoreManagerException |
            NoSuchAlgorithmException |
            CertificateEncodingException e ) {
            throw new ResourceLibraryException("Failed to access truststore", e); //$NON-NLS-1$
        }

    }


    /**
     * @param x509Certificate
     * @return
     * @throws NoSuchAlgorithmException
     * @throws CertificateEncodingException
     */
    private static byte[] certHash ( X509Certificate x509Certificate ) throws NoSuchAlgorithmException, CertificateEncodingException {
        return MessageDigest.getInstance("SHA-256").digest(x509Certificate.getEncoded()); //$NON-NLS-1$
    }


    /**
     * 
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.system.cfgfiles.ResourceLibrarySynchronizationHandler#synchronize(eu.agno3.orchestrator.config.model.realm.StructuralObjectReference,
     *      java.lang.String, java.util.Set, java.util.Set, java.util.Set)
     */
    @Override
    public List<ResourceLibraryEntry> synchronize ( StructuralObjectReference serviceTarget, String hint, Set<ResourceLibraryEntry> update,
            Set<ResourceLibraryEntry> add, Set<ResourceLibraryEntry> delete ) throws ResourceLibraryException {

        if ( !HostConfigServiceTypeDescriptor.HOSTCONFIG_SERVICE_TYPE.equals(serviceTarget.getLocalType()) ) {
            throw new ResourceLibraryException("Target is not hostconfig"); //$NON-NLS-1$
        }

        if ( StringUtils.isEmpty(hint) ) {
            throw new ResourceLibraryException("Cannot synchronize without a hint"); //$NON-NLS-1$
        }

        if ( log.isDebugEnabled() ) {
            log.debug("Synchronizing truststore " + hint); //$NON-NLS-1$
        }

        boolean modified = false;

        try {

            if ( this.truststoresManager.hasTrustStore(hint) && this.truststoresManager.isReadOnly(hint) ) {
                throw new ResourceLibraryException("Truststore is read-only: " + hint); //$NON-NLS-1$
            }

            if ( !this.truststoresManager.hasTrustStore(hint) ) {
                this.truststoresManager.createTrustStore(hint);
            }

            TruststoreManager trustStoreManager = this.truststoresManager.getTrustStoreManager(hint);

            Map<String, X509Certificate> byHash = new HashMap<>();
            for ( X509Certificate cert : trustStoreManager.listCertificates() ) {
                String certHash = Hex.encodeHexString(certHash(cert));
                if ( log.isDebugEnabled() ) {
                    log.debug(String.format("Found hash %s for %s", certHash, cert.getSubjectX500Principal().getName())); //$NON-NLS-1$
                }
                byHash.put(certHash, cert);
            }

            for ( ResourceLibraryEntry toAdd : add ) {
                X509Certificate cert = (X509Certificate) CertificateFactory.getInstance("X509").generateCertificate( //$NON-NLS-1$
                    new ByteArrayInputStream(toAdd.getContent()));
                log.info("Adding certificate " + cert.getSubjectX500Principal().getName()); //$NON-NLS-1$
                trustStoreManager.addCertificate(cert);
                modified = true;
            }

            for ( ResourceLibraryEntry toUpdate : update ) {
                String hash = toUpdate.getOldHash();
                X509Certificate cert = byHash.get(hash);
                if ( cert == null ) {
                    log.warn("Failed to find certificate for update with hash " + hash); //$NON-NLS-1$
                    continue;
                }
                log.info("Updating certificate " + cert.getSubjectX500Principal().getName()); //$NON-NLS-1$
                trustStoreManager.removeCertificate(cert);
                trustStoreManager.addCertificate((X509Certificate) CertificateFactory.getInstance("X509").generateCertificate( //$NON-NLS-1$
                    new ByteArrayInputStream(toUpdate.getContent())));
                modified = true;
            }

            for ( ResourceLibraryEntry toDelete : delete ) {
                String hash = toDelete.getHash();
                X509Certificate cert = byHash.get(hash);
                if ( cert == null ) {
                    log.warn("Failed to find certificate for deletion with hash " + hash); //$NON-NLS-1$
                    continue;
                }
                log.info("Removing certificate " + cert.getSubjectX500Principal().getName()); //$NON-NLS-1$
                trustStoreManager.removeCertificate(cert);
                modified = true;
            }
        }
        catch (
            TruststoreManagerException |
            CertificateException |
            NoSuchAlgorithmException e ) {
            throw new ResourceLibraryException("Failed to access truststore", e); //$NON-NLS-1$
        }

        List<ResourceLibraryEntry> list = list(serviceTarget, hint);

        if ( modified ) {
            triggerServicesRefresh(hint);
        }

        return list;
    }


    /**
     * @param trustStore
     */
    private void triggerServicesRefresh ( String trustStore ) {
        try {
            for ( StructuralObjectReference service : this.configRepo.getServiceReferences() ) {
                try {

                    BaseServiceManager sm = this.serviceManager.getServiceManager(service, BaseServiceManager.class);
                    if ( sm instanceof RuntimeServiceManager ) {
                        if ( log.isDebugEnabled() ) {
                            log.debug("Reloading truststore on service " + service); //$NON-NLS-1$
                        }
                        RuntimeServiceManager rsm = (RuntimeServiceManager) sm;
                        rsm.forceReloadAll("truststore@" + trustStore); //$NON-NLS-1$
                    }
                    else if ( log.isDebugEnabled() ) {
                        log.debug("Not a runtime service " + service); //$NON-NLS-1$
                    }
                }
                catch ( ServiceManagementException e ) {
                    log.warn("Failed to reload service configuration for " + service, e); //$NON-NLS-1$
                }
            }
        }
        catch ( ConfigRepositoryException e ) {
            log.warn("Failure looking up services", e); //$NON-NLS-1$
        }
    }
}
