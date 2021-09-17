/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 02.11.2015 by mbechler
 */
package eu.agno3.orchestrator.system.packagekit;


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


/**
 * @author mbechler
 *
 */
public interface PackageKitListener {

    /**
     * @param p
     * @throws PackageKitException
     */
    void havePackage ( org.freedesktop.PackageKit.Transaction.Package p ) throws PackageKitException;


    /**
     * @param details
     * @throws PackageKitException
     */
    void havePackageDetails ( Details details ) throws PackageKitException;


    /**
     * @param category
     * @throws PackageKitException
     */
    void haveCategory ( Category category ) throws PackageKitException;


    /**
     * @param detail
     * @throws PackageKitException
     */
    void haveRepoDetail ( RepoDetail detail ) throws PackageKitException;


    /**
     * @param updateDetail
     * @throws PackageKitException
     */
    void haveUpdateDetail ( UpdateDetail updateDetail ) throws PackageKitException;


    /**
     * @param distroUpdate
     * @throws PackageKitException
     */
    void haveDistroUpdate ( DistroUpgrade distroUpdate ) throws PackageKitException;


    /**
     * @param itemProgress
     * @throws PackageKitException
     */
    void haveItemProgress ( ItemProgress itemProgress ) throws PackageKitException;


    /**
     * @param repoSigReq
     * @throws PackageKitException
     */
    void needRepoSignature ( RepoSignatureRequired repoSigReq ) throws PackageKitException;


    /**
     * @param eulaReq
     * @throws PackageKitException
     */
    void needEULA ( EulaRequired eulaReq ) throws PackageKitException;


    /**
     * @param mediaChangeReq
     * @throws PackageKitException
     */
    void needMediaChange ( MediaChangeRequired mediaChangeReq ) throws PackageKitException;


    /**
     * @param restartReq
     * @throws PackageKitException
     */
    void needRestart ( RequireRestart restartReq ) throws PackageKitException;

}
