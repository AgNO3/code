/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 16.06.2014 by mbechler
 */
package eu.agno3.orchestrator.config.model.realm;


import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;


/**
 * @author mbechler
 * 
 */
public interface GroupStructuralObject extends StructuralObject {

    /**
     * {@inheritDoc}
     * 
     * @see eu.agno3.orchestrator.config.model.realm.StructuralObject#getDisplayName()
     */
    @Override
    @NotNull
    @Size ( min = 1, max = 80 )
    String getDisplayName ();
}