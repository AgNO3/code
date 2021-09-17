/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 20.10.2015 by mbechler
 */
package eu.agno3.fileshare.model;


import javax.persistence.Embeddable;


/**
 * @author mbechler
 *
 */
@Embeddable
public interface EntityKey extends Comparable<EntityKey> {

}
