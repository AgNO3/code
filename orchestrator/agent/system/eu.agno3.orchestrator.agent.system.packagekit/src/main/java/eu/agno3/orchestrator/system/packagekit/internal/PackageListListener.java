/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 05.11.2015 by mbechler
 */
package eu.agno3.orchestrator.system.packagekit.internal;


import java.util.ArrayList;
import java.util.List;

import org.freedesktop.PackageKit.Transaction.Package;

import eu.agno3.orchestrator.system.packagekit.PackageId;
import eu.agno3.orchestrator.system.packagekit.PackageKitException;


/**
 * @author mbechler
 *
 */
public class PackageListListener extends BasePackageKitListener {

    private List<PackageId> packages = new ArrayList<>();


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.system.packagekit.internal.BasePackageKitListener#havePackage(org.freedesktop.PackageKit.Transaction.Package)
     */
    @Override
    public synchronized void havePackage ( Package p ) throws PackageKitException {
        PackageId pid = PackageId.fromString(p.package_id);
        this.packages.add(pid);
    }


    /**
     * @return the packages
     */
    public List<PackageId> getPackages () {
        return this.packages;
    }
}
