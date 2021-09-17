/**
 * © 2017 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: Oct 30, 2017 by mbechler
 */
package eu.agno3.runtime.net.krb5.internal;


import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.Writer;
import java.nio.channels.Channels;
import java.nio.channels.FileChannel;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Dictionary;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.joda.time.Duration;

import eu.agno3.runtime.net.krb5.ETypesUtil;
import eu.agno3.runtime.net.krb5.KerberosRealmConfig;
import eu.agno3.runtime.net.krb5.RealmType;
import eu.agno3.runtime.util.config.ConfigUtil;


/**
 * @author mbechler
 *
 */
public abstract class AbstractKerberosRealmConfigImpl implements KerberosRealmConfig {

    private static final Logger log = Logger.getLogger(AbstractKerberosRealmConfigImpl.class);

    private String adminServer;
    private String kpasswdServer;
    private List<String> kdcs;

    private String overrideLocalHostname;
    private List<String> domainMappings;
    private Map<String, String> caPaths;

    private final String realm;

    private Duration maxiumumTicketLifetime;

    private boolean rekeyServices;
    private Duration rekeyServicesInterval;

    private RealmType realmType;

    private int authFactors;
    private Set<Integer> defaultTGSEnctypes;
    private Set<Integer> defaultTicketEnctypes;
    private Set<Integer> permittedEnctypes;

    private Map<String, String> properties = Collections.EMPTY_MAP;


    /**
     * @param realm
     */
    public AbstractKerberosRealmConfigImpl ( String realm ) {
        this.realm = realm;
    }


    /**
     * 
     */
    public void reload () {
        // ignore
    }


    /**
     * @return whether the backend config has been modified
     */
    public boolean isModified () {
        return false;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.net.krb5.KerberosRealmConfig#getAdminServer()
     */
    @Override
    public String getAdminServer () {
        return this.adminServer;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.net.krb5.KerberosRealmConfig#getKpasswdServer()
     */
    @Override
    public String getKpasswdServer () {
        return this.kpasswdServer;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.net.krb5.KerberosRealmConfig#getKdcs()
     */
    @Override
    public List<String> getKdcs () {
        return this.kdcs;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.net.krb5.KerberosRealmConfig#getRealmType()
     */
    @Override
    public RealmType getRealmType () {
        return this.realmType;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.net.krb5.KerberosRealmConfig#getOverrideLocalHostname()
     */
    @Override
    public String getOverrideLocalHostname () {
        return this.overrideLocalHostname;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.net.krb5.KerberosRealmConfig#getMaxiumumTicketLifetime()
     */
    @Override
    public Duration getMaxiumumTicketLifetime () {
        return this.maxiumumTicketLifetime;
    }


    /**
     * @return the rekeyServices
     */
    public boolean isRekeyServices () {
        return this.rekeyServices;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.net.krb5.KerberosRealmConfig#getRekeyServicesInterval()
     */
    @Override
    public Duration getRekeyServicesInterval () {
        return this.rekeyServicesInterval;
    }


    /**
     * @return the numer of auth factors to assume when an user is authenticated against this realm
     */
    @Override
    public int getAuthFactors () {
        return this.authFactors;
    }


    /**
     * @return realm property configuration
     */
    public Map<String, String> getProperties () {
        return Collections.unmodifiableMap(this.properties);
    }


    protected void loadProperties ( Map<String, String> props ) {
        this.properties = props;
        String realmTypeAttr = props.get("realmType"); //$NON-NLS-1$
        this.realmType = RealmType.UNSPECIFIED;
        if ( !StringUtils.isBlank(realmTypeAttr) ) {
            try {
                this.realmType = RealmType.valueOf(realmTypeAttr);
            }
            catch ( IllegalArgumentException e ) {
                log.warn("Failed to parse realm type " + realmTypeAttr, e); //$NON-NLS-1$
            }
        }

        String localHostname = props.get("localHostname"); //$NON-NLS-1$
        if ( !StringUtils.isBlank(localHostname) ) {
            this.overrideLocalHostname = localHostname.trim();
        }

        String authFactorsSpec = props.get("authFactors"); //$NON-NLS-1$
        if ( !StringUtils.isBlank(authFactorsSpec) ) {
            this.authFactors = Integer.parseInt(authFactorsSpec);
        }
        else {
            this.authFactors = 1;
        }

        String adminServerAttr = props.get("adminServer"); //$NON-NLS-1$
        if ( !StringUtils.isBlank(adminServerAttr) ) {
            this.adminServer = adminServerAttr.trim();
        }
        String kpasswdServerAttr = props.get("kpasswdServer"); //$NON-NLS-1$
        if ( !StringUtils.isBlank(kpasswdServerAttr) ) {
            this.kpasswdServer = kpasswdServerAttr.trim();
        }

        String kdcsAttr = props.get("kdcs"); //$NON-NLS-1$
        if ( !StringUtils.isBlank(kdcsAttr) ) {
            String kdcSpecs[] = StringUtils.split(kdcsAttr, ","); //$NON-NLS-1$
            this.kdcs = new ArrayList<>();
            for ( String kdcSpec : kdcSpecs ) {
                this.kdcs.add(kdcSpec.trim());
            }
        }

        String maxTicketLifetimeAttr = props.get("maximumTicketLifetime"); //$NON-NLS-1$
        if ( !StringUtils.isBlank(maxTicketLifetimeAttr) ) {
            this.maxiumumTicketLifetime = Duration.parse(maxTicketLifetimeAttr);
        }
        else {
            this.maxiumumTicketLifetime = Duration.standardDays(1);
        }

        String rekeyServicesAttr = props.get("rekeyServices"); //$NON-NLS-1$
        this.rekeyServices = !StringUtils.isBlank(rekeyServicesAttr) && Boolean.parseBoolean(rekeyServicesAttr.trim());

        String rekeyServicesIntervalAttr = props.get("rekeyServicesInterval"); //$NON-NLS-1$
        if ( !StringUtils.isBlank(rekeyServicesIntervalAttr) ) {
            this.rekeyServicesInterval = Duration.parse(rekeyServicesIntervalAttr.trim());
        }
        else {
            this.rekeyServicesInterval = Duration.standardDays(30);
        }

        List<String> maps = getDomainMappings(props);
        if ( !maps.isEmpty() ) {
            this.domainMappings = maps;
        }
        else {
            this.domainMappings = null;
        }

        loadCAPaths(props);
        loadETypes(props);
    }


    /**
     * @param props
     */
    private void loadCAPaths ( Map<String, String> props ) {
        String caPathsAttrs = props.get("caPaths"); //$NON-NLS-1$
        if ( !StringUtils.isBlank(caPathsAttrs) ) {
            String caPathSpecs[] = StringUtils.split(caPathsAttrs, ","); //$NON-NLS-1$
            this.caPaths = new LinkedHashMap<>();
            for ( String caPathSpec : caPathSpecs ) {
                String[] split = StringUtils.split(caPathSpec, '=');
                if ( split == null || split.length != 2 ) {
                    log.warn("Not a valid CA path spec " + caPathSpec); //$NON-NLS-1$
                    continue;
                }

                if ( this.caPaths.put(split[ 0 ].trim(), split[ 1 ].trim()) != null ) {
                    log.warn("Duplicate ca path specification for realm " + split[ 0 ].trim()); //$NON-NLS-1$
                }
            }
        }
    }


    /**
     * @param props
     */
    private void loadETypes ( Map<String, String> props ) {
        Dictionary<String, Object> cfg = new Hashtable<>(props.entrySet().stream().collect(Collectors.toMap(e -> {
            return e.getKey();
        }, x -> x.getValue())));

        this.defaultTGSEnctypes = parseEnctypes(cfg, "defaultTGSEnctypes"); //$NON-NLS-1$
        this.defaultTicketEnctypes = parseEnctypes(cfg, "defaultTicketEnctypes"); //$NON-NLS-1$
        this.permittedEnctypes = parseEnctypes(cfg, "permittedEnctypes"); //$NON-NLS-1$

        if ( this.permittedEnctypes == null ) {
            String securityLevel = ConfigUtil.parseString(cfg, "securityLevel", null); //$NON-NLS-1$
            this.permittedEnctypes = toETypes(securityLevel);
        }

        if ( this.permittedEnctypes != null ) {
            if ( this.defaultTicketEnctypes == null ) {
                this.defaultTicketEnctypes = this.permittedEnctypes;
            }
            if ( this.defaultTGSEnctypes == null ) {
                this.defaultTGSEnctypes = this.permittedEnctypes;
            }
        }
    }


    protected void readProperties ( Path file ) throws FileNotFoundException, IOException {
        Properties props = new Properties();

        try ( FileChannel fc = FileChannel.open(file, StandardOpenOption.READ);
              InputStream is = Channels.newInputStream(fc) ) {
            props.load(is);
        }

        Map<String, String> map = new LinkedHashMap<>();
        for ( final String name : props.stringPropertyNames() )
            map.put(name, props.getProperty(name));
        loadProperties(map);
    }


    /**
     * @param securityLevel
     * @return
     */
    private static Set<Integer> toETypes ( String securityLevel ) {
        if ( securityLevel == null ) {
            return ETypesUtil.DEFAULT_ETYPES;
        }

        switch ( securityLevel ) {
        case "HIGH256": //$NON-NLS-1$
            return ETypesUtil.DEFAULT_ETYPES_256;
        case "HIGH": //$NON-NLS-1$
            return ETypesUtil.DEFAULT_ETYPES;
        case "LEGACY": //$NON-NLS-1$
            return ETypesUtil.DEFAULT_LEGACY_ETYPES;
        case "WEAK": //$NON-NLS-1$
            return ETypesUtil.DEFAULT_WEAK_ETYPES;
        default:
            return null;
        }
    }


    /**
     * @param cfg
     * @param string
     * @return
     */
    private static Set<Integer> parseEnctypes ( Dictionary<String, Object> cfg, String prop ) {

        Set<String> etypes = ConfigUtil.parseStringSet(cfg, prop, null);
        if ( etypes == null || etypes.isEmpty() ) {
            return null;
        }

        Set<Integer> etypeVals = new HashSet<>();
        for ( String etype : etypes ) {
            Integer etypeVal = ETypesUtil.eTypeFromMITString(etype.trim());
            if ( etypeVal != null ) {
                etypeVals.add(etypeVal);
            }
        }
        return etypeVals;
    }


    /**
     * @param props
     * @return
     */
    protected List<String> getDomainMappings ( Map<String, String> props ) {
        List<String> maps = new ArrayList<>();
        String domainMapAttr = props.get("mapDomain"); //$NON-NLS-1$
        if ( !StringUtils.isBlank(domainMapAttr) ) {
            String domainSpecs[] = StringUtils.split(domainMapAttr, ","); //$NON-NLS-1$
            for ( String domainSpec : domainSpecs ) {
                maps.add(domainSpec.trim());
            }
        }
        return maps;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.net.krb5.KerberosRealmConfig#getRealm()
     */
    @Override
    public String getRealm () {
        return this.realm;
    }


    /**
     * @param w
     * @throws IOException
     */
    public void writeRealmSection ( Writer w ) throws IOException {

        if ( this.adminServer == null && this.kpasswdServer == null && this.kdcs == null ) {
            return;
        }

        writeLine(w, String.format("  %s = {", this.getRealm())); //$NON-NLS-1$
        if ( !StringUtils.isBlank(this.adminServer) ) {
            writeLine(w, "    admin_server = " + this.adminServer); //$NON-NLS-1$
        }
        if ( !StringUtils.isBlank(this.kpasswdServer) ) {
            writeLine(w, "    kpasswd_server = " + this.kpasswdServer); //$NON-NLS-1$
        }
        if ( this.kdcs != null ) {
            for ( String kdc : this.kdcs ) {
                writeLine(w, "    kdc = " + kdc); //$NON-NLS-1$
            }
        }
        writeLine(w, "}"); //$NON-NLS-1$
    }


    /**
     * @return the defaultTGSEnctypes
     */
    public Set<Integer> getDefaultTGSEnctypes () {
        return this.defaultTGSEnctypes;
    }


    /**
     * @return the defaultTicketEnctypes
     */
    public Set<Integer> getDefaultTicketEnctypes () {
        return this.defaultTicketEnctypes;
    }


    /**
     * @return the permittedEnctypes
     */
    public Set<Integer> getPermittedEnctypes () {
        return this.permittedEnctypes;
    }


    /**
     * 
     * @param w
     * @throws IOException
     */
    public void writeDomainMapSection ( Writer w ) throws IOException {
        if ( this.domainMappings != null ) {
            for ( String mappedDomain : this.domainMappings ) {
                writeLine(w, String.format("  %s = %s", mappedDomain, this.getRealm())); //$NON-NLS-1$
            }
        }
    }


    /**
     * 
     * @param w
     * @throws IOException
     */
    public void writeCAPathSection ( Writer w ) throws IOException {
        if ( this.caPaths == null || this.caPaths.isEmpty() ) {
            return;
        }
        writeLine(w, String.format("  %s = {", this.getRealm())); //$NON-NLS-1$
        if ( this.caPaths != null ) {
            for ( Entry<String, String> caPath : this.caPaths.entrySet() ) {
                writeLine(w, String.format("  %s = %s", caPath.getKey(), caPath.getValue())); //$NON-NLS-1$
            }
        }
        writeLine(w, "}"); //$NON-NLS-1$
    }


    private static final void writeLine ( Writer w, String data ) throws IOException {
        w.write(data);
        w.write("\n"); //$NON-NLS-1$
    }


    /**
     * {@inheritDoc}
     *
     * @see java.lang.Object#hashCode()
     */
    // + GENERATED
    @Override
    public int hashCode () {
        final int prime = 31;
        int result = 1;
        result = prime * result + ( ( this.realm == null ) ? 0 : this.realm.hashCode() );
        return result;
    }


    // - GENERATED

    /**
     * {@inheritDoc}
     *
     * @see java.lang.Object#equals(java.lang.Object)
     */
    // + GENERATED
    @Override
    public boolean equals ( Object obj ) {
        if ( this == obj )
            return true;
        if ( obj == null )
            return false;
        if ( getClass() != obj.getClass() )
            return false;
        AbstractKerberosRealmConfigImpl other = (AbstractKerberosRealmConfigImpl) obj;
        if ( this.realm == null ) {
            if ( other.realm != null )
                return false;
        }
        else if ( !this.realm.equals(other.realm) )
            return false;
        return true;
    }

    // - GENERATED

}
