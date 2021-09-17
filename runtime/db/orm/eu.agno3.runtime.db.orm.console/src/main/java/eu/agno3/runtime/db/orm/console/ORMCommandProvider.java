/**
 * © 2013 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 15.08.2013 by mbechler
 */
package eu.agno3.runtime.db.orm.console;


import java.util.Iterator;

import org.apache.commons.lang3.StringUtils;
import org.apache.karaf.shell.api.action.Action;
import org.apache.karaf.shell.api.action.Argument;
import org.apache.karaf.shell.api.action.Command;
import org.apache.karaf.shell.api.action.Completion;
import org.apache.karaf.shell.api.console.Session;
import org.apache.log4j.Logger;
import org.fusesource.jansi.Ansi;
import org.hibernate.boot.Metadata;
import org.hibernate.mapping.MetaAttribute;
import org.hibernate.mapping.PersistentClass;
import org.hibernate.mapping.Table;
import org.osgi.framework.Constants;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;

import eu.agno3.runtime.console.CommandProvider;
import eu.agno3.runtime.db.orm.DynamicPersistenceProvider;
import eu.agno3.runtime.db.orm.EntityManagerConfigurationFailedException;
import eu.agno3.runtime.db.orm.hibernate.HibernateConfigurationRegistry;


/**
 * @author mbechler
 * 
 */
@Component ( service = {
    CommandProvider.class
} )
public class ORMCommandProvider implements CommandProvider {

    private static final Logger log = Logger.getLogger(ORMCommandProvider.class);

    private DynamicPersistenceProvider persistenceProvider;
    private HibernateConfigurationRegistry configurationRegistry;
    private ComponentContext context;


    @Activate
    protected void activate ( ComponentContext ctx ) {
        this.context = ctx;
    }


    @Deactivate
    protected void deactivate ( ComponentContext ctx ) {
        this.context = null;
    }


    /**
     * @return the log
     */
    static Logger getLog () {
        return log;
    }


    @Reference
    protected synchronized void setDynamicPersistenceProvider ( DynamicPersistenceProvider prov ) {
        this.persistenceProvider = prov;
    }


    protected synchronized void unsetDynamicPersistenceProvider ( DynamicPersistenceProvider prov ) {
        if ( this.persistenceProvider == prov ) {
            this.persistenceProvider = null;
        }
    }


    @Reference
    protected synchronized void setConfigurationRegistry ( HibernateConfigurationRegistry reg ) {
        this.configurationRegistry = reg;
    }


    protected synchronized void unsetConfigurationRegistry ( HibernateConfigurationRegistry reg ) {
        if ( this.configurationRegistry == reg ) {
            this.configurationRegistry = null;
        }
    }


    ComponentContext getContext () {
        return this.context;
    }


    synchronized HibernateConfigurationRegistry getConfigurationRegistry () {
        return this.configurationRegistry;
    }


    synchronized DynamicPersistenceProvider getPersistenceProvider () {
        return this.persistenceProvider;
    }

    /**
     * List registered entities
     * 
     * @author mbechler
     * 
     */
    @Command ( scope = "orm", name = "entities", description = "List registered entities" )
    public class EntitiesCommand implements Action {

        /**
         * 
         */
        private static final String UNKNOWN = "<<UNKNOWN>>"; //$NON-NLS-1$

        @Argument ( index = 0, name = "persistenceUnit", required = false )
        @Completion ( PersistenceUnitCompleter.class )
        private String pu;

        @Argument ( index = 1, name = "filter", required = false )
        private String filter;

        @org.apache.karaf.shell.api.action.lifecycle.Reference
        private Session session;


        @Override
        public Object execute () {
            if ( this.pu == null ) {
                for ( String puName : getPersistenceProvider().getPersistenceUnits() ) {
                    showPu(this.session, puName);
                }
            }
            else {
                showPu(this.session, this.pu);
            }
            return null;
        }


        /**
         * @param ci
         * @param puName
         */
        private void showPu ( Session ci, String puName ) {
            Ansi out = Ansi.ansi();

            out.a("Persistence unit ").bold().a(puName).boldOff(); //$NON-NLS-1$
            out.a(" entity classes"); //$NON-NLS-1$

            if ( this.filter != null ) {
                out.a(" matching ").a(this.filter); //$NON-NLS-1$
            }

            out.a(":").newline(); //$NON-NLS-1$

            Metadata cfg = getConfigurationRegistry().getMetadata(puName);

            for ( PersistentClass entityClass : cfg.getEntityBindings() ) {
                printEntityClass(out, entityClass);

            }

            ci.getConsole().print(out.toString());
        }


        /**
         * @param out
         * @param entityClass
         */
        private void printEntityClass ( Ansi out, PersistentClass entityClass ) {
            String exportingBundleName = getExportingBundle(entityClass);

            if ( this.filter != null && !StringUtils.containsIgnoreCase(entityClass.getClassName(), this.filter) ) {
                return;
            }

            out.fg(Ansi.Color.BLUE).a(entityClass.getClassName()).fg(Ansi.Color.DEFAULT).a(":"); //$NON-NLS-1$
            out.a("  exported by ").fgBright(Ansi.Color.BLACK).a(exportingBundleName).fg(Ansi.Color.DEFAULT).newline(); //$NON-NLS-1$

            Table t = entityClass.getTable();
            out.a("  mapped to ")//$NON-NLS-1$
                    .fgBright(Ansi.Color.GREEN).a(String.format("%s:%s:%s", //$NON-NLS-1$
                        t.getCatalog() == null ? StringUtils.EMPTY : t.getCatalog(),
                        t.getSchema() == null ? StringUtils.EMPTY : t.getSchema(),
                        t.getName())).fg(Ansi.Color.DEFAULT).newline();

            if ( entityClass.getSuperclass() != null ) {
                printSuperclasses(out, entityClass);
            }

            if ( entityClass.hasSubclasses() ) {
                printSubclasses(out, entityClass);
            }
        }


        /**
         * @param entityClass
         * @return
         */
        private String getExportingBundle ( PersistentClass entityClass ) {
            MetaAttribute metaAttribute = entityClass.getMetaAttribute(Constants.BUNDLE_SYMBOLICNAME);
            String exportingBundleName;
            if ( metaAttribute != null && metaAttribute.getValue() != null ) {
                exportingBundleName = metaAttribute.getValue().toString();
            }
            else {
                exportingBundleName = UNKNOWN;
            }
            return exportingBundleName;
        }


        /**
         * @param out
         * @param entityClass
         */
        private void printSuperclasses ( Ansi out, PersistentClass entityClass ) {
            out.a("  superclass hierarchy "); //$NON-NLS-1$
            PersistentClass superClass = entityClass.getSuperclass();

            do {
                out.a(" -> ").a(superClass.getClassName()); //$NON-NLS-1$
                superClass = superClass.getSuperclass();
            }
            while ( superClass != null );

            out.newline();
        }


        /**
         * @param out
         * @param entityClass
         */
        private void printSubclasses ( Ansi out, PersistentClass entityClass ) {
            Iterator<PersistentClass> subclassIt = entityClass.getSubclassClosureIterator();

            out.a("  subclasses:").newline(); //$NON-NLS-1$

            while ( subclassIt.hasNext() ) {
                PersistentClass subclass = subclassIt.next();
                MetaAttribute metaAttribute = subclass.getMetaAttribute(Constants.BUNDLE_SYMBOLICNAME);
                String subclassExportingBundle;
                if ( metaAttribute != null && metaAttribute.getValue() != null ) {
                    subclassExportingBundle = metaAttribute.getValue().toString();
                }
                else {
                    subclassExportingBundle = UNKNOWN;
                }

                out.a("    ").a(subclass.getClassName()); //$NON-NLS-1$
                out.a(" exported by ").fgBright(Ansi.Color.BLACK).a(subclassExportingBundle).fg(Ansi.Color.DEFAULT).newline(); //$NON-NLS-1$
            }
        }

    }

    /**
     * Refresh the entity manager factory
     * 
     * @author mbechler
     * 
     */
    @Command ( scope = "orm", name = "rebuild", description = "Refresh entity manager factory" )
    public class RefreshCommand implements Action {

        @Argument ( index = 0, name = "persistenceUnit", required = true )
        @Completion ( PersistenceUnitCompleter.class )
        private String pu;

        @org.apache.karaf.shell.api.action.lifecycle.Reference
        private Session session;


        @Override
        public Object execute () {
            try {
                getPersistenceProvider().rebuildEntityManagerFactory(this.pu);
            }
            catch ( EntityManagerConfigurationFailedException e ) {
                getLog().warn("Failed to rebuild entity manager", e); //$NON-NLS-1$
                this.session.getConsole().println(e.getMessage());
            }

            return null;
        }

    }
}
