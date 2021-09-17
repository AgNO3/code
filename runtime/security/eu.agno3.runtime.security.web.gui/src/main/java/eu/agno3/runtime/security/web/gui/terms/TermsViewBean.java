/**
 * © 2016 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 13.09.2016 by mbechler
 */
package eu.agno3.runtime.security.web.gui.terms;


import javax.faces.view.ViewScoped;
import javax.inject.Named;


/**
 * @author mbechler
 *
 */
@ViewScoped
@Named ( "agsec_termsViewBean" )
public class TermsViewBean extends AbstractTermsHandlerBean {

    /**
     * 
     */
    private static final long serialVersionUID = 2101421666139232412L;

    private String termsId;


    @Override
    public String getTermsId () {
        return this.termsId;
    }


    /**
     * @param termsId
     *            the termsId to set
     */
    public void setTermsId ( String termsId ) {
        this.termsId = termsId;
    }

}
