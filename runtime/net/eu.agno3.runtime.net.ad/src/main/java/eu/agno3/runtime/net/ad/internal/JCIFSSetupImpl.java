/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 01.04.2015 by mbechler
 */
package eu.agno3.runtime.net.ad.internal;


import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Modified;


/**
 * @author mbechler
 *
 */
@Component ( service = CIFSSetup.class )
public class JCIFSSetupImpl implements CIFSSetup {

    @Activate
    protected synchronized void activate ( ComponentContext ctx ) {}


    @Modified
    protected synchronized void modified ( ComponentContext ctx ) {}


    @Deactivate
    protected synchronized void deactivate ( ComponentContext ctx ) {

    }
}
