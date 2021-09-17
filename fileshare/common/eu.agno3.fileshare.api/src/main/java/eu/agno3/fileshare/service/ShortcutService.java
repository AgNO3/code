/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 15.05.2015 by mbechler
 */
package eu.agno3.fileshare.service;


import java.util.List;

import eu.agno3.fileshare.exceptions.FileshareException;
import eu.agno3.fileshare.model.shortcut.Shortcut;


/**
 * @author mbechler
 *
 */
public interface ShortcutService {

    /**
     * 
     * @return the current user's shortcuts
     * @throws FileshareException
     */
    List<Shortcut> getUserShortcuts () throws FileshareException;
}
