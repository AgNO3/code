/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 21.01.2015 by mbechler
 */
package eu.agno3.fileshare.webgui.admin;


import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.inject.Named;

import org.apache.commons.lang3.StringUtils;

import eu.agno3.fileshare.model.GroupInfo;
import eu.agno3.fileshare.model.SubjectInfo;
import eu.agno3.fileshare.model.UserDetails;
import eu.agno3.fileshare.model.UserInfo;


/**
 * @author mbechler
 *
 */
@ApplicationScoped
@Named ( "app_fs_adm_subjectDisplayBean" )
public class SubjectDisplayBean {

    @Inject
    private FileshareAdminServiceProvider serviceProvider;


    /**
     * 
     * @param s
     * @return whether this is a user
     */
    public boolean isUser ( SubjectInfo s ) {
        return s instanceof UserInfo;
    }


    /**
     * 
     * @param s
     * @return whether this is a group
     */
    public boolean isGroup ( SubjectInfo s ) {
        return s instanceof GroupInfo;
    }


    /**
     * @return
     */
    private boolean isMultiRealm () {
        return this.serviceProvider.getConfigurationProvider().isMultiRealm();
    }


    /**
     * 
     * @param s
     * @return subject display name, including realm if necessary
     */
    public String getSubjectDisplayName ( SubjectInfo s ) {
        if ( s == null ) {
            return null;
        }
        if ( isMultiRealm() && !StringUtils.isBlank(s.getRealm()) ) {
            return String.format("%s (%s)", getSubjectDisplayNameOnly(s), s.getRealm()); //$NON-NLS-1$
        }

        return getSubjectDisplayNameOnly(s);
    }


    /**
     * 
     * @param s
     * @return subject display name
     */
    public static String getSubjectDisplayNameOnly ( SubjectInfo s ) {

        if ( s instanceof UserInfo ) {
            return getUserDisplayName((UserInfo) s);
        }
        else if ( s instanceof GroupInfo ) {
            return getGroupDisplayName((GroupInfo) s);
        }
        return null;
    }


    /**
     * @param subjectInfo
     * @return a subject type description
     */
    public static String getSubjectType ( SubjectInfo subjectInfo ) {

        if ( subjectInfo instanceof UserInfo ) {
            return FileshareAdminMessages.get("subject.type.user"); //$NON-NLS-1$
        }
        else if ( subjectInfo instanceof GroupInfo ) {
            return FileshareAdminMessages.get("subject.type.group"); //$NON-NLS-1$
        }

        return null;
    }


    /**
     * @param s
     * @return subject type icon class
     */
    public static String getSubjectIconClass ( SubjectInfo s ) {

        if ( s instanceof UserInfo ) {
            return getUserIconClass();
        }
        else if ( s instanceof GroupInfo ) {
            return getGroupIconClass();
        }

        return StringUtils.EMPTY;
    }


    /**
     * @return the user icon class
     */
    public static String getUserIconClass () {
        return "ui-icon-person"; //$NON-NLS-1$
    }


    /**
     * @return the group icon class
     */
    public static String getGroupIconClass () {
        return "ui-icon-star"; //$NON-NLS-1$
    }


    /**
     * @param s
     * @return group display name
     */
    public static String getGroupDisplayName ( GroupInfo s ) {
        if ( s == null ) {
            return null;
        }
        return s.getName();
    }


    /**
     * @param s
     * @return user display name
     */
    public static String getUserDisplayName ( UserInfo s ) {
        if ( s == null ) {
            return null;
        }
        return s.getUserDisplayName();
    }


    /**
     * 
     * @param s
     * @param fallbackName
     * @return the preferredName, fullName or fallbackName
     */
    public static String getNameFromDetails ( UserDetails s, String fallbackName ) {
        if ( s == null ) {
            return fallbackName;
        }

        if ( !StringUtils.isBlank(s.getPreferredName()) && s.getPreferredNameVerified() ) {
            return s.getPreferredName();
        }

        if ( !StringUtils.isBlank(s.getFullName()) && s.getFullNameVerified() ) {
            return s.getFullName();
        }

        return fallbackName;
    }

}
