/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 21.01.2015 by mbechler
 */
package eu.agno3.fileshare.webgui.roles;


import java.util.ArrayList;
import java.util.List;

import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.inject.Named;

import org.apache.commons.lang3.StringUtils;
import org.primefaces.component.autocomplete.AutoComplete;


/**
 * @author mbechler
 *
 */
@RequestScoped
@Named ( "app_fs_adm_roleAutocompleteBean" )
public class RoleAutoCompleteBean {

    @Inject
    private RolesBean roles;

    private AutoComplete component;

    private String value;


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
    public String getValue () {
        return this.value;
    }


    /**
     * @param value
     *            the value to set
     */
    public void setValue ( String value ) {
        this.value = value;
    }


    /**
     * @param query
     * @return completion results
     */
    public List<String> complete ( String query ) {

        if ( StringUtils.isBlank(query) ) {
            return this.roles.getAvailableRoles();
        }

        List<String> res = new ArrayList<>();

        for ( String role : this.roles.getAvailableRoles() ) {
            if ( role.contains(query) ) {
                res.add(role);
            }
        }

        return res;
    }
}
