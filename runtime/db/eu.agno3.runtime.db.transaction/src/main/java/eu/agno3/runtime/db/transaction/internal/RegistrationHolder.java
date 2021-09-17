/**
 * © 2016 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 05.02.2016 by mbechler
 */
package eu.agno3.runtime.db.transaction.internal;


import javax.sql.DataSource;

import org.osgi.framework.ServiceRegistration;

import com.atomikos.datasource.xa.XATransactionalResource;


/**
 * @author mbechler
 *
 */
public class RegistrationHolder {

    private XATransactionalResource jdbcTransactionalResource;
    private ServiceRegistration<DataSource> serviceRegistration;


    /**
     * @param jdbcTransactionalResource
     * @param registerService
     */
    public RegistrationHolder ( XATransactionalResource jdbcTransactionalResource, ServiceRegistration<DataSource> registerService ) {
        this.jdbcTransactionalResource = jdbcTransactionalResource;
        this.serviceRegistration = registerService;
    }


    /**
     * @return the jdbcTransactionalResource
     */
    public XATransactionalResource getJdbcTransactionalResource () {
        return this.jdbcTransactionalResource;
    }


    /**
     * @return the registerService
     */
    public ServiceRegistration<DataSource> getServiceRegistration () {
        return this.serviceRegistration;
    }

}
