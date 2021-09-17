/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 16.01.2015 by mbechler
 */
package eu.agno3.fileshare.webgui.i18n;


import java.util.Locale;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Named;

import eu.agno3.runtime.jsf.i18n.FacesMessageBundle;


/**
 * @author mbechler
 *
 */

@SuppressWarnings ( {
    "nls", "javadoc"
} )
@Named ( "msgs" )
@ApplicationScoped
public class FileshareMessages extends FacesMessageBundle {

    public static final String FS_MESSAGES_BASE = "eu.agno3.fileshare.webgui.i18n.messages";
    public static final String DIRECTORY_TYPE_DISPLAY_NAME = "tree.type.dir";
    public static final String USER_ROOT_TYPE_DISPLAY_NAME = "tree.type.user-root";
    public static final String USER_ROOT_DISPLAY_NAME = "tree.name.user-root";

    public static final String EMPTY_DIRECTORY_WRITEABLE_MESSAGE = "empty-dir-rw";
    public static final String EMPTY_DIRECTORY_READONLY_MESSAGE = "empty-dir-ro";
    public static final String EMPTY_GROUPS_MESSAGE = "empty-groups";
    public static final String EMPTY_SHARES_MESSAGE = "empty-shares";
    public static final String EMPTY_FAVORITES_MESSAGE = "empty-favorites";

    public static final String INVALID_QUERY_MESSAGE = "invalid-query";
    public static final String EMPTY_SEARCH_RESULT_MESSAGE = "empty-search-result";

    public static final String GROUPS_ROOT_DISPLAY_NAME = "tree.name.group-root";
    public static final String SHARED_DISPLAY_NAME = "tree.name.shared-root";
    public static final String GROUP_ROOT_TYPE_DISPLAY_NAME = "tree.type.group-root";

    public static final String SHARE_TYPE_FILE_DISPLAY_NAME = "tree.type.share-root-file";
    public static final String SHARE_TYPE_DIRECTORY_DISPLAY_NAME = "tree.type.share-root-dir";
    public static final String SHARE_TYPE_DIRECTORY_WRITEABLE_DISPLAY_NAME = "tree.type.share-root-dir-writeable";
    public static final String SHARE_TYPE_FILE_WRITEABLE_DISPLAY_NAME = "tree.type.share-root-file-writeable";
    public static final String FAVORITES_ROOT_DISPLAY_NAME = "tree.type.favorites";
    public static final String ALL_SHARED_DISPLAY_NAME = "tree.type.all-shared";
    public static final String PEERS_ROOT_DISPLAY_NAME = "tree.type.peers-root";
    public static final String SEARCH_RESULT_DISPLAY_NAME = "tree.type.search-result";
    public static final String ROOT_DISPLAY_NAME = "tree.type.root";

    public static final String PREVIEW_TITLE_NO_SECURITY = "preview.title.noSecurity";
    public static final String PREVIEW_TITLE_SIZE_EXCEEDED = "preview.title.sizeExceeded";
    public static final String PREVIEW_TITLE_UNSUPPORTED_TYPE = "preview.title.unsupportedType";

    public static final String PREVIEW_TILE = "preview.title";
    public static final String UNLABELED_ENTITY = "entity.unlabeled";
    public static final String REUSED_EXISTING_SHARE = "share.reusedExisting";
    public static final String REUSED_EXISTING_SUBJECT_SHARE = "share.resuedExistingSubject";
    public static final String MISSING_TOKEN_IDENTIFIER = "share.missingTokenIdentifier";
    public static final String MISSING_SHARE_USER = "share.missingUser";
    public static final String MISSING_MAIL_ADDRESS = "share.missingMailAddress";

    public static final String ANONYMOUS = "anonymous";
    public static final String INVALID_REGISTRATION_TOKEN = "registration.tokenInvalid";
    public static final String REGISTRATION_USERNAME_EXISTS = "registration.userNameExists";
    public static final String PASSWORDS_DO_NOT_MATCH = "password.confirmNoMatch";
    public static final String USER_CREATED = "user.created";

    public static final String QUOTA_DETAIL = "quota.detail";
    public static final String QUOTA_SHORT = "quota.short";
    public static final String USER_QUOTA_DETAIL = "quota.user.detail";
    public static final String USER_QUOTA_SHORT = "quota.user.short";

    public static final String PEER_SHARED_BY_TITLE = "peer.shared.by";
    public static final String PEER_SHARED_TO_TITLE = "peer.shared.to";

    public static final String WRONG_GRANT_PASSWORD = "tokenauth.wrongPassword";


    /**
     * 
     * @param key
     *            message id
     * @return the message localized according to the JSF ViewRoot locale
     */
    public static String get ( String key ) {
        return get(FS_MESSAGES_BASE, key);
    }


    /**
     * 
     * @param key
     *            message id
     * @param l
     *            desired locale
     * @return the message localized according to the given locale
     */
    public static String get ( String key, Locale l ) {
        return get(FS_MESSAGES_BASE, key, l);
    }


    /**
     * @param key
     * @param args
     * @return the template formatted to the JSF ViewRoot locale
     */
    public static String format ( String key, Object... args ) {
        return format(FS_MESSAGES_BASE, key, args);
    }


    /**
     * 
     * @param en
     * @param val
     * @return the translated enum value
     */
    public static <TEnum extends Enum<TEnum>> String translateEnumValue ( Class<TEnum> en, Object val ) {
        if ( val == null || !en.isAssignableFrom(val.getClass()) ) {
            return null;
        }
        @SuppressWarnings ( "unchecked" )
        TEnum enumVal = (TEnum) val;
        StringBuilder key = new StringBuilder();
        key.append(en.getSimpleName());
        key.append('.');
        key.append(enumVal.name());
        return get(key.toString());
    }


    public static String formatEL ( String key, Object a1 ) {
        return format(key, a1);
    }


    public static String formatEL ( String key, Object a1, Object a2 ) {
        return format(key, a1, a2);
    }


    public static String formatEL ( String key, Object a1, Object a2, Object a3 ) {
        return format(key, a1, a2, a3);
    }
}
