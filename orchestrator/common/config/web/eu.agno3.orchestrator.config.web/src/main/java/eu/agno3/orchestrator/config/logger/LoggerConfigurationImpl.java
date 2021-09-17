/**
 * © 2016 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: Sep 17, 2016 by mbechler
 */
package eu.agno3.orchestrator.config.logger;


import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.PersistenceUnit;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.eclipse.jdt.annotation.NonNull;
import org.hibernate.envers.Audited;

import eu.agno3.orchestrator.config.model.realm.AbstractConfigurationObject;
import eu.agno3.runtime.xml.binding.MapAs;


/**
 * @author mbechler
 *
 */
@MapAs ( LoggerConfiguration.class )
@Entity
@Table ( name = "config_logger" )
@Audited
@PersistenceUnit ( unitName = "config" )
@DiscriminatorValue ( "loggerc" )
public class LoggerConfigurationImpl extends AbstractConfigurationObject<LoggerConfiguration>
        implements LoggerConfiguration, LoggerConfigurationMutable {

    /**
     * 
     */
    private static final long serialVersionUID = 8657412193148259055L;

    private Integer retentionDays;
    private IPLogAnonymizationType ipAnonymizationType;


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.config.model.realm.AbstractConfigurationObject#getType()
     */
    @Override
    @Transient
    public @NonNull Class<LoggerConfiguration> getType () {
        return LoggerConfiguration.class;
    }


    /**
     * @return the retentionDays
     */
    @Override
    public Integer getRetentionDays () {
        return this.retentionDays;
    }


    /**
     * @param retentionDays
     *            the retentionDays to set
     */
    @Override
    public void setRetentionDays ( Integer retentionDays ) {
        this.retentionDays = retentionDays;
    }


    /**
     * @return the ipAnonymizationType
     */
    @Override
    public IPLogAnonymizationType getIpAnonymizationType () {
        return this.ipAnonymizationType;
    }


    /**
     * @param ipAnonymizationType
     *            the ipAnonymizationType to set
     */
    @Override
    public void setIpAnonymizationType ( IPLogAnonymizationType ipAnonymizationType ) {
        this.ipAnonymizationType = ipAnonymizationType;
    }

}
