/**
 * © 2016 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 14.09.2016 by mbechler
 */
package eu.agno3.runtime.security.web.gui.terms;


/**
 * @author mbechler
 *
 */
public class AcceptanceWrapper {

    private TermsAcceptance comp;
    private String id;


    /**
     * @param id
     * @param comp
     * 
     */
    public AcceptanceWrapper ( String id, TermsAcceptance comp ) {
        this.id = id;
        this.comp = comp;
    }


    /**
     * 
     * @return acceptance value
     */
    public boolean getAccepted () {
        return this.comp.getAcceptanceMap().getOrDefault(this.id, false);
    }


    /**
     * 
     * @param accepted
     */
    public void setAccepted ( boolean accepted ) {
        this.comp.getAcceptanceMap().put(this.id, accepted);
    }
}
