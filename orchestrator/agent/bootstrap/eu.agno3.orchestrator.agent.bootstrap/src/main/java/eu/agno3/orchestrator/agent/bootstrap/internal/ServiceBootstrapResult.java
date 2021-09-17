/**
 * © 2016 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 20.10.2016 by mbechler
 */
package eu.agno3.orchestrator.agent.bootstrap.internal;


import eu.agno3.orchestrator.config.model.realm.ServiceStructuralObject;


/**
 * @author mbechler
 *
 */
public class ServiceBootstrapResult {

    private CryptoBootstrapResult cryptoResult;
    private ServiceStructuralObject hostService;
    private ServiceStructuralObject serverService;


    /**
     * @param cryptoResult
     * @param hostService
     * @param serverService
     * 
     */
    public ServiceBootstrapResult ( CryptoBootstrapResult cryptoResult, ServiceStructuralObject hostService, ServiceStructuralObject serverService ) {
        this.cryptoResult = cryptoResult;
        this.hostService = hostService;
        this.serverService = serverService;
    }


    /**
     * @return the cryptoResult
     */
    public CryptoBootstrapResult getCryptoResult () {
        return this.cryptoResult;
    }


    /**
     * @return the hostService
     */
    public ServiceStructuralObject getHostService () {
        return this.hostService;
    }


    /**
     * @return the serverService
     */
    public ServiceStructuralObject getServerService () {
        return this.serverService;
    }
}
