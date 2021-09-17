/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 29.05.2014 by mbechler
 */
package eu.agno3.orchestrator.system.base;


import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;


/**
 * @author mbechler
 * 
 */
@Retention ( RetentionPolicy.RUNTIME )
@Documented
public @interface SystemServiceType {

    /**
     * @return the system service type
     */
    Class<? extends SystemService> value();
}
