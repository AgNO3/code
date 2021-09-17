/**
 * © 2013 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 18.07.2013 by mbechler
 */
package eu.agno3.runtime.http.service.resource;


import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;


/**
 * @author mbechler
 * @see AnnotationResource
 */
@Retention ( RetentionPolicy.RUNTIME )
public @interface ResourceConfig {

    /**
     * 
     * @return path pattern for this registration
     */
    String paths() default "/";


    /**
     * 
     * @return contexts under which to register
     */
    String[] contexts() default {};


    /**
     * 
     * @return resource directory to use
     */
    String resourceBase() default "/www-static/";


    /**
     * 
     * @return priority of registration
     */
    int priority() default 0;

}
