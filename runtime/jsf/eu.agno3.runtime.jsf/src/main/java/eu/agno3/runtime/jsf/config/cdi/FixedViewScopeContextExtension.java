/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 09.06.2015 by mbechler
 */
package eu.agno3.runtime.jsf.config.cdi;


import org.apache.myfaces.cdi.view.ViewScopeContextExtension;


/**
 * @author mbechler
 *
 */
public class FixedViewScopeContextExtension extends ViewScopeContextExtension {

    /**
     * {@inheritDoc}
     *
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString () {
        return "JSF_VIEW_SCOPED"; //$NON-NLS-1$
    }
}
