/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 12.08.2014 by mbechler
 */
package eu.agno3.orchestrator.server.webgui.structure.menu.base;


import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.Set;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Any;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;

import org.primefaces.model.menu.DefaultMenuItem;
import org.primefaces.model.menu.DefaultMenuModel;
import org.primefaces.model.menu.MenuModel;

import eu.agno3.orchestrator.config.model.realm.StructuralObjectType;
import eu.agno3.orchestrator.server.webgui.CoreServiceProvider;


/**
 * @author mbechler
 * 
 */
@ApplicationScoped
public class ActionMenuBuilder {

    @Inject
    private CoreServiceProvider csp;

    @Inject
    @Any
    private Instance<ActionMenuContributor> actionContributors;


    /**
     * 
     * @param fromContext
     *            whether the selection is from structure context or the menu
     * @param type
     * @param l
     * @return an action model
     */
    public MenuModel createActionModel ( boolean fromContext, StructuralObjectType type, Locale l ) {
        DefaultMenuModel model = new DefaultMenuModel();

        List<ActionMenuContribution> contributions = new ArrayList<>();

        for ( ActionMenuContributor contributor : this.actionContributors ) {
            if ( contributor.isApplicable(type, fromContext) ) {
                Set<ActionMenuContribution> ctr = contributor.getContributions(fromContext);

                if ( ctr != null ) {
                    contributions.addAll(ctr);
                }
            }
        }

        Collections.sort(contributions, new ActionMenuContributionComparator());

        for ( ActionMenuContribution contrib : contributions ) {
            ActionMenuContributor ctrbt = contrib.getSource();

            ResourceBundle rb = this.csp.getLocalizationService().getBundle(ctrbt.getBaseName(), l, Thread.currentThread().getContextClassLoader());
            String label = rb.getString(ctrbt.getLabelKeyPrefix() + contrib.getLabelKey());
            DefaultMenuItem itm = new DefaultMenuItem(label);
            itm.setIcon(contrib.getIcon());
            itm.setCommand(contrib.getAction());
            model.addElement(itm);
        }

        model.generateUniqueIds();

        return model;
    }

}
