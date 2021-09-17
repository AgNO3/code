/**
 * © 2016 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 08.11.2016 by mbechler
 */
package eu.agno3.orchestrator.system.info.network.java.impl.dhcp;


import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import eu.agno3.orchestrator.system.info.network.DHCPLeaseStatus;
import eu.agno3.orchestrator.system.info.network.DHCPOptions;


/**
 * @author mbechler
 *
 */
public class DHCPInterfaceStatus implements Serializable {

    /**
     * 
     */
    private static final long serialVersionUID = -2880488295526975818L;

    private String interfaceName;
    private DHCPOptions options = new DHCPOptions();
    private List<DHCPAssociation> associations = new ArrayList<>();
    private DHCPLeaseStatus status = DHCPLeaseStatus.ACTIVE;


    /**
     * @return the interfaceName
     */
    public String getInterfaceName () {
        return this.interfaceName;
    }


    /**
     * @param interfaceName
     *            the interfaceName to set
     */
    public void setInterfaceName ( String interfaceName ) {
        this.interfaceName = interfaceName;
    }


    /**
     * @return the status
     */
    public DHCPLeaseStatus getStatus () {
        return this.status;
    }


    /**
     * @param status
     *            the status to set
     */
    public void setStatus ( DHCPLeaseStatus status ) {
        this.status = status;
    }


    /**
     * @return the options
     */
    public DHCPOptions getOptions () {
        return this.options;
    }


    /**
     * @param options
     *            the options to set
     */
    public void setOptions ( DHCPOptions options ) {
        this.options = options;
    }


    /**
     * @return the associations
     */
    public List<DHCPAssociation> getAssociations () {
        return this.associations;
    }


    /**
     * @param associations
     *            the associations to set
     */
    public void setAssociations ( List<DHCPAssociation> associations ) {
        this.associations = associations;
    }


    /**
     * {@inheritDoc}
     *
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString () {
        return String.format(
            "%s[status=%s,opts=%s,assocs=%s]", //$NON-NLS-1$
            this.interfaceName,
            this.status,
            this.options,
            this.associations);
    }

}
