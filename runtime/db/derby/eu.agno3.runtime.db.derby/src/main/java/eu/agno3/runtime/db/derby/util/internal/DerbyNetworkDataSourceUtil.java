/**
 * © 2013 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 12.08.2013 by mbechler
 */
package eu.agno3.runtime.db.derby.util.internal;


import javax.sql.DataSource;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.jdbc.DataSourceFactory;

import eu.agno3.runtime.db.DataSourceMetaData;
import eu.agno3.runtime.db.DatabaseDriverUtil;


/**
 * @author mbechler
 * 
 */
@Component ( service = {
    DatabaseDriverUtil.class
}, property = {
    DataSourceFactory.OSGI_JDBC_DRIVER_CLASS + "=org.apache.derby.jdbc.ClientDriver"
} )
public class DerbyNetworkDataSourceUtil extends AbstractDerbyDataSourceUtil {

    /**
     * {@inheritDoc}
     * 
     * @see eu.agno3.runtime.db.DatabaseDriverUtil#createMetaDataFor(javax.sql.DataSource)
     */
    @Override
    public DataSourceMetaData createMetaDataFor ( DataSource ds ) {
        return new DerbyNetworkDataSourceMetaData();
    }
}
