/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 14.10.2014 by mbechler
 */
package eu.agno3.orchestrator.server.auth.webapp.cas;


import java.io.Serializable;

import javax.enterprise.context.SessionScoped;


/**
 * @author mbechler
 *
 */
@SessionScoped
public class CASSession implements Serializable {

    /**
     * 
     */
    private static final long serialVersionUID = -7812750464029278496L;

    private String tgt;


    /**
     * @return whether this session has a TGT set up
     */
    public boolean hasTgt () {
        return this.tgt != null;
    }


    /**
     * @return the tgt
     */
    public String getTgt () {
        return this.tgt;
    }


    /**
     * @param tgt
     *            the tgt to set
     */
    public void setTgt ( String tgt ) {
        this.tgt = tgt;
    }
}
