/**
 * © 2017 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: Sep 20, 2017 by mbechler
 */
package eu.agno3.runtime.db.postgres.internal;


import javax.sql.DataSource;

import eu.agno3.runtime.db.DataSourceMetaData;


/**
 * @author mbechler
 *
 */
public class PostgresDataSourceMetaData implements DataSourceMetaData {

    private String defaultSchema;


    /**
     * @param ds
     * @param defaultSchema
     */
    public PostgresDataSourceMetaData ( DataSource ds, String defaultSchema ) {
        this.defaultSchema = defaultSchema;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.db.DataSourceMetaData#getDefaultCatalog()
     */
    @Override
    public String getDefaultCatalog () {
        return null;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.db.DataSourceMetaData#getDefaultSchema()
     */
    @Override
    public String getDefaultSchema () {
        return this.defaultSchema;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.db.DataSourceMetaData#getHibernateDialect()
     */
    @Override
    public String getHibernateDialect () {
        return "org.hibernate.dialect.PostgreSQL95Dialect"; //$NON-NLS-1$
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.db.DataSourceMetaData#getDriverClassName()
     */
    @Override
    public String getDriverClassName () {
        return "org.postgresql.Driver"; //$NON-NLS-1$
    }

}
