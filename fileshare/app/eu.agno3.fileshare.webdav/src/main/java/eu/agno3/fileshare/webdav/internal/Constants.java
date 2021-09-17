/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 11.06.2015 by mbechler
 */
package eu.agno3.fileshare.webdav.internal;


import java.math.BigInteger;

import javax.xml.namespace.QName;

import org.apache.jackrabbit.webdav.DavConstants;
import org.apache.jackrabbit.webdav.property.DavPropertyName;
import org.apache.jackrabbit.webdav.property.ResourceType;
import org.apache.jackrabbit.webdav.xml.Namespace;


/**
 * @author mbechler
 *
 */
public final class Constants {

    /**
     * 
     */
    private Constants () {}

    /**
     * DAV Tree layout
     */
    public static final String LAYOUT = "layout"; //$NON-NLS-1$

    /**
     * User agent identifier for native clients
     */
    public static final String FS_UA_PREFIX = "fileShield Client"; //$NON-NLS-1$

    /**
     * 
     */
    public static final DavPropertyName QUOTA_USED = DavPropertyName.create("quota-used-bytes"); //$NON-NLS-1$
    /**
     * 
     */
    public static final DavPropertyName QUOTA_AVAIL = DavPropertyName.create("quota-available-bytes"); //$NON-NLS-1$

    /**
     * 
     */
    public static final Namespace OC_NS = Namespace.getNamespace(
        "oc", //$NON-NLS-1$
        "http://owncloud.org/ns"); //$NON-NLS-1$

    /**
     * Namespace for custom webdav elements
     */
    public static final Namespace AGNO3_NS = Namespace.getNamespace(
        "A", //$NON-NLS-1$
        "http://agno3.eu/ns/dav/"); //$NON-NLS-1$

    /**
     * 
     */
    public static final QName GRANT_ID = new QName(AGNO3_NS.getURI(), "grant-id"); //$NON-NLS-1$

    /**
     * 
     */
    public static final QName EXPIRES = new QName(AGNO3_NS.getURI(), "expires"); //$NON-NLS-1$

    /**
     * 
     */
    public static final QName NOTIFY = new QName(AGNO3_NS.getURI(), "notify"); //$NON-NLS-1$

    /**
     * 
     */
    public static final QName TOK_ID = new QName(AGNO3_NS.getURI(), "token-id"); //$NON-NLS-1$

    /**
     * 
     */
    public static final QName MODIFY = new QName(AGNO3_NS.getURI(), "modify"); //$NON-NLS-1$

    /**
     * 
     */
    public static final QName ADD = new QName(AGNO3_NS.getURI(), "add"); //$NON-NLS-1$

    /**
     * 
     */
    public static final DavPropertyName TOK_ID_PROP = DavPropertyName.create("token-id", AGNO3_NS); //$NON-NLS-1$

    /**
     * 
     */
    public static final QName TOK_COMMENT = new QName(AGNO3_NS.getURI(), "token-comment"); //$NON-NLS-1$

    /**
     * 
     */
    public static final DavPropertyName TOK_COMMENT_PROP = DavPropertyName.create("token-comment", AGNO3_NS); //$NON-NLS-1$

    /**
     * 
     */
    public static final QName TOKEN_PASSWORD = new QName(AGNO3_NS.getURI(), "token-password"); //$NON-NLS-1$

    /**
     * 
     */
    public static final QName MAIL_SUBJECT = new QName(AGNO3_NS.getURI(), "mail-subject"); //$NON-NLS-1$

    /**
     * 
     */
    public static final QName MAIL_TEXT = new QName(AGNO3_NS.getURI(), "mail-text"); //$NON-NLS-1$

    /**
     * 
     */
    public static final QName MAIL_RESEND = new QName(AGNO3_NS.getURI(), "mail-resend"); //$NON-NLS-1$

    /**
     * Principal resource type per webdav spec
     */
    public static final int PRINCIPAL_RESOURCE_TYPE = ResourceType.registerResourceType("principal", DavConstants.NAMESPACE); //$NON-NLS-1$

    /**
     * Resource type for user principals
     */
    public static final int USER_RESOURCE_TYPE = ResourceType.registerResourceType("user", AGNO3_NS); //$NON-NLS-1$

    /**
     * Resource type for group principals
     */
    public static final int GROUP_RESOURCE_TYPE = ResourceType.registerResourceType("group", AGNO3_NS); //$NON-NLS-1$

    /**
     * Resource type for mail principals
     */
    public static final int MAIL_RESOURCE_TYPE = ResourceType.registerResourceType("mail", AGNO3_NS); //$NON-NLS-1$

    /**
     * Resource type for token principals
     */
    public static final int TOKEN_RESOURCE_TYPE = ResourceType.registerResourceType("token", AGNO3_NS); //$NON-NLS-1$

    /**
     * Resource type for non-entity nodes
     */
    public static final int VIRTUAL_RESOURCE_TYPE = ResourceType.registerResourceType("virtual", AGNO3_NS); //$NON-NLS-1$

    /**
     * 
     */
    public static final int GROUPS_ROOT_RESOURCE_TYPE = ResourceType.registerResourceType("groups-root", AGNO3_NS); //$NON-NLS-1$

    /**
     * 
     */
    public static final int SHARED_ROOT_RESOURCE_TYPE = ResourceType.registerResourceType("shares-root", AGNO3_NS); //$NON-NLS-1$

    /**
     * 
     */
    public static final int SUBJECT_SHARES_ROOT_RESOURCE_TYPE = ResourceType.registerResourceType("subj-shares-root", AGNO3_NS); //$NON-NLS-1$

    /**
     * 
     */
    public static final int ROOT_RESOURCE_TYPE = ResourceType.registerResourceType("root", AGNO3_NS); //$NON-NLS-1$

    /**
     * 
     */
    public static final int USER_ROOT_RESOURCE_TYPE = ResourceType.registerResourceType("user-root", AGNO3_NS); //$NON-NLS-1$

    /**
     * 
     */
    public static final int GROUP_ROOT_RESOURCE_TYPE = ResourceType.registerResourceType("group-root", AGNO3_NS); //$NON-NLS-1$

    /**
     * 
     */
    public static final int NOT_SHAREABLE_RESOURCE_TYPE = ResourceType.registerResourceType("not-shareable", AGNO3_NS); //$NON-NLS-1$

    /**
     * 
     */
    public static final int TRANSFER_RESOURCE_TYPE = ResourceType.registerResourceType("transfer", AGNO3_NS); //$NON-NLS-1$

    /**
     * 
     */
    public static final DavPropertyName SUBJECT_ID = DavPropertyName.create("subject-id", AGNO3_NS); //$NON-NLS-1$

    /**
     * 
     */
    public static final DavPropertyName SUBJECT_NAME = DavPropertyName.create("subject-name", AGNO3_NS); //$NON-NLS-1$

    /**
     * 
     */
    public static final DavPropertyName SUBJECT_REALM = DavPropertyName.create("subject-realm", AGNO3_NS); //$NON-NLS-1$

    /**
     * 
     */
    public static final DavPropertyName MAIL = DavPropertyName.create("mail", AGNO3_NS); //$NON-NLS-1$

    /**
     * 
     */
    public static final DavPropertyName SUBJECT_NAME_SOURCE = DavPropertyName.create("name-source", AGNO3_NS); //$NON-NLS-1$

    /**
     * 
     */
    public static final DavPropertyName OC_ID = DavPropertyName.create("id", OC_NS); //$NON-NLS-1$

    /**
     * 
     */
    public static final DavPropertyName OC_PERMISSIONS = DavPropertyName.create("permissions", OC_NS); //$NON-NLS-1$

    /**
     * 
     */
    public static final DavPropertyName OC_SIZE = DavPropertyName.create("size", OC_NS); //$NON-NLS-1$

    /**
     * 
     */
    public static final DavPropertyName TOKEN_URL = DavPropertyName.create("token-url", AGNO3_NS); //$NON-NLS-1$

    /**
     * 
     */
    public static final DavPropertyName TOK_PASSWORD_PROP = DavPropertyName.create("token-password", AGNO3_NS); //$NON-NLS-1$

    /**
     * 
     */
    public static final DavPropertyName USER_FULLNAME = DavPropertyName.create("user-full-name", AGNO3_NS); //$NON-NLS-1$

    /**
     * 
     */
    public static final DavPropertyName USER_MAIL = DavPropertyName.create("user-mail", AGNO3_NS); //$NON-NLS-1$

    /**
     * 
     */
    public static final DavPropertyName USER_JOB_TITLE = DavPropertyName.create("user-job-title", AGNO3_NS); //$NON-NLS-1$

    /**
     * 
     */
    public static final DavPropertyName USER_ORGANIZATION = DavPropertyName.create("user-organization", AGNO3_NS); //$NON-NLS-1$

    /**
     * 
     */
    public static final DavPropertyName USER_ORGANIZATION_UNIT = DavPropertyName.create("user-organization-unit", AGNO3_NS); //$NON-NLS-1$

    /**
     * 
     */
    public static final DavPropertyName TRUST_LEVEL = DavPropertyName.create("trust-level", AGNO3_NS); //$NON-NLS-1$

    /**
     * 
     */
    public static final DavPropertyName SECURITY_LABEL = DavPropertyName.create("security-label", AGNO3_NS); //$NON-NLS-1$

    /**
     * 
     */
    public static final DavPropertyName CREATOR = DavPropertyName.create("creator", AGNO3_NS); //$NON-NLS-1$

    /**
     * 
     */
    public static final DavPropertyName LAST_MODIFIER = DavPropertyName.create("last-modifier", AGNO3_NS); //$NON-NLS-1$

    /**
     * 
     */
    public static final DavPropertyName LAST_MODIFIED = DavPropertyName.create("last-modified", AGNO3_NS); //$NON-NLS-1$

    /**
     * 
     */
    public static final DavPropertyName PERMISSIONS = DavPropertyName.create("permissions", AGNO3_NS); //$NON-NLS-1$

    /**
     * 
     */
    public static final DavPropertyName EXPIRES_PROP = DavPropertyName.create(EXPIRES.getLocalPart(), AGNO3_NS);

    /**
     * 
     */
    public static final DavPropertyName PARENT_ID = DavPropertyName.create("parent-id", AGNO3_NS); //$NON-NLS-1$

    /**
     * 
     */
    public static final DavPropertyName CHUNK_SIZE = DavPropertyName.create("chunk-size", AGNO3_NS); //$NON-NLS-1$

    /**
     * 
     */
    public static final DavPropertyName MISSING_CHUNKS = DavPropertyName.create("missing-chunks", AGNO3_NS); //$NON-NLS-1$

    /**
     * 
     */
    public static final DavPropertyName GRANT_SETTINGS = DavPropertyName.create("grant-settings", AGNO3_NS); //$NON-NLS-1$

    /**
     * 
     */
    public static final DavPropertyName ALLOWED_SECURITY_LEVELS = DavPropertyName.create("allowed-security-levels", AGNO3_NS); //$NON-NLS-1$

    /**
     * 
     */
    public static final DavPropertyName SEND_NOTIFICATIONS = DavPropertyName.create("send-notifications", AGNO3_NS); //$NON-NLS-1$

    /**
     * 
     */
    public static final DavPropertyName ALLOW_FILE_OVERWRITE = DavPropertyName.create("allow-file-overwrite", AGNO3_NS); //$NON-NLS-1$

    /**
     * 
     */
    public static final DavPropertyName GRANT_ID_PROP = DavPropertyName.create("grant-id", AGNO3_NS); //$NON-NLS-1$

    /**
     * 
     */
    public static final DavPropertyName GRANT_DETAILS = DavPropertyName.create("grant-details", AGNO3_NS); //$NON-NLS-1$

    /**
     * Owncloud ETAG overriding the default ETAG for their clients
     * 
     * Used to provide recursive changes for collections, which are not legitimate for default ETAGs
     */
    public static final String OC_ETAG = "OC-ETag"; //$NON-NLS-1$

    /**
     * Methods generally supported by this implementation
     */
    public static final String[] BASE_METHODS = new String[] {
        "OPTIONS", //$NON-NLS-1$
        "GET", //$NON-NLS-1$
        "HEAD", //$NON-NLS-1$
        "PROPFIND", //$NON-NLS-1$
        "DELETE", //$NON-NLS-1$
        "LOCK", //$NON-NLS-1$
        "UNLOCK", //$NON-NLS-1$
        "COPY", //$NON-NLS-1$
        "PUT", //$NON-NLS-1$
        "DELETE", //$NON-NLS-1$
        "MOVE", //$NON-NLS-1$
        "MKCOL", //$NON-NLS-1$
        "REPORT", //$NON-NLS-1$
        "ACL" //$NON-NLS-1$
    };


    /**
     * @param inode
     * @return an OC compat identifier
     */
    public static String makeOcId ( byte[] inode ) {
        return new BigInteger(inode).toString(16);
    }

}
