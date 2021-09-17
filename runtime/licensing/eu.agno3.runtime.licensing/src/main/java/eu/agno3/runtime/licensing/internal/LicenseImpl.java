/**
 * © 2016 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 22.02.2016 by mbechler
 */
package eu.agno3.runtime.licensing.internal;


import java.io.Serializable;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.UUID;

import org.apache.commons.lang3.StringUtils;
import org.joda.time.DateTime;

import eu.agno3.runtime.update.License;
import eu.agno3.runtime.update.LicensingException;


/**
 * @author mbechler
 *
 */
public class LicenseImpl implements License, Serializable {

    /**
     * 
     */
    private static final long serialVersionUID = 3889703761193942951L;

    private UUID licenseId;
    private String description;
    private DateTime issueDate;
    private String licensedTo;
    private DateTime expirationDate;
    private Map<String, Long> licenseLimits = new HashMap<>();
    private Set<String> serviceTypes = new HashSet<>();
    private byte[] rawData;


    /**
     * @param props
     * @param raw
     *            raw data
     * @throws LicensingException
     */
    public void fromProperties ( Properties props, byte[] raw ) throws LicensingException {

        try {
            String lid = props.getProperty("id"); //$NON-NLS-1$
            if ( StringUtils.isBlank(lid) ) {
                throw new LicensingException("Missing id"); //$NON-NLS-1$
            }
            this.licenseId = UUID.fromString(lid);

            String iss = props.getProperty("issued"); //$NON-NLS-1$
            if ( StringUtils.isBlank(iss) ) {
                throw new LicensingException("Missing issued"); //$NON-NLS-1$
            }
            this.issueDate = DateTime.parse(iss);

            String types = props.getProperty("types"); //$NON-NLS-1$
            if ( StringUtils.isBlank(types) ) { // $NON-NLS-1$
                throw new LicensingException("Missing types"); //$NON-NLS-1$
            }

            String[] ts = StringUtils.split(types, ',');
            if ( ts.length == 0 ) {
                throw new LicensingException("Invalid types"); //$NON-NLS-1$
            }
            this.serviceTypes = new HashSet<>(Arrays.asList(ts));

            String exp = props.getProperty("expires"); //$NON-NLS-1$
            if ( StringUtils.isBlank(exp) ) {
                throw new LicensingException("Missing expires"); //$NON-NLS-1$
            }
            this.expirationDate = DateTime.parse(exp);
            this.licensedTo = props.getProperty("subject"); //$NON-NLS-1$
            this.description = props.getProperty("description"); //$NON-NLS-1$

            this.rawData = raw;

            Enumeration<?> keys = props.propertyNames();
            while ( keys.hasMoreElements() ) {
                String key = (String) keys.nextElement();
                if ( !key.startsWith("limit.") ) { //$NON-NLS-1$
                    continue;
                }

                if ( this.licenseLimits.put(key.substring(6), Long.parseLong(props.getProperty(key))) != null ) {
                    throw new LicensingException("Contains duplicate limit for " + key); //$NON-NLS-1$
                }
            }
        }
        catch ( IllegalArgumentException e ) {
            throw new LicensingException("Contains invalid data", e); //$NON-NLS-1$
        }
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.update.License#getLicenseId()
     */
    @Override
    public UUID getLicenseId () {
        return this.licenseId;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.update.License#getIssueDate()
     */
    @Override
    public DateTime getIssueDate () {
        return this.issueDate;
    }


    /**
     * @return the description
     */
    @Override
    public String getDescription () {
        return this.description;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.update.License#getLicensedTo()
     */
    @Override
    public String getLicensedTo () {
        return this.licensedTo;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.update.License#getExpirationDate()
     */
    @Override
    public DateTime getExpirationDate () {
        return this.expirationDate;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.update.License#getLicenseLimits()
     */
    @Override
    public Map<String, Long> getLicenseLimits () {
        return this.licenseLimits;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.update.License#getServiceTypes()
     */
    @Override
    public Set<String> getServiceTypes () {
        return this.serviceTypes;
    }


    /**
     * @return the rawData
     */
    @Override
    public byte[] getRawData () {
        return this.rawData;
    }
}
