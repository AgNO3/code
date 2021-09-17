/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 04.11.2015 by mbechler
 */
package eu.agno3.orchestrator.system.update;


import java.io.Serializable;
import java.util.HashSet;
import java.util.List;
import java.util.Set;


/**
 * @author mbechler
 *
 */
public class ServiceUpdateDescriptor implements Serializable {

    /**
     * 
     */
    private static final long serialVersionUID = -6107574150267071922L;

    private String serviceType;
    private List<AbstractServiceUpdateUnit<?>> units;
    private Set<String> reconfigureServices = new HashSet<>();


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


    /**
     * @return the units
     */
    public List<AbstractServiceUpdateUnit<?>> getUnits () {
        return this.units;
    }


    /**
     * @param units
     *            the units to set
     */
    public void setUnits ( List<AbstractServiceUpdateUnit<?>> units ) {
        this.units = units;
    }


    /**
     * @return the reconfigureServices
     */
    public Set<String> getReconfigureServices () {
        return this.reconfigureServices;
    }


    /**
     * @param reconfigureServices
     *            the reconfigureServices to set
     */
    public void setReconfigureServices ( Set<String> reconfigureServices ) {
        this.reconfigureServices = reconfigureServices;
    }
}
