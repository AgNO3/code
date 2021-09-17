/**
 * © 2016 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: Sep 17, 2016 by mbechler
 */
package eu.agno3.fileshare.orch.common.config;


import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.PersistenceUnit;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.eclipse.jdt.annotation.NonNull;
import org.hibernate.envers.Audited;

import eu.agno3.orchestrator.config.logger.LoggerConfigurationImpl;
import eu.agno3.orchestrator.config.logger.LoggerConfigurationMutable;
import eu.agno3.orchestrator.config.model.realm.AbstractConfigurationObject;
import eu.agno3.runtime.xml.binding.MapAs;


/**
 * @author mbechler
 *
 */
@MapAs ( FileshareLoggerConfig.class )
@Entity
@PersistenceUnit ( unitName = "config" )
@Table ( name = "config_fileshare_logger" )
@Audited
@DiscriminatorValue ( "filesh_log" )
public class FileshareLoggerConfigImpl extends AbstractConfigurationObject<FileshareLoggerConfig>
        implements FileshareLoggerConfig, FileshareLoggerConfigMutable {

    /**
     * 
     */
    private static final long serialVersionUID = 5986976691594433210L;

    private LoggerConfigurationImpl unauthLoggerConfig;
    private LoggerConfigurationImpl defaultLoggerConfig;


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.config.model.realm.AbstractConfigurationObject#getType()
     */
    @Override
    @Transient
    public @NonNull Class<FileshareLoggerConfig> getType () {
        return FileshareLoggerConfig.class;
    }


    /**
     * @return the unauthLoggerConfig
     */
    @Override
    @JoinColumn ( name = "unauthlog" )
    @ManyToOne ( optional = true, fetch = FetchType.LAZY, targetEntity = LoggerConfigurationImpl.class )
    public LoggerConfigurationMutable getUnauthLoggerConfig () {
        return this.unauthLoggerConfig;
    }


    /**
     * @param unauthLoggerConfig
     *            the unauthLoggerConfig to set
     */
    @Override
    public void setUnauthLoggerConfig ( LoggerConfigurationMutable unauthLoggerConfig ) {
        this.unauthLoggerConfig = (LoggerConfigurationImpl) unauthLoggerConfig;
    }


    /**
     * @return the defaultLoggerConfig
     */
    @Override
    @JoinColumn ( name = "defaultlog" )
    @ManyToOne ( optional = true, fetch = FetchType.LAZY, targetEntity = LoggerConfigurationImpl.class )
    public LoggerConfigurationMutable getDefaultLoggerConfig () {
        return this.defaultLoggerConfig;
    }


    /**
     * @param defaultLoggerConfig
     *            the defaultLoggerConfig to set
     */
    @Override
    public void setDefaultLoggerConfig ( LoggerConfigurationMutable defaultLoggerConfig ) {
        this.defaultLoggerConfig = (LoggerConfigurationImpl) defaultLoggerConfig;
    }

}
