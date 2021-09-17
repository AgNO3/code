/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 15.05.2015 by mbechler
 */
package eu.agno3.fileshare.webgui.service.shortcut;


import java.io.Serializable;
import java.lang.reflect.UndeclaredThrowableException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.enterprise.context.SessionScoped;
import javax.inject.Inject;
import javax.inject.Named;

import org.apache.log4j.Logger;
import org.primefaces.context.RequestContext;

import eu.agno3.fileshare.exceptions.FileshareException;
import eu.agno3.fileshare.model.shortcut.Shortcut;
import eu.agno3.fileshare.model.shortcut.ShortcutType;
import eu.agno3.fileshare.webgui.exceptions.ExceptionHandler;
import eu.agno3.fileshare.webgui.i18n.FileshareMessages;
import eu.agno3.fileshare.webgui.service.FileshareServiceProvider;
import eu.agno3.fileshare.webgui.subject.SubjectDisplayBean;


/**
 * @author mbechler
 *
 */
@SessionScoped
@Named ( "shortcutBean" )
public class ShortcutBean implements Serializable {

    /**
     * 
     */
    private static final Logger log = Logger.getLogger(ShortcutBean.class);
    private static final long serialVersionUID = 1591542038366215484L;

    private Map<ShortcutType, List<Shortcut>> shortcuts = new HashMap<>();
    private boolean shortcutsLoaded;

    @Inject
    private FileshareServiceProvider fsp;


    /**
     * 
     * @param t
     * @return the shortcuts by type
     */
    public List<Shortcut> getShortcutsByType ( ShortcutType t ) {
        ensureShortcutsLoaded();
        List<Shortcut> sc = this.shortcuts.get(t);
        if ( sc == null ) {
            return Collections.EMPTY_LIST;
        }
        return sc;
    }


    /**
     * 
     * @return group membership favorites
     */
    public List<Shortcut> getMemberGroupFavorites () {
        return getShortcutsByType(ShortcutType.MEMBER_GROUP);
    }


    /**
     * 
     * @return all entity favorites
     */
    public List<Shortcut> getEntityFavorites () {
        return getShortcutsByType(ShortcutType.FAVORITE);
    }


    /**
     * 
     * @return first 5 entity favorites
     */
    public List<Shortcut> getFirstEntityFavorites () {
        List<Shortcut> shortcutsByType = getShortcutsByType(ShortcutType.FAVORITE);
        List<Shortcut> first = new ArrayList<>(shortcutsByType.subList(0, Math.min(shortcutsByType.size(), 5)));
        Collections.sort(first, new ShortcutComparator(true));
        return first;
    }


    /**
     * 
     * @return peer favorites
     */
    public List<Shortcut> getPeerFavorites () {
        List<Shortcut> res = new LinkedList<>();
        res.addAll(getShortcutsByType(ShortcutType.PEER_GROUP));
        res.addAll(getShortcutsByType(ShortcutType.PEER));
        res.addAll(getShortcutsByType(ShortcutType.PEER_MAIL));
        res.addAll(getShortcutsByType(ShortcutType.PEER_LINK));
        return res;
    }


    /**
     * 
     * @param sc
     * @return icon class
     */
    public String getIcon ( Shortcut sc ) {

        if ( sc == null || sc.getType() == null ) {
            return null;
        }

        switch ( sc.getType() ) {
        case PEER:
            return SubjectDisplayBean.getUserIconClass();
        case PEER_GROUP:
            return SubjectDisplayBean.getGroupIconClass();
        case PEER_MAIL:
            return "ui-icon-mail-closed"; //$NON-NLS-1$
        case PEER_LINK:
            return "ui-icon-link"; //$NON-NLS-1$
        default:
            return null;
        }
    }


    /**
     * 
     * @param sc
     * @return the label for the shortcut
     */
    public String getLabel ( Shortcut sc ) {
        if ( sc == null || sc.getType() == null ) {
            return null;
        }

        if ( sc.getType() == ShortcutType.PEER_LINK ) {
            return FileshareMessages.get("menu.peers.links"); //$NON-NLS-1$
        }

        return sc.getLabel();
    }


    /**
     * 
     * @return null
     */
    public String refresh () {
        this.shortcutsLoaded = false;
        this.shortcuts.clear();
        RequestContext.getCurrentInstance().update("form:menuPanel"); //$NON-NLS-1$
        return null;
    }


    /**
     * 
     */
    private void ensureShortcutsLoaded () {

        if ( !this.shortcutsLoaded ) {
            this.shortcutsLoaded = true;
            log.debug("Loading shortcuts"); //$NON-NLS-1$
            try {
                List<Shortcut> scs = this.fsp.getShortcutService().getUserShortcuts();
                Collections.sort(scs, new ShortcutComparator());
                if ( scs != null ) {
                    this.shortcuts.clear();
                    for ( Shortcut sc : scs ) {
                        List<Shortcut> byType = this.shortcuts.get(sc.getType());

                        if ( byType == null ) {
                            byType = new LinkedList<>();
                            this.shortcuts.put(sc.getType(), byType);
                        }
                        byType.add(sc);
                    }
                }
            }
            catch (
                FileshareException |
                UndeclaredThrowableException e ) {
                ExceptionHandler.handleException(e);
            }
        }
    }
}
