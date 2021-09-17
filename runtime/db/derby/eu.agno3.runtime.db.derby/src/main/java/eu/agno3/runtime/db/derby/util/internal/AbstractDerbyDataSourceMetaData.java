/**
 * © 2013 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 12.08.2013 by mbechler
 */
package eu.agno3.runtime.db.derby.util.internal;


import eu.agno3.runtime.db.DataSourceMetaData;


/**
 * @author mbechler
 * 
 */
public abstract class AbstractDerbyDataSourceMetaData implements DataSourceMetaData {

    /**
     * 
     */
    private static final String DEFAULT_SCHEMA = "APP"; //$NON-NLS-1$


    /**
     */
    protected AbstractDerbyDataSourceMetaData () {}


    /**
     * {@inheritDoc}
     * 
     * @see eu.agno3.runtime.db.DataSourceMetaData#getDefaultCatalog()
     */
    @Override
    public String getDefaultCatalog () {
        // no catalog support in derby
        return null;
    }


    /**
     * {@inheritDoc}
     * 
     * @see eu.agno3.runtime.db.DataSourceMetaData#getDefaultSchema()
     */
    @Override
    public String getDefaultSchema () {
        return DEFAULT_SCHEMA;
    }


    /**
     * {@inheritDoc}
     * 
     * @see eu.agno3.runtime.db.DataSourceMetaData#getHibernateDialect()
     */
    @Override
    public String getHibernateDialect () {
        return "eu.agno3.runtime.db.orm.dialect.FixedDerbyDialect"; //$NON-NLS-1$
    }

}
