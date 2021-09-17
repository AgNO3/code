/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 12.08.2014 by mbechler
 */
package eu.agno3.orchestrator.server.webgui.structure.menu.base;


/**
 * @author mbechler
 * 
 */
public class ActionMenuContributionImpl implements ActionMenuContribution {

    private ActionMenuContributor source;
    private String labelKey;
    private String icon;
    private String action;
    private float weight;


    /**
     * 
     * @param src
     * @param labelKey
     * @param action
     * @param weight
     */
    public ActionMenuContributionImpl ( ActionMenuContributor src, String labelKey, String action, float weight ) {
        this(src, labelKey, action, weight, null);
    }


    /**
     * 
     * @param src
     * @param labelKey
     * @param action
     * @param weight
     * @param icon
     */
    public ActionMenuContributionImpl ( ActionMenuContributor src, String labelKey, String action, float weight, String icon ) {
        this.source = src;
        this.labelKey = labelKey;
        this.action = action;
        this.weight = weight;
        this.icon = icon;
    }


    /**
     * @return the source
     */
    @Override
    public ActionMenuContributor getSource () {
        return this.source;
    }


    /**
     * {@inheritDoc}
     * 
     * @see eu.agno3.orchestrator.server.webgui.structure.menu.base.ActionMenuContribution#getWeight()
     */
    @Override
    public float getWeight () {
        return this.weight;
    }


    /**
     * {@inheritDoc}
     * 
     * @see eu.agno3.orchestrator.server.webgui.structure.menu.base.ActionMenuContribution#getLabelKey()
     */
    @Override
    public String getLabelKey () {
        return this.labelKey;
    }


    /**
     * {@inheritDoc}
     * 
     * @see eu.agno3.orchestrator.server.webgui.structure.menu.base.ActionMenuContribution#getAction()
     */
    @Override
    public String getAction () {
        return this.action;
    }


    /**
     * {@inheritDoc}
     * 
     * @see eu.agno3.orchestrator.server.webgui.structure.menu.base.ActionMenuContribution#getIcon()
     */
    @Override
    public String getIcon () {
        return this.icon;
    }

}
