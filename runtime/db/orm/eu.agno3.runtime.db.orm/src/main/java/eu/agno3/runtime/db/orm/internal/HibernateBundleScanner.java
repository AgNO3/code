/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 18.01.2014 by mbechler
 */
package eu.agno3.runtime.db.orm.internal;


import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.persistence.Embeddable;
import javax.persistence.Entity;
import javax.persistence.MappedSuperclass;
import javax.persistence.PersistenceUnit;

import org.apache.log4j.Logger;
import org.jboss.jandex.AnnotationInstance;
import org.jboss.jandex.AnnotationValue;
import org.jboss.jandex.ClassInfo;
import org.jboss.jandex.DotName;
import org.jboss.jandex.Index;
import org.jboss.jandex.Indexer;
import org.osgi.framework.Bundle;
import org.osgi.framework.namespace.PackageNamespace;
import org.osgi.framework.wiring.BundleRevision;
import org.osgi.framework.wiring.BundleWire;
import org.osgi.framework.wiring.BundleWiring;

import eu.agno3.runtime.db.orm.hibernate.HibernateConfigurationException;
import eu.agno3.runtime.util.osgi.ResourceUtil;


/**
 * @author mbechler
 * 
 */
public final class HibernateBundleScanner {

    private static final DotName EMBEDDABLE = DotName.createSimple(Embeddable.class.getName());
    private static final DotName MAPPED_SUPERCLASS = DotName.createSimple(MappedSuperclass.class.getName());
    private static final DotName ENTITY = DotName.createSimple(Entity.class.getName());

    /**
     * Base path for hibernate configuration files
     */
    private static final String HBM_CONFIG_PATH = "/orm/"; //$NON-NLS-1$
    private static final String HBM_CONFIG_PATTERN = "*.xml"; //$NON-NLS-1$

    private static final String CLASS_PATH = "/"; //$NON-NLS-1$
    private static final String CLASS_FILES = "*.class"; //$NON-NLS-1$

    /**
     * Bundle header to specify bundle contributions
     */
    public static final String DYNAMIC_HIBERNATE_HEADER = "Hibernate-Contribution"; //$NON-NLS-1$

    private static final Logger log = Logger.getLogger(HibernateBundleScanner.class);


    /**
     * 
     */
    private HibernateBundleScanner () {}


    /**
     * @param entries
     * @param puAnnot
     */
    private static void addClassToEntries ( Map<String, List<String>> entries, AnnotationInstance puAnnot ) {
        ClassInfo clazz = (ClassInfo) puAnnot.target();

        if ( clazz.annotations().containsKey(ENTITY) || clazz.annotations().containsKey(MAPPED_SUPERCLASS)
                || clazz.annotations().containsKey(EMBEDDABLE) ) {
            AnnotationValue value = puAnnot.value("unitName"); //$NON-NLS-1$
            if ( value == null ) {
                log.warn("Missing unitName parameter"); //$NON-NLS-1$
                return;
            }
            String puName = value.asString();
            String className = clazz.name().toString();

            if ( log.isTraceEnabled() ) {
                log.trace(String.format("Adding %s to PU %s", className, puName)); //$NON-NLS-1$
            }

            if ( !entries.containsKey(puName) ) {
                entries.put(puName, new ArrayList<String>());
            }

            entries.get(puName).add(className);
        }
        else {
            log.debug("PersistenceUnit annotation found but not an Entity, skip"); //$NON-NLS-1$
        }
    }


    /**
     * @param bundle
     * @return
     */
    static Map<String, List<String>> scanBundle ( Bundle bundle ) {
        List<URL> classFiles = ResourceUtil.safeFindPattern(bundle, CLASS_PATH, CLASS_FILES, true);
        Indexer annotIndexer = new Indexer();

        for ( URL classFile : classFiles ) {
            if ( log.isTraceEnabled() ) {
                log.trace("Indexing class file: " + classFile); //$NON-NLS-1$
            }
            try {
                annotIndexer.index(classFile.openStream());
            }
            catch ( IOException e ) {
                log.warn("Failed to index class file annotations:", e); //$NON-NLS-1$
            }
        }

        Map<String, List<String>> entries = new HashMap<>();

        Index annotIndex = annotIndexer.complete();
        for ( AnnotationInstance puAnnot : annotIndex.getAnnotations(DotName.createSimple(PersistenceUnit.class.getName())) ) {
            if ( ! ( puAnnot.target() instanceof ClassInfo ) ) {
                continue;
            }

            addClassToEntries(entries, puAnnot);
        }
        return entries;
    }


    /**
     * @param bundle
     * @return
     */
    static Map<String, List<URL>> getBundleMappingFiles ( Bundle bundle ) {
        Map<String, List<URL>> res = new HashMap<>();

        List<URL> entries = ResourceUtil.safeFindPattern(bundle, HBM_CONFIG_PATH, HBM_CONFIG_PATTERN, true);

        if ( entries.isEmpty() ) {
            log.trace("No mapping files found in bundle"); //$NON-NLS-1$
            return res;
        }

        for ( URL u : entries ) {
            try {

                File f = new File(u.getPath());
                String pu = f.getParentFile().getName();

                if ( log.isDebugEnabled() ) {
                    log.debug(String.format("Adding mapping file %s to persistence unit %s", u.toString(), pu)); //$NON-NLS-1$
                }

                if ( !res.containsKey(pu) ) {
                    res.put(pu, new ArrayList<URL>());
                }
                res.get(pu).add(u);
            }
            catch ( Exception e ) {
                log.warn("Failed to load mapping file:", e); //$NON-NLS-1$
            }
        }

        return res;
    }


    /**
     * @param bundle
     * @param header
     * @return
     * @throws HibernateConfigurationException
     */
    static Map<String, List<String>> parseBundleHeader ( Bundle bundle, String header ) throws HibernateConfigurationException {
        Map<String, List<String>> entries = new HashMap<>();

        if ( header != null ) {
            for ( Entry<String, List<String>> e : scanBundle(bundle).entrySet() ) {
                if ( !entries.containsKey(e.getKey()) ) {
                    entries.put(e.getKey(), new ArrayList<String>());
                }

                entries.get(e.getKey()).addAll(e.getValue());
            }
        }

        if ( header != null && !"auto".equals(header.trim()) ) { //$NON-NLS-1$
            for ( Entry<String, List<String>> e : HibernateBundleScanner.parseHeader(header).entrySet() ) {
                if ( !entries.containsKey(e.getKey()) ) {
                    entries.put(e.getKey(), new ArrayList<String>());
                }

                entries.get(e.getKey()).addAll(e.getValue());
            }
        }

        if ( !entries.isEmpty() ) {
            checkImports(bundle);
        }

        return entries;
    }


    /**
     * @param bundle
     */
    private static void checkImports ( Bundle bundle ) {

        boolean foundHibernateProxy = false;
        boolean foundHibernateAnnotations = false;
        boolean foundPersistence = false;
        boolean foundJavassist = false;

        BundleWiring wiring = bundle.adapt(BundleWiring.class);
        for ( BundleWire bundleWire : wiring.getRequiredWires(PackageNamespace.PACKAGE_NAMESPACE) ) { // $NON-NLS-1$
            String importPkg = (String) bundleWire.getCapability().getAttributes().get(BundleRevision.PACKAGE_NAMESPACE);

            if ( "org.hibernate.proxy".equals(importPkg) ) { //$NON-NLS-1$
                foundHibernateProxy = true;
            }
            else if ( "javassist.util.proxy".equals(importPkg) ) { //$NON-NLS-1$
                foundJavassist = true;
            }
            else if ( "javax.persistence".equals(importPkg) ) { //$NON-NLS-1$
                foundPersistence = true;
            }
        }

        if ( !foundHibernateProxy || !foundJavassist || !foundPersistence ) {
            log.warn(
                String.format(
                    "Bundle %s does not have required imports (org.hibernate.proxy: %s, javassist.util.proxy: %s, javax.persistence:%s", //$NON-NLS-1$
                    bundle.getSymbolicName(),
                    foundHibernateAnnotations,
                    foundJavassist,
                    foundPersistence));
        }
    }


    static Map<String, List<String>> parseHeader ( String header ) throws HibernateConfigurationException {
        Map<String, List<String>> entries = new HashMap<>();

        String[] puParts = header.split(";"); //$NON-NLS-1$

        for ( String puPart : puParts ) {
            puPart = puPart.trim();
            if ( "auto".equals(puPart) ) { //$NON-NLS-1$
                continue;
            }

            String[] tokens = puPart.split("="); //$NON-NLS-1$
            if ( tokens.length != 2 ) {
                throw new HibernateConfigurationException("Malformed dynamic hibernate header: " + header); //$NON-NLS-1$
            }

            String pu = tokens[ 0 ];
            String classSpec = tokens[ 1 ];

            entries.put(pu, Arrays.asList(classSpec.split(","))); //$NON-NLS-1$
        }

        return entries;

    }


    /**
     * @param bundle
     * @return
     */
    static String getBundleHibernateHeader ( Bundle bundle ) {
        String header = bundle.getHeaders().get(DYNAMIC_HIBERNATE_HEADER);
        log.trace(DYNAMIC_HIBERNATE_HEADER + ": " + header); //$NON-NLS-1$
        return header;
    }

}
