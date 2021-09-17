/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 21.01.2015 by mbechler
 */
package eu.agno3.fileshare.webgui.subject;


import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.inject.Named;

import org.apache.commons.lang3.StringUtils;

import eu.agno3.fileshare.model.Grant;
import eu.agno3.fileshare.model.GroupInfo;
import eu.agno3.fileshare.model.MailGrant;
import eu.agno3.fileshare.model.SubjectGrant;
import eu.agno3.fileshare.model.SubjectInfo;
import eu.agno3.fileshare.model.TokenGrant;
import eu.agno3.fileshare.model.UserDetails;
import eu.agno3.fileshare.model.UserInfo;
import eu.agno3.fileshare.model.VFSEntity;
import eu.agno3.fileshare.model.query.MailPeerInfo;
import eu.agno3.fileshare.model.query.PeerInfo;
import eu.agno3.fileshare.model.query.SubjectPeerInfo;
import eu.agno3.fileshare.model.query.TokenPeerInfo;
import eu.agno3.fileshare.util.GrantComparator;
import eu.agno3.fileshare.webgui.i18n.FileshareMessages;
import eu.agno3.fileshare.webgui.login.AuthInfoBean;


/**
 * @author mbechler
 *
 */
@ApplicationScoped
@Named ( "subjectDisplayBean" )
public class SubjectDisplayBean {

    @Inject
    private AuthInfoBean authInfo;


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
     * 
     * @param g
     * @return whether this is a subject grant
     */
    public boolean isSubjectGrant ( Grant g ) {
        return g instanceof SubjectGrant;
    }


    /**
     * 
     * @param s
     * @return subject display name, potentially with realm
     */
    public String getSubjectDisplayName ( SubjectInfo s ) {
        if ( this.authInfo.getMultiRealm() ) {
            return getSubjectDisplayNameWithRealm(s);
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
     * 
     * @param s
     * @return subject display name plus realm information
     */
    public static String getSubjectDisplayNameWithRealm ( SubjectInfo s ) {
        if ( s instanceof UserInfo ) {
            return String.format("%s (%s)", getUserDisplayName((UserInfo) s), s.getRealm()); //$NON-NLS-1$
        }
        else if ( s instanceof GroupInfo ) {
            if ( StringUtils.isBlank(s.getRealm()) ) {
                return getGroupDisplayName((GroupInfo) s);
            }
            return String.format("%s (%s)", getGroupDisplayName((GroupInfo) s), s.getRealm()); //$NON-NLS-1$
        }
        return null;
    }


    /**
     * 
     * @param o
     * @param len
     * @return n grants sorted
     */
    public List<Grant> getOrderedFirstEntityGrants ( Object o, int len ) {
        if ( ! ( o instanceof VFSEntity ) ) {
            return Collections.EMPTY_LIST;
        }
        VFSEntity e = (VFSEntity) o;
        return getOrderedFirstGrants(e.getLocalValidGrants(), len);
    }


    /**
     * @param grants
     * @param len
     * @return n grants sorted
     */
    public List<Grant> getOrderedFirstGrants ( Collection<Grant> grants, int len ) {
        if ( grants == null ) {
            return Collections.EMPTY_LIST;
        }
        List<Grant> sorted = new ArrayList<>(grants);
        Collections.sort(sorted, new GrantComparator());
        List<Grant> res = new ArrayList<>();
        for ( int i = 0; i < Math.min(len, grants.size()); i++ ) {
            res.add(sorted.get(i));
        }
        return res;
    }


    /**
     * 
     * @param g
     * @return a descriptive name for a grant
     */
    public String getGrantDisplayName ( Grant g ) {
        if ( g instanceof SubjectGrant ) {
            return getSubjectDisplayName( ( (SubjectGrant) g ).getTarget());
        }
        else if ( g instanceof MailGrant ) {
            return ( (MailGrant) g ).getMailAddress();
        }
        else if ( g instanceof TokenGrant ) {
            if ( !StringUtils.isBlank( ( (TokenGrant) g ).getIdentifier()) ) {
                return ( (TokenGrant) g ).getIdentifier();
            }
        }

        return FileshareMessages.get(FileshareMessages.ANONYMOUS);
    }


    /**
     * @param g
     * @return a grant type class
     */
    public static String getGrantClass ( Grant g ) {
        if ( g instanceof SubjectGrant ) {
            if ( ( (SubjectGrant) g ).getTarget() instanceof GroupInfo ) {
                return "group"; //$NON-NLS-1$
            }
            return "user"; //$NON-NLS-1$
        }
        else if ( g instanceof MailGrant ) {
            return "mail"; //$NON-NLS-1$
        }
        else if ( g instanceof TokenGrant ) {
            return "link"; //$NON-NLS-1$
        }
        return StringUtils.EMPTY;
    }


    /**
     * 
     * @param g
     * @return the grant type for displaying
     */
    public static String getGrantType ( Grant g ) {
        if ( g instanceof SubjectGrant ) {
            return getSubjectType( ( (SubjectGrant) g ).getTarget());
        }
        else if ( g instanceof MailGrant ) {
            return FileshareMessages.get("grant.type.mail"); //$NON-NLS-1$
        }
        else if ( g instanceof TokenGrant ) {
            return FileshareMessages.get("grant.type.link"); //$NON-NLS-1$
        }
        return StringUtils.EMPTY;
    }


    /**
     * 
     * @param g
     * @return abbreviated grant name
     */
    public String getAbbreviatedGrantName ( Grant g ) {

        if ( g instanceof SubjectGrant ) {
            String name = getSubjectDisplayNameOnly( ( (SubjectGrant) g ).getTarget());
            if ( ( (SubjectGrant) g ).getTarget() instanceof GroupInfo ) {
                return name.substring(0, Math.min(2, name.length()));
            }

            String[] split = StringUtils.split(name, ' ');
            StringBuilder sb = new StringBuilder();

            for ( String part : split ) {
                if ( !StringUtils.isEmpty(part) ) {
                    sb.append(part.charAt(0));
                }
            }

            return sb.toString();
        }
        else if ( g instanceof MailGrant ) {
            String addr = ( (MailGrant) g ).getMailAddress();
            int atPos = addr.indexOf('@');
            return addr.charAt(0) + "@" + addr.charAt(atPos + 1); //$NON-NLS-1$
        }
        else if ( g instanceof TokenGrant ) {
            return String.valueOf( ( (TokenGrant) g ).getIdentifier().charAt(0));
        }

        return StringUtils.EMPTY;
    }


    /**
     * @param subjectInfo
     * @return a subject type description
     */
    public static String getSubjectType ( SubjectInfo subjectInfo ) {

        if ( subjectInfo instanceof UserInfo ) {
            return FileshareMessages.get("subject.type.user"); //$NON-NLS-1$
        }
        else if ( subjectInfo instanceof GroupInfo ) {
            return FileshareMessages.get("subject.type.group"); //$NON-NLS-1$
        }

        return null;
    }


    /**
     * @param info
     * @return a subject type description
     */
    public static String getPeerType ( PeerInfo info ) {

        if ( info instanceof SubjectPeerInfo ) {
            return getSubjectType( ( (SubjectPeerInfo) info ).getSubject());
        }
        else if ( info instanceof MailPeerInfo ) {
            return FileshareMessages.get("peer.type.mail"); //$NON-NLS-1$
        }
        else if ( info instanceof TokenPeerInfo ) {
            return FileshareMessages.get("peer.type.link"); //$NON-NLS-1$
        }

        return null;
    }


    /**
     * 
     * @param g
     * @return a icon for the grant type
     */
    public static String getGrantIconClass ( Grant g ) {
        if ( g instanceof MailGrant ) {
            return "ui-icon-mail-closed"; //$NON-NLS-1$
        }

        return "ui-icon-link"; //$NON-NLS-1$

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
     * @param peer
     * @return the peer name
     */
    public String getPeerDisplayName ( PeerInfo peer ) {

        if ( peer instanceof SubjectPeerInfo ) {
            return getSubjectDisplayName( ( (SubjectPeerInfo) peer ).getSubject());
        }
        else if ( peer instanceof MailPeerInfo ) {
            return ( (MailPeerInfo) peer ).getMailAddress();
        }
        else if ( peer instanceof TokenPeerInfo ) {
            return "link"; //$NON-NLS-1$
        }

        return null;
    }


    /**
     * 
     * @param peer
     * @return the peer icon class
     */
    public static String getPeerIconClass ( PeerInfo peer ) {
        if ( peer instanceof SubjectPeerInfo ) {
            return getSubjectIconClass( ( (SubjectPeerInfo) peer ).getSubject());
        }
        else if ( peer instanceof MailPeerInfo ) {
            return "ui-icon-mail-closed"; //$NON-NLS-1$
        }
        else if ( peer instanceof TokenPeerInfo ) {
            return "ui-icon-link"; //$NON-NLS-1$
        }
        return null;
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
        return "ui-icon-group"; //$NON-NLS-1$
    }


    /**
     * @param s
     * @return group display name
     */
    public static String getGroupDisplayName ( GroupInfo s ) {
        return s.getName();
    }


    /**
     * @param s
     * @return user display name
     */
    public static String getUserDisplayName ( UserInfo s ) {
        return s.getUserDisplayName();
    }


    /**
     * 
     * @param s
     * @param fallbackName
     * @return the preferredName, fullName or fallbackName
     */
    public static String getNameFromDetails ( UserDetails s, String fallbackName ) {

        if ( !StringUtils.isBlank(s.getPreferredName()) && s.getPreferredNameVerified() ) {
            return s.getPreferredName();
        }

        if ( !StringUtils.isBlank(s.getFullName()) && s.getFullNameVerified() ) {
            return s.getFullName();
        }

        return fallbackName;
    }

}
