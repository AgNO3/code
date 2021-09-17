/**
 * © 2016 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 13.09.2016 by mbechler
 */
package eu.agno3.runtime.security.web.gui.terms;


import java.io.Serializable;

import javax.faces.context.FacesContext;
import javax.inject.Inject;

import eu.agno3.runtime.security.terms.TermsDefinition;


/**
 * @author mbechler
 *
 */
public abstract class AbstractTermsHandlerBean implements Serializable {

    /**
     * 
     */
    private static final long serialVersionUID = 7272155365037199592L;

    protected boolean termLoaded;
    protected transient TermsDefinition definition;

    @Inject
    protected AbstractTermsBean terms;


    /**
     * 
     */
    public AbstractTermsHandlerBean () {
        super();
    }


    /**
     * @return the termid
     */
    public TermsDefinition getDefinition () {
        if ( this.definition == null ) {
            String termsId = getTermsId();
            if ( termsId == null ) {
                return null;
            }
            this.definition = this.terms.getDefinition(termsId);
        }
        return this.definition;
    }


    /**
     * @return
     */
    protected abstract String getTermsId ();


    /**
     * @return the definition label
     */
    public String getLabel () {
        TermsDefinition def = getDefinition();
        if ( def != null ) {
            return def.getLabel(FacesContext.getCurrentInstance().getViewRoot().getLocale());
        }
        return null;
    }


    /**
     * 
     * @return the definition description
     */
    public String getDescription () {
        TermsDefinition def = getDefinition();
        if ( def != null ) {
            return def.getDescription(FacesContext.getCurrentInstance().getViewRoot().getLocale());
        }
        return null;
    }


    /**
     * 
     */
    protected void reset () {
        this.termLoaded = false;
        this.definition = null;
    }

}