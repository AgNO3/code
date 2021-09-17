/**
 * © 2016 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 22.02.2016 by mbechler
 */
package eu.agno3.runtime.licensing.internal;


import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Dictionary;
import java.util.List;
import java.util.Properties;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.joda.time.DateTime;
import org.joda.time.DateTimeFieldType;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.DateTimeFormatterBuilder;
import org.osgi.framework.BundleContext;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.ConfigurationPolicy;
import org.osgi.service.component.annotations.Modified;
import org.osgi.service.component.annotations.Reference;

import eu.agno3.runtime.update.License;
import eu.agno3.runtime.update.LicenseParser;
import eu.agno3.runtime.update.LicensingException;
import eu.agno3.runtime.update.LicensingService;
import eu.agno3.runtime.util.config.ConfigUtil;


/**
 * @author mbechler
 *
 */
@Component ( service = LicensingService.class, configurationPid = "license", immediate = true, configurationPolicy = ConfigurationPolicy.REQUIRE )
public class LicensingServiceImpl implements LicensingService {

    private static final Logger log = Logger.getLogger(LicensingServiceImpl.class);

    private static final DateTimeFormatter TIMESTAMP_FORMAT = new DateTimeFormatterBuilder().appendFixedDecimal(DateTimeFieldType.year(), 4)
            .appendFixedDecimal(DateTimeFieldType.monthOfYear(), 2).appendFixedDecimal(DateTimeFieldType.dayOfMonth(), 2)
            .appendFixedDecimal(DateTimeFieldType.hourOfDay(), 2).appendFixedDecimal(DateTimeFieldType.minuteOfHour(), 2).toFormatter();

    private static final DateTimeFormatter DISPLAY_FORMAT = DateTimeFormat.forStyle("MS"); //$NON-NLS-1$

    private static final int EXPIRATION_WARNING_DAYS = 30;
    private static final int DEMO_LICENSE_TIME = 3;
    private static final String DEFAULT_LICENSE_FILE = "/etc/license.lic"; //$NON-NLS-1$

    private License license;
    private DateTime demoLicenseExpiry;
    private DateTime buildDate;
    private boolean warnedExpiration;

    private LicenseParser licenseParser;

    private Path licensePath;


    @Activate
    protected synchronized void activate ( ComponentContext ctx ) {
        if ( !checkBuildTimestamp(ctx.getBundleContext()) ) {
            System.exit(-1);
        }
        configure(ctx.getProperties());
        parseLicense(ctx);
    }


    @Modified
    protected synchronized void modified ( ComponentContext ctx ) {
        this.warnedExpiration = false;
        configure(ctx.getProperties());
        parseLicense(ctx);
    }


    /**
     * @param properties
     */
    private void configure ( Dictionary<String, Object> properties ) {
        String licfile = ConfigUtil.parseString(properties, "licenseFile", DEFAULT_LICENSE_FILE); //$NON-NLS-1$
        licfile = licfile.replace(
            "${user.home}", //$NON-NLS-1$
            System.getProperty("user.home")); //$NON-NLS-1$
        this.licensePath = Paths.get(licfile);
    }


    @Reference
    protected synchronized void setLicenseParser ( LicenseParser lp ) {
        this.licenseParser = lp;
    }


    protected synchronized void unsetLicenseParser ( LicenseParser lp ) {
        if ( this.licenseParser == lp ) {
            this.licenseParser = null;
        }
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.update.LicensingService#getLicensePath()
     */
    @Override
    public Path getLicensePath () {
        return this.licensePath;
    }


    /**
     * @return the license
     */
    @Override
    public License getLicense () {
        return this.license;
    }


    /**
     * @param bundleContext
     * 
     */
    private boolean checkBuildTimestamp ( BundleContext bndl ) {
        URL resource = bndl.getBundle().getResource("/build.properties"); //$NON-NLS-1$
        if ( resource == null ) {
            log.error("Build timestamp not found"); //$NON-NLS-1$
            return false;
        }

        try ( InputStream is = resource.openStream() ) {
            Properties p = new Properties();
            p.load(is);
            String ts = p.getProperty("timestamp"); //$NON-NLS-1$
            DateTime bd = DateTime.parse(ts, TIMESTAMP_FORMAT);
            if ( log.isDebugEnabled() ) {
                log.debug("Build date is " + bd); //$NON-NLS-1$
            }
            if ( DateTime.now().isBefore(bd) ) {
                log.error("Host time is extremely wrong"); //$NON-NLS-1$
                return false;
            }
            this.buildDate = bd;
            this.demoLicenseExpiry = this.buildDate.plusMonths(DEMO_LICENSE_TIME);
            return true;
        }
        catch ( IOException e ) {
            log.error("Cannot read build timestamp", e); //$NON-NLS-1$
        }

        return false;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.update.LicensingService#refreshLicense()
     */
    @Override
    public synchronized License refreshLicense () {
        if ( Files.exists(this.licensePath) ) {
            setNewLicense(getLicenseDataFromFile());
        }
        return getLicense();
    }


    /**
     * @param ctx
     */
    private void parseLicense ( ComponentContext ctx ) {
        setNewLicense(getLicenseData(ctx));

    }


    /**
     * @param licData
     */
    private void setNewLicense ( String licData ) {
        if ( log.isDebugEnabled() ) {
            log.debug("Loading license " + licData); //$NON-NLS-1$
        }
        if ( licData == null ) {
            this.license = null;
            return;
        }

        try {
            License l = this.licenseParser.parseLicense(licData);
            if ( DateTime.now().isBefore(l.getIssueDate()) ) {
                throw new LicensingException("License is issued in the future, fix system clock"); //$NON-NLS-1$
            }
            this.license = l;

            if ( DateTime.now().isAfter(l.getExpirationDate()) ) {
                log.error(String.format(
                    "License for %s has expired on %s", //$NON-NLS-1$
                    l.getLicensedTo(),
                    getExpirationDate().toString(DISPLAY_FORMAT)));
                this.warnedExpiration = true;
            }
            else {
                log.info(String.format(
                    "Licensed to %s, valid until %s", //$NON-NLS-1$
                    this.license.getLicensedTo(),
                    this.license.getExpirationDate().toString(DISPLAY_FORMAT)));
            }
        }
        catch ( LicensingException e ) {
            log.error("Failed to read license", e); //$NON-NLS-1$
            this.license = null;
        }
    }


    /**
     * 
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.update.LicensingService#checkValid(byte[], java.util.Set)
     */
    @Override
    public void checkValid ( byte[] data, Set<String> services ) throws LicensingException {
        License lic = this.licenseParser.parseLicense(data);
        if ( DateTime.now().isAfter(lic.getExpirationDate()) ) {
            throw new LicensingException("License is already expired"); //$NON-NLS-1$
        }
        if ( DateTime.now().isBefore(lic.getIssueDate()) ) {
            throw new LicensingException("License is issued in the future (wrong system time)"); //$NON-NLS-1$
        }
        if ( !lic.getServiceTypes().containsAll(services) ) {
            throw new LicensingException("Host has unlicensed services"); //$NON-NLS-1$
        }
    }


    /**
     * @param ctx
     * @return
     */
    protected String getLicenseData ( ComponentContext ctx ) {
        String licData = (String) ctx.getProperties().get("license"); //$NON-NLS-1$
        if ( !StringUtils.isBlank(licData) ) {
            return licData;
        }
        return getLicenseDataFromFile();
    }


    /**
     * @return
     */
    private String getLicenseDataFromFile () {
        try {
            if ( !Files.exists(this.licensePath) ) {
                log.warn(String.format(
                    "No license file found at %s, running unlicensed, valid until %s", //$NON-NLS-1$
                    this.licensePath,
                    this.demoLicenseExpiry.toString(DISPLAY_FORMAT)));
                return null;
            }
            List<String> readAllLines = Files.readAllLines(this.licensePath, StandardCharsets.US_ASCII);
            return StringUtils.join(readAllLines, '\n');
        }
        catch ( IOException e ) {
            log.error("Failed to read license file", e); //$NON-NLS-1$
            return null;
        }
    }


    @Override
    public DateTime getExpirationDate () {
        if ( this.license != null ) {
            return this.license.getExpirationDate();
        }
        return this.demoLicenseExpiry;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.update.LicensingService#isDemoLicense()
     */
    @Override
    public boolean isDemoLicense () {
        return this.license == null && DateTime.now().isBefore(this.demoLicenseExpiry);
    }


    /**
     * 
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.update.LicensingService#isLicenseValid(java.lang.String)
     */
    @Override
    public boolean isLicenseValid ( String serviceType ) {
        warnExpiration();
        License lic = this.license;
        return isDemoLicense() || licenseValidInternal(serviceType, lic);
    }


    /**
     * @param serviceType
     * @param lic
     * @return
     */
    protected boolean licenseValidInternal ( String serviceType, License lic ) {
        return lic != null && lic.getServiceTypes().contains(serviceType) && !this.isLicenseExpired();
    }


    /**
     * 
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.update.LicensingService#warnExpiration()
     */
    @Override
    public boolean warnExpiration () {
        if ( this.warnedExpiration ) {
            return true;
        }
        if ( getExpirationDate().isBefore(DateTime.now()) ) {
            log.error("License has expired on " + getExpirationDate().toString(DISPLAY_FORMAT)); //$NON-NLS-1$
            this.warnedExpiration = true;
            return true;
        }
        else if ( getExpirationDate().isBefore(DateTime.now().plusDays(EXPIRATION_WARNING_DAYS)) ) {
            log.warn("License is going to expire at " + getExpirationDate().toString(DISPLAY_FORMAT)); //$NON-NLS-1$
            this.warnedExpiration = true;
            return true;
        }

        return false;
    }


    /**
     * @return whether the license is expired
     */
    public boolean isLicenseExpired () {
        return DateTime.now().isAfter(getExpirationDate());
    }


    /**
     * 
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.update.LicensingService#checkLicenseValid(java.lang.String)
     */
    @Override
    public void checkLicenseValid ( String serviceType ) throws LicensingException {
        if ( !isLicenseValid(serviceType) ) {
            if ( isLicenseExpired() ) {
                throw new LicensingException("License is expired"); //$NON-NLS-1$
            }
            throw new LicensingException("License is not valid"); //$NON-NLS-1$
        }
    }


    /**
     * 
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.update.LicensingService#withinLicenseLimit(java.lang.String, java.lang.String, long,
     *      java.lang.Long)
     */
    @Override
    public boolean withinLicenseLimit ( String serviceType, String key, long val, Long def ) {
        if ( !isLicenseValid(serviceType) ) {
            return false;
        }
        Long limit = getLicenseLimit(key, def);
        if ( limit == null ) {
            return true;
        }
        return val <= limit;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.update.LicensingService#getLicenseLimit(java.lang.String, java.lang.String, java.lang.Long)
     */
    @Override
    public Long getLicenseLimit ( String serviceType, String key, Long def ) {
        if ( serviceType != null && !isLicenseValid(serviceType) ) {
            return def;
        }
        else if ( serviceType == null && ( isDemoLicense() || isLicenseExpired() ) ) {
            return def;
        }
        return getLicenseLimit(key, def);
    }


    /**
     * 
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.update.LicensingService#checkWithinLicenseLimit(java.lang.String, java.lang.String,
     *      long, java.lang.Long)
     */
    @Override
    public void checkWithinLicenseLimit ( String serviceType, String key, long val, Long def ) throws LicensingException {
        checkLicenseValid(serviceType);
        if ( !withinLicenseLimit(serviceType, key, val, def) ) {
            throw new LicensingException(
                String.format("License limit '%s' exceeded, allowed are %d, value is $d", key, getLicenseLimit(key, def), val)); //$NON-NLS-1$
        }
    }


    /**
     * @param key
     * @param def
     * @return the applied license limit
     */
    protected Long getLicenseLimit ( String key, Long def ) {
        if ( this.license != null ) {
            return this.license.getLicenseLimits().getOrDefault(key, def);
        }
        return def;
    }

}
