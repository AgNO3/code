/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 09.09.2015 by mbechler
 */
package eu.agno3.runtime.http.service.webapp;


import java.util.Map;


/**
 * @author mbechler
 *
 */
public interface WebAppConfiguration {

    /**
     * 
     */
    public static final String TARGET_BUNDLE_ATTR = "bundle"; //$NON-NLS-1$

    /**
     * 
     */
    public static final String CONTEXT_PATH_ATTR = "contextPath"; //$NON-NLS-1$


    /**
     * @return the bundle symbolic name to match
     */
    String getBundleSymbolicName ();


    /**
     * @return the configured properties
     */
    Map<String, String> getProperties ();


    /**
     * @return the connector id bound to
     */
    String getConnector ();


    /**
     * @return instance id for dependencies
     */
    String getDependencies ();

}