/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 30.10.2015 by mbechler
 */
package eu.agno3.runtime.db.orm.dialect;


import org.hibernate.dialect.DerbyTenSevenDialect;
import org.hibernate.hql.spi.id.IdTableSupportStandardImpl;
import org.hibernate.hql.spi.id.MultiTableBulkIdStrategy;
import org.hibernate.hql.spi.id.local.AfterUseAction;
import org.hibernate.hql.spi.id.local.LocalTemporaryTableBulkIdStrategy;


/**
 * @author mbechler
 *
 */
public class FixedDerbyDialect extends DerbyTenSevenDialect {

    /**
     * 
     */
    public FixedDerbyDialect () {
        super();
    }


    /**
     * {@inheritDoc}
     *
     * @see org.hibernate.dialect.DB2Dialect#getDefaultMultiTableBulkIdStrategy()
     */
    @Override
    public MultiTableBulkIdStrategy getDefaultMultiTableBulkIdStrategy () {
        return new LocalTemporaryTableBulkIdStrategy(new IdTableSupportStandardImpl() {

            @Override
            public String generateIdTableName ( String baseName ) {
                return "session." + super.generateIdTableName(baseName); //$NON-NLS-1$
            }


            @Override
            public String getCreateIdTableCommand () {
                return "declare global temporary table"; //$NON-NLS-1$
            }


            @Override
            public String getCreateIdTableStatementOptions () {
                return "not logged"; //$NON-NLS-1$
            }
        }, AfterUseAction.DROP, null);
    }

}
