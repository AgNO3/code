/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 19.03.2015 by mbechler
 */
package eu.agno3.fileshare.service.api.internal;


import java.util.Locale;

import javax.servlet.ServletRequest;

import eu.agno3.fileshare.exceptions.PolicyNotFulfilledException;
import eu.agno3.fileshare.model.PolicyViolation;
import eu.agno3.fileshare.model.SecurityLabel;
import eu.agno3.fileshare.model.VFSEntity;
import eu.agno3.fileshare.vfs.VFSContext;


/**
 * @author mbechler
 *
 */
public interface PolicyEvaluator {

    /**
     * 
     * @param v
     * @param entity
     * @param req
     * @throws PolicyNotFulfilledException
     */
    public void checkPolicy ( VFSContext v, VFSEntity entity, ServletRequest req ) throws PolicyNotFulfilledException;


    /**
     * 
     * @param v
     * @param entity
     * @param req
     * @return whether the policy is fulfilled
     */
    public PolicyViolation isPolicyFulfilled ( VFSContext v, VFSEntity entity, ServletRequest req );


    /**
     * 
     * @param label
     * @param req
     * @return whether the policy is fulfilled
     */
    public PolicyViolation isPolicyFulfilled ( SecurityLabel label, ServletRequest req );


    /**
     * 
     * @param label
     * @param req
     * @return whether the policy is fulfilled
     */
    public PolicyViolation isPolicyFulfilled ( String label, ServletRequest req );


    /**
     * @param violation
     * @param l
     * @return a formatted violation message
     */
    public String getViolationMessage ( PolicyViolation violation, Locale l );
}
