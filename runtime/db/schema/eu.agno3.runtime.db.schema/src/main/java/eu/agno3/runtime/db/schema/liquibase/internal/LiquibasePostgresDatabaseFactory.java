/**
 * © 2017 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: Sep 20, 2017 by mbechler
 */
package eu.agno3.runtime.db.schema.liquibase.internal;


import java.sql.Connection;
import java.sql.SQLException;

import javax.sql.DataSource;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.jdbc.DataSourceFactory;

import eu.agno3.runtime.db.DatabaseDriverUtil;
import eu.agno3.runtime.db.schema.liquibase.LiquibaseDatabaseFactory;

import liquibase.database.Database;
import liquibase.database.core.PostgresDatabase;
import liquibase.database.jvm.JdbcConnection;


/**
 * @author mbechler
 *
 */
@Component ( service = {
    LiquibaseDatabaseFactory.class
}, property = {
    DataSourceFactory.OSGI_JDBC_DRIVER_CLASS + "=org.postgresql.Driver",
} )
public class LiquibasePostgresDatabaseFactory implements LiquibaseDatabaseFactory {

    private DatabaseDriverUtil dbUtil;


    @Reference ( target = "(" + DataSourceFactory.OSGI_JDBC_DRIVER_CLASS + "=org.postgresql.Driver)" )
    protected synchronized void setDriverUtil ( DatabaseDriverUtil dbu ) {
        this.dbUtil = dbu;
    }


    protected synchronized void unsetDriverUtil ( DatabaseDriverUtil dbu ) {
        if ( this.dbUtil == dbu ) {
            this.dbUtil = null;
        }
    }


    /**
     * {@inheritDoc}
     * 
     * @see eu.agno3.runtime.db.schema.liquibase.LiquibaseDatabaseFactory#buildDatabase(javax.sql.DataSource)
     */
    @SuppressWarnings ( "resource" )
    @Override
    public Database buildDatabase ( DataSource ds ) throws SQLException {
        PostgresDatabase db = new PostgresDatabase();
        Connection connection = ds.getConnection();
        db.setConnection(new JdbcConnection(connection));
        db.setDefaultSchemaName(this.dbUtil.getDefaultSchema(ds));
        return db;
    }

}
