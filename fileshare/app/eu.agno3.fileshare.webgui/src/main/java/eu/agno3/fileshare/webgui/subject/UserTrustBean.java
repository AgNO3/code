/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 19.05.2015 by mbechler
 */
package eu.agno3.fileshare.webgui.subject;


import java.awt.Color;

import javax.enterprise.context.ApplicationScoped;
import javax.faces.context.FacesContext;
import javax.inject.Inject;
import javax.inject.Named;

import org.apache.commons.lang3.StringUtils;
import org.apache.webbeans.util.StringUtil;

import eu.agno3.fileshare.model.Grant;
import eu.agno3.fileshare.model.MailGrant;
import eu.agno3.fileshare.model.Subject;
import eu.agno3.fileshare.model.SubjectGrant;
import eu.agno3.fileshare.model.SubjectInfo;
import eu.agno3.fileshare.model.TokenGrant;
import eu.agno3.fileshare.model.TrustLevel;
import eu.agno3.fileshare.model.query.MailPeerInfo;
import eu.agno3.fileshare.model.query.PeerInfo;
import eu.agno3.fileshare.model.query.SubjectPeerInfo;
import eu.agno3.fileshare.model.query.SubjectQueryResult;
import eu.agno3.fileshare.model.query.TokenPeerInfo;
import eu.agno3.fileshare.webgui.i18n.FileshareMessages;
import eu.agno3.fileshare.webgui.service.FileshareServiceProvider;


/**
 * @author mbechler
 *
 */
@ApplicationScoped
@Named ( "userTrustBean" )
public class UserTrustBean {

    @Inject
    private FileshareServiceProvider fsp;


    /**
     * 
     * @param s
     * @return the display trust level title
     */
    public String getDisplayTrustLevelTitle ( SubjectInfo s ) {

        TrustLevel l = getSubjectTrustLevel(s);
        if ( l == null ) {
            return FileshareMessages.get("trustLevel.unclassifiedTitle"); //$NON-NLS-1$
        }
        return l.getTitle(FacesContext.getCurrentInstance().getViewRoot().getLocale());
    }


    /**
     * 
     * @param mailAddress
     * @return the display trust level title
     */
    public String getDisplayMailTrustLevelTitle ( String mailAddress ) {
        TrustLevel l = getMailTrustLevel(mailAddress);
        if ( l == null ) {
            return FileshareMessages.get("trustLevel.unclassifiedTitle"); //$NON-NLS-1$
        }
        return l.getTitle(FacesContext.getCurrentInstance().getViewRoot().getLocale());
    }


    /**
     * 
     * @return the display trust level title
     */
    public String getDisplayLinkTrustLevelTitle () {
        TrustLevel l = getLinkTrustLevel();
        if ( l == null ) {
            return FileshareMessages.get("trustLevel.unclassifiedTitle"); //$NON-NLS-1$
        }
        return l.getTitle(FacesContext.getCurrentInstance().getViewRoot().getLocale());
    }


    /**
     * 
     * @param g
     * @return the display trust level title
     */
    public String getDisplayGrantTrustLevelTitle ( Grant g ) {
        if ( g instanceof SubjectGrant ) {
            return getDisplayTrustLevelTitle( ( (SubjectGrant) g ).getTarget());
        }
        else if ( g instanceof MailGrant ) {
            return getDisplayMailTrustLevelTitle( ( (MailGrant) g ).getMailAddress());
        }
        else if ( g instanceof TokenGrant ) {
            return getDisplayLinkTrustLevelTitle();
        }
        return null;
    }


    /**
     * 
     * @param g
     * @return the display trust level color
     */
    public String getDisplayGrantTrustLevelColor ( Grant g ) {
        if ( g instanceof SubjectGrant ) {
            return getDisplayTrustLevelColor( ( (SubjectGrant) g ).getTarget());
        }
        else if ( g instanceof MailGrant ) {
            return getDisplayMailTrustLevelColor( ( (MailGrant) g ).getMailAddress());
        }
        else if ( g instanceof TokenGrant ) {
            return getDisplayLinkTrustLevelColor();
        }
        return null;
    }


    /**
     * 
     * @param g
     * @return style for displaying grants
     */
    public String getDisplayGrantTrustLevelStyle ( Grant g ) {
        String color = getDisplayGrantTrustLevelColor(g);
        if ( StringUtil.isBlank(color) ) {
            return StringUtils.EMPTY;
        }
        String lightColor = lightColor(color);
        return String.format("border-color: %s; color: %s; background-color: %s;", color, color, lightColor); //$NON-NLS-1$
    }


    /**
     * @param color
     * @return
     */
    private static String lightColor ( String color ) {
        try {
            Color decode = Color.decode(color);
            return String.format("rgba(%d,%d,%d,%f)", decode.getRed(), decode.getGreen(), decode.getBlue(), 0.2f); //$NON-NLS-1$
        }
        catch ( NumberFormatException e ) {
            return "inherit"; //$NON-NLS-1$
        }

    }


    /**
     * 
     * @param s
     * @return whether there is no user classification
     */
    public boolean isUnclassifiedSubject ( SubjectInfo s ) {
        return getSubjectTrustLevel(s) == null;
    }


    /**
     * 
     * @param mailAddress
     * @return whether there is no classification for the mail
     */
    public boolean isUnclassifiedMail ( String mailAddress ) {
        return getMailTrustLevel(mailAddress) == null;
    }


    /**
     * 
     * @return whether there is no classification for link
     */
    public boolean isUnclassifiedLink () {
        return getLinkTrustLevel() == null;
    }


    /**
     * 
     * @param pi
     * @return the peer display trust level
     */
    public String getPeerDisplayTrustLevelTitle ( PeerInfo pi ) {
        if ( pi instanceof SubjectPeerInfo ) {
            return getDisplayTrustLevelTitle( ( (SubjectPeerInfo) pi ).getSubject());
        }
        else if ( pi instanceof MailPeerInfo ) {
            return getDisplayMailTrustLevelTitle( ( (MailPeerInfo) pi ).getMailAddress());
        }
        else if ( pi instanceof TokenPeerInfo ) {
            return getDisplayLinkTrustLevelTitle();
        }
        return null;
    }


    /**
     * 
     * @param s
     * @return the subject display color
     */
    public String getDisplayTrustLevelColor ( SubjectInfo s ) {
        TrustLevel l = getSubjectTrustLevel(s);

        if ( l == null ) {
            return "#FF0000"; //$NON-NLS-1$
        }

        return l.getColor();
    }


    /**
     * 
     * @param mailAddress
     * @return the subject display color
     */
    public String getDisplayMailTrustLevelColor ( String mailAddress ) {
        TrustLevel l = getMailTrustLevel(mailAddress);

        if ( l == null ) {
            return "red"; //$NON-NLS-1$
        }

        return l.getColor();
    }


    /**
     * 
     * @return the subject display color
     */
    public String getDisplayLinkTrustLevelColor () {
        TrustLevel l = getLinkTrustLevel();

        if ( l == null ) {
            return "red"; //$NON-NLS-1$
        }

        return l.getColor();
    }


    /**
     * 
     * @param pi
     * @return the peer display trust level color
     */
    public String getPeerDisplayTrustLevelColor ( PeerInfo pi ) {

        if ( pi instanceof SubjectPeerInfo ) {
            return getDisplayTrustLevelColor( ( (SubjectPeerInfo) pi ).getSubject());
        }
        else if ( pi instanceof MailPeerInfo ) {
            return getDisplayMailTrustLevelColor( ( (MailPeerInfo) pi ).getMailAddress());
        }
        else if ( pi instanceof TokenPeerInfo ) {
            return getDisplayLinkTrustLevelColor();
        }

        return null;
    }


    /**
     * @param s
     * @return an icon class
     */
    public String getNameIconClass ( SubjectInfo s ) {
        TrustLevel l = getSubjectTrustLevel(s);
        if ( l == null ) {
            return "ui-icon-cancel"; //$NON-NLS-1$
        }

        return SubjectDisplayBean.getSubjectIconClass(s);
    }


    /**
     * 
     * @param s
     * @return display style class for the subject name
     */
    public String getNameDisplayClass ( SubjectInfo s ) {
        if ( s == null || s.getNameSource() == null ) {
            return null;
        }
        switch ( s.getNameSource() ) {
        case FULL_NAME:
            return "full-name"; //$NON-NLS-1$
        case GROUP_NAME:
            return "group-name"; //$NON-NLS-1$
        case MAIL:
            return "mail"; //$NON-NLS-1$
        case USERNAME:
            return "username"; //$NON-NLS-1$
        default:
            return null;
        }
    }


    /**
     * 
     * @param s
     * @return the trust level display message
     */
    public String getDisplayTrustLevelMessage ( SubjectInfo s ) {
        TrustLevel l = getSubjectTrustLevel(s);

        if ( l == null ) {
            return FileshareMessages.get("trustLevel.unclassified"); //$NON-NLS-1$
        }

        return l.getMessage(FacesContext.getCurrentInstance().getViewRoot().getLocale());
    }


    /**
     * 
     * @param mailAddress
     * @return the trust level display message
     */
    public String getDisplayMailTrustLevelMessage ( String mailAddress ) {
        TrustLevel l = getMailTrustLevel(mailAddress);

        if ( l == null ) {
            return FileshareMessages.get("trustLevel.unclassified"); //$NON-NLS-1$
        }

        return l.getMessage(FacesContext.getCurrentInstance().getViewRoot().getLocale());
    }


    /**
     * 
     * @return the trust level display message
     */
    public String getDisplayLinkTrustLevelMessage () {
        TrustLevel l = getLinkTrustLevel();

        if ( l == null ) {
            return FileshareMessages.get("trustLevel.unclassified"); //$NON-NLS-1$
        }

        return l.getMessage(FacesContext.getCurrentInstance().getViewRoot().getLocale());
    }


    /**
     * 
     * @param g
     * @return the display trust level color
     */
    public String getDisplayGrantTrustLevelMessage ( Grant g ) {
        if ( g instanceof SubjectGrant ) {
            return getDisplayTrustLevelMessage( ( (SubjectGrant) g ).getTarget());
        }
        else if ( g instanceof MailGrant ) {
            return getDisplayMailTrustLevelMessage( ( (MailGrant) g ).getMailAddress());
        }
        else if ( g instanceof TokenGrant ) {
            return getDisplayLinkTrustLevelMessage();
        }
        return null;
    }


    /**
     * @param s
     * @return
     */
    private TrustLevel getSubjectTrustLevel ( SubjectInfo s ) {
        if ( s instanceof Subject ) {
            return this.fsp.getConfigurationProvider().getTrustLevelConfiguration().getTrustLevel((Subject) s);
        }
        else if ( s instanceof SubjectQueryResult ) {
            return this.fsp.getConfigurationProvider().getTrustLevelConfiguration().getTrustLevel( ( (SubjectQueryResult) s ).getTrustLevel());
        }

        return null;
    }


    /**
     * @param mailAddress
     * @return
     */
    private TrustLevel getMailTrustLevel ( String mailAddress ) {
        return this.fsp.getConfigurationProvider().getTrustLevelConfiguration().getMailTrustLevel(mailAddress);
    }


    /**
     * @return
     */
    private TrustLevel getLinkTrustLevel () {
        return this.fsp.getConfigurationProvider().getTrustLevelConfiguration().getLinkTrustLevel();
    }

}
