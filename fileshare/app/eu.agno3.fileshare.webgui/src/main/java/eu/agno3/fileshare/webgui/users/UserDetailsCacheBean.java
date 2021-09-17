/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 03.02.2015 by mbechler
 */
package eu.agno3.fileshare.webgui.users;


import java.io.Serializable;
import java.util.Map;
import java.util.UUID;

import javax.enterprise.context.SessionScoped;
import javax.inject.Inject;
import javax.inject.Named;

import org.apache.commons.collections4.map.LRUMap;
import org.apache.commons.lang3.StringUtils;

import eu.agno3.fileshare.exceptions.FileshareException;
import eu.agno3.fileshare.model.UserDetails;
import eu.agno3.fileshare.webgui.exceptions.ExceptionHandler;
import eu.agno3.fileshare.webgui.service.FileshareServiceProvider;


/**
 * @author mbechler
 *
 */
@SessionScoped
@Named ( "userDetailsCacheBean" )
public class UserDetailsCacheBean implements Serializable {

    /**
     * 
     */
    private static final long serialVersionUID = -1350056111481207828L;

    private static final int CACHE_SIZE = 10;

    private Map<UUID, UserDetails> userDetailsCache = new LRUMap<>(CACHE_SIZE);

    @Inject
    private FileshareServiceProvider fsp;


    /**
     * @param userId
     * @return the user details for the user
     */
    public UserDetails getUserDetails ( UUID userId ) {

        if ( userId == null ) {
            return null;
        }

        UserDetails cached = this.userDetailsCache.get(userId);

        if ( cached != null ) {
            return cached;
        }

        try {
            cached = this.fsp.getUserService().getUserDetails(userId);
        }
        catch ( FileshareException e ) {
            ExceptionHandler.handleException(e);
            cached = new UserDetails();
        }

        this.userDetailsCache.put(userId, cached);

        return cached;
    }


    /**
     * 
     * @param userId
     * @return whether verified information is available for the user
     */
    public boolean haveVerifiedUserDetails ( UUID userId ) {
        UserDetails details = getUserDetails(userId);

        if ( details == null ) {
            return false;
        }

        if ( !StringUtils.isBlank(details.getPreferredName()) && details.getPreferredNameVerified() ) {
            return true;
        }

        if ( !StringUtils.isBlank(details.getMailAddress()) && details.getMailAddressVerified() ) {
            return true;
        }

        if ( !StringUtils.isBlank(details.getJobTitle()) || !StringUtils.isBlank(details.getOrganization())
                || !StringUtils.isBlank(details.getOrganizationUnit()) ) {
            return true;
        }

        return false;
    }


    /**
     * 
     * @param userId
     * @return whether verified information is available for the user
     */
    public boolean haveUnverifiedUserDetails ( UUID userId ) {
        UserDetails details = getUserDetails(userId);

        if ( details == null ) {
            return false;
        }

        if ( !StringUtils.isBlank(details.getPreferredName()) && !details.getPreferredNameVerified() ) {
            return true;
        }

        if ( !StringUtils.isBlank(details.getMailAddress()) && !details.getMailAddressVerified() ) {
            return true;
        }

        return false;
    }
}
