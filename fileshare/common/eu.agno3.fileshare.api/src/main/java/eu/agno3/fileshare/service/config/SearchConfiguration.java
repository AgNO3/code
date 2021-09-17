/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 29.05.2015 by mbechler
 */
package eu.agno3.fileshare.service.config;


/**
 * @author mbechler
 *
 */
public interface SearchConfiguration {

    /**
     * 
     * @return whether to allow paging (search results require post filtering, therefor paging leaks information about
     *         potentially non accessible file's in other user accounts)
     */
    public boolean isAllowPaging ();


    /**
     * 
     * @return the page size
     */
    public int getPageSize ();


    /**
     * @return whether searching is disabled
     */
    boolean isSearchDisabled ();
}
