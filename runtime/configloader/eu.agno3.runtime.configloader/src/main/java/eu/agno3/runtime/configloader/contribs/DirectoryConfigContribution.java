/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 02.09.2014 by mbechler
 */
package eu.agno3.runtime.configloader.contribs;


import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Set;

import org.apache.log4j.Logger;

import eu.agno3.runtime.configloader.ConfigContribution;
import eu.agno3.runtime.configloader.FactoryContribution;


/**
 * @author mbechler
 * 
 */
public class DirectoryConfigContribution implements ConfigContribution {

    private static final Logger log = Logger.getLogger(DirectoryConfigContribution.class);

    private static final String CONF_EXTENSION = ".conf"; //$NON-NLS-1$
    private static final String DEFAULTS_FOLDER = "defaults"; //$NON-NLS-1$
    private static final String HIDDEN_PREFIX = "."; //$NON-NLS-1$

    private File baseDir;
    private int priority;
    private Map<String, Map<String, Object>> regularPropertites = new HashMap<>();
    private Map<String, Map<String, FactoryContribution>> factoryContributions = new HashMap<>();


    /**
     * @param dir
     * @param prio
     */
    public DirectoryConfigContribution ( File dir, int prio ) {
        this.baseDir = dir;
        this.priority = prio;
    }


    /**
     * @return the baseDir
     */
    public File getBaseDir () {
        return this.baseDir;
    }


    /**
     * {@inheritDoc}
     * 
     * @see eu.agno3.runtime.configloader.ConfigContribution#getPriority()
     */
    @Override
    public int getPriority () {
        return this.priority;
    }


    /**
     * {@inheritDoc}
     * 
     * @see eu.agno3.runtime.configloader.ConfigContribution#load()
     */
    @Override
    public void load () {
        if ( !this.baseDir.isDirectory() || !this.baseDir.canRead() ) {
            return;
        }

        for ( File f : this.baseDir.listFiles() ) {
            if ( isIgnored(f.getName()) ) {
                continue;
            }

            if ( f.isFile() && f.canRead() && f.getName().endsWith(CONF_EXTENSION) ) {
                this.loadPIDFile(f);
            }
            else if ( f.isDirectory() && f.canRead() && !"files".equals(f.getName()) ) { //$NON-NLS-1$
                this.loadInstanceDirectory(f);
            }
            else if ( log.isDebugEnabled() ) {
                log.debug(String.format("Ignored config directory entry %s", f.getAbsolutePath())); //$NON-NLS-1$
            }
        }
    }


    /**
     * {@inheritDoc}
     * 
     * @see eu.agno3.runtime.configloader.ConfigContribution#reload(java.lang.String)
     */
    @Override
    public void reload ( String hint ) {
        if ( log.isDebugEnabled() ) {
            log.debug("Reloading " + hint); //$NON-NLS-1$
        }
        if ( hint == null || hint.isEmpty() ) {
            this.load();
            return;
        }
        else if ( hint.indexOf('@') >= 0 ) {
            reloadInstance(hint);
            return;
        }

        File f = new File(this.baseDir, hint.concat(CONF_EXTENSION));
        if ( !f.exists() || !f.canRead() ) {
            this.regularPropertites.remove(hint);
        }
        else if ( f.isDirectory() ) {
            this.loadInstanceDirectory(f);
        }
        else {
            this.loadPIDFile(f);
        }
    }


    private void reloadInstance ( String hint ) {
        int sepPos = hint.indexOf('@');
        String factoryPid = hint.substring(0, sepPos);
        String instanceId = hint.substring(sepPos + 1);

        File factoryDir = new File(this.baseDir, factoryPid);

        if ( log.isDebugEnabled() ) {
            log.debug(String.format("Parsed factory dir %s instance %s", factoryDir, instanceId)); //$NON-NLS-1$
        }

        if ( !factoryDir.isDirectory() || !factoryDir.canRead() ) {
            if ( log.isDebugEnabled() ) {
                log.debug("Factory does not exist, remove " + hint); //$NON-NLS-1$
            }
            this.factoryContributions.remove(factoryPid);
            return;
        }

        Map<String, FactoryContribution> instances = getFactoryInstances(factoryPid);
        File f = new File(factoryDir, instanceId.concat(CONF_EXTENSION));

        if ( !f.isFile() || !f.canRead() ) {
            if ( log.isDebugEnabled() ) {
                log.debug("Instance does not exist, remove " + hint); //$NON-NLS-1$
            }
            instances.remove(instanceId);
        }
        else {
            if ( log.isDebugEnabled() ) {
                log.debug("Loading instance " + hint); //$NON-NLS-1$
            }
            this.loadInstanceFile(factoryPid, instances, f);
        }
    }


    /**
     * @param f
     */
    private void loadInstanceDirectory ( File dir ) {
        String factoryPid = dir.getName();
        Map<String, FactoryContribution> instances = getFactoryInstances(factoryPid);
        for ( File f : dir.listFiles(new ConfigFileFilter()) ) {
            loadInstanceFile(factoryPid, instances, f);
        }

    }


    private void loadInstanceFile ( String factoryPid, Map<String, FactoryContribution> instances, File f ) {
        String instanceId = getConfigBaseName(f);
        FactoryContributionImpl factoryContrib = new FactoryContributionImpl(this, factoryPid, instanceId);

        for ( Entry<Object, Object> e : loadPropertiesFile(f).entrySet() ) {
            factoryContrib.getProperties().put((String) e.getKey(), e.getValue());
        }

        instances.put(instanceId, factoryContrib);
    }


    private Map<String, FactoryContribution> getFactoryInstances ( String factoryPid ) {
        Map<String, FactoryContribution> instances = this.factoryContributions.get(factoryPid);

        if ( instances == null ) {
            instances = new HashMap<>();
            this.factoryContributions.put(factoryPid, instances);
        }
        return instances;
    }


    /**
     * @param f
     */
    private void loadPIDFile ( File f ) {
        String pid = getConfigBaseName(f);

        Map<String, Object> map = this.regularPropertites.get(pid);

        if ( map == null ) {
            map = new HashMap<>();
            this.regularPropertites.put(pid, map);
        }

        Set<String> toRemove = new HashSet<>(map.keySet());
        Properties newProperties = loadPropertiesFile(f);
        toRemove.removeAll(newProperties.keySet());

        for ( Entry<Object, Object> e : newProperties.entrySet() ) {
            map.put((String) e.getKey(), e.getValue());
        }

        for ( String rem : toRemove ) {
            map.remove(rem);
        }
    }


    private static String getConfigBaseName ( File f ) {
        return f.getName().substring(0, f.getName().length() - CONF_EXTENSION.length());
    }


    /**
     * @param f
     * @return
     */
    private static Properties loadPropertiesFile ( File f ) {
        try ( InputStream is = new FileInputStream(f) ) {
            Properties p = new Properties();
            p.load(is);
            return p;
        }
        catch ( IOException e ) {
            log.debug("Failed to load properties file", e); //$NON-NLS-1$
            log.warn("Failed to load properties file " + f.getAbsolutePath()); //$NON-NLS-1$
            return new Properties();
        }
    }


    /**
     * @param name
     * @return
     */
    private static boolean isIgnored ( String name ) {
        return name.startsWith(HIDDEN_PREFIX) || name.equals(DEFAULTS_FOLDER);
    }


    /**
     * {@inheritDoc}
     * 
     * @see eu.agno3.runtime.configloader.ConfigContribution#getRegularProperties()
     */
    @Override
    public Map<String, Map<String, Object>> getRegularProperties () {
        return this.regularPropertites;
    }


    /**
     * {@inheritDoc}
     * 
     * @see eu.agno3.runtime.configloader.ConfigContribution#getFactoryContributions()
     */
    @Override
    public Map<String, Map<String, FactoryContribution>> getFactoryContributions () {
        return this.factoryContributions;
    }

    /**
     * @author mbechler
     * 
     */
    private static final class ConfigFileFilter implements FileFilter {

        /**
         * 
         */
        public ConfigFileFilter () {}


        @Override
        public boolean accept ( File file ) {
            return file.isFile() && file.canRead() && file.getName().endsWith(CONF_EXTENSION);
        }
    }
}
