/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 13.12.2015 by mbechler
 */
package eu.agno3.runtime.update.internal;


import java.io.File;
import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.SignatureException;
import java.security.cert.CertificateException;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.eclipse.osgi.signedcontent.SignedContent;
import org.eclipse.osgi.signedcontent.SignedContentFactory;
import org.eclipse.osgi.signedcontent.SignerInfo;
import org.osgi.framework.Bundle;
import org.osgi.framework.Constants;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;


/**
 * @author mbechler
 *
 */
@Component ( service = SignedContentFactory.class, property = {
    Constants.SERVICE_RANKING + ":Integer=9999999"
} )
public class SignedContentFactoryImpl implements SignedContentFactory {

    private static final Logger log = Logger.getLogger(SignedContentFactoryImpl.class);

    private SignedContentFactory delegate;


    @Reference ( target = "(service.bundleid=0)" )
    protected synchronized void setDelegate ( SignedContentFactory scf ) {
        this.delegate = scf;
    }


    protected synchronized void unsetDelegate ( SignedContentFactory scf ) {
        if ( this.delegate == scf ) {
            this.delegate = null;
        }
    }


    /**
     * {@inheritDoc}
     *
     * @see org.eclipse.osgi.signedcontent.SignedContentFactory#getSignedContent(java.io.File)
     */
    @Override
    public SignedContent getSignedContent ( File content )
            throws IOException, InvalidKeyException, SignatureException, CertificateException, NoSuchAlgorithmException, NoSuchProviderException {
        SignedContent signedContent = this.delegate.getSignedContent(content);
        return wrapSignedContent(signedContent);
    }


    /**
     * @param signedContent
     * @return
     */
    private static SignedContent wrapSignedContent ( SignedContent signedContent ) {
        List<SignerInfo> trusted = new ArrayList<>();
        List<SignerInfo> untrusted = new ArrayList<>();

        for ( SignerInfo si : signedContent.getSignerInfos() ) {
            if ( !si.isTrusted() ) {
                untrusted.add(si);
                continue;
            }
            try {
                // check validity here as it's impossible to do this in the correct spot
                signedContent.checkValidity(si);
            }
            catch ( CertificateException e ) {
                log.warn("Validity check failed", e); //$NON-NLS-1$
                continue;
            }
            trusted.add(si);
        }

        if ( !trusted.isEmpty() ) {
            // if we have one valid signature, drop the other ones
            // as the eclipse implemenetation is braindead
            return new SignedContentWrapper(trusted, signedContent);
        }

        return new SignedContentWrapper(untrusted, signedContent);
    }


    /**
     * {@inheritDoc}
     *
     * @see org.eclipse.osgi.signedcontent.SignedContentFactory#getSignedContent(org.osgi.framework.Bundle)
     */
    @Override
    public SignedContent getSignedContent ( Bundle bundle )
            throws IOException, InvalidKeyException, SignatureException, CertificateException, NoSuchAlgorithmException, NoSuchProviderException {
        SignedContent signedContent = this.delegate.getSignedContent(bundle);
        return wrapSignedContent(signedContent);
    }

}
