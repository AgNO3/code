/**
 * © 2013 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 17.07.2013 by mbechler
 */
package eu.agno3.runtime.http.service.servlet;


import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;


/**
 * @author mbechler
 * 
 */
@Retention ( RetentionPolicy.RUNTIME )
public @interface ServletConfig {

    /**
     * Bind servlet to context
     * 
     */
    String CONTEXT_ATTR = "context"; //$NON-NLS-1$
}
