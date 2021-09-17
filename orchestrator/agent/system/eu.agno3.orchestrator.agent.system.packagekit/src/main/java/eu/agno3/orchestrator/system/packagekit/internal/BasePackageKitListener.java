/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 02.11.2015 by mbechler
 */
package eu.agno3.orchestrator.system.packagekit.internal;


import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.freedesktop.PackageKit.Transaction.Category;
import org.freedesktop.PackageKit.Transaction.Details;
import org.freedesktop.PackageKit.Transaction.DistroUpgrade;
import org.freedesktop.PackageKit.Transaction.EulaRequired;
import org.freedesktop.PackageKit.Transaction.ItemProgress;
import org.freedesktop.PackageKit.Transaction.MediaChangeRequired;
import org.freedesktop.PackageKit.Transaction.RepoDetail;
import org.freedesktop.PackageKit.Transaction.RepoSignatureRequired;
import org.freedesktop.PackageKit.Transaction.RequireRestart;
import org.freedesktop.PackageKit.Transaction.UpdateDetail;

import eu.agno3.orchestrator.system.packagekit.PackageKitException;
import eu.agno3.orchestrator.system.packagekit.PackageKitListener;


/**
 * @author mbechler
 *
 */
public class BasePackageKitListener implements PackageKitListener {

    private static final Logger log = Logger.getLogger(BasePackageKitListener.class);

    protected List<String> collectedPackageNames = new ArrayList<>();
    protected List<String> collectedPackageIds = new ArrayList<>();
    protected List<String> collectedRepositoryIds = new ArrayList<>();


    /**
     * 
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.system.packagekit.PackageKitListener#havePackage(org.freedesktop.PackageKit.Transaction.Package)
     */
    @Override
    public void havePackage ( org.freedesktop.PackageKit.Transaction.Package p ) throws PackageKitException {
        // ignore
        String packageId = p.package_id;
        if ( StringUtils.isBlank(packageId) ) {

        }
        if ( log.isDebugEnabled() ) {
            log.debug(String.format("Unhandled package %s: %s", packageId, p.summary)); //$NON-NLS-1$
        }

        int sepPos = packageId.indexOf(';');

        if ( sepPos >= 0 ) {
            this.collectedPackageNames.add(packageId.substring(0, sepPos));
        }

        this.collectedPackageIds.add(packageId);
    }


    /**
     * @return the collectedPackageIds
     */
    public List<String> getCollectedPackageIds () {
        return this.collectedPackageIds;
    }


    /**
     * @return the collectedPackageNames
     */
    public List<String> getCollectedPackageNames () {
        return this.collectedPackageNames;
    }


    /**
     * @return the collectedRepositoryIds
     */
    public List<String> getCollectedRepositoryIds () {
        return this.collectedRepositoryIds;
    }


    /**
     * 
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.system.packagekit.PackageKitListener#havePackageDetails(org.freedesktop.PackageKit.Transaction.Details)
     */
    @Override
    public void havePackageDetails ( Details details ) throws PackageKitException {
        // ignore

        if ( log.isDebugEnabled() ) {
            log.debug(String.format("Unhandled package details: %s", details.data)); //$NON-NLS-1$
        }
    }


    /**
     * 
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.system.packagekit.PackageKitListener#haveCategory(org.freedesktop.PackageKit.Transaction.Category)
     */
    @Override
    public void haveCategory ( Category category ) throws PackageKitException {
        // ignore

        if ( log.isDebugEnabled() ) {
            log.debug(String.format("Unhandled category: %s", category.cat_id)); //$NON-NLS-1$
        }
    }


    /**
     * 
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.system.packagekit.PackageKitListener#haveRepoDetail(org.freedesktop.PackageKit.Transaction.RepoDetail)
     */
    @Override
    public void haveRepoDetail ( RepoDetail detail ) throws PackageKitException {
        this.collectedRepositoryIds.add(detail.repo_id);
        if ( log.isDebugEnabled() ) {
            log.debug(String.format("Unhandled repo detail: %s", detail.repo_id)); //$NON-NLS-1$
        }
    }


    /**
     * 
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.system.packagekit.PackageKitListener#haveUpdateDetail(org.freedesktop.PackageKit.Transaction.UpdateDetail)
     */
    @Override
    public void haveUpdateDetail ( UpdateDetail updateDetail ) throws PackageKitException {
        // ignore

        if ( log.isDebugEnabled() ) {
            log.debug(String.format("Unhandled update detail %s: %s", updateDetail.package_id, updateDetail.updates)); //$NON-NLS-1$
        }
    }


    /**
     * 
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.system.packagekit.PackageKitListener#haveDistroUpdate(org.freedesktop.PackageKit.Transaction.DistroUpgrade)
     */
    @Override
    public void haveDistroUpdate ( DistroUpgrade distroUpdate ) throws PackageKitException {
        // ignore
        if ( log.isDebugEnabled() ) {
            log.debug(String.format("Unhandled distro upgrade: %s", distroUpdate.name)); //$NON-NLS-1$
        }
    }


    /**
     * 
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.system.packagekit.PackageKitListener#haveItemProgress(org.freedesktop.PackageKit.Transaction.ItemProgress)
     */
    @Override
    public void haveItemProgress ( ItemProgress itemProgress ) throws PackageKitException {
        // ignore
        if ( log.isDebugEnabled() ) {
            log.debug(String.format(
                "Unhandled item progress %s: %s", //$NON-NLS-1$
                itemProgress.id,
                itemProgress.percentage != null ? String.valueOf(itemProgress.percentage.intValue()) : null));
        }
    }


    /**
     * 
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.system.packagekit.PackageKitListener#needRepoSignature(org.freedesktop.PackageKit.Transaction.RepoSignatureRequired)
     */
    @Override
    public void needRepoSignature ( RepoSignatureRequired repoSigReq ) throws PackageKitException {
        throw new PackageKitException("Invalid repo signature and not handled"); //$NON-NLS-1$
    }


    /**
     * 
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.system.packagekit.PackageKitListener#needEULA(org.freedesktop.PackageKit.Transaction.EulaRequired)
     */
    @Override
    public void needEULA ( EulaRequired eulaReq ) throws PackageKitException {
        throw new PackageKitException("EULA required and not handled"); //$NON-NLS-1$
    }


    /**
     * 
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.system.packagekit.PackageKitListener#needMediaChange(org.freedesktop.PackageKit.Transaction.MediaChangeRequired)
     */
    @Override
    public void needMediaChange ( MediaChangeRequired mediaChangeReq ) throws PackageKitException {
        throw new PackageKitException("Media change required and not handled"); //$NON-NLS-1$
    }


    /**
     * 
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.system.packagekit.PackageKitListener#needRestart(org.freedesktop.PackageKit.Transaction.RequireRestart)
     */
    @Override
    public void needRestart ( RequireRestart restartReq ) throws PackageKitException {
        throw new PackageKitException("Restart required and not handled"); //$NON-NLS-1$
    }

}
