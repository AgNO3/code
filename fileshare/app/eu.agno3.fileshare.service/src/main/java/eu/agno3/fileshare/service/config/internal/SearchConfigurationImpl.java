/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 29.05.2015 by mbechler
 */
package eu.agno3.fileshare.service.config.internal;


import java.util.Dictionary;

import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Modified;

import eu.agno3.fileshare.service.config.SearchConfiguration;
import eu.agno3.runtime.util.config.ConfigUtil;


/**
 * @author mbechler
 *
 */
@Component ( service = SearchConfiguration.class, configurationPid = "search" )
public class SearchConfigurationImpl implements SearchConfiguration {

    private boolean allowPaging;
    private int pageSize;
    private boolean searchDisabled;


    @Activate
    protected synchronized void activate ( ComponentContext ctx ) {
        parseConfig(ctx.getProperties());
    }


    @Modified
    protected synchronized void modified ( ComponentContext ctx ) {
        parseConfig(ctx.getProperties());
    }


    /**
     * @param cfg
     */
    private void parseConfig ( Dictionary<String, Object> cfg ) {
        this.searchDisabled = ConfigUtil.parseBoolean(cfg, "searchDisabled", false); //$NON-NLS-1$
        this.allowPaging = ConfigUtil.parseBoolean(cfg, "allowPaging", true); //$NON-NLS-1$
        this.pageSize = ConfigUtil.parseInt(cfg, "pageSize", 20); //$NON-NLS-1$
    }


    /**
     * 
     * {@inheritDoc}
     *
     * @see eu.agno3.fileshare.service.config.SearchConfiguration#isSearchDisabled()
     */
    @Override
    public boolean isSearchDisabled () {
        return this.searchDisabled;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.fileshare.service.config.SearchConfiguration#isAllowPaging()
     */
    @Override
    public boolean isAllowPaging () {
        return this.allowPaging;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.fileshare.service.config.SearchConfiguration#getPageSize()
     */
    @Override
    public int getPageSize () {
        return this.pageSize;
    }

}
