/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 18.01.2014 by mbechler
 */
package eu.agno3.runtime.logging;


/**
 * @author mbechler
 * 
 */
public interface DynamicVerbosityLayout {

    /**
     * @return the verbosity
     */
    TracingVerbosity getVerbosity ();


    /**
     * @param verbosity
     *            the verbosity to set
     */
    void setVerbosity ( TracingVerbosity verbosity );

}