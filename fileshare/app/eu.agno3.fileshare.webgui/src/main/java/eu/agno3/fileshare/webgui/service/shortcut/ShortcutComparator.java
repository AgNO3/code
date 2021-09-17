/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 16.05.2015 by mbechler
 */
package eu.agno3.fileshare.webgui.service.shortcut;


import java.io.Serializable;
import java.text.Collator;
import java.util.Comparator;

import javax.faces.context.FacesContext;

import eu.agno3.fileshare.model.shortcut.Shortcut;
import eu.agno3.fileshare.model.shortcut.ShortcutType;


/**
 * @author mbechler
 *
 */
public class ShortcutComparator implements Comparator<Shortcut>, Serializable {

    /**
     * 
     */
    private static final long serialVersionUID = -4562859815195644808L;
    private boolean sortFavorites;


    /**
     * 
     */
    public ShortcutComparator () {
        this(false);
    }


    /**
     * @param sortFavorites
     */
    public ShortcutComparator ( boolean sortFavorites ) {
        this.sortFavorites = sortFavorites;
    }


    /**
     * {@inheritDoc}
     *
     * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
     */
    @Override
    public int compare ( Shortcut o1, Shortcut o2 ) {
        int res = o1.getType().compareTo(o2.getType());
        if ( res != 0 ) {
            return res;
        }

        if ( !this.sortFavorites && ShortcutType.FAVORITE == o1.getType() ) {
            return 0;
        }

        return Collator.getInstance(FacesContext.getCurrentInstance().getViewRoot().getLocale()).compare(o1.getLabel(), o2.getLabel());
    }

}
