/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 27.11.2014 by mbechler
 */
package eu.agno3.orchestrator.server.webgui.menu;


import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Any;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;

import org.apache.log4j.Logger;
import org.primefaces.model.menu.DefaultSubMenu;

import eu.agno3.orchestrator.config.model.realm.StructuralObject;


/**
 * @author mbechler
 *
 */
@ApplicationScoped
public class MenuContributionManager {

    private static final Logger log = Logger.getLogger(MenuContributionManager.class);

    private Set<MenuContributor> contributors = new HashSet<>();


    @Inject
    void loadContributors ( @Any Instance<MenuContributor> contribs ) {
        for ( MenuContributor contrib : contribs ) {
            if ( log.isDebugEnabled() ) {
                log.debug("adding contributor " + contrib.getClass().getName()); //$NON-NLS-1$
            }
            this.contributors.add(contrib);
        }
    }


    /**
     * @param menu
     * @param selectedObject
     * @param refObject
     *            referenced object, potentially null
     * @return the events to listen to for refresh
     */
    public Set<String> addMenuContributions ( DefaultSubMenu menu, StructuralObject selectedObject, StructuralObject refObject ) {
        Set<String> listenTo = new HashSet<>();
        List<WeightedMenuElement> elements = new ArrayList<>();
        for ( MenuContributor contributor : this.contributors ) {
            if ( contributor.isApplicable(selectedObject, refObject) ) {
                elements.addAll(contributor.getContributions(selectedObject, refObject));
                Collection<String> lt = contributor.getListenTo(selectedObject, refObject);
                if ( lt != null ) {
                    listenTo.addAll(lt);
                }
            }
        }
        Collections.sort(elements, new MenuElementComparator());
        for ( WeightedMenuElement elem : elements ) {
            menu.addElement(elem);
        }
        return listenTo;
    }


    /**
     * @param path
     * @param payload
     * @param selectedObject
     * @param refObject
     * @return whether any contributor signals a change
     */
    public boolean notifyRefresh ( String path, String payload, StructuralObject selectedObject, StructuralObject refObject ) {
        log.debug("Refreshing contributors"); //$NON-NLS-1$
        boolean changed = false;
        for ( MenuContributor contributor : this.contributors ) {
            if ( contributor.isApplicable(selectedObject, refObject) ) {
                changed |= contributor.notifyRefresh(path, payload, selectedObject, refObject);
            }
        }
        return changed;
    }

}
