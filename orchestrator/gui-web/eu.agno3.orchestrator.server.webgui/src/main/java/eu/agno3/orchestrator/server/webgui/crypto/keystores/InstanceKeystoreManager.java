/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 14.04.2015 by mbechler
 */
package eu.agno3.orchestrator.server.webgui.crypto.keystores;


import java.io.IOException;
import java.security.KeyPair;
import java.security.cert.Certificate;
import java.security.cert.CertificateEncodingException;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.apache.commons.codec.binary.Base64;
import org.joda.time.Duration;

import eu.agno3.orchestrator.config.model.base.exceptions.AgentCommunicationErrorException;
import eu.agno3.orchestrator.config.model.base.exceptions.AgentDetachedException;
import eu.agno3.orchestrator.config.model.base.exceptions.AgentOfflineException;
import eu.agno3.orchestrator.config.model.base.exceptions.ModelObjectNotFoundException;
import eu.agno3.orchestrator.config.model.base.exceptions.ModelServiceException;
import eu.agno3.orchestrator.crypto.keystore.CertRequestData;
import eu.agno3.orchestrator.crypto.keystore.ExtensionData;
import eu.agno3.orchestrator.crypto.keystore.KeyInfo;
import eu.agno3.orchestrator.crypto.keystore.KeyStoreInfo;
import eu.agno3.orchestrator.crypto.keystore.KeystoreManagerException;
import eu.agno3.orchestrator.crypto.service.KeystoreManagementService;
import eu.agno3.orchestrator.gui.connector.ws.GuiWebServiceException;
import eu.agno3.orchestrator.jobs.JobInfo;
import eu.agno3.orchestrator.server.webgui.connector.ServerServiceProvider;
import eu.agno3.orchestrator.server.webgui.structure.StructureViewContextBean;
import eu.agno3.runtime.crypto.x509.CertExtension;


/**
 * @author mbechler
 *
 */
@ApplicationScoped
public class InstanceKeystoreManager {

    @Inject
    private StructureViewContextBean structureContext;

    @Inject
    private ServerServiceProvider ssp;


    public List<KeyStoreInfo> getKeystores ( boolean includeInternal ) throws ModelObjectNotFoundException, ModelServiceException,
            GuiWebServiceException, AgentDetachedException, AgentOfflineException, AgentCommunicationErrorException, KeystoreManagerException {
        return this.ssp.getService(KeystoreManagementService.class).getKeystores(this.structureContext.getSelectedInstance(), includeInternal);
    }


    /**
     * @param keystore
     * @return the keystore info
     * @throws GuiWebServiceException
     * @throws KeystoreManagerException
     * @throws ModelServiceException
     * @throws AgentCommunicationErrorException
     * @throws AgentOfflineException
     * @throws AgentDetachedException
     * @throws ModelObjectNotFoundException
     */
    public KeyStoreInfo getKeystoreInfo ( String keystore ) throws ModelObjectNotFoundException, AgentDetachedException, AgentOfflineException,
            AgentCommunicationErrorException, ModelServiceException, KeystoreManagerException, GuiWebServiceException {
        return this.ssp.getService(KeystoreManagementService.class).getKeystore(this.structureContext.getSelectedInstance(), keystore);
    }


    /**
     * @param keystore
     * @param keyAlias
     * @param keyType
     * @return the key generation job info
     * @throws GuiWebServiceException
     * @throws KeystoreManagerException
     * @throws ModelServiceException
     * @throws AgentCommunicationErrorException
     * @throws AgentDetachedException
     * @throws ModelObjectNotFoundException
     */
    public JobInfo generateKey ( String keystore, String keyAlias, String keyType ) throws ModelObjectNotFoundException, AgentDetachedException,
            AgentCommunicationErrorException, ModelServiceException, KeystoreManagerException, GuiWebServiceException {
        return this.ssp.getService(KeystoreManagementService.class)
                .generateKey(this.structureContext.getSelectedInstance(), keystore, keyAlias, keyType);
    }


    /**
     * @param keystore
     * @param keyAlias
     * @param algo
     * @param importKeyPair
     * @param importChain
     * @throws GuiWebServiceException
     * @throws KeystoreManagerException
     * @throws ModelServiceException
     * @throws AgentCommunicationErrorException
     * @throws AgentDetachedException
     * @throws ModelObjectNotFoundException
     * @throws CertificateEncodingException
     */
    public void importKey ( String keystore, String keyAlias, String algo, KeyPair importKeyPair, List<Certificate> importChain )
            throws ModelObjectNotFoundException, AgentDetachedException, AgentCommunicationErrorException, ModelServiceException,
            KeystoreManagerException, GuiWebServiceException, CertificateEncodingException {
        this.ssp.getService(KeystoreManagementService.class).importKey(
            this.structureContext.getSelectedInstance(),
            keystore,
            keyAlias,
            algo,
            Base64.encodeBase64String(importKeyPair.getPrivate().getEncoded()),
            wrapChain(importChain));

    }


    /**
     * 
     * @param keystore
     * @param chain
     * @return whether the chain validates
     * @throws ModelObjectNotFoundException
     * @throws AgentDetachedException
     * @throws AgentCommunicationErrorException
     * @throws CertificateEncodingException
     * @throws ModelServiceException
     * @throws KeystoreManagerException
     * @throws GuiWebServiceException
     */
    public String validateChain ( String keystore, List<Certificate> chain ) throws ModelObjectNotFoundException, AgentDetachedException,
            AgentCommunicationErrorException, CertificateEncodingException, ModelServiceException, KeystoreManagerException, GuiWebServiceException {
        return this.ssp.getService(KeystoreManagementService.class)
                .validateCertificate(this.structureContext.getSelectedInstance(), keystore, wrapChain(chain));
    }


    /**
     * @param chain
     * @return
     * @throws CertificateEncodingException
     */
    private static List<String> wrapChain ( List<Certificate> chain ) throws CertificateEncodingException {
        if ( chain == null ) {
            return null;
        }
        List<String> certData = new LinkedList<>();

        for ( Certificate cert : chain ) {
            certData.add(Base64.encodeBase64String(cert.getEncoded()));
        }
        return certData;
    }


    /**
     * @param keystore
     * @param keyAlias
     * @param requestPassword
     * @return the generated CSR
     * @throws GuiWebServiceException
     * @throws KeystoreManagerException
     * @throws ModelServiceException
     * @throws AgentCommunicationErrorException
     * @throws AgentDetachedException
     * @throws ModelObjectNotFoundException
     */
    public String getRenewalCSR ( String keystore, String keyAlias, String requestPassword ) throws ModelObjectNotFoundException,
            AgentDetachedException, AgentCommunicationErrorException, ModelServiceException, KeystoreManagerException, GuiWebServiceException {
        return this.ssp.getService(KeystoreManagementService.class)
                .generateRenewalCSR(this.structureContext.getSelectedInstance(), keystore, keyAlias, requestPassword);
    }


    /**
     * @param keystore
     * @param keyAlias
     * @throws GuiWebServiceException
     * @throws KeystoreManagerException
     * @throws ModelServiceException
     * @throws AgentCommunicationErrorException
     * @throws AgentDetachedException
     * @throws ModelObjectNotFoundException
     */
    public void deleteKey ( String keystore, String keyAlias ) throws ModelObjectNotFoundException, AgentDetachedException,
            AgentCommunicationErrorException, ModelServiceException, KeystoreManagerException, GuiWebServiceException {
        this.ssp.getService(KeystoreManagementService.class).deleteKey(this.structureContext.getSelectedInstance(), keystore, keyAlias);
    }


    /**
     * @param keystore
     * @param keyAlias
     * @param chain
     * @throws GuiWebServiceException
     * @throws KeystoreManagerException
     * @throws ModelServiceException
     * @throws CertificateEncodingException
     * @throws AgentCommunicationErrorException
     * @throws AgentDetachedException
     * @throws ModelObjectNotFoundException
     */
    public void updateChain ( String keystore, String keyAlias, List<Certificate> chain ) throws ModelObjectNotFoundException, AgentDetachedException,
            AgentCommunicationErrorException, CertificateEncodingException, ModelServiceException, KeystoreManagerException, GuiWebServiceException {
        this.ssp.getService(KeystoreManagementService.class)
                .updateChain(this.structureContext.getSelectedInstance(), keystore, keyAlias, wrapChain(chain));
    }


    /**
     * @param keystore
     * @param keyAlias
     * @param subject
     * @param extensions
     * @param requestPassword
     * @param lifetime
     * @return the generated CSR
     * @throws GuiWebServiceException
     * @throws KeystoreManagerException
     * @throws ModelServiceException
     * @throws AgentCommunicationErrorException
     * @throws AgentDetachedException
     * @throws ModelObjectNotFoundException
     * @throws IOException
     */
    public String getCSR ( String keystore, String keyAlias, String subject, Set<CertExtension> extensions, String requestPassword,
            Duration lifetime ) throws ModelObjectNotFoundException, AgentDetachedException, AgentCommunicationErrorException, ModelServiceException,
                    KeystoreManagerException, GuiWebServiceException, IOException {
        CertRequestData certReqData = new CertRequestData();
        certReqData.setSubject(subject);
        certReqData.setRequestPassword(requestPassword);
        certReqData.setExtensions(wrapExtensions(extensions));
        certReqData.setLifetimeDays((int) lifetime.getStandardDays());
        return this.ssp.getService(KeystoreManagementService.class)
                .generateCSR(this.structureContext.getSelectedInstance(), keystore, keyAlias, certReqData);
    }


    /**
     * @param extensions
     * @return
     * @throws IOException
     */
    private static Set<ExtensionData> wrapExtensions ( Set<CertExtension> extensions ) throws IOException {
        if ( extensions == null ) {
            return null;
        }
        Set<ExtensionData> data = new HashSet<>();
        for ( CertExtension ext : extensions ) {
            ExtensionData extdata = new ExtensionData();
            extdata.setOid(ext.getObjectIdentifier().toString());
            extdata.setCritical(ext.isCritical());
            extdata.setData(Base64.encodeBase64String(ext.getExtension().toASN1Primitive().getEncoded()));
            data.add(extdata);
        }
        return data;
    }


    /**
     * @param keystore
     * @param keyAlias
     * @return the key info
     * @throws GuiWebServiceException
     * @throws KeystoreManagerException
     * @throws ModelServiceException
     * @throws AgentCommunicationErrorException
     * @throws AgentDetachedException
     * @throws ModelObjectNotFoundException
     */
    public KeyInfo getKeyInfo ( String keystore, String keyAlias ) throws ModelObjectNotFoundException, AgentDetachedException,
            AgentCommunicationErrorException, ModelServiceException, KeystoreManagerException, GuiWebServiceException {
        return this.ssp.getService(KeystoreManagementService.class).getKeyInfo(this.structureContext.getSelectedInstance(), keystore, keyAlias);
    }


    /**
     * @param keystore
     * @param keyAlias
     * @param subject
     * @param requestPassword
     * @param extensions
     * @param lifetime
     * @throws GuiWebServiceException
     * @throws KeystoreManagerException
     * @throws ModelServiceException
     * @throws AgentCommunicationErrorException
     * @throws AgentDetachedException
     * @throws ModelObjectNotFoundException
     * @throws IOException
     * 
     */
    public void makeSelfSigned ( String keystore, String keyAlias, String subject, String requestPassword, Set<CertExtension> extensions,
            Duration lifetime ) throws ModelObjectNotFoundException, AgentDetachedException, AgentCommunicationErrorException, ModelServiceException,
                    KeystoreManagerException, GuiWebServiceException, IOException {
        CertRequestData certReqData = new CertRequestData();
        certReqData.setSubject(subject);
        certReqData.setLifetimeDays((int) lifetime.getStandardDays());
        certReqData.setRequestPassword(requestPassword);
        certReqData.setExtensions(wrapExtensions(extensions));
        this.ssp.getService(KeystoreManagementService.class).makeSelfSignedCert(
            this.structureContext.getSelectedInstance(),
            keystore,
            keyAlias,
            certReqData);
    }

}
