/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 26.10.2015 by mbechler
 */
package eu.agno3.fileshare.service.vfs;


/**
 * @author mbechler
 * @param <TPath>
 * @param <TData>
 *
 */
public interface FilesystemWatchListener <TPath, TData> {

    /**
     * @param p
     * @param kind
     */
    void fileChanged ( TPath p, TData kind );

}
