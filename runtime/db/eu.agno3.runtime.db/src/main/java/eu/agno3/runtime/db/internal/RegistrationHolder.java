/**
 * © 2016 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 05.02.2016 by mbechler
 */
package eu.agno3.runtime.db.internal;


import javax.sql.DataSource;

import org.osgi.framework.ServiceRegistration;


/**
 * @author mbechler
 *
 */
public class RegistrationHolder {

    private ServiceRegistration<DataSource> serviceRegistration;


    /**
     * @param registerService
     */
    public RegistrationHolder ( ServiceRegistration<DataSource> registerService ) {
        this.serviceRegistration = registerService;
    }


    /**
     * @return the registerService
     */
    public ServiceRegistration<DataSource> getServiceRegistration () {
        return this.serviceRegistration;
    }

}
