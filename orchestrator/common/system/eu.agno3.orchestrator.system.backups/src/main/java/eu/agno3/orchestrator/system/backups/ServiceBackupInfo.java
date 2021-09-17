/**
 * © 2016 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 08.03.2016 by mbechler
 */
package eu.agno3.orchestrator.system.backups;


import java.io.Serializable;
import java.util.UUID;


/**
 * @author mbechler
 *
 */
public class ServiceBackupInfo implements Serializable {

    /**
     * 
     */
    private static final long serialVersionUID = 4270590455580516259L;
    private UUID serviceId;
    private Long size;
    private String serviceType;


    /**
     * @return the serviceId
     */
    public UUID getServiceId () {
        return this.serviceId;
    }


    /**
     * @param serviceId
     *            the serviceId to set
     */
    public void setServiceId ( UUID serviceId ) {
        this.serviceId = serviceId;
    }


    /**
     * @return the size
     */
    public Long getSize () {
        return this.size;
    }


    /**
     * @param size
     *            the size to set
     */
    public void setSize ( Long size ) {
        this.size = size;
    }


    /**
     * @return the serviceType
     */
    public String getServiceType () {
        return this.serviceType;
    }


    /**
     * @param serviceType
     *            the serviceType to set
     */
    public void setServiceType ( String serviceType ) {
        this.serviceType = serviceType;
    }
}
