/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 11.06.2014 by mbechler
 */
package eu.agno3.orchestrator.config.model.realm;


import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.OneToOne;
import javax.persistence.PersistenceUnit;
import javax.persistence.Table;
import javax.xml.bind.annotation.XmlTransient;

import org.hibernate.envers.Audited;


/**
 * @author mbechler
 * @param <T>
 * 
 */
@Entity
@Table ( name = "config_instances" )
@PersistenceUnit ( unitName = "config" )
@Audited
@XmlTransient
@DiscriminatorValue ( "instance" )
public abstract class AbstractConfigurationInstance <T extends ConfigurationInstance> extends AbstractConfigurationObject<T> implements
        ConfigurationInstance {

    /**
     * 
     */
    private static final long serialVersionUID = 6242605367167627011L;

    private ServiceStructuralObject forService;


    /**
     * @return the forService
     */
    @Override
    @OneToOne ( mappedBy = "configuration", fetch = FetchType.LAZY, cascade = {}, targetEntity = ServiceStructuralObjectImpl.class )
    public ServiceStructuralObject getForService () {
        return this.forService;
    }


    /**
     * @param forService
     *            the forService to set
     */
    public void setForService ( ServiceStructuralObject forService ) {
        this.forService = forService;
    }
}
