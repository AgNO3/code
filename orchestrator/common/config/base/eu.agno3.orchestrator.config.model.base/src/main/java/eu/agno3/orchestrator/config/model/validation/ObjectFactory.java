/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 01.12.2014 by mbechler
 */
package eu.agno3.orchestrator.config.model.validation;



/**
 * @author mbechler
 *
 */
public class ObjectFactory {

    /**
     * @return a default implementation
     */
    public ViolationEntry createViolationEntry () {
        return new ViolationEntryImpl();
    }

}
