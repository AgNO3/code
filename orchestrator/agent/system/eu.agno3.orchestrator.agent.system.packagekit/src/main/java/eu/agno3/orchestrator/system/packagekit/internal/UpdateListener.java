/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 05.11.2015 by mbechler
 */
package eu.agno3.orchestrator.system.packagekit.internal;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;
import org.freedesktop.PackageKit.Transaction.Package;
import org.freedesktop.PackageKit.Transaction.UpdateDetail;

import eu.agno3.orchestrator.system.packagekit.PackageId;
import eu.agno3.orchestrator.system.packagekit.PackageKitException;
import eu.agno3.orchestrator.system.packagekit.PackageUpdate;


/**
 * @author mbechler
 *
 */
public class UpdateListener extends BasePackageKitListener {

    private static final Logger log = Logger.getLogger(UpdateListener.class);

    private Set<PackageUpdate> updates = new HashSet<>();

    private Map<String, PackageId> installed = new HashMap<>();


    /**
     * @param installedSoftware
     */
    public UpdateListener ( Set<PackageId> installedSoftware ) {
        for ( PackageId i : installedSoftware ) {
            this.installed.put(i.getPackageName(), i);
        }
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.system.packagekit.internal.BasePackageKitListener#havePackage(org.freedesktop.PackageKit.Transaction.Package)
     */
    @Override
    public synchronized void havePackage ( Package p ) throws PackageKitException {
        if ( p.info.intValue() == 9 ) {
            log.error(String.format("Package is blocked %s", p.package_id)); //$NON-NLS-1$
            return;
        }
        this.collectedPackageIds.add(p.package_id);
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.system.packagekit.internal.BasePackageKitListener#haveUpdateDetail(org.freedesktop.PackageKit.Transaction.UpdateDetail)
     */
    @Override
    public synchronized void haveUpdateDetail ( UpdateDetail updateDetail ) throws PackageKitException {
        PackageUpdate update = new PackageUpdate();
        update.setUpdate(PackageId.fromString(updateDetail.package_id));

        if ( updateDetail.updates != null ) {
            List<PackageId> updatesPkg = new ArrayList<>();
            for ( String pid : updateDetail.updates ) {
                if ( pid.endsWith(";now") ) { //$NON-NLS-1$
                    pid = pid.substring(0, pid.length() - 4);
                }
                PackageId packageId = this.installed.get(pid);
                if ( packageId != null ) {
                    updatesPkg.add(packageId);
                }
                else {
                    log.debug("Did not find updated package " + pid); //$NON-NLS-1$
                }
            }
            update.setOld(updatesPkg);
        }
        this.updates.add(update);
    }


    /**
     * @return the updates
     */
    public Set<PackageUpdate> getUpdates () {
        return this.updates;
    }
}
