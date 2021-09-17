/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 08.04.2015 by mbechler
 */
package eu.agno3.orchestrator.agent.realms.internal;


import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import org.apache.log4j.Logger;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicyOption;

import eu.agno3.orchestrator.agent.realms.RealmManager;
import eu.agno3.orchestrator.agent.realms.RealmsManager;
import eu.agno3.orchestrator.jobs.agent.service.ServiceManager;
import eu.agno3.orchestrator.system.base.SystemService;
import eu.agno3.orchestrator.system.base.SystemServiceType;
import eu.agno3.runtime.net.ad.ADRealmManager;
import eu.agno3.runtime.net.krb5.KerberosException;
import eu.agno3.runtime.net.krb5.KrbRealmManager;
import eu.agno3.runtime.net.krb5.RealmType;


/**
 * @author mbechler
 *
 */
@Component ( service = {
    RealmsManager.class, SystemService.class
} )
@SystemServiceType ( RealmsManager.class )
public class RealmsManagerImpl implements RealmsManager {

    private static final Logger log = Logger.getLogger(RealmsManagerImpl.class);

    private static final String INDEX_FILE = ".index"; //$NON-NLS-1$

    private KrbRealmManager krbRealmManager;
    private ADRealmManager adRealmManager;
    private ServiceManager serviceManager;


    @Reference
    protected synchronized void setKrbRealmManager ( KrbRealmManager krm ) {
        this.krbRealmManager = krm;
    }


    protected synchronized void unsetKrbRealmManager ( KrbRealmManager krm ) {
        if ( this.krbRealmManager == krm ) {
            this.krbRealmManager = null;
        }
    }


    @Reference
    protected synchronized void setADRealmManager ( ADRealmManager adrm ) {
        this.adRealmManager = adrm;
    }


    protected synchronized void unsetADRealmManager ( ADRealmManager adrm ) {
        if ( this.adRealmManager == adrm ) {
            this.adRealmManager = null;
        }
    }


    @Reference ( cardinality = ReferenceCardinality.OPTIONAL, policyOption = ReferencePolicyOption.GREEDY )
    protected synchronized void setServiceManager ( ServiceManager sm ) {
        this.serviceManager = sm;
    }


    protected synchronized void unsetServiceManager ( ServiceManager sm ) {
        if ( this.serviceManager == sm ) {
            this.serviceManager = null;
        }
    }


    @Override
    public Map<String, RealmType> listRealms () throws KerberosException {
        Map<String, RealmType> res = new HashMap<>();
        for ( KrbRealmManager manager : getKrbRealmManagers() ) {
            try {
                for ( Path realmDir : Files.list(manager.getRealmBase()).filter(x -> RealmManagerImpl.isRealm(x)).collect(Collectors.toList()) ) {
                    String name = realmDir.getFileName().toString();
                    if ( res.put(name, manager.getRealmType(name)) != null ) {
                        throw new KerberosException("Duplicate realm " + name); //$NON-NLS-1$
                    }
                }
            }
            catch ( IOException e ) {
                throw new KerberosException("Failed to enumerate realms", e); //$NON-NLS-1$
            }
        }
        return res;
    }


    @Override
    public boolean exists ( String realmName ) {
        for ( KrbRealmManager manager : this.getKrbRealmManagers() ) {
            if ( manager.exists(realmName) ) {
                return true;
            }
        }
        return false;
    }


    @Override
    public RealmType getType ( String realmName ) throws KerberosException {
        for ( KrbRealmManager manager : this.getKrbRealmManagers() ) {
            if ( manager.exists(realmName) ) {
                return manager.getRealmType(realmName);
            }
        }
        throw new KerberosException("No realm found with name " + realmName); //$NON-NLS-1$
    }


    @Override
    public RealmManager getRealmManager ( String realm ) throws KerberosException {
        return getRealmManager(realm, getType(realm));
    }


    @Override
    public RealmManager getRealmManager ( String realm, RealmType type ) throws KerberosException {
        if ( type == RealmType.AD ) {
            return new ADRealmManagerImpl(
                realm,
                type,
                this.adRealmManager.getRealmBase().resolve(realm.toUpperCase(Locale.ROOT)),
                this,
                this.adRealmManager,
                this.serviceManager);
        }
        return new RealmManagerImpl(
            realm,
            type,
            this.krbRealmManager.getRealmBase().resolve(realm.toUpperCase(Locale.ROOT)),
            this,
            this.serviceManager);
    }


    private KrbRealmManager[] getKrbRealmManagers () {
        return new KrbRealmManager[] {
            this.krbRealmManager, this.adRealmManager
        };
    }


    /**
     * 
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.agent.realms.RealmsManager#runMaintenance()
     */
    @Override
    public void runMaintenance () {
        try {
            for ( Entry<String, RealmType> entry : listRealms().entrySet() ) {
                log.debug("Running maintenance for realm", entry.getKey()); //$NON-NLS-1$
                getRealmManager(entry.getKey(), entry.getValue()).runMaintenance();
            }
        }
        catch ( KerberosException e ) {
            log.error("Failed to run realm maintenance", e); //$NON-NLS-1$
        }
    }


    /**
     * @param type
     * @return
     */
    KrbRealmManager getKrbRealmManager ( RealmType type ) {
        if ( type == RealmType.AD ) {
            return this.adRealmManager;
        }
        return this.krbRealmManager;
    }


    /**
     * @param type
     * @return a new index
     * @throws KerberosException
     */
    public synchronized int getNewIndex ( RealmType type ) throws KerberosException {
        Path indexFile = getKrbRealmManager(type).getRealmBase().resolve(INDEX_FILE);
        int index = 0;
        OpenOption[] opts;
        try {
            if ( Files.exists(indexFile) ) {
                int oldIndex = Integer.parseInt(new String(Files.readAllBytes(indexFile), StandardCharsets.UTF_8));
                index = oldIndex + 1;
                opts = new OpenOption[] {
                    StandardOpenOption.TRUNCATE_EXISTING
                };
            }
            else {
                opts = new OpenOption[] {
                    StandardOpenOption.CREATE_NEW
                };
            }

            Files.write(indexFile, String.valueOf(index).getBytes(StandardCharsets.UTF_8), opts);
            return index;
        }
        catch ( IOException e ) {
            throw new KerberosException("Failed to increment realm index", e); //$NON-NLS-1$
        }
    }

}
