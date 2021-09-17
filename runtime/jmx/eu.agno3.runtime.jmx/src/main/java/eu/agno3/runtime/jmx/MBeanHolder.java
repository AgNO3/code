/**
 * © 2017 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: Mar 2, 2017 by mbechler
 */
package eu.agno3.runtime.jmx;


/**
 * @author mbechler
 *
 */
public interface MBeanHolder {

    /**
     * @return mbean object name
     */
    String getObjectName ();


    /**
     * @return mbean instance
     */
    Object getMBean ();

}
