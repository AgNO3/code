/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 04.11.2015 by mbechler
 */
package eu.agno3.orchestrator.system.update;


import java.io.Serializable;


/**
 * @author mbechler
 *
 */
public class ServiceInstruction implements Serializable {

    /**
     * 
     */
    private static final long serialVersionUID = 5353503942474117150L;

    private String serviceName;
    private ServiceInstructionType type;


    /**
     * @return the serviceName
     */
    public String getServiceName () {
        return this.serviceName;
    }


    /**
     * @param serviceName
     *            the serviceName to set
     */
    public void setServiceName ( String serviceName ) {
        this.serviceName = serviceName;
    }


    /**
     * @return the type
     */
    public ServiceInstructionType getType () {
        return this.type;
    }


    /**
     * @param type
     *            the type to set
     */
    public void setType ( ServiceInstructionType type ) {
        this.type = type;
    }


    /**
     * @param other
     * @return whether this instruction should supersede the other one
     */
    public boolean supersedes ( ServiceInstruction other ) {
        if ( this.type == ServiceInstructionType.RESTART && other.type == ServiceInstructionType.RELOAD ) {
            return true;
        }
        return false;
    }
}
