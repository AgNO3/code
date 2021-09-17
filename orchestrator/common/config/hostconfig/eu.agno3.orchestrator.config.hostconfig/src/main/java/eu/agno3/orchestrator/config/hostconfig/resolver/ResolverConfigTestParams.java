/**
 * © 2017 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: Jan 19, 2017 by mbechler
 */
package eu.agno3.orchestrator.config.hostconfig.resolver;


import eu.agno3.orchestrator.config.model.validation.ConfigTestParams;


/**
 * @author mbechler
 *
 */
public class ResolverConfigTestParams implements ConfigTestParams {

    /**
     * 
     */
    private static final long serialVersionUID = 6509376433178999986L;

    private String hostname;


    /**
     * @return the hostname
     */
    public String getHostname () {
        return this.hostname;
    }


    /**
     * @param hostname
     *            the hostname to set
     */
    public void setHostname ( String hostname ) {
        this.hostname = hostname;
    }
}
