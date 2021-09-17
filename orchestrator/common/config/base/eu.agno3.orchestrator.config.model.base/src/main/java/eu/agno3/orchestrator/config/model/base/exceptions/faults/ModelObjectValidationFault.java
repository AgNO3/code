/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 17.04.2014 by mbechler
 */
package eu.agno3.orchestrator.config.model.base.exceptions.faults;


import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import eu.agno3.orchestrator.config.model.validation.ViolationEntry;


/**
 * @author mbechler
 * 
 */
public class ModelObjectValidationFault extends ModelObjectFault {

    private Set<ViolationEntry> entries;


    /**
     * @param type
     * @param id
     * @param violations
     */
    public ModelObjectValidationFault ( Class<?> type, UUID id, Collection<ViolationEntry> violations ) {
        super(type, id);
        this.entries = new HashSet<>(violations);
    }


    /**
     * 
     */
    public ModelObjectValidationFault () {}


    /**
     * @return the violation entries
     */
    public Set<ViolationEntry> getEntries () {
        return this.entries;
    }


    /**
     * @param entries
     *            the entries to set
     */
    public void setEntries ( Set<ViolationEntry> entries ) {
        this.entries = entries;
    }

}
