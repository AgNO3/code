/**
 * © 2016 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: Nov 6, 2016 by mbechler
 */
package eu.agno3.runtime.elasticsearch;


/**
 * @author mbechler
 *
 */
public interface Mapping {

    /**
     * Mapping this applies to all undefined
     */
    String DEFAULT = "_default_"; //$NON-NLS-1$


    /**
     * @return mapping source
     */
    String toSource ();


    /**
     * @return target type
     */
    String getTargetType ();

}
