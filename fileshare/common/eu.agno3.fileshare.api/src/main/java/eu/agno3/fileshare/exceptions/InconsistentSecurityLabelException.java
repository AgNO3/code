/**
 * © 2016 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 15.09.2016 by mbechler
 */
package eu.agno3.fileshare.exceptions;


import java.util.Collection;
import java.util.Collections;
import java.util.Map;

import eu.agno3.fileshare.model.EntityKey;


/**
 * @author mbechler
 *
 */
public class InconsistentSecurityLabelException extends InvalidSecurityLabelException {

    /**
     * 
     */
    private static final long serialVersionUID = 2717321175572666230L;

    private Map<String, Collection<EntityKey>> blockers = Collections.EMPTY_MAP;


    /**
     * 
     */
    public InconsistentSecurityLabelException () {
        super();
    }


    /**
     * @param label
     * @param msg
     * @param t
     */
    public InconsistentSecurityLabelException ( String label, String msg, Throwable t ) {
        super(label, msg, t);
    }


    /**
     * @param label
     * @param msg
     */
    public InconsistentSecurityLabelException ( String label, String msg ) {
        super(label, msg);
    }


    /**
     * @param label
     * @param cause
     */
    public InconsistentSecurityLabelException ( String label, Throwable cause ) {
        super(label, cause);
    }


    /**
     * @return the blockers
     */
    public Map<String, Collection<EntityKey>> getBlockers () {
        return this.blockers;
    }


    /**
     * @param blockers
     *            the blockers to set
     */
    public void setBlockers ( Map<String, Collection<EntityKey>> blockers ) {
        this.blockers = blockers;
    }

}
