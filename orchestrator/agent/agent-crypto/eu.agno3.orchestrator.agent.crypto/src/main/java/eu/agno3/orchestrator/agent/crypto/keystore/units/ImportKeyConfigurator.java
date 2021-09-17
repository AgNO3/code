/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 11.12.2014 by mbechler
 */
package eu.agno3.orchestrator.agent.crypto.keystore.units;


import java.security.KeyPair;
import java.security.cert.X509Certificate;
import java.util.List;

import eu.agno3.orchestrator.system.base.execution.impl.AbstractConfigurator;
import eu.agno3.orchestrator.system.base.execution.result.StatusOnlyResult;
import eu.agno3.orchestrator.types.entities.crypto.KeyPairEntry;
import eu.agno3.orchestrator.types.entities.crypto.X509CertEntry;


/**
 * @author mbechler
 *
 */
public class ImportKeyConfigurator extends AbstractConfigurator<StatusOnlyResult, ImportKey, ImportKeyConfigurator> {

    /**
     * @param unit
     */
    protected ImportKeyConfigurator ( ImportKey unit ) {
        super(unit);
    }


    /**
     * @param ksName
     * @return this configurator
     */
    public ImportKeyConfigurator keystore ( String ksName ) {
        this.getExecutionUnit().setKeystoreName(ksName);
        return this.self();
    }


    /**
     * @param alias
     * @return this configurator
     */
    public ImportKeyConfigurator alias ( String alias ) {
        this.getExecutionUnit().setAlias(alias);
        return this.self();
    }


    /**
     * 
     * @param kp
     * @return this configurator
     */
    public ImportKeyConfigurator keyPair ( KeyPair kp ) {
        this.getExecutionUnit().setKeyPair(kp);
        return this.self();
    }


    /**
     * @param keyPair
     * @return this configurator
     */
    public ImportKeyConfigurator keyPair ( KeyPairEntry keyPair ) {
        return this.keyPair(keyPair.getKeyPair());
    }


    /**
     * 
     * @param chain
     * @return this configurator
     */
    public ImportKeyConfigurator certChain ( X509Certificate[] chain ) {
        this.getExecutionUnit().setChain(chain);
        return this.self();
    }


    /**
     * @param certificateChain
     * @return this configurator
     */
    public ImportKeyConfigurator certChain ( List<X509CertEntry> certificateChain ) {
        if ( certificateChain == null ) {
            return certChain((X509Certificate[]) null);
        }
        X509Certificate[] array = new X509Certificate[certificateChain.size()];

        int i = 0;
        for ( X509CertEntry e : certificateChain ) {
            array[ i ] = e.getCertificate();
            i++;
        }

        return certChain(array);
    }

}
