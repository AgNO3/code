/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 31.05.2015 by mbechler
 */
package eu.agno3.runtime.net.icap.internal;


import java.util.HashSet;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.joda.time.DateTime;

import eu.agno3.runtime.net.icap.ICAPException;
import eu.agno3.runtime.net.icap.ICAPOptions;
import eu.agno3.runtime.net.icap.ICAPResponse;


/**
 * @author mbechler
 *
 */
public class ICAPOptionsImpl implements ICAPOptions {

    private static final Logger log = Logger.getLogger(ICAPOptionsImpl.class);
    private static final int DEFAULT_OPTIONS_TIMEOUT = 3600;

    private int previewSize = -1;
    private DateTime optionsExpire;
    private String istag;
    private boolean allowRespmod;
    private boolean allowReqmod;
    private boolean allow204;

    private Set<String> transferComplete = new HashSet<>();
    private Set<String> transferPreview = new HashSet<>();
    private Set<String> transferIgnore = new HashSet<>();

    private String service;


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.net.icap.ICAPOptions#getPreviewSize()
     */
    @Override
    public int getPreviewSize () {
        return this.previewSize;
    }


    /**
     * @return the service
     */
    public String getService () {
        return this.service;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.net.icap.ICAPOptions#isExpired()
     */
    @Override
    public boolean isExpired () {
        return this.optionsExpire.isBeforeNow();
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.net.icap.ICAPOptions#getIstag()
     */
    @Override
    public String getIstag () {
        return this.istag;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.net.icap.ICAPOptions#isAllow204()
     */
    @Override
    public boolean isAllow204 () {
        return this.allow204;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.net.icap.ICAPOptions#isAllowReqmod()
     */
    @Override
    public boolean isAllowReqmod () {
        return this.allowReqmod;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.net.icap.ICAPOptions#isAllowRespmod()
     */
    @Override
    public boolean isAllowRespmod () {
        return this.allowRespmod;
    }


    /**
     * @param resp
     * @return parsed icap options
     * @throws ICAPException
     */
    public static ICAPOptions fromResponse ( ICAPResponse resp ) throws ICAPException {
        ICAPOptionsImpl options = new ICAPOptionsImpl();
        options.previewSize = -1;
        options.istag = null;
        options.optionsExpire = DateTime.now().plusSeconds(DEFAULT_OPTIONS_TIMEOUT);
        options.allowReqmod = false;
        options.allowRespmod = false;
        options.allow204 = false;

        for ( Entry<String, List<String>> header : resp.getResponseHeaders().entrySet() ) {

            switch ( header.getKey() ) {

            case "options-ttl": //$NON-NLS-1$
                options.optionsExpire = DateTime.now().plusSeconds(Integer.parseInt(header.getValue().get(0)));
                break;

            case "istag": //$NON-NLS-1$
                options.istag = header.getValue().get(0).trim();
                break;

            case "preview": //$NON-NLS-1$
                options.previewSize = Integer.parseInt(header.getValue().get(0));
                break;

            case "methods": //$NON-NLS-1$
                String fullMethods = StringUtils.join(header.getValue(), ", "); //$NON-NLS-1$
                for ( String method : StringUtils.split(fullMethods, ",") ) { //$NON-NLS-1$
                    method = method.trim().toLowerCase();
                    if ( "respmod".equals(method) ) { //$NON-NLS-1$
                        options.allowRespmod = true;
                    }
                    else if ( "reqmod".equals(method) ) { //$NON-NLS-1$
                        options.allowReqmod = true;
                    }

                }
                break;

            case "service": //$NON-NLS-1$
                options.service = header.getValue().get(0).trim();
                break;

            case "allow": //$NON-NLS-1$
                String fullAllow = StringUtils.join(header.getValue(), ", "); //$NON-NLS-1$
                for ( String allow : StringUtils.split(fullAllow, ",") ) { //$NON-NLS-1$
                    allow = allow.trim().toLowerCase();
                    if ( "204".equals(allow) ) { //$NON-NLS-1$
                        options.allow204 = true;
                    }

                }
                break;

            case "transfer-preview": //$NON-NLS-1$
                String fullPreview = StringUtils.join(header.getValue(), ", "); //$NON-NLS-1$
                for ( String allow : StringUtils.split(fullPreview, ",") ) { //$NON-NLS-1$
                    options.transferPreview.add(allow.trim());
                }
                break;

            case "transfer-complete": //$NON-NLS-1$
                String fullComplete = StringUtils.join(header.getValue(), ", "); //$NON-NLS-1$
                for ( String allow : StringUtils.split(fullComplete, ",") ) { //$NON-NLS-1$
                    options.transferComplete.add(allow.trim());
                }
                break;

            case "transfer-ignore": //$NON-NLS-1$
                String fullIgnore = StringUtils.join(header.getValue(), ", "); //$NON-NLS-1$
                for ( String allow : StringUtils.split(fullIgnore, ",") ) { //$NON-NLS-1$
                    options.transferIgnore.add(allow.trim());
                }
                break;

            case "date": //$NON-NLS-1$
            case "encapsulated": //$NON-NLS-1$
                break;
            default:
                log.debug(String.format("Unhandeled header %s: %s", header.getKey(), header.getValue())); //$NON-NLS-1$
            }
        }

        if ( options.transferPreview.isEmpty() && options.transferComplete.isEmpty() && options.transferIgnore.isEmpty() ) {
            options.transferComplete.add("*"); //$NON-NLS-1$
        }
        else if ( !options.transferComplete.contains("*") && //$NON-NLS-1$
                !options.transferPreview.contains("*") && //$NON-NLS-1$
                !options.transferIgnore.contains("*") ) { //$NON-NLS-1$
            throw new ICAPException("Selective transfer enabled, but no default value given"); //$NON-NLS-1$
        }

        return options;
    }
}
