/**
 * © 2016 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: Oct 14, 2016 by mbechler
 */
package eu.agno3.orchestrator.server.webgui.licensing;


import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import javax.activation.DataHandler;
import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;
import javax.mail.util.ByteArrayDataSource;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang3.StringUtils;
import org.joda.time.DateTime;
import org.primefaces.event.FileUploadEvent;
import org.primefaces.event.SelectEvent;

import eu.agno3.orchestrator.config.model.realm.InstanceStructuralObject;
import eu.agno3.orchestrator.config.model.realm.license.LicenseInfo;
import eu.agno3.orchestrator.config.model.realm.service.LicensingService;
import eu.agno3.orchestrator.server.webgui.connector.ServerServiceProvider;
import eu.agno3.orchestrator.server.webgui.exceptions.ExceptionHandler;
import eu.agno3.orchestrator.server.webgui.structure.StructureViewContextBean;


/**
 * @author mbechler
 *
 */
@Named ( "instanceLicensingBean" )
@ViewScoped
public class InstanceLicensingBean implements Serializable {

    /**
     * 
     */
    private static final long serialVersionUID = -1533839531464476968L;

    @Inject
    private ServerServiceProvider ssp;

    @Inject
    private StructureViewContextBean structureContext;

    private boolean assignedLicenseLoaded;
    private LicenseInfo assignedLicense;

    private boolean availableLicenseLoaded;
    private List<LicenseInfo> availableLicenses;

    private String addLicenseData;

    private LicenseInfo selectedLicense;
    private UUID selectedLicenseId;

    private DateTime demoExpiration;


    public void refresh () {
        this.assignedLicenseLoaded = false;
        this.assignedLicense = null;
    }


    public LicenseInfo getAssignedLicense () {
        try {

            if ( !this.assignedLicenseLoaded ) {
                this.assignedLicenseLoaded = true;
                LicenseInfo license = this.ssp.getService(LicensingService.class).getAssignedLicense(this.structureContext.getSelectedInstance());
                if ( license != null && license.getLicenseId() != null ) {
                    this.assignedLicense = license;
                }
                else if ( license != null ) {
                    this.demoExpiration = license.getExpirationDate();
                }
            }
        }
        catch ( Exception e ) {
            ExceptionHandler.handle(e);
        }
        return this.assignedLicense;
    }


    /**
     * @return the availableLicenses
     */
    public List<LicenseInfo> getAvailableLicenses () {
        if ( !this.availableLicenseLoaded ) {
            this.availableLicenseLoaded = true;
            try {
                InstanceStructuralObject instance = this.structureContext.getSelectedInstance();
                Set<LicenseInfo> applicableLicenses = this.ssp.getService(LicensingService.class).getApplicableLicenses(instance);
                this.availableLicenses = new ArrayList<>(applicableLicenses);
            }
            catch ( Exception e ) {
                ExceptionHandler.handle(e);
                this.availableLicenses = Collections.EMPTY_LIST;
            }
        }
        return this.availableLicenses;
    }


    /**
     * @return the addLicenseData
     */
    public String getAddLicenseData () {
        return this.addLicenseData;
    }


    /**
     * @param addLicenseData
     *            the addLicenseData to set
     */
    public void setAddLicenseData ( String addLicenseData ) {
        this.addLicenseData = addLicenseData;
    }


    /**
     * @return the selectedLicense
     */
    public LicenseInfo getSelectedLicense () {
        return this.selectedLicense;
    }


    /**
     * @return the selectedLicenseId
     */
    public UUID getSelectedLicenseId () {
        return this.selectedLicenseId;
    }


    /**
     * @param selectedLicenseId
     *            the selectedLicenseId to set
     */
    public void setSelectedLicenseId ( UUID selectedLicenseId ) {
        this.selectedLicenseId = selectedLicenseId;
    }


    /**
     * @return the demoExpiration
     */
    public DateTime getDemoExpiration () {
        return this.demoExpiration;
    }


    public void licenseSelected ( SelectEvent ev ) {
        this.selectedLicense = null;
        if ( this.selectedLicenseId == null ) {
            return;
        }

        for ( LicenseInfo l : this.getAvailableLicenses() ) {
            if ( l.getLicenseId().equals(this.selectedLicenseId) ) {
                this.selectedLicense = l;
                break;
            }
        }
    }


    /**
     * 
     * @param lic
     * @return display label for license selection
     */
    public String getDisplayLabel ( LicenseInfo lic ) {
        if ( !StringUtils.isBlank(lic.getDescription()) ) {
            return String.format("%s - %s", lic.getDescription(), lic.getLicensedTo()); //$NON-NLS-1$
        }

        return lic.getLicensedTo();
    }


    /**
     * @param selectedLicense
     *            the selectedLicense to set
     */
    public void setSelectedLicense ( LicenseInfo selectedLicense ) {
        this.selectedLicense = selectedLicense;
    }


    public String addAndAssignLicense () {
        if ( StringUtils.isBlank(this.addLicenseData) ) {
            return null;
        }
        return addAndAssignLicense(Base64.decodeBase64(this.addLicenseData));
    }


    public String assignLicense () {
        if ( this.selectedLicense == null ) {
            return null;
        }
        try {
            this.ssp.getService(LicensingService.class)
                    .assignLicense(this.selectedLicense.getLicenseId(), this.structureContext.getSelectedInstance());
            this.assignedLicenseLoaded = false;
            this.demoExpiration = null;
            this.availableLicenseLoaded = false;
            this.selectedLicense = null;
            this.selectedLicenseId = null;
        }
        catch ( Exception e ) {
            ExceptionHandler.handle(e);
        }
        return null;
    }


    public String removeLicense () {
        if ( this.assignedLicense == null ) {
            return null;
        }

        try {
            this.ssp.getService(LicensingService.class)
                    .removeLicense(this.structureContext.getSelectedInstance(), this.assignedLicense.getLicenseId());
            this.assignedLicenseLoaded = false;
            this.assignedLicense = null;
            this.demoExpiration = null;
            this.availableLicenseLoaded = false;
            this.selectedLicense = null;
            this.selectedLicenseId = null;
        }
        catch ( Exception e ) {
            ExceptionHandler.handle(e);
        }
        return null;
    }


    /**
     * @param decodeBase64
     * @return
     */
    private String addAndAssignLicense ( byte[] decodeBase64 ) {
        try {
            LicenseInfo addLicense = this.ssp.getService(LicensingService.class).addLicense(
                this.structureContext.getSelectedInstance(),
                new DataHandler(new ByteArrayDataSource(decodeBase64, "application/octet-stream"))); //$NON-NLS-1$
            this.ssp.getService(LicensingService.class).assignLicense(addLicense.getLicenseId(), this.structureContext.getSelectedInstance());
            this.assignedLicenseLoaded = false;
            this.demoExpiration = null;
            this.availableLicenseLoaded = false;
        }
        catch ( Exception e ) {
            ExceptionHandler.handle(e);
        }
        return null;
    }


    public void handleFileUpload ( FileUploadEvent ev ) {
        addAndAssignLicense(Base64.decodeBase64(ev.getFile().getContents()));

    }

}
