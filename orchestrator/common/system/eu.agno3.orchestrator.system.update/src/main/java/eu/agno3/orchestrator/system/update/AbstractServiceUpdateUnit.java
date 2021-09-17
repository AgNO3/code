/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 04.11.2015 by mbechler
 */
package eu.agno3.orchestrator.system.update;


import java.io.Serializable;


/**
 * @author mbechler
 * @param <T>
 *
 */
public abstract class AbstractServiceUpdateUnit <T extends AbstractServiceUpdateUnit<T>> implements Serializable {

    /**
     * 
     */
    private static final long serialVersionUID = 4886685402663226194L;


    /**
     * 
     * @return the unit type
     */
    public abstract Class<T> getType ();


    /**
     * 
     * @param next
     * @return a merged instance, next argument supersedes local entries
     */
    public abstract T merge ( T next );
}
