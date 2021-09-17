/**
 * © 2013 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 05.08.2013 by mbechler
 */
package eu.agno3.runtime.db.schema.orm.internal.hibernate.ownership;


import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.hibernate.boot.Metadata;
import org.hibernate.mapping.Collection;
import org.hibernate.mapping.MetaAttribute;
import org.hibernate.mapping.PersistentClass;
import org.hibernate.mapping.Table;
import org.osgi.framework.BundleContext;
import org.osgi.framework.Constants;
import org.osgi.service.component.ComponentContext;

import eu.agno3.runtime.db.schema.orm.hibernate.HibernateOwnershipStrategy;


/**
 * @author mbechler
 * 
 */
public class HibernateOwnershipStrategyImpl implements HibernateOwnershipStrategy {

    private static final Logger log = Logger.getLogger(HibernateOwnershipStrategyImpl.class);
    private ComponentContext context;
    private Metadata config;
    private boolean indexed;

    private Map<String, Map<String, Map<String, String>>> index = new HashMap<>();


    /**
     * @param cfg
     * @param ctx
     */
    public HibernateOwnershipStrategyImpl ( Metadata cfg, ComponentContext ctx ) {
        this.config = cfg;
        this.context = ctx;
        this.indexed = false;
    }


    protected void ensureIndexed () {
        if ( !this.indexed ) {
            this.createIndex();
            this.indexed = true;
        }
    }


    protected BundleContext getBundleContext () {
        return this.context.getBundleContext();
    }


    private void indexTable ( Table tbl, String owner ) {
        if ( !tbl.isPhysicalTable() ) {
            return;
        }

        String catalogName = tbl.getCatalog();
        String schemaName = tbl.getSchema();
        String tableName = tbl.getName();

        log.debug(String.format("Map %s:%s:%s to owner %s", catalogName, schemaName, tableName, owner)); //$NON-NLS-1$

        if ( !this.index.containsKey(catalogName) ) {
            this.index.put(catalogName, new HashMap<String, Map<String, String>>());
        }

        if ( !this.index.get(catalogName).containsKey(schemaName) ) {
            this.index.get(catalogName).put(schemaName, new HashMap<String, String>());
        }

        this.index.get(catalogName).get(schemaName).put(tableName, owner);
    }


    /**
     * 
     */
    protected void createIndex () {

        java.util.Collection<PersistentClass> classIt = this.config.getEntityBindings();

        List<PersistentClass> secondPass = new LinkedList<>();
        List<Collection> collSecondPass = new LinkedList<>();
        for ( PersistentClass clazz : classIt ) {
            MetaAttribute metaAttribute = clazz.getMetaAttribute(Constants.BUNDLE_SYMBOLICNAME);
            if ( metaAttribute != null ) {
                String bundleSymbolicName = metaAttribute.getValue();
                this.indexTable(clazz.getTable(), bundleSymbolicName);
            }
            else if ( clazz.getEntityName().endsWith("_AUD") ) { //$NON-NLS-1$
                secondPass.add(clazz);
            }
            else {
                log.warn("No bundle name known for " + clazz.getEntityName()); //$NON-NLS-1$

            }
        }

        java.util.Collection<Collection> collIt = this.config.getCollectionBindings();

        for ( Collection coll : collIt ) {
            MetaAttribute metaAttribute = coll.getOwner().getMetaAttribute(Constants.BUNDLE_SYMBOLICNAME);
            if ( metaAttribute != null ) {
                String bundleSymbolicName = metaAttribute.getValue();
                this.indexTable(coll.getCollectionTable(), bundleSymbolicName);
            }
            else if ( coll.getCollectionTable().getName().endsWith("_AUD") ) { //$NON-NLS-1$
                collSecondPass.add(coll);
            }
            else {
                log.warn("No bundle name known for " + coll.getTypeName()); //$NON-NLS-1$
            }
        }

        for ( PersistentClass clazz : secondPass ) {
            Table t = clazz.getTable();
            String owner = getOwner(t.getCatalog(), t.getSchema(), t.getName().substring(0, t.getName().length() - 4));
            if ( StringUtils.isBlank(owner) ) {
                log.warn("Failed to find owner for table " + t.getName()); //$NON-NLS-1$
            }
            this.indexTable(t, owner);
        }

        for ( Collection coll : collSecondPass ) {
            Table t = coll.getCollectionTable();
            String owner = getOwner(t.getCatalog(), t.getSchema(), t.getName().substring(0, t.getName().length() - 4));
            if ( StringUtils.isBlank(owner) ) {
                log.warn("Failed to find owner for table " + t.getName()); //$NON-NLS-1$
            }
            this.indexTable(t, owner);
        }
    }


    private String getOwner ( String catalogName, String schemaName, String tableName ) {
        if ( !this.index.containsKey(catalogName) ) {
            log.warn("Catalog not found: " + catalogName); //$NON-NLS-1$
            return null;
        }

        if ( !this.index.get(catalogName).containsKey(schemaName) ) {
            log.warn("Schema not found: " + schemaName); //$NON-NLS-1$
            return null;
        }

        if ( !this.index.get(catalogName).get(schemaName).containsKey(tableName) ) {
            log.warn("Table not found: " + tableName); //$NON-NLS-1$
            return null;
        }

        return this.index.get(catalogName).get(schemaName).get(tableName);
    }


    /**
     * {@inheritDoc}
     * 
     * @see eu.agno3.runtime.db.schema.orm.hibernate.HibernateOwnershipStrategy#getOwner(org.hibernate.mapping.Table)
     */
    @Override
    public String getOwner ( Table t ) {
        this.ensureIndexed();
        return getOwner(t.getCatalog(), t.getSchema(), t.getName());
    }
}
