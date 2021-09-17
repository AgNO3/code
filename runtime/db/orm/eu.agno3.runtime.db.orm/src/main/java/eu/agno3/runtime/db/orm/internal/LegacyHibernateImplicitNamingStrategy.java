/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 30.10.2015 by mbechler
 */
package eu.agno3.runtime.db.orm.internal;


import org.hibernate.boot.model.naming.Identifier;
import org.hibernate.boot.model.naming.ImplicitJoinColumnNameSource;
import org.hibernate.boot.model.naming.ImplicitNamingStrategyLegacyJpaImpl;


/**
 * @author mbechler
 *
 */
public class LegacyHibernateImplicitNamingStrategy extends ImplicitNamingStrategyLegacyJpaImpl {

    /**
     * 
     */
    private static final long serialVersionUID = -8469677536088008828L;


    @Override
    public Identifier determineJoinColumnName ( ImplicitJoinColumnNameSource source ) {
        // legacy JPA-based naming strategy preferred to use {TableName}_{ReferencedColumnName}
        // where JPA was later clarified to prefer {EntityName}_{ReferencedColumnName}.
        //
        // The spec-compliant one implements the clarified {EntityName}_{ReferencedColumnName}
        // naming. Here we implement the older {TableName}_{ReferencedColumnName} naming
        final String name;

        if ( source.getNature() == ImplicitJoinColumnNameSource.Nature.ELEMENT_COLLECTION || source.getAttributePath() == null ) {
            name = source.getReferencedTableName().getText() + '_' + source.getReferencedColumnName().getText();
        }
        else {
            name = transformAttributePath(source.getAttributePath()) + '_' + source.getReferencedColumnName().getText();
        }

        return toIdentifier(name, source.getBuildingContext());
    }
}
