/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 14.04.2015 by mbechler
 */
package eu.agno3.orchestrator.config.realms;


import java.util.HashSet;
import java.util.Set;
import java.util.TreeSet;

import javax.persistence.CollectionTable;
import javax.persistence.DiscriminatorValue;
import javax.persistence.ElementCollection;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.PersistenceUnit;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.eclipse.jdt.annotation.NonNull;
import org.hibernate.envers.Audited;

import eu.agno3.orchestrator.config.model.realm.AbstractConfigurationObject;
import eu.agno3.orchestrator.realms.KeyData;
import eu.agno3.runtime.xml.binding.MapAs;


/**
 * @author mbechler
 *
 */
@Entity
@Table ( name = "config_realms_ktimport" )
@Audited
@PersistenceUnit ( unitName = "config" )
@DiscriminatorValue ( "re_kti" )
@MapAs ( KeytabEntry.class )
public class KeytabEntryImpl extends AbstractConfigurationObject<KeytabEntry> implements KeytabEntry, KeytabEntryMutable {

    /**
     * 
     */
    private static final long serialVersionUID = -7024360151033155195L;

    private String keytabId;
    private Set<KeyData> keyImportEntries = new TreeSet<>();


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.config.model.realm.AbstractConfigurationObject#getType()
     */
    @Override
    @Transient
    public @NonNull Class<KeytabEntry> getType () {
        return KeytabEntry.class;
    }


    /**
     * @return the keytabId
     */
    @Override
    public String getKeytabId () {
        return this.keytabId;
    }


    /**
     * @param keytabId
     *            the keytabId to set
     */
    @Override
    public void setKeytabId ( String keytabId ) {
        this.keytabId = keytabId;
    }


    /**
     * @param keyEntries
     *            the keyEntries to set
     */
    @Override
    public void setKeyImportEntries ( Set<KeyData> keyEntries ) {
        this.keyImportEntries = keyEntries;
    }


    /**
     * @return the keyEntries
     */
    @Override
    @ElementCollection
    @CollectionTable ( name = "config_realms_ktimport_entries" )
    @Embedded
    public Set<KeyData> getKeyImportEntries () {
        return this.keyImportEntries;
    }


    /**
     * @param importKeytabs
     * @return cloned set
     */
    public static Set<KeytabEntry> clone ( Set<KeytabEntry> importKeytabs ) {
        Set<KeytabEntry> cloned = new HashSet<>();
        for ( KeytabEntry e : importKeytabs ) {
            cloned.add(clone(e));
        }
        return cloned;
    }


    /**
     * @param e
     * @return cloned entry
     */
    public static KeytabEntry clone ( KeytabEntry e ) {
        KeytabEntryImpl cloned = new KeytabEntryImpl();
        cloned.keytabId = e.getKeytabId();
        cloned.keyImportEntries = KeyData.clone(e.getKeyImportEntries());
        return cloned;
    }
}
