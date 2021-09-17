/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 23.01.2014 by mbechler
 */
package eu.agno3.orchestrator.config.model.base.exceptions;


import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import javax.validation.ConstraintViolation;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.ws.WebFault;

import eu.agno3.orchestrator.config.model.base.exceptions.faults.ModelObjectValidationFault;
import eu.agno3.orchestrator.config.model.validation.ViolationEntry;
import eu.agno3.orchestrator.config.model.validation.ViolationEntryImpl;


/**
 * @author mbechler
 * 
 */
@WebFault (
    faultBean = "eu.agno3.orchestrator.config.model.base.exceptions.faults.ModelObjectValidationFault",
    targetNamespace = "urn:agno3:model:1.0:base:exceptions" )
public class ModelObjectValidationException extends ModelObjectException {

    /**
     * 
     */
    private static final long serialVersionUID = -1677650376802433212L;


    /**
     * @param type
     * @param id
     * @param t
     */
    public ModelObjectValidationException ( Class<?> type, UUID id, Throwable t ) {
        super(new ModelObjectValidationFault(type, id, Collections.EMPTY_SET), t);
    }


    /**
     * @param type
     * @param id
     * @param violations
     */
    public <T> ModelObjectValidationException ( Class<T> type, UUID id, Set<ConstraintViolation<T>> violations ) {
        super(new ModelObjectValidationFault(type, id, makeViolationEntries(violations)));
    }


    /**
     * 
     * @param message
     * @param faultInfo
     */
    public ModelObjectValidationException ( String message, ModelObjectValidationFault faultInfo ) {
        super(message, faultInfo);
    }


    /**
     * 
     * @param message
     * @param faultInfo
     * @param cause
     */
    public ModelObjectValidationException ( String message, ModelObjectValidationFault faultInfo, Throwable cause ) {
        super(message, faultInfo, cause);
    }


    /**
     * {@inheritDoc}
     * 
     * @see eu.agno3.orchestrator.config.model.base.exceptions.ModelObjectException#getFaultInfo()
     */
    @Override
    public ModelObjectValidationFault getFaultInfo () {
        return (ModelObjectValidationFault) super.getFaultInfo();
    }


    private static final <T> Set<ViolationEntry> makeViolationEntries ( Set<ConstraintViolation<T>> violations ) {
        Set<ViolationEntry> res = new HashSet<>();

        for ( ConstraintViolation<?> violation : violations ) {
            res.add(makeViolationEntry(violation));
        }

        return res;
    }


    private static final <T> ViolationEntry makeViolationEntry ( ConstraintViolation<T> violation ) {
        return ViolationEntryImpl.fromConstraintViolation(violation, null);
    }


    /**
     * {@inheritDoc}
     * 
     * @see java.lang.Throwable#getMessage()
     */
    @Override
    @XmlTransient
    public String getMessage () {
        StringBuilder sb = new StringBuilder();

        if ( this.getFaultInfo() == null ) {
            return "Validation failed: No information available"; //$NON-NLS-1$
        }

        ModelObjectValidationFault vf = this.getFaultInfo();

        sb.append(String.format("Validation of %s @%s failed:", vf.getObjectType(), vf.getId())); //$NON-NLS-1$

        if ( this.getCause() != null ) {
            sb.append(this.getCause().getMessage());
        }

        if ( vf.getEntries() != null ) {
            for ( ViolationEntry e : this.getFaultInfo().getEntries() ) {
                sb.append('\n');
                sb.append(e);
            }
        }

        return sb.toString();
    }
}
