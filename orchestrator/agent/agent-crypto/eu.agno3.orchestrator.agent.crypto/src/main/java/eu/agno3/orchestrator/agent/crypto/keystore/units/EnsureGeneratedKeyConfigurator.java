/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 07.01.2015 by mbechler
 */
package eu.agno3.orchestrator.agent.crypto.keystore.units;


import eu.agno3.orchestrator.system.base.execution.impl.AbstractConfigurator;
import eu.agno3.orchestrator.system.base.execution.result.StatusOnlyResult;
import eu.agno3.runtime.crypto.keystore.KeyType;


/**
 * @author mbechler
 *
 */
public class EnsureGeneratedKeyConfigurator extends AbstractConfigurator<StatusOnlyResult, EnsureGeneratedKey, EnsureGeneratedKeyConfigurator> {

    /**
     * @param unit
     */
    protected EnsureGeneratedKeyConfigurator ( EnsureGeneratedKey unit ) {
        super(unit);
    }


    /**
     * 
     * @param keystore
     * @return this configurator
     */
    public EnsureGeneratedKeyConfigurator keystore ( String keystore ) {
        this.getExecutionUnit().setKeystoreName(keystore);
        return this.self();
    }


    /**
     * @param alias
     * @return this configurator
     */
    public EnsureGeneratedKeyConfigurator alias ( String alias ) {
        this.getExecutionUnit().setKeyAlias(alias);
        return this.self();
    }


    /**
     * 
     * @param kt
     * @return this configurator
     */
    public EnsureGeneratedKeyConfigurator type ( KeyType kt ) {
        this.getExecutionUnit().setKeyType(kt);
        return this.self();
    }

}
