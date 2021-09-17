/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 02.04.2015 by mbechler
 */
package eu.agno3.runtime.net.ad.internal;


import eu.agno3.runtime.util.sid.SID;


/**
 * @author mbechler
 *
 */
public class JoinResult {

    private String newMachinePassword;
    private SID machineSID;
    private SID domainSID;
    private int kvno;


    /**
     * @param newMachinePassword
     * @param domainSID
     * @param machineSID
     * @param kvno
     */
    public JoinResult ( String newMachinePassword, SID domainSID, SID machineSID, int kvno ) {
        super();
        this.newMachinePassword = newMachinePassword;
        this.domainSID = domainSID;
        this.machineSID = machineSID;
        this.kvno = kvno;
    }


    /**
     * @return the newMachinePassword
     */
    public String getNewMachinePassword () {
        return this.newMachinePassword;
    }


    /**
     * @return the domainSID
     */
    public SID getDomainSID () {
        return this.domainSID;
    }


    /**
     * @return the machineSID
     */
    public SID getMachineSID () {
        return this.machineSID;
    }


    /**
     * @return the kvno
     */
    public int getKvno () {
        return this.kvno;
    }

}
