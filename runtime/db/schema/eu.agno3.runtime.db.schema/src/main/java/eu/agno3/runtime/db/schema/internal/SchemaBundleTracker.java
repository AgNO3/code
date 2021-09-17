/**
 * © 2013 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 30.07.2013 by mbechler
 */
package eu.agno3.runtime.db.schema.internal;


import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.FileTime;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;

import org.apache.commons.collections4.MultiValuedMap;
import org.apache.commons.collections4.multimap.HashSetValuedHashMap;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleEvent;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.util.tracker.BundleTracker;
import org.osgi.util.tracker.BundleTrackerCustomizer;

import eu.agno3.runtime.db.schema.ChangeFileProvider;
import eu.agno3.runtime.db.schema.SchemaRegistration;
import eu.agno3.runtime.util.osgi.ResourceUtil;


/**
 * @author mbechler
 * 
 */
@Component ( service = {
    ChangeFileProvider.class
}, immediate = true )
public class SchemaBundleTracker implements BundleTrackerCustomizer<Set<SchemaRegistration>>, ChangeFileProvider {

    private static final String SCHEMA_FILE_PATTERN = "*.xml"; //$NON-NLS-1$
    private static final String SCHEMA_FILE_BASE = "/schema/"; //$NON-NLS-1$

    private static final Logger log = Logger.getLogger(SchemaBundleTracker.class);

    private BundleTracker<Set<SchemaRegistration>> tracker;
    private Map<Bundle, Map<String, SchemaRegistration>> registrations = new HashMap<>();
    private MultiValuedMap<String, SchemaRegistration> dsMap = new HashSetValuedHashMap<>();

    private Path stateDir;


    @Activate
    protected void activate ( ComponentContext context ) {
        log.debug("Starting schema tracker"); //$NON-NLS-1$
        this.tracker = new BundleTracker<>(context.getBundleContext(), Bundle.RESOLVED | Bundle.ACTIVE, this);
        this.tracker.open();

        String stDir = (String) context.getProperties().get("stateDir"); //$NON-NLS-1$
        if ( stDir == null ) {
            String confArea = System.getProperty("osgi.configuration.area"); //$NON-NLS-1$
            if ( !StringUtils.isBlank(confArea) ) {
                try {
                    URI u = new URI(confArea.replace(
                        " ", //$NON-NLS-1$
                        "%20")); //$NON-NLS-1$
                    if ( "file".equals(u.getScheme()) ) { //$NON-NLS-1$
                        stDir = u.getPath() + context.getBundleContext().getBundle().getSymbolicName() + "/"; //$NON-NLS-1$ +
                    }
                }
                catch ( URISyntaxException e ) {
                    log.warn("Invalid configuration area " + confArea, e); //$NON-NLS-1$
                }
            }
        }

        if ( stDir != null ) {
            Path st = Paths.get(stDir);

            if ( !Files.exists(st) ) {
                try {
                    Files.createDirectories(st);
                }
                catch ( IOException e ) {
                    log.warn("Failed to create state directory " + st, e); //$NON-NLS-1$
                }
            }

            if ( !Files.isDirectory(st) || !Files.isWritable(st) || !Files.isReadable(st) ) {
                log.warn("Invalid state directory " + st); //$NON-NLS-1$
            }
            else {
                this.stateDir = st;
            }
        }
    }


    @Deactivate
    protected void deactivate ( ComponentContext context ) {
        this.tracker.close();
    }


    /**
     * @param b
     * @param dataSource
     * @return the registrations for the given datasource from the given bundle
     */
    public SchemaRegistration getRegistration ( Bundle b, String dataSource ) {

        if ( !this.registrations.containsKey(b) || !this.registrations.get(b).containsKey(dataSource) ) {
            throw new IllegalArgumentException("No registration available"); //$NON-NLS-1$
        }

        return this.registrations.get(b).get(dataSource);
    }


    /**
     * @param dataSource
     * @return all change files for dataSource
     */
    @Override
    public SortedMap<URL, SchemaRegistration> getChangeFiles ( String dataSource, boolean onlyModified ) {
        SortedMap<URL, SchemaRegistration> orderedChangeFiles = new TreeMap<>(new URLSchemaVersionComperator());

        if ( this.dsMap.containsKey(dataSource) ) {
            for ( SchemaRegistration reg : this.dsMap.get(dataSource) ) {
                for ( URL changeFile : reg.getChangeFiles() ) {

                    if ( onlyModified ) {
                        long lm = -1;
                        try {
                            lm = changeFile.openConnection().getLastModified();
                        }
                        catch ( Exception e ) {
                            log.debug("Failed to get last modified date for " + changeFile.getPath(), e); //$NON-NLS-1$
                        }

                        long lastApplied = getLastApplied(reg, changeFile);

                        String symbolicName = reg.getBundle().getSymbolicName();
                        if ( log.isDebugEnabled() ) {
                            log.debug(
                                String.format("%s:%s last modified is %d last applied is %d", symbolicName, changeFile.getPath(), lm, lastApplied)); //$NON-NLS-1$
                        }

                        if ( lm > 0 && lastApplied > 0 && lm < lastApplied ) {
                            if ( log.isDebugEnabled() ) {
                                log.debug("Not modified since, skipping changelog file " + changeFile.getPath()); //$NON-NLS-1$
                            }
                            continue;
                        }
                    }

                    orderedChangeFiles.put(changeFile, reg);
                }
            }
        }

        return orderedChangeFiles;
    }


    /**
     * @param reg
     * @param changeFile
     * @return
     */
    private long getLastApplied ( SchemaRegistration reg, URL changeFile ) {
        if ( this.stateDir == null ) {
            return -1;
        }

        String symbolicName = reg.getBundle().getSymbolicName();
        String dataSourceName = reg.getDataSourceName();
        String id = String.format(
            "%s-%s-%s", //$NON-NLS-1$
            symbolicName,
            dataSourceName,
            changeFile.getPath().substring(SCHEMA_FILE_BASE.length() + dataSourceName.length() + 1));

        Path p = this.stateDir.resolve(id);
        try {
            if ( Files.exists(p) ) {
                return Files.getLastModifiedTime(p).toMillis();
            }
        }
        catch ( IOException e ) {
            log.warn("Failed to get last modified time " + p, e); //$NON-NLS-1$
        }
        return -1;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.db.schema.ChangeFileProvider#trackApplied(java.lang.String, long)
     */
    @Override
    public void trackApplied ( String dataSourceName, long time ) {
        Collection<SchemaRegistration> regs = this.dsMap.get(dataSourceName);
        if ( regs == null || this.stateDir == null ) {
            return;
        }

        for ( SchemaRegistration reg : regs ) {
            for ( URL changeFile : reg.getChangeFiles() ) {
                long lm = -1;
                try {
                    lm = changeFile.openConnection().getLastModified();
                }
                catch ( Exception e ) {
                    log.debug("Failed to get last modified date for " + changeFile.getPath(), e); //$NON-NLS-1$
                }

                if ( lm == -1 || lm > time ) {
                    continue;
                }

                String id = String.format(
                    "%s-%s-%s", //$NON-NLS-1$
                    reg.getBundle().getSymbolicName(),
                    dataSourceName,
                    changeFile.getPath().substring(SCHEMA_FILE_BASE.length() + dataSourceName.length() + 1));

                Path p = this.stateDir.resolve(id);
                try {
                    if ( !Files.exists(p) ) {
                        Files.write(p, new byte[0]);
                    }
                    Files.setLastModifiedTime(p, FileTime.fromMillis(time));
                }
                catch ( Exception e ) {
                    log.warn("Failed to track last applied time " + p, e); //$NON-NLS-1$
                }
            }
        }
    }


    /**
     * {@inheritDoc}
     * 
     * @see org.osgi.util.tracker.BundleTrackerCustomizer#addingBundle(org.osgi.framework.Bundle,
     *      org.osgi.framework.BundleEvent)
     */
    @Override
    public Set<SchemaRegistration> addingBundle ( Bundle bundle, BundleEvent event ) {

        if ( log.isTraceEnabled() ) {
            log.trace("Checking " + bundle.getSymbolicName()); //$NON-NLS-1$
        }

        MultiValuedMap<String, URL> dataSourceMap = new HashSetValuedHashMap<>();
        List<URL> schemaFiles = ResourceUtil.safeFindPattern(bundle, SCHEMA_FILE_BASE, SCHEMA_FILE_PATTERN, true);

        if ( schemaFiles.isEmpty() ) {
            return new TreeSet<>();
        }

        for ( URL schemaFile : schemaFiles ) {
            if ( log.isDebugEnabled() ) {
                log.debug("Found schema file " + schemaFile); //$NON-NLS-1$
            }
            dataSourceMap.put(extractDataSource(schemaFile), schemaFile);
        }

        Map<String, SchemaRegistration> bundleRegistrationsMap = new HashMap<>();

        for ( String dataSource : dataSourceMap.keySet() ) {
            if ( log.isDebugEnabled() ) {
                log.debug(String.format("Found %d registrations for datasource %s", dataSourceMap.get(dataSource).size(), dataSource)); //$NON-NLS-1$
            }
            SortedSet<URL> orderedChangeFiles = new TreeSet<>(new URLSchemaVersionComperator());
            Collection<URL> dsRegistrations = dataSourceMap.get(dataSource);
            orderedChangeFiles.addAll(dsRegistrations);
            SchemaRegistration reg = new SchemaRegistrationImpl(bundle, dataSource, orderedChangeFiles);
            bundleRegistrationsMap.put(dataSource, reg);
            this.dsMap.put(dataSource, reg);
        }

        return new HashSet<>(bundleRegistrationsMap.values());
    }


    /**
     * @param schemaFile
     * @return
     */
    protected static String extractDataSource ( URL schemaFile ) {
        if ( schemaFile == null ) {
            return null;
        }

        File f = new File(schemaFile.getPath());
        return f.getParentFile().getName();
    }


    protected static String extractChangeVersion ( URL schemaFile ) {
        if ( schemaFile == null ) {
            return null;
        }

        File f = new File(schemaFile.getPath());
        return f.getName().replace(".xml", StringUtils.EMPTY); //$NON-NLS-1$
    }


    /**
     * {@inheritDoc}
     * 
     * @see org.osgi.util.tracker.BundleTrackerCustomizer#modifiedBundle(org.osgi.framework.Bundle,
     *      org.osgi.framework.BundleEvent, java.lang.Object)
     */
    @Override
    public void modifiedBundle ( Bundle bundle, BundleEvent event, Set<SchemaRegistration> reg ) {
        // unneeded
    }


    /**
     * {@inheritDoc}
     * 
     * @see org.osgi.util.tracker.BundleTrackerCustomizer#removedBundle(org.osgi.framework.Bundle,
     *      org.osgi.framework.BundleEvent, java.lang.Object)
     */
    @Override
    public void removedBundle ( Bundle bundle, BundleEvent event, Set<SchemaRegistration> reg ) {
        if ( this.registrations.containsKey(bundle) ) {
            for ( Entry<String, SchemaRegistration> e : this.registrations.get(bundle).entrySet() ) {
                this.dsMap.removeMapping(e.getKey(), e.getValue());
            }
            this.registrations.remove(bundle);
        }
    }

}
