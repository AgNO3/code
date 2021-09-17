/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 11.12.2014 by mbechler
 */
package eu.agno3.orchestrator.agent.crypto.truststore.units;


import java.security.cert.X509Certificate;
import java.util.HashSet;
import java.util.Set;

import eu.agno3.orchestrator.system.base.execution.impl.AbstractConfigurator;
import eu.agno3.orchestrator.system.base.execution.result.StatusOnlyResult;
import eu.agno3.orchestrator.types.entities.crypto.X509CertEntry;


/**
 * @author mbechler
 *
 */
public class SynchronizeTruststoreConfigurator extends
        AbstractConfigurator<StatusOnlyResult, SynchronizeTruststore, SynchronizeTruststoreConfigurator> {

    /**
     * @param unit
     */
    protected SynchronizeTruststoreConfigurator ( SynchronizeTruststore unit ) {
        super(unit);
    }


    /**
     * @param name
     * @return this configurator
     */
    public SynchronizeTruststoreConfigurator truststore ( String name ) {
        this.getExecutionUnit().setTruststore(name);
        return this.self();
    }


    /**
     * @param trustAnchors
     * @return this configurator
     */
    public SynchronizeTruststoreConfigurator anchors ( Set<X509Certificate> trustAnchors ) {
        this.getExecutionUnit().setTrustAnchors(trustAnchors);
        return this.self();
    }


    /**
     * @param trustAnchors
     * @return this configurator
     */
    public SynchronizeTruststoreConfigurator anchorsEntry ( Set<X509CertEntry> trustAnchors ) {
        Set<X509Certificate> certs = new HashSet<>();

        for ( X509CertEntry e : trustAnchors ) {
            certs.add(e.getCertificate());
        }

        return this.anchors(certs);
    }
}
