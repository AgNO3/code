/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 09.01.2014 by mbechler
 */
package eu.agno3.runtime.db.internal;


import org.osgi.service.cm.ConfigurationAdmin;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;

import eu.agno3.runtime.db.AdministrativeDataSource;


/**
 * @author mbechler
 * 
 */
@Component ( immediate = true )
public class AdministrativeDataSourceUtilWrapper extends AbstractDataSourceUtilWrapper<AdministrativeDataSource> {

    /**
     */
    public AdministrativeDataSourceUtilWrapper () {
        super(AdministrativeDataSource.class, AdministrativeDataSourceUtilProxy.ADMIN_PID);
    }


    /**
     * {@inheritDoc}
     * 
     * @see eu.agno3.runtime.db.internal.AbstractDataSourceUtilWrapper#activate(org.osgi.service.component.ComponentContext)
     */
    @Activate
    @Override
    protected void activate ( ComponentContext ctx ) {
        super.activate(ctx);
    }


    /**
     * {@inheritDoc}
     * 
     * @see eu.agno3.runtime.db.internal.AbstractDataSourceUtilWrapper#deactivate(org.osgi.service.component.ComponentContext)
     */
    @Deactivate
    @Override
    protected void deactivate ( ComponentContext ctx ) {
        super.deactivate(ctx);
    }


    /**
     * {@inheritDoc}
     * 
     * @see eu.agno3.runtime.db.internal.AbstractDataSourceUtilWrapper#setConfigAdmin(org.osgi.service.cm.ConfigurationAdmin)
     */
    @Reference
    @Override
    protected synchronized void setConfigAdmin ( ConfigurationAdmin cm ) {
        super.setConfigAdmin(cm);
    }


    /**
     * {@inheritDoc}
     * 
     * @see eu.agno3.runtime.db.internal.AbstractDataSourceUtilWrapper#unsetConfigAdmin(org.osgi.service.cm.ConfigurationAdmin)
     */
    @Override
    protected synchronized void unsetConfigAdmin ( ConfigurationAdmin cm ) {
        super.unsetConfigAdmin(cm);
    }

}
