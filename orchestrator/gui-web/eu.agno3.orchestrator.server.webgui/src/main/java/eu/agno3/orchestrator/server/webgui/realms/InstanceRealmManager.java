/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 09.04.2015 by mbechler
 */
package eu.agno3.orchestrator.server.webgui.realms;


import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.inject.Named;

import org.apache.log4j.Logger;

import eu.agno3.orchestrator.config.model.base.exceptions.AgentCommunicationErrorException;
import eu.agno3.orchestrator.config.model.base.exceptions.AgentDetachedException;
import eu.agno3.orchestrator.config.model.base.exceptions.AgentOfflineException;
import eu.agno3.orchestrator.config.model.base.exceptions.ModelObjectNotFoundException;
import eu.agno3.orchestrator.config.model.base.exceptions.ModelServiceException;
import eu.agno3.orchestrator.config.model.realm.InstanceStructuralObject;
import eu.agno3.orchestrator.gui.connector.ws.GuiWebServiceException;
import eu.agno3.orchestrator.realms.KeyData;
import eu.agno3.orchestrator.realms.KeyInfo;
import eu.agno3.orchestrator.realms.RealmInfo;
import eu.agno3.orchestrator.realms.RealmManagementException;
import eu.agno3.orchestrator.realms.RealmType;
import eu.agno3.orchestrator.realms.service.RealmManagementService;
import eu.agno3.orchestrator.server.webgui.connector.ServerServiceProvider;
import eu.agno3.orchestrator.server.webgui.crypto.InstanceCredentialWrapper;
import eu.agno3.orchestrator.server.webgui.structure.StructureViewContextBean;
import eu.agno3.runtime.security.credentials.UsernamePasswordCredential;


/**
 * @author mbechler
 *
 */
@Named ( "instanceRealmManager" )
@ApplicationScoped
public class InstanceRealmManager {

    private static final Logger log = Logger.getLogger(InstanceRealmManager.class);

    @Inject
    private StructureViewContextBean structureContext;

    @Inject
    private ServerServiceProvider ssp;

    @Inject
    private InstanceCredentialWrapper credWrapper;


    public List<RealmInfo> getRealms () throws ModelObjectNotFoundException, ModelServiceException, RealmManagementException, GuiWebServiceException,
            AgentDetachedException, AgentOfflineException, AgentCommunicationErrorException {
        return this.ssp.getService(RealmManagementService.class).getRealms(this.structureContext.getSelectedInstance());
    }


    /**
     * @param realm
     * @return the realm info
     * @throws GuiWebServiceException
     * @throws RealmManagementException
     * @throws ModelServiceException
     * @throws AgentCommunicationErrorException
     * @throws AgentOfflineException
     * @throws AgentDetachedException
     * @throws ModelObjectNotFoundException
     */
    public RealmInfo getRealm ( String realm ) throws ModelObjectNotFoundException, AgentDetachedException, AgentOfflineException,
            AgentCommunicationErrorException, ModelServiceException, RealmManagementException, GuiWebServiceException {
        return this.ssp.getService(RealmManagementService.class).getRealm(this.structureContext.getSelectedInstance(), realm);
    }


    /**
     * @param ki
     * @throws GuiWebServiceException
     * @throws RealmManagementException
     * @throws ModelServiceException
     * @throws ModelObjectNotFoundException
     * @throws AgentOfflineException
     * @throws AgentDetachedException
     * @throws AgentCommunicationErrorException
     */
    public void removeKey ( KeyInfoWrapper ki ) throws ModelObjectNotFoundException, ModelServiceException, RealmManagementException,
            GuiWebServiceException, AgentDetachedException, AgentOfflineException, AgentCommunicationErrorException {
        log.info("Remove key"); //$NON-NLS-1$
        this.ssp.getService(RealmManagementService.class).removeKeys(
            this.structureContext.getSelectedInstance(),
            ki.getKeytab().getRealm().getRealmName(),
            ki.getKeytab().getRealm().getType(),
            ki.getKeytab().getKeytab().getId(),
            Arrays.asList(ki.getKey()));
    }


    /**
     * @param kti
     * @throws GuiWebServiceException
     * @throws RealmManagementException
     * @throws ModelServiceException
     * @throws ModelObjectNotFoundException
     * @throws AgentOfflineException
     * @throws AgentDetachedException
     * @throws AgentCommunicationErrorException
     */
    public void removeKeytab ( KeytabInfoWrapper kti ) throws ModelObjectNotFoundException, ModelServiceException, RealmManagementException,
            GuiWebServiceException, AgentDetachedException, AgentOfflineException, AgentCommunicationErrorException {
        log.info("Remove keytab"); //$NON-NLS-1$
        this.ssp.getService(RealmManagementService.class).removeKeytab(
            this.structureContext.getSelectedInstance(),
            kti.getRealm().getRealmName(),
            kti.getRealm().getType(),
            kti.getKeytab().getId());
    }


    /**
     * @param domain
     * @param user
     * @param pass
     * @throws GuiWebServiceException
     * @throws RealmManagementException
     * @throws ModelServiceException
     * @throws ModelObjectNotFoundException
     * @throws AgentOfflineException
     * @throws AgentDetachedException
     * @throws AgentCommunicationErrorException
     */
    public void joinDomain ( String domain, String user, String pass ) throws ModelObjectNotFoundException, ModelServiceException,
            RealmManagementException, GuiWebServiceException, AgentDetachedException, AgentOfflineException, AgentCommunicationErrorException {
        InstanceStructuralObject inst = this.structureContext.getSelectedInstance();
        this.ssp.getService(RealmManagementService.class)
                .joinAD(inst, domain, this.credWrapper.wrap(inst, new UsernamePasswordCredential(user, pass)));
    }


    /**
     * @param domain
     * @param machinePassword
     * @throws GuiWebServiceException
     * @throws RealmManagementException
     * @throws ModelServiceException
     * @throws AgentCommunicationErrorException
     * @throws AgentOfflineException
     * @throws AgentDetachedException
     * @throws ModelObjectNotFoundException
     */
    public void joinDomainWithMachinePassword ( String domain, String machinePassword ) throws ModelObjectNotFoundException, AgentDetachedException,
            AgentOfflineException, AgentCommunicationErrorException, ModelServiceException, RealmManagementException, GuiWebServiceException {
        InstanceStructuralObject inst = this.structureContext.getSelectedInstance();
        this.ssp.getService(RealmManagementService.class).joinADWithMachinePassword(inst, domain, machinePassword);
    }


    /**
     * @param domain
     * @param user
     * @param pass
     * @throws GuiWebServiceException
     * @throws RealmManagementException
     * @throws ModelServiceException
     * @throws ModelObjectNotFoundException
     * @throws AgentOfflineException
     * @throws AgentDetachedException
     * @throws AgentCommunicationErrorException
     */
    public void leaveDomain ( String domain, String user, String pass ) throws ModelObjectNotFoundException, ModelServiceException,
            RealmManagementException, GuiWebServiceException, AgentDetachedException, AgentOfflineException, AgentCommunicationErrorException {
        InstanceStructuralObject inst = this.structureContext.getSelectedInstance();
        this.ssp.getService(RealmManagementService.class)
                .leaveAD(inst, domain, this.credWrapper.wrap(inst, new UsernamePasswordCredential(user, pass)));
    }


    /**
     * @param domain
     * @throws GuiWebServiceException
     * @throws RealmManagementException
     * @throws ModelServiceException
     * @throws ModelObjectNotFoundException
     * @throws AgentOfflineException
     * @throws AgentDetachedException
     * @throws AgentCommunicationErrorException
     */
    public void rekeyDomain ( String domain ) throws ModelObjectNotFoundException, ModelServiceException, RealmManagementException,
            GuiWebServiceException, AgentDetachedException, AgentOfflineException, AgentCommunicationErrorException {
        this.ssp.getService(RealmManagementService.class).rekeyAD(this.structureContext.getSelectedInstance(), domain);
    }


    /**
     * @param realm
     * @param keytab
     * @param type
     * @throws GuiWebServiceException
     * @throws RealmManagementException
     * @throws ModelServiceException
     * @throws AgentOfflineException
     * @throws AgentDetachedException
     * @throws ModelObjectNotFoundException
     * @throws AgentCommunicationErrorException
     */
    public void deleteKeytab ( String realm, RealmType type, String keytab ) throws ModelObjectNotFoundException, AgentDetachedException,
            AgentOfflineException, ModelServiceException, RealmManagementException, GuiWebServiceException, AgentCommunicationErrorException {
        this.ssp.getService(RealmManagementService.class).removeKeytab(this.structureContext.getSelectedInstance(), realm, type, keytab);
    }


    /**
     * @param realm
     * @param type
     * @param keytab
     * @param initialKeys
     * @throws GuiWebServiceException
     * @throws RealmManagementException
     * @throws ModelServiceException
     * @throws AgentOfflineException
     * @throws AgentDetachedException
     * @throws ModelObjectNotFoundException
     * @throws AgentCommunicationErrorException
     */
    public void createKeytab ( String realm, RealmType type, String keytab, List<KeyData> initialKeys )
            throws ModelObjectNotFoundException, AgentDetachedException, AgentOfflineException, ModelServiceException, RealmManagementException,
            GuiWebServiceException, AgentCommunicationErrorException {
        this.ssp.getService(RealmManagementService.class).createKeytab(this.structureContext.getSelectedInstance(), realm, type, keytab, initialKeys);
    }


    /**
     * @param realm
     * @param type
     * @param keytab
     * @param keysToAdd
     * @throws GuiWebServiceException
     * @throws RealmManagementException
     * @throws ModelServiceException
     * @throws AgentOfflineException
     * @throws AgentDetachedException
     * @throws ModelObjectNotFoundException
     * @throws AgentCommunicationErrorException
     */
    public void addKeys ( String realm, RealmType type, String keytab, List<KeyData> keysToAdd )
            throws ModelObjectNotFoundException, AgentDetachedException, AgentOfflineException, ModelServiceException, RealmManagementException,
            GuiWebServiceException, AgentCommunicationErrorException {
        this.ssp.getService(RealmManagementService.class).addKeys(this.structureContext.getSelectedInstance(), realm, type, keytab, keysToAdd);
    }


    /**
     * @param keys
     * @throws GuiWebServiceException
     * @throws RealmManagementException
     * @throws ModelServiceException
     * @throws AgentCommunicationErrorException
     * @throws AgentOfflineException
     * @throws AgentDetachedException
     * @throws ModelObjectNotFoundException
     */
    public void deleteKeys ( Set<KeyInfoWrapper> keys ) throws ModelObjectNotFoundException, AgentDetachedException, AgentOfflineException,
            AgentCommunicationErrorException, ModelServiceException, RealmManagementException, GuiWebServiceException {
        Map<String, RealmType> realmTypes = new HashMap<>();
        Map<String, Map<String, List<KeyInfo>>> keyPerKeytab = new HashMap<>();
        buildKeyMap(keys, realmTypes, keyPerKeytab);
        for ( String realm : keyPerKeytab.keySet() ) {
            for ( String keytabId : keyPerKeytab.get(realm).keySet() ) {
                this.ssp.getService(RealmManagementService.class).removeKeys(
                    this.structureContext.getSelectedInstance(),
                    realm,
                    realmTypes.get(realm),
                    keytabId,
                    keyPerKeytab.get(realm).get(keytabId));
            }
        }
    }


    /**
     * @param keys
     * @param realmTypes
     * @param keyPerKeytab
     */
    private static void buildKeyMap ( Set<KeyInfoWrapper> keys, Map<String, RealmType> realmTypes,
            Map<String, Map<String, List<KeyInfo>>> keyPerKeytab ) {
        for ( KeyInfoWrapper key : keys ) {
            String realmName = key.getKeytab().getRealm().getRealmName();
            String keytabId = key.getKeytab().getKeytab().getId();
            if ( !keyPerKeytab.containsKey(realmName) ) {
                realmTypes.put(realmName, key.getKeytab().getRealm().getType());
                keyPerKeytab.put(realmName, new HashMap<>());
            }

            Map<String, List<KeyInfo>> perRealmMap = keyPerKeytab.get(realmName);

            if ( !perRealmMap.containsKey(keytabId) ) {
                perRealmMap.put(keytabId, new LinkedList<>());
            }

            perRealmMap.get(keytabId).add(key.getKey());
        }
    }

}
