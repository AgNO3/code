/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 18.01.2014 by mbechler
 */
package eu.agno3.runtime.db.derby.server;


import java.util.ArrayList;
import java.util.List;

import javax.sql.CommonDataSource;

import org.osgi.framework.ServiceRegistration;

import eu.agno3.runtime.db.AdministrativeDataSource;


class DataSourceRegistrations {

    private ServiceRegistration<AdministrativeDataSource> adminDataSource = null;
    private List<ServiceRegistration<? extends CommonDataSource>> dataSources = new ArrayList<>();


    /**
     * 
     */
    public DataSourceRegistrations () {}


    /**
     * @return the dataSources
     */
    public List<ServiceRegistration<? extends CommonDataSource>> getDataSources () {
        return this.dataSources;
    }


    /**
     * @param dataSource
     *            the dataSources to add
     */
    public void addDataSource ( ServiceRegistration<? extends CommonDataSource> dataSource ) {
        this.dataSources.add(dataSource);
    }


    /**
     * @return the adminDataSource
     */
    public ServiceRegistration<AdministrativeDataSource> getAdminDataSource () {
        return this.adminDataSource;
    }


    /**
     * @param adminDataSource
     *            the adminDataSource to set
     */
    public void setAdminDataSource ( ServiceRegistration<AdministrativeDataSource> adminDataSource ) {
        this.adminDataSource = adminDataSource;
    }

}
