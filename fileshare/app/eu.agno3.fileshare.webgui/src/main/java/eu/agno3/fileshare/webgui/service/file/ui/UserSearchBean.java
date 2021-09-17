/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 29.05.2015 by mbechler
 */
package eu.agno3.fileshare.webgui.service.file.ui;


import java.io.Serializable;

import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;

import eu.agno3.fileshare.webgui.service.FileshareServiceProvider;


/**
 * @author mbechler
 *
 */
@Named ( "userSearchBean" )
@ViewScoped
public class UserSearchBean implements Serializable {

    /**
     * 
     */
    private static final long serialVersionUID = 4649273961506668107L;
    private String query;

    @Inject
    private FileshareServiceProvider fsp;


    /**
     * @return the query
     */
    public String getQuery () {
        return this.query;
    }


    /**
     * @param query
     *            the query to set
     */
    public void setQuery ( String query ) {
        this.query = query;
    }


    /**
     * 
     * @return whether searching is disabled
     */
    public boolean getSearchingDisabled () {
        return this.fsp.getConfigurationProvider().getSearchConfiguration().isSearchDisabled();
    }
}
