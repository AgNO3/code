/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 30.01.2014 by mbechler
 */
package eu.agno3.runtime.jsf.view.stacking.components;


import javax.faces.view.facelets.ComponentConfig;
import javax.faces.view.facelets.ComponentHandler;
import javax.faces.view.facelets.MetaRule;
import javax.faces.view.facelets.MetaRuleset;

import org.apache.myfaces.view.facelets.tag.MethodRule;

import eu.agno3.runtime.jsf.view.stacking.DialogConstants;


/**
 * @author mbechler
 * 
 */
public class DialogOpenComponentHandler extends ComponentHandler {

    /**
     * @param config
     */
    public DialogOpenComponentHandler ( ComponentConfig config ) {
        super(config);
    }


    /**
     * {@inheritDoc}
     * 
     * @see javax.faces.view.facelets.DelegatingMetaTagHandler#createMetaRuleset(java.lang.Class)
     */
    @Override
    protected MetaRuleset createMetaRuleset ( Class type ) {
        MetaRuleset ruleset = super.createMetaRuleset(type);
        MetaRule rule = new MethodRule(DialogConstants.PRE_OPEN_HANDLER, boolean.class, new Class[] {});
        ruleset.addRule(rule);
        return ruleset;
    }

}
