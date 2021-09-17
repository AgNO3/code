/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 18.12.2014 by mbechler
 */
package eu.agno3.orchestrator.agent.crypto.keystore.units;


import java.nio.file.attribute.UserPrincipal;

import eu.agno3.orchestrator.system.base.execution.impl.AbstractConfigurator;
import eu.agno3.orchestrator.system.base.execution.result.StatusOnlyResult;


/**
 * @author mbechler
 *
 */
public class EnsureKeystoreConfigurator extends AbstractConfigurator<StatusOnlyResult, EnsureKeystore, EnsureKeystoreConfigurator> {

    /**
     * @param unit
     */
    protected EnsureKeystoreConfigurator ( EnsureKeystore unit ) {
        super(unit);
    }


    /**
     * @param ksName
     * @return this configurator
     */
    public EnsureKeystoreConfigurator keystore ( String ksName ) {
        this.getExecutionUnit().setKeystoreName(ksName);
        return this.self();
    }


    /**
     * 
     * @return this configurator
     */
    public EnsureKeystoreConfigurator internal () {
        this.getExecutionUnit().setInternal(true);
        return this.self();
    }


    /**
     * @param user
     * @return this configurator
     */
    public EnsureKeystoreConfigurator user ( UserPrincipal user ) {
        this.getExecutionUnit().setUser(user);
        return this.self();
    }


    /**
     * @param validationTrustStore
     * @return this configurator
     */
    public EnsureKeystoreConfigurator validationTruststore ( String validationTrustStore ) {
        this.getExecutionUnit().setValidationTruststore(validationTrustStore);
        return this.self();
    }

}
