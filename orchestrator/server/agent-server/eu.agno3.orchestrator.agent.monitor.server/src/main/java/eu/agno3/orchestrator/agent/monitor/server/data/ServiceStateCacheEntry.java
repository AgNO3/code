/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 14.12.2015 by mbechler
 */
package eu.agno3.orchestrator.agent.monitor.server.data;


import java.util.UUID;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Id;
import javax.persistence.PersistenceUnit;
import javax.persistence.Table;
import javax.persistence.Version;

import org.hibernate.annotations.OptimisticLock;

import eu.agno3.orchestrator.system.monitor.ServiceRuntimeStatus;


/**
 * @author mbechler
 *
 */
@PersistenceUnit ( unitName = "orchestrator" )
@Entity
@Table ( name = "service_state_cache" )
public class ServiceStateCacheEntry {

    private UUID serviceId;
    private long version;

    private ServiceRuntimeStatus runtimeState;


    /**
     * 
     * @return the object id
     */
    @Id
    @Column ( length = 16 )
    public UUID getServiceId () {
        return this.serviceId;
    }


    /**
     * @param id
     *            the id to set
     */
    public void setServiceId ( UUID id ) {
        this.serviceId = id;
    }


    /**
     * 
     * @return the version
     */
    @Version
    public long getVersion () {
        return this.version;
    }


    /**
     * @param version
     *            the version to set
     */
    public void setVersion ( long version ) {
        this.version = version;
    }


    /**
     * @return the cachedState
     */
    @OptimisticLock ( excluded = true )
    @Enumerated ( EnumType.STRING )
    public ServiceRuntimeStatus getRuntimeState () {
        return this.runtimeState;
    }


    /**
     * @param runtimeState
     *            the runtimeState to set
     */
    public void setRuntimeState ( ServiceRuntimeStatus runtimeState ) {
        this.runtimeState = runtimeState;
    }

}
