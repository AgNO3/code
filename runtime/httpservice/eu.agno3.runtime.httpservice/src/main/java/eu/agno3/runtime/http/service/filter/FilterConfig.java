/**
 * © 2013 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 17.07.2013 by mbechler
 */
package eu.agno3.runtime.http.service.filter;


import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;


/**
 * @author mbechler
 * 
 */
@Retention ( RetentionPolicy.RUNTIME )
public @interface FilterConfig {

    /**
     * Bind servlet to context
     * 
     */
    public static final String CONTEXT_ATTR = "context"; //$NON-NLS-1$


    /**
     * Priority in filter chain, the higher the later
     */
    int priority() default 0;
}
