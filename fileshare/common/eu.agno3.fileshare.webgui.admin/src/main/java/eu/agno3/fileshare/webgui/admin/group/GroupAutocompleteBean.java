/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 21.01.2015 by mbechler
 */
package eu.agno3.fileshare.webgui.admin.group;


import java.util.Collections;
import java.util.List;

import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.inject.Named;

import org.primefaces.component.autocomplete.AutoComplete;

import eu.agno3.fileshare.exceptions.FileshareException;
import eu.agno3.fileshare.model.query.GroupQueryResult;
import eu.agno3.fileshare.webgui.admin.FileshareAdminExceptionHandler;
import eu.agno3.fileshare.webgui.admin.FileshareAdminServiceProvider;


/**
 * @author mbechler
 *
 */
@RequestScoped
@Named ( "app_fs_adm_groupAutocompleteBean" )
public class GroupAutocompleteBean {

    @Inject
    private FileshareAdminServiceProvider fsp;

    @Inject
    private FileshareAdminExceptionHandler exceptionHandler;

    private AutoComplete component;

    private GroupQueryResult value;


    /**
     * @param component
     *            the component to set
     */
    public void setComponent ( AutoComplete component ) {
        this.component = component;
    }


    /**
     * @return the component
     */
    public AutoComplete getComponent () {
        return this.component;
    }


    /**
     * @return the value
     */
    public GroupQueryResult getValue () {
        return this.value;
    }


    /**
     * @param value
     *            the value to set
     */
    public void setValue ( GroupQueryResult value ) {
        this.value = value;
    }


    /**
     * @param query
     * @return completion results
     */
    public List<GroupQueryResult> complete ( String query ) {
        try {
            return this.fsp.getGroupService().queryGroups(query, 20);
        }
        catch ( FileshareException e ) {
            this.exceptionHandler.handleException(e);
            return Collections.EMPTY_LIST;
        }
    }

}
