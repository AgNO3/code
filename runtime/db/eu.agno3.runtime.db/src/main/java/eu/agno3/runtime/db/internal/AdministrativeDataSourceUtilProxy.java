/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 09.01.2014 by mbechler
 */
package eu.agno3.runtime.db.internal;


import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.ConfigurationPolicy;
import org.osgi.service.component.annotations.Reference;

import eu.agno3.runtime.db.AdministrativeDataSource;
import eu.agno3.runtime.db.AdministrativeDataSourceUtil;
import eu.agno3.runtime.db.DatabaseDriverUtil;


/**
 * @author mbechler
 * 
 */
@Component (
    service = AdministrativeDataSourceUtil.class,
    configurationPid = AdministrativeDataSourceUtilProxy.ADMIN_PID,
    configurationPolicy = ConfigurationPolicy.REQUIRE )
public class AdministrativeDataSourceUtilProxy extends DataSourceUtilProxy implements AdministrativeDataSourceUtil {

    /**
     * 
     */
    public static final String ADMIN_PID = "eu.agno3.runtime.db.AdministrativeDataSourceUtilFactory"; //$NON-NLS-1$


    @Reference
    protected synchronized void setDataSource ( AdministrativeDataSource ds ) {
        super.setDataSource(ds);
    }


    protected synchronized void unsetDataSource ( AdministrativeDataSource ds ) {
        super.unsetDataSource(ds);
    }


    /**
     * {@inheritDoc}
     * 
     * @see eu.agno3.runtime.db.internal.DataSourceUtilProxy#setDatabaseDriverUtil(eu.agno3.runtime.db.DatabaseDriverUtil)
     */
    @Reference
    @Override
    protected synchronized void setDatabaseDriverUtil ( DatabaseDriverUtil util ) {
        super.setDatabaseDriverUtil(util);
    }


    /**
     * {@inheritDoc}
     * 
     * @see eu.agno3.runtime.db.internal.DataSourceUtilProxy#unsetDatabaseDriverUtil(eu.agno3.runtime.db.DatabaseDriverUtil)
     */
    @Override
    protected synchronized void unsetDatabaseDriverUtil ( DatabaseDriverUtil util ) {
        super.unsetDatabaseDriverUtil(util);
    }

}
