/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 15.06.2015 by mbechler
 */
package eu.agno3.fileshare.model.audit;


import java.util.UUID;


/**
 * @author mbechler
 *
 */
@SuppressWarnings ( "javadoc" )
public abstract class EntityFileshareEvent extends BaseFileshareEvent {

    /**
     * 
     */
    public static final String TOKEN_GRANT_ID = "tokenGrantId"; //$NON-NLS-1$

    public static final String TOKEN_GRANT_TYPE = "tokenGrantType"; //$NON-NLS-1$

    /**
     * 
     */
    public static final String GRANT_ID = "grantId"; //$NON-NLS-1$

    /**
     * 
     */
    public static final String POLICY_VIOLATION = "policyViolation"; //$NON-NLS-1$

    public static final String DOWNLOAD_ACTION = "DOWNLOAD"; //$NON-NLS-1$
    public static final String DOWNLOAD_FOLDER_ACTION = "DOWNLOAD_FOLDER"; //$NON-NLS-1$
    public static final String DOWNLOAD_MULTI_ACTION = "DOWNLOAD_MULTI"; //$NON-NLS-1$
    public static final String CREATE_FOLDER_ACTION = "CREATE_FOLDER"; //$NON-NLS-1$
    public static final String CREATE_ACTION = "CREATE"; //$NON-NLS-1$
    public static final String REPLACE_ACTION = "REPLACE"; //$NON-NLS-1$
    public static final String CREATE_OR_REPLACE_ACTION = "CREATE_OR_REPLACE"; //$NON-NLS-1$
    public static final String DELETE_ACTION = "DELETE"; //$NON-NLS-1$
    public static final String DELETE_OUTER_ACTION = "DELETE_OUTER_ACTION"; //$NON-NLS-1$
    public static final String SHARE_SUBJECT_ACTION = "SHARE_SUBJECT"; //$NON-NLS-1$
    public static final String SHARE_MAIL_ACTION = "SHARE_MAIL"; //$NON-NLS-1$
    public static final String RECREATE_SHARE_LINK_ACTION = "RECREATE_SHARE_LINK"; //$NON-NLS-1$
    public static final String SHARE_LINK_ACTION = "SHARE_LINK"; //$NON-NLS-1$
    public static final String GRANT_SET_EXPIRY_ACTION = "GRANT_SET_EXPIRY"; //$NON-NLS-1$
    public static final String GRANT_SET_PERMISSIONS_ACTION = "GRANT_SET_PERMISSIONS"; //$NON-NLS-1$
    public static final String GRANT_SET_COMMENT_ACTION = "GRANT_SET_COMMENT"; //$NON-NLS-1$
    public static final String GRANT_SET_IDENTIFIER_ACTION = "GRANT_SET_IDENTIFIER"; //$NON-NLS-1$
    public static final String SET_MIMETYPE_ACTION = "SET_MIMETYPE"; //$NON-NLS-1$
    public static final String SET_EXPIRY_ACTION = "SET_EXPIRY"; //$NON-NLS-1$
    public static final String UNSET_EXPIRY_ACTION = "UNSET_EXPIRY"; //$NON-NLS-1$
    public static final String SET_ALLOW_FILEOVERRIDE_ACTION = "SET_ALLOW_FILEOVERRIDE"; //$NON-NLS-1$
    public static final String UNSET_ALLOW_FILEOVERRIDE_ACTION = "UNSET_ALLOW_FILEOVERRIDE"; //$NON-NLS-1$
    public static final String SET_SENDNOTIFICATIONS_ACTION = "SET_SENDNOTIFICATIONS"; //$NON-NLS-1$
    public static final String UNSET_SENDNOTIFICATIONS_ACTION = "UNSET_SENDNOTIFICATIONS"; //$NON-NLS-1$
    public static final String SET_SECURITY_LABEL_ACTION = "SET_SECURITY_LABEL"; //$NON-NLS-1$
    public static final String SET_SECURITY_LABEL_RECURSIVE_ACTION = "SET_SECURITY_LABEL_RECURSIVE"; //$NON-NLS-1$
    public static final String RENAME_ACTION = "RENAME"; //$NON-NLS-1$
    public static final String EXPIRE_ACTION = "EXPIRE"; //$NON-NLS-1$

    public static final String REVOKE_GRANT_ACTION = "REVOKE_GRANT_ACTION"; //$NON-NLS-1$
    public static final String GRANT_EXPIRE_ACTION = "GRANT_EXPIRE"; //$NON-NLS-1$

    /**
     * 
     */
    private static final long serialVersionUID = -8755266930451732685L;

    private UUID grantId;
    private String tokenGrantId;
    private String tokenGrantType;
    private String policyViolation;


    /**
     * 
     */
    public EntityFileshareEvent () {
        super();
    }


    /**
     * @param grantId
     */
    public void setGrantId ( UUID grantId ) {
        this.grantId = grantId;
    }


    /**
     * @return the grantId
     */
    public UUID getGrantId () {
        return this.grantId;
    }


    /**
     * @param key
     */
    public void setPolicyViolation ( String key ) {
        this.policyViolation = key;
    }


    /**
     * @return the policyViolation
     */
    public String getPolicyViolation () {
        return this.policyViolation;
    }


    /**
     * @return the tokenGrantId
     */
    public String getTokenGrantId () {
        return this.tokenGrantId;
    }


    /**
     * @param tokenGrantId
     *            the tokenGrantId to set
     */
    public void setTokenGrantId ( String tokenGrantId ) {
        this.tokenGrantId = tokenGrantId;
    }


    /**
     * @return the tokenGrantType
     */
    public String getTokenGrantType () {
        return this.tokenGrantType;
    }


    /**
     * @param tokenGrantType
     *            the tokenGrantType to set
     */
    public void setTokenGrantType ( String tokenGrantType ) {
        this.tokenGrantType = tokenGrantType;
    }
}