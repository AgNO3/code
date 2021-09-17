/**
 * © 2013 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 30.07.2013 by mbechler
 */
package eu.agno3.runtime.db.schema.liquibase.internal;


import java.sql.Connection;
import java.sql.SQLException;

import javax.sql.DataSource;

import liquibase.database.Database;
import liquibase.database.core.DerbyDatabase;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.jdbc.DataSourceFactory;

import eu.agno3.runtime.db.schema.liquibase.LiquibaseDatabaseFactory;


/**
 * @author mbechler
 * 
 */
@Component ( service = {
    LiquibaseDatabaseFactory.class
}, property = {
    DataSourceFactory.OSGI_JDBC_DRIVER_CLASS + "=org.apache.derby.jdbc.ClientDriver",
} )
public class LiquibaseNetworkedDerbyDatabaseFactory implements LiquibaseDatabaseFactory {

    /**
     * {@inheritDoc}
     * 
     * @see eu.agno3.runtime.db.schema.liquibase.LiquibaseDatabaseFactory#buildDatabase(javax.sql.DataSource)
     */
    @SuppressWarnings ( "resource" )
    @Override
    public Database buildDatabase ( DataSource ds ) throws SQLException {
        DerbyDatabase db = new LiquibaseDerbyDatabase();
        Connection conn = ds.getConnection();
        db.setConnection(new LiquibaseDerbyConnection(conn));
        db.setDefaultCatalogName("APP"); //$NON-NLS-1$
        return db;
    }

}
