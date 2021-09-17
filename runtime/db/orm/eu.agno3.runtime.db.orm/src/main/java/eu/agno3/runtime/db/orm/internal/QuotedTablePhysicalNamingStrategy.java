/**
 * © 2013 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 06.08.2013 by mbechler
 */
package eu.agno3.runtime.db.orm.internal;


import org.hibernate.boot.model.naming.Identifier;
import org.hibernate.boot.model.naming.PhysicalNamingStrategy;
import org.hibernate.boot.model.naming.PhysicalNamingStrategyStandardImpl;
import org.hibernate.engine.jdbc.env.spi.JdbcEnvironment;


/**
 * @author mbechler
 * 
 */
public class QuotedTablePhysicalNamingStrategy extends PhysicalNamingStrategyStandardImpl implements PhysicalNamingStrategy {

    /**
     * 
     */
    private static final long serialVersionUID = 5266262255550377899L;


    /**
     * {@inheritDoc}
     *
     * @see org.hibernate.boot.model.naming.PhysicalNamingStrategyStandardImpl#toPhysicalTableName(org.hibernate.boot.model.naming.Identifier,
     *      org.hibernate.engine.jdbc.env.spi.JdbcEnvironment)
     */
    @Override
    public Identifier toPhysicalTableName ( Identifier name, JdbcEnvironment context ) {
        return new QuotedButCaseInsensitiveIdentifier(super.toPhysicalTableName(name, context).getText(), true);
    }


    /**
     * {@inheritDoc}
     *
     * @see org.hibernate.boot.model.naming.PhysicalNamingStrategyStandardImpl#toPhysicalColumnName(org.hibernate.boot.model.naming.Identifier,
     *      org.hibernate.engine.jdbc.env.spi.JdbcEnvironment)
     */
    @Override
    public Identifier toPhysicalColumnName ( Identifier name, JdbcEnvironment context ) {
        return Identifier.toIdentifier(super.toPhysicalColumnName(name, context).getCanonicalName().toUpperCase());
    }

    /**
     * Temporary workaround for HHH-10820
     * 
     * 
     * @author mbechler
     *
     */
    private class QuotedButCaseInsensitiveIdentifier extends Identifier {

        /**
         * @param text
         * @param quoted
         */
        public QuotedButCaseInsensitiveIdentifier ( String text, boolean quoted ) {
            super(text, quoted);
        }


        /**
         * {@inheritDoc}
         *
         * @see org.hibernate.boot.model.naming.Identifier#equals(java.lang.Object)
         */
        @Override
        public boolean equals ( Object o ) {
            if ( o instanceof Identifier ) {
                return getCanonicalName().equalsIgnoreCase( ( (Identifier) o ).getCanonicalName());
            }
            return super.equals(o);
        }
    }
}
