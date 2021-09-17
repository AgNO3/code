/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 29.09.2015 by mbechler
 */
package eu.agno3.runtime.update.internal;


import java.io.File;

import org.eclipse.equinox.internal.p2.engine.Profile;
import org.eclipse.equinox.internal.p2.engine.SimpleProfileRegistry;
import org.eclipse.equinox.p2.core.IProvisioningAgent;
import org.eclipse.equinox.p2.engine.IProfile;
import org.eclipse.equinox.p2.engine.IProfileRegistry;


/**
 * @author mbechler
 *
 */
@SuppressWarnings ( "restriction" )
public class ProfileRegistryWrapper extends SimpleProfileRegistry implements IProfileRegistry {

    private String installLocation;


    /**
     * @param agent
     * @param targetArea
     * @param installLocation
     */
    public ProfileRegistryWrapper ( IProvisioningAgent agent, File targetArea, String installLocation ) {
        super(agent, targetArea);
        this.installLocation = installLocation;
    }


    /**
     * {@inheritDoc}
     *
     * @see org.eclipse.equinox.p2.engine.IProfileRegistry#getProfile(java.lang.String)
     */

    @Override
    public synchronized IProfile getProfile ( String id ) {
        IProfile profile = super.getProfile(id);
        if ( profile == null ) {
            return profile;
        }
        addProperties(profile);
        return profile;
    }


    /**
     * @param profile
     */
    private void addProperties ( IProfile profile ) {
        if ( profile instanceof Profile ) {
            Profile p = (Profile) profile;
            p.setProperty(IProfile.PROP_INSTALL_FOLDER, this.installLocation);
            p.setProperty(IProfile.PROP_CACHE, this.installLocation);
            p.setChanged(false);
        }
    }


    /**
     * {@inheritDoc}
     *
     * @see org.eclipse.equinox.p2.engine.IProfileRegistry#getProfile(java.lang.String, long)
     */
    @Override
    public synchronized IProfile getProfile ( String id, long timestamp ) {
        IProfile profile = super.getProfile(id, timestamp);
        if ( profile == null ) {
            return profile;
        }
        addProperties(profile);
        return profile;
    }

}
