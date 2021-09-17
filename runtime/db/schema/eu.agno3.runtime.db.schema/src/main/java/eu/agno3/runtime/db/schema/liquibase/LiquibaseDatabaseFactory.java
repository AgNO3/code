/**
 * © 2013 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 30.07.2013 by mbechler
 */
package eu.agno3.runtime.db.schema.liquibase;


import java.sql.SQLException;

import javax.sql.DataSource;

import liquibase.database.Database;


/**
 * @author mbechler
 * 
 */
public interface LiquibaseDatabaseFactory {

    /**
     * @param ds
     * @return a liquibase database instance
     * @throws SQLException
     */
    Database buildDatabase ( DataSource ds ) throws SQLException;

}