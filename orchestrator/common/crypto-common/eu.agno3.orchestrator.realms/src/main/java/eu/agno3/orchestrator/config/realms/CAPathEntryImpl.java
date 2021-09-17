/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 08.04.2015 by mbechler
 */
package eu.agno3.orchestrator.config.realms;


import java.util.HashSet;
import java.util.Set;

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
@Entity
@Table ( name = "config_realms_capath" )
@Audited
@PersistenceUnit ( unitName = "config" )
@DiscriminatorValue ( "re_capath" )
@MapAs ( CAPathEntry.class )
public class CAPathEntryImpl extends AbstractConfigurationObject<CAPathEntry> implements CAPathEntry, CAPathEntryMutable {

    /**
     * 
     */
    private static final long serialVersionUID = -7549969842243628993L;

    private String targetRealm;
    private String nextRealm;


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.config.model.realm.AbstractConfigurationObject#getType()
     */
    @Override
    @Transient
    public @NonNull Class<CAPathEntry> getType () {
        return CAPathEntry.class;
    }


    /**
     * @return the targetRealm
     */
    @Override
    public String getTargetRealm () {
        return this.targetRealm;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.config.realms.CAPathEntryMutable#setTargetRealm(java.lang.String)
     */
    @Override
    public void setTargetRealm ( String targetRealm ) {
        this.targetRealm = targetRealm;
    }


    /**
     * @return the nextRealm
     */
    @Override
    public String getNextRealm () {
        return this.nextRealm;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.config.realms.CAPathEntryMutable#setNextRealm(java.lang.String)
     */
    @Override
    public void setNextRealm ( String nextRealm ) {
        this.nextRealm = nextRealm;
    }


    /**
     * @param caPaths
     * @return cloned set
     */
    public static Set<CAPathEntry> clone ( Set<CAPathEntry> caPaths ) {
        Set<CAPathEntry> cloned = new HashSet<>();
        for ( CAPathEntry e : caPaths ) {
            cloned.add(clone(e));
        }
        return cloned;
    }


    /**
     * @param e
     * @return
     */
    private static CAPathEntry clone ( CAPathEntry e ) {
        CAPathEntryImpl cpe = new CAPathEntryImpl();
        cpe.targetRealm = e.getTargetRealm();
        cpe.nextRealm = e.getNextRealm();
        return cpe;
    }

}
