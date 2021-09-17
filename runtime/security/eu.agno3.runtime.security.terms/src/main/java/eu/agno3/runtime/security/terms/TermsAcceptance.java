/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 25.10.2014 by mbechler
 */
package eu.agno3.runtime.security.terms;


import java.util.Calendar;
import java.util.UUID;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Index;
import javax.persistence.PersistenceUnit;
import javax.persistence.Table;

import eu.agno3.runtime.security.db.BaseAuthObject;


/**
 * @author mbechler
 *
 */
@Entity
@PersistenceUnit ( unitName = "auth" )
@Table ( name = "terms_acceptance", indexes = @Index ( columnList = "user_id, terms_id", unique = true ) )
public class TermsAcceptance extends BaseAuthObject {

    /**
     * 
     */
    private static final long serialVersionUID = -8561615320635898860L;
    private UUID userId;
    private String termsId;
    private Calendar acceptanceDate;


    /**
     * @return the userId
     */
    @Column ( name = "user_id", length = 16, nullable = false, updatable = false )
    public UUID getUserId () {
        return this.userId;
    }


    /**
     * @param userId
     *            the userId to set
     */
    public void setUserId ( UUID userId ) {
        this.userId = userId;
    }


    /**
     * @return the termsId
     */
    @Column ( name = "terms_id", nullable = false, updatable = false )
    public String getTermsId () {
        return this.termsId;
    }


    /**
     * @param termsId
     *            the termsId to set
     */
    public void setTermsId ( String termsId ) {
        this.termsId = termsId;
    }


    /**
     * @return the acceptanceDate
     */
    @Column ( name = "acceptance_date", nullable = false )
    public Calendar getAcceptanceDate () {
        return this.acceptanceDate;
    }


    /**
     * @param acceptanceDate
     *            the acceptanceDate to set
     */
    public void setAcceptanceDate ( Calendar acceptanceDate ) {
        this.acceptanceDate = acceptanceDate;
    }
}
