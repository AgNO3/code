/**
 * © 2013 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 16.09.2013 by mbechler
 */
package eu.agno3.runtime.xml.binding;


/**
 * @author mbechler
 * 
 */
public interface UnmarshallingSessionProvider {

    /**
     * 
     * 
     * @return the unmarshalling session to use for this component
     */
    UnmarshallingSession getUnmarshallingSession ();
}
