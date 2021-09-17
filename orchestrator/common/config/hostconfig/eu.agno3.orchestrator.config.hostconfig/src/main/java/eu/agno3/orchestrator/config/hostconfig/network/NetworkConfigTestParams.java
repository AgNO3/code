/**
 * © 2017 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: Jan 19, 2017 by mbechler
 */
package eu.agno3.orchestrator.config.hostconfig.network;


import eu.agno3.orchestrator.config.model.validation.ConfigTestParams;


/**
 * @author mbechler
 *
 */
public class NetworkConfigTestParams implements ConfigTestParams {

    /**
     * 
     */
    private static final long serialVersionUID = 1686398003958189126L;

    private String target;
    private Integer port;
    private boolean runPing = true;
    private boolean runTraceroute;


    /**
     * @return the target
     */
    public String getTarget () {
        return this.target;
    }


    /**
     * @param target
     *            the target to set
     */
    public void setTarget ( String target ) {
        this.target = target;
    }


    /**
     * @return the port
     */
    public Integer getPort () {
        return this.port;
    }


    /**
     * @param port
     *            the port to set
     */
    public void setPort ( Integer port ) {
        this.port = port;
    }


    /**
     * @return the runPing
     */
    public boolean getRunPing () {
        return this.runPing;
    }


    /**
     * @param runPing
     *            the runPing to set
     */
    public void setRunPing ( boolean runPing ) {
        this.runPing = runPing;
    }


    /**
     * @return the runTraceroute
     */
    public boolean getRunTraceroute () {
        return this.runTraceroute;
    }


    /**
     * @param runTraceroute
     *            the runTraceroute to set
     */
    public void setRunTraceroute ( boolean runTraceroute ) {
        this.runTraceroute = runTraceroute;
    }
}
