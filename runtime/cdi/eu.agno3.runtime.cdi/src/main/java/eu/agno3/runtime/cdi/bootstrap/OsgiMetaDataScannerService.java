/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package eu.agno3.runtime.cdi.bootstrap;


import java.io.InputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.zip.ZipEntry;

import javax.servlet.ServletContext;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.apache.webbeans.exception.WebBeansDeploymentException;
import org.apache.webbeans.spi.BDABeansXmlScanner;
import org.apache.webbeans.spi.ScannerService;
import org.apache.xbean.finder.BundleAssignableClassFinder;
import org.apache.xbean.osgi.bundle.util.BundleClassFinder;
import org.apache.xbean.osgi.bundle.util.BundleResourceFinder;
import org.apache.xbean.osgi.bundle.util.ClassDiscoveryFilter;
import org.apache.xbean.osgi.bundle.util.DiscoveryRange;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.ServiceReference;
import org.osgi.service.packageadmin.PackageAdmin;

import eu.agno3.runtime.cdi.CDICacheEntry;
import eu.agno3.runtime.cdi.CDIMetadataCache;
import eu.agno3.runtime.util.osgi.BundleUtil;
import eu.agno3.runtime.util.osgi.ResourceUtil;


/**
 * In an OSGi environment, resources will not be delivered in
 * jars or file URLs, but as 'bundle://'.
 * This {@link org.apache.webbeans.spi.ScannerService} parses for all classes
 * in such a bundle.
 */
@SuppressWarnings ( "all" )
public class OsgiMetaDataScannerService implements ScannerService {

    private static final Logger log = Logger.getLogger(OsgiMetaDataScannerService.class);

    protected ServletContext servletContext = null;
    private static final String META_INF_BEANS_XML = "META-INF/beans.xml";

    /** All classes which have to be scanned for Bean information */
    private Set<Class<?>> beanClasses = new HashSet<Class<?>>();

    /** the paths of all META-INF/beans.xml files */
    private Set<URL> beanXMLs = new HashSet<URL>();

    /** contains all the JARs we found with valid beans.xml in it */
    private Set<String> beanArchiveJarNames = new HashSet<String>();
    private Map<String, Set<String>> classAnnotations = new HashMap<String, Set<String>>();


    @Override
    public void init ( Object object ) {
        if ( object instanceof ServletContext ) {
            servletContext = (ServletContext) object;
        }
    }


    @Override
    public void release () {
        beanClasses = new HashSet<Class<?>>();
        beanXMLs = new HashSet<URL>();
        beanArchiveJarNames = new HashSet<String>();
        classAnnotations.clear();
        servletContext = null;
    }


    @Override
    public void scan () throws WebBeansDeploymentException {
        log.debug("Using OsgiMetaDataScannerService!");
        Bundle mainBundle = (Bundle) this.servletContext.getAttribute("context.bundle");

        if ( mainBundle == null ) {
            throw new WebBeansDeploymentException("No context.bundle set in servletContext");
        }

        if ( log.isTraceEnabled() ) {
            Enumeration<String> attrNames = this.servletContext.getAttributeNames();
            while ( attrNames.hasMoreElements() ) {
                log.trace("ATTR: " + attrNames.nextElement());
            }
        }

        ServiceReference<PackageAdmin> reference = mainBundle.getBundleContext().getServiceReference(PackageAdmin.class);
        try {
            PackageAdmin packageAdmin = mainBundle.getBundleContext().getService(reference);

            Set<String> blacklistPackages = getCDIBlacklistPackages(mainBundle);

            // search for all META-INF/beans.xml files
            findBeansXml(mainBundle, packageAdmin);

            // search for all classes
            findBeanClasses(mainBundle, packageAdmin, blacklistPackages, mainBundle.getHeaders().get("CDI-Filter"));

            // scan required bundles
            scanExtraBundles(packageAdmin, mainBundle, blacklistPackages);

        }
        catch ( Exception e ) {
            throw new WebBeansDeploymentException("problem while scanning OSGi bundle", e);
        }
        finally {
            mainBundle.getBundleContext().ungetService(reference);
        }

    }


    private Set<String> getCDIBlacklistPackages ( Bundle mainBundle ) {
        Set<String> blacklistPackages = new HashSet<>();
        String blacklistPackagesSpec = mainBundle.getHeaders().get("CDI-Blacklist-Packages");
        if ( !StringUtils.isBlank(blacklistPackagesSpec) ) {
            String[] blacklistPackagesSpecs = StringUtils.split(blacklistPackagesSpec, ",");
            blacklistPackages.addAll(Arrays.asList(blacklistPackagesSpecs));
        }
        return blacklistPackages;
    }


    private Map<String, String> getExtraCDIBundles ( Bundle mainBundle ) {
        Map<String, String> extraBundles = new HashMap<>();
        String extraScanBundles = mainBundle.getHeaders().get("Extra-CDI-Scan");
        if ( !StringUtils.isBlank(extraScanBundles) ) {
            for ( String extraBundleSpec : StringUtils.split(extraScanBundles, ",") ) {
                int filterSep = extraBundleSpec.indexOf('[');
                if ( filterSep >= 0 ) {
                    int filterEnd = extraBundleSpec.indexOf(']', filterSep + 1);
                    if ( filterEnd > 0 ) {
                        String bundle = extraBundleSpec.substring(0, filterSep);
                        String filter = extraBundleSpec.substring(filterSep + 1, filterEnd);
                        extraBundles.put(bundle, filter);
                    }
                    else {
                        log.warn("Invalid extra bundle header " + extraBundleSpec);
                    }
                }
                else {
                    extraBundles.put(extraBundleSpec, StringUtils.EMPTY);
                }
            }
        }
        return extraBundles;
    }


    /**
     * @param packageAdmin
     * @param blacklistPackages
     * @param extraScanBundles
     */
    private void scanExtraBundles ( PackageAdmin packageAdmin, Bundle mainBundle, Set<String> blacklistPackages ) {
        List<Bundle> requiredBundles = BundleUtil.getRequiredBundles(mainBundle);
        Map<String, String> extraBundles = getExtraCDIBundles(mainBundle);

        for ( Bundle b : requiredBundles ) {
            if ( log.isTraceEnabled() ) {
                log.trace("Scanning required bundle " + b.getSymbolicName());
            }

            Enumeration<URL> e = b.findEntries("/META-INF/", "beans.xml", false);

            if ( !extraBundles.containsKey(b.getSymbolicName()) && ( e == null || !e.hasMoreElements() ) ) {
                if ( log.isTraceEnabled() ) {
                    log.trace("Skipping bundle, no beans.xml");
                }
                continue;
            }

            while ( e != null && e.hasMoreElements() ) {
                this.beanXMLs.add(e.nextElement());
            }

            findBeanClasses(b, packageAdmin, blacklistPackages, extraBundles.get(b.getSymbolicName()));

        }
    }


    private void findBeanClasses ( Bundle b, PackageAdmin packageAdmin, Set<String> blacklistPackages, String filter ) {

        long lastMod = b.getLastModified();
        CDICacheEntry cached = getCached(b);

        if ( log.isTraceEnabled() ) {
            log.trace("Last modified " + lastMod);

            if ( cached != null ) {
                log.trace("Last cached " + cached.getLastModified());
            }
            else {
                log.trace("No cache entry");
            }
        }

        if ( cached != null && cached.getLastModified() >= lastMod ) {
            if ( log.isTraceEnabled() ) {
                log.trace("Using cached class info for " + b.getSymbolicName());
            }
            restoreCached(b, cached);
            return;
        }
        else {
            if ( log.isDebugEnabled() ) {
                log.debug("Scanning bundle " + b.getSymbolicName());
            }
            cached = new CDICacheEntry(lastMod);
            cached.getBeanClasses().clear();
            cached.getClassAnnotations().clear();
        }

        String[] filterEntries = null;;
        if ( !StringUtils.isBlank(filter) ) {
            filterEntries = StringUtils.split(filter, ':');
        }

        BundleClassFinder bundleClassFinder = new BundleAssignableClassFinder(packageAdmin, b, new Class<?>[] {
            Object.class
        }, new BundleClassDiscoveryFilter(filterEntries));

        long start = System.currentTimeMillis();
        Set<String> acceptedClassNames = bundleClassFinder.find();
        if ( log.isDebugEnabled() ) {
            log.debug(String.format("Classpath scanning took %.2f s", ( System.currentTimeMillis() - start ) / 1000.0));
        }
        start = System.currentTimeMillis();

        handleClasses(b, cached, acceptedClassNames, blacklistPackages);

        putCache(b, cached);

        if ( log.isDebugEnabled() ) {
            log.debug(String.format("Annotation collection took %.2f s", ( System.currentTimeMillis() - start ) / 1000.0));
        }
    }


    private CDIMetadataCache getCache () {
        Bundle b = FrameworkUtil.getBundle(this.getClass());

        if ( b == null ) {
            return null;
        }

        BundleContext ctx = b.getBundleContext();

        if ( ctx == null ) {
            return null;
        }

        ServiceReference<CDIMetadataCache> cache = ctx.getServiceReference(CDIMetadataCache.class);

        if ( cache == null ) {
            return null;
        }

        return ctx.getService(cache);
    }


    private void putCache ( Bundle b, CDICacheEntry cached ) {
        CDIMetadataCache cache = this.getCache();

        if ( cache == null ) {
            log.warn("No metadata cache found");
            return;
        }
        cache.putCache(b, cached);
    }


    private CDICacheEntry getCached ( Bundle b ) {
        CDIMetadataCache cache = this.getCache();

        if ( cache == null ) {
            log.warn("No metadata cache found");
            return null;
        }
        return cache.getCached(b);
    }


    private void handleClasses ( Bundle mainBundle, CDICacheEntry cached, Set<String> acceptedClassNames, Set<String> blacklistPackages ) {
        for ( String clsName : acceptedClassNames ) {
            try {
                int lastDot = clsName.lastIndexOf('.');

                if ( lastDot >= 0 ) {
                    String pkgName = clsName.substring(0, lastDot);

                    if ( blacklistPackages.contains(pkgName) ) {
                        if ( log.isDebugEnabled() ) {
                            log.debug("Ignore blacklisted class " + clsName);
                        }
                        continue;
                    }
                }

                if ( log.isTraceEnabled() ) {
                    log.trace("Found class " + clsName);
                }

                Class<?> cls = mainBundle.loadClass(clsName);
                // make sure the classes are valid, i.e. interface does not reference unloadable classes
                validateClass(cls);
                Set<String> annots = collectAnnotations(cls);
                classAnnotations.put(clsName, annots);
                beanClasses.add(cls);
                cached.getBeanClasses().add(clsName);
                cached.getClassAnnotations().put(clsName, annots);
            }
            // need to catch trowable here
            catch ( Throwable e ) {
                if ( log.isDebugEnabled() ) {
                    log.debug(String.format("Skipping class %s because of load errors: ", clsName, e.getMessage()));
                }

                if ( log.isTraceEnabled() ) {
                    log.trace("cannot load class from bundle: " + clsName, e);
                }
            }
        }
    }


    /**
     * @param cls
     */
    private void validateClass ( Class<?> cls ) {
        for ( Method m : cls.getDeclaredMethods() ) {
            m.getGenericExceptionTypes();
            m.getGenericParameterTypes();
            m.getGenericReturnType();
        }
        for ( Constructor c : cls.getDeclaredConstructors() ) {
            c.getGenericExceptionTypes();
            c.getGenericParameterTypes();
        }
        for ( Field f : cls.getDeclaredFields() ) {
            f.getGenericType();
        }
        cls.getAnnotations();

    }


    private void restoreCached ( Bundle mainBundle, CDICacheEntry cached ) {
        for ( String clsName : cached.getBeanClasses() ) {
            try {
                if ( log.isTraceEnabled() ) {
                    log.trace("Found class " + clsName);
                }
                Class<?> cls = mainBundle.loadClass(clsName);
                beanClasses.add(cls);
            }
            // need to catch trowable here
            catch ( Throwable e ) {
                if ( log.isDebugEnabled() ) {
                    log.debug(String.format("Skipping class %s because of load errors: ", clsName, e.getMessage()));
                }
                if ( log.isTraceEnabled() ) {
                    log.trace("cannot load class from bundle: " + clsName, e);
                }
            }
        }
        classAnnotations.putAll(cached.getClassAnnotations());
    }


    private Set<String> collectAnnotations ( Class<?> cls ) {
        Set<String> annotations = new HashSet<String>();

        addAnnotations(annotations, cls.getAnnotations());

        Constructor[] constructors = cls.getDeclaredConstructors();
        for ( Constructor c : constructors ) {
            addAnnotations(annotations, c.getAnnotations());
        }

        Field[] fields = cls.getDeclaredFields();
        for ( Field f : fields ) {
            addAnnotations(annotations, f.getAnnotations());
        }

        Method[] methods = cls.getDeclaredMethods();
        for ( Method m : methods ) {
            addAnnotations(annotations, m.getAnnotations());

            Annotation[][] paramsAnns = m.getParameterAnnotations();
            for ( Annotation[] pAnns : paramsAnns ) {
                addAnnotations(annotations, pAnns);
            }
        }

        return annotations;
    }


    private void addAnnotations ( Set<String> annStrings, Annotation[] annotations ) {
        for ( Annotation ann : annotations ) {
            annStrings.add(ann.getClass().getSimpleName());
        }
    }


    private void findBeansXml ( Bundle mainBundle, PackageAdmin packageAdmin ) throws Exception {
        BundleResourceFinder brfXmlJar = new BundleResourceFinder(packageAdmin, mainBundle, "", META_INF_BEANS_XML);

        BundleResourceFinder.ResourceFinderCallback rfCallback = new BundleResourceFinder.ResourceFinderCallback() {

            @Override
            public boolean foundInDirectory ( Bundle bundle, String basePath, URL url ) throws Exception {
                if ( log.isDebugEnabled() ) {
                    log.debug("adding the following beans.xml URL: " + url);
                }
                beanXMLs.add(url);

                return true;
            }


            @Override
            public boolean foundInJar ( Bundle bundle, String jarName, ZipEntry entry, InputStream in ) throws Exception {
                URL jarURL = bundle.getEntry(jarName);

                if ( log.isDebugEnabled() ) {
                    log.debug("adding the following beans.xml URL: " + jarURL.toExternalForm());
                }

                beanXMLs.add(jarURL);
                beanArchiveJarNames.add(jarName);

                return true;
            }

        };

        brfXmlJar.find(rfCallback);

        URL webBeansXml = ResourceUtil.safeFindEntry(mainBundle, "/WEB-INF/", "beans.xml");
        if ( webBeansXml != null ) {
            beanXMLs.add(webBeansXml);
        }
    }


    @Override
    public Set<URL> getBeanXmls () {
        return beanXMLs;
    }


    @Override
    public Set<Class<?>> getBeanClasses () {
        return beanClasses;
    }


    public Set<String> getAllAnnotations ( String className ) {
        return classAnnotations.get(className);
    }


    @Override
    public BDABeansXmlScanner getBDABeansXmlScanner () {
        return null;
    }


    @Override
    public boolean isBDABeansXmlScanningEnabled () {
        return false;
    }

    /**
     * @author mbechler
     * 
     */
    private final class BundleClassDiscoveryFilter implements ClassDiscoveryFilter {

        private final String[] filterEntries;


        /**
         * @param filterEntries
         */
        public BundleClassDiscoveryFilter ( String[] filterEntries ) {
            this.filterEntries = filterEntries;
        }


        @Override
        public boolean directoryDiscoveryRequired ( String directory ) {
            return true;
        }


        @Override
        public boolean jarFileDiscoveryRequired ( String jarUrl ) {
            return beanArchiveJarNames.contains(jarUrl);
        }


        @Override
        public boolean packageDiscoveryRequired ( String packageName ) {
            if ( this.filterEntries == null ) {
                return true;
            }

            for ( String filterEntry : this.filterEntries ) {
                if ( packageName.startsWith(filterEntry) ) {
                    return true;
                }
            }

            if ( log.isDebugEnabled() ) {
                log.debug("Skipping package not included in filter " + packageName);
            }
            return false;
        }


        @Override
        public boolean rangeDiscoveryRequired ( DiscoveryRange discoveryRange ) {
            return discoveryRange.equals(DiscoveryRange.BUNDLE_CLASSPATH) || discoveryRange.equals(DiscoveryRange.FRAGMENT_BUNDLES);
        }
    }

}
