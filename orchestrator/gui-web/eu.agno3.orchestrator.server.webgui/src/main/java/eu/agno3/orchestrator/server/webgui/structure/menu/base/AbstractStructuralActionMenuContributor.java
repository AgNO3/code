/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 12.08.2014 by mbechler
 */
package eu.agno3.orchestrator.server.webgui.structure.menu.base;


import java.util.Set;

import eu.agno3.orchestrator.server.webgui.GuiMessages;


/**
 * @author mbechler
 * 
 */
public abstract class AbstractStructuralActionMenuContributor implements ActionMenuContributor {

    /**
     * {@inheritDoc}
     * 
     * @see eu.agno3.orchestrator.server.webgui.structure.menu.base.ActionMenuContributor#getBaseName()
     */
    @Override
    public String getBaseName () {
        return GuiMessages.GUI_MESSAGES_BASE;
    }


    protected abstract String getControllerName ();


    protected void makeContribution ( boolean fromContext, Set<ActionMenuContribution> contributions, String labelKey, float weight ) {
        makeContribution(fromContext, contributions, labelKey, weight, null);
    }


    protected void makeContribution ( boolean fromContext, Set<ActionMenuContribution> contributions, String labelKey, float weight, String icon ) {
        contributions.add(new ActionMenuContributionImpl(this, labelKey, makeAction(getControllerName(), labelKey, fromContext), weight, icon));
    }


    protected String makeAction ( String controller, String method, boolean fromContext ) {
        StringBuilder sb = new StringBuilder();
        sb.append('#').append('{');
        sb.append(controller);
        sb.append('.');
        sb.append(method);
        sb.append('(');
        sb.append(String.valueOf(fromContext));
        sb.append(')');
        sb.append('}');
        return sb.toString();
    }

}