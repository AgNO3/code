/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 14.04.2015 by mbechler
 */
package eu.agno3.orchestrator.agent.realms.units;


import java.io.IOException;
import java.util.Collection;

import javax.security.auth.kerberos.KerberosKey;

import eu.agno3.orchestrator.agent.realms.KeyTabManager;
import eu.agno3.orchestrator.agent.realms.RealmManager;
import eu.agno3.orchestrator.system.base.execution.Context;
import eu.agno3.orchestrator.system.base.execution.Status;
import eu.agno3.orchestrator.system.base.execution.exception.ExecutionException;
import eu.agno3.orchestrator.system.base.execution.result.StatusOnlyResult;
import eu.agno3.runtime.net.krb5.KerberosException;


/**
 * @author mbechler
 *
 */
public class ImportKey extends RealmExecutionUnit<StatusOnlyResult, ImportKey, ImportKeyConfigurator> {

    /**
     * 
     */
    private static final long serialVersionUID = 2562083304791058572L;

    private String keytab;

    private Collection<KerberosKey> keys;


    /**
     * @return the keytab
     */
    public String getKeytab () {
        return this.keytab;
    }


    /**
     * @param keytab
     *            the keytab to set
     */
    void setKeytab ( String keytab ) {
        this.keytab = keytab;
    }


    /**
     * @return the keys to import
     */
    public Collection<KerberosKey> getKeys () {
        return this.keys;
    }


    /**
     * @param keys
     *            the keys to set
     */
    void setKeys ( Collection<KerberosKey> keys ) {
        this.keys = keys;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.system.base.execution.ExecutionUnit#prepare(eu.agno3.orchestrator.system.base.execution.Context)
     */
    @Override
    public StatusOnlyResult prepare ( Context context ) throws ExecutionException {
        return new StatusOnlyResult(Status.SUCCESS);
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.system.base.execution.ExecutionUnit#execute(eu.agno3.orchestrator.system.base.execution.Context)
     */
    @Override
    public StatusOnlyResult execute ( Context context ) throws ExecutionException {
        try {
            if ( !checkRealmExists(context) ) {
                throw new ExecutionException("Realm does not exist " + this.getRealmName()); //$NON-NLS-1$
            }
            RealmManager realmManager = getRealmsManager(context).getRealmManager(this.realmName);

            KeyTabManager ktm = realmManager.getKeytabManager(this.getKeytab());

            if ( !ktm.exists() ) {
                throw new ExecutionException("Keytab does not exist " + this.getKeytab()); //$NON-NLS-1$
            }

            ktm.addKeys(this.getKeys());
            ktm.save();
        }
        catch (
            KerberosException |
            IOException e ) {
            throw new ExecutionException("Keytab check/creation failed", e); //$NON-NLS-1$
        }

        return new StatusOnlyResult(Status.SUCCESS);
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.system.base.execution.ExecutionUnit#createConfigurator()
     */
    @Override
    public ImportKeyConfigurator createConfigurator () {
        return new ImportKeyConfigurator(this);
    }

}
