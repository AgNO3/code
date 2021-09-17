/**
 * © 2013 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 11.09.2013 by mbechler
 */
package eu.agno3.orchestrator.config.model.realm;


/**
 * @author mbechler
 * 
 */
public class ObjectFactory {

    /**
     * 
     */
    public ObjectFactory () {}


    /**
     * @return a default implementation
     */
    public GroupStructuralObject createGroupStructuralObject () {
        return new GroupStructuralObjectImpl();
    }


    /**
     * @return a default implementation
     */
    public InstanceStructuralObject createInstanceStructuralObject () {
        return new InstanceStructuralObjectImpl();
    }


    /**
     * @return a default implementation
     */
    public ServiceStructuralObjectImpl createServiceStructuralObject () {
        return new ServiceStructuralObjectImpl();
    }

}
