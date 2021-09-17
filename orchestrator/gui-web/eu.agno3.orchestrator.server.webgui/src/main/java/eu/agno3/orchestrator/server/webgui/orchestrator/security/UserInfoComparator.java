/**
 * © 2016 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 20.02.2016 by mbechler
 */
package eu.agno3.orchestrator.server.webgui.orchestrator.security;


import java.text.Collator;
import java.util.Comparator;
import java.util.Objects;

import javax.faces.context.FacesContext;

import eu.agno3.runtime.security.principal.UserInfo;


/**
 * @author mbechler
 *
 */
public class UserInfoComparator implements Comparator<UserInfo> {

    private Collator collator;


    /**
     * 
     */
    public UserInfoComparator () {
        this.collator = Collator.getInstance(FacesContext.getCurrentInstance().getViewRoot().getLocale());
    }


    /**
     * {@inheritDoc}
     *
     * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
     */
    @Override
    public int compare ( UserInfo o1, UserInfo o2 ) {
        int res = Objects.compare(o1.getUserPrincipal().getUserName(), o2.getUserPrincipal().getUserName(), this.collator);
        if ( res != 0 ) {
            return res;
        }
        return Objects.compare(o1.getUserPrincipal().getRealmName(), o2.getUserPrincipal().getRealmName(), this.collator);
    }

}
