/**
 * © 2016 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: Oct 14, 2016 by mbechler
 */
package eu.agno3.orchestrator.config.model.realm.license;


import java.io.Serializable;
import java.util.HashSet;
import java.util.Map.Entry;
import java.util.Set;
import java.util.UUID;

import org.joda.time.DateTime;

import eu.agno3.orchestrator.config.model.realm.InstanceStructuralObject;
import eu.agno3.orchestrator.config.model.realm.StructuralObject;
import eu.agno3.runtime.update.License;


/**
 * @author mbechler
 *
 */
public class LicenseInfo implements Serializable {

    /**
     * 
     */
    private static final long serialVersionUID = -3852212782477859133L;

    private UUID licenseId;
    private DateTime issueDate;
    private String licensedTo;
    private String description;
    private DateTime expirationDate;
    private Set<LimitEntry> licenseLimits;
    private Set<String> serviceTypes;

    private StructuralObject anchor;
    private InstanceStructuralObject assignedTo;

    private byte[] data;


    /**
     * 
     * @return the license id
     */
    public UUID getLicenseId () {
        return this.licenseId;
    }


    /**
     * @param licenseId
     *            the licenseId to set
     */
    public void setLicenseId ( UUID licenseId ) {
        this.licenseId = licenseId;
    }


    /**
     * @return the description
     */
    public String getDescription () {
        return this.description;
    }


    /**
     * @param description
     *            the description to set
     */
    public void setDescription ( String description ) {
        this.description = description;
    }


    /**
     * @return the anchor
     */
    public StructuralObject getAnchor () {
        return this.anchor;
    }


    /**
     * @param anchor
     *            the anchor to set
     */
    public void setAnchor ( StructuralObject anchor ) {
        this.anchor = anchor;
    }


    /**
     * @return the assignedTo
     */
    public InstanceStructuralObject getAssignedTo () {
        return this.assignedTo;
    }


    /**
     * @param assignedTo
     *            the assignedTo to set
     */
    public void setAssignedTo ( InstanceStructuralObject assignedTo ) {
        this.assignedTo = assignedTo;
    }


    /**
     * 
     * @return the issue date
     */
    public DateTime getIssueDate () {
        return this.issueDate;
    }


    /**
     * @param issueDate
     *            the issueDate to set
     */
    public void setIssueDate ( DateTime issueDate ) {
        this.issueDate = issueDate;
    }


    /**
     * 
     * @return the license subject
     */
    public String getLicensedTo () {
        return this.licensedTo;
    }


    /**
     * @param licensedTo
     *            the licensedTo to set
     */
    public void setLicensedTo ( String licensedTo ) {
        this.licensedTo = licensedTo;
    }


    /**
     * @return the expiration date
     */
    public DateTime getExpirationDate () {
        return this.expirationDate;
    }


    /**
     * @param expirationDate
     *            the expirationDate to set
     */
    public void setExpirationDate ( DateTime expirationDate ) {
        this.expirationDate = expirationDate;
    }


    /**
     * 
     * @return the license limits
     */
    public Set<LimitEntry> getLicenseLimits () {
        return this.licenseLimits;
    }


    /**
     * @param licenseLimits
     *            the licenseLimits to set
     */
    public void setLicenseLimits ( Set<LimitEntry> licenseLimits ) {
        this.licenseLimits = licenseLimits;
    }


    /**
     * @return the service types this license applies to
     */
    public Set<String> getServiceTypes () {
        return this.serviceTypes;
    }


    /**
     * @param serviceTypes
     *            the serviceTypes to set
     */
    public void setServiceTypes ( Set<String> serviceTypes ) {
        this.serviceTypes = serviceTypes;
    }


    /**
     * @return data
     */
    public byte[] getData () {
        return this.data;
    }


    /**
     * @param data
     *            the data to set
     */
    public void setData ( byte[] data ) {
        this.data = data;
    }


    /**
     * @param lic
     * @return license info containg data of the given license
     */
    public static LicenseInfo fromLicense ( License lic ) {
        LicenseInfo li = new LicenseInfo();
        li.setLicenseId(lic.getLicenseId());
        li.setDescription(lic.getDescription());
        li.setIssueDate(lic.getIssueDate());
        li.setExpirationDate(lic.getExpirationDate());
        li.setLicensedTo(lic.getLicensedTo());
        li.setServiceTypes(lic.getServiceTypes());
        Set<LimitEntry> limits = new HashSet<>();
        for ( Entry<String, Long> entry : lic.getLicenseLimits().entrySet() ) {
            LimitEntry e = new LimitEntry();
            e.setKey(entry.getKey());
            e.setValue(entry.getValue());
        }
        li.setLicenseLimits(limits);
        li.setData(lic.getRawData());
        return li;
    }

}
