/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 31.05.2015 by mbechler
 */
package eu.agno3.runtime.net.icap;


/**
 * @author mbechler
 *
 */
public interface ICAPOptions {

    /**
     * @return the previewSize
     */
    int getPreviewSize ();


    /**
     * 
     * @return whether the options are expired
     */
    boolean isExpired ();


    /**
     * @return the istag
     */
    String getIstag ();


    /**
     * @return the allow204
     */
    boolean isAllow204 ();


    /**
     * @return the allowReqmod
     */
    boolean isAllowReqmod ();


    /**
     * @return the allowRespmod
     */
    boolean isAllowRespmod ();

}