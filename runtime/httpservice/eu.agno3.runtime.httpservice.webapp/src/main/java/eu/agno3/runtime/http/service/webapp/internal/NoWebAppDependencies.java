/**
 * © 2017 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: Jun 29, 2017 by mbechler
 */
package eu.agno3.runtime.http.service.webapp.internal;


import org.osgi.service.component.annotations.Component;

import eu.agno3.runtime.http.service.webapp.WebAppDependencies;


/**
 * @author mbechler
 *
 */
@Component ( service = WebAppDependencies.class, property = "instanceId=none" )
public class NoWebAppDependencies implements WebAppDependencies {

}
