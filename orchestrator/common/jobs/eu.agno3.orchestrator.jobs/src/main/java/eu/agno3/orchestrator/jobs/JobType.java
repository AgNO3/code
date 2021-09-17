/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 27.05.2014 by mbechler
 */
package eu.agno3.orchestrator.jobs;


import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;


/**
 * @author mbechler
 * 
 */
@Retention ( RetentionPolicy.RUNTIME )
@Documented
public @interface JobType {

    /**
     * 
     * @return job type
     */
    Class<? extends Job> value();
}
