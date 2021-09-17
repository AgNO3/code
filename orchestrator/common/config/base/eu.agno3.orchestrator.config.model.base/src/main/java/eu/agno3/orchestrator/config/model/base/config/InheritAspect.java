/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 18.04.2014 by mbechler
 */
package eu.agno3.orchestrator.config.model.base.config;


import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;


/**
 * Marker that a delegate proxy shall be created for the method return value
 * 
 * @author mbechler
 * 
 */
@Retention ( RetentionPolicy.RUNTIME )
@Documented
public @interface InheritAspect {

}
