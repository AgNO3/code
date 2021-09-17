/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 13.12.2015 by mbechler
 */
package eu.agno3.runtime.update.internal;


import java.security.cert.CertificateExpiredException;
import java.security.cert.CertificateNotYetValidException;
import java.util.Date;
import java.util.List;

import org.eclipse.osgi.signedcontent.SignedContent;
import org.eclipse.osgi.signedcontent.SignedContentEntry;
import org.eclipse.osgi.signedcontent.SignerInfo;


/**
 * @author mbechler
 *
 */
public class SignedContentWrapper implements SignedContent {

    private SignedContent signedContent;
    private List<SignerInfo> signers;


    /**
     * @param signers
     * @param signedContent
     */
    public SignedContentWrapper ( List<SignerInfo> signers, SignedContent signedContent ) {
        this.signers = signers;
        this.signedContent = signedContent;
    }


    @Override
    public SignedContentEntry[] getSignedEntries () {
        return this.signedContent.getSignedEntries();
    }


    @Override
    public SignedContentEntry getSignedEntry ( String name ) {
        return this.signedContent.getSignedEntry(name);
    }


    @Override
    public SignerInfo[] getSignerInfos () {
        return this.signers.toArray(new SignerInfo[] {});
    }


    @Override
    public boolean isSigned () {
        return this.signedContent.isSigned() && !this.signers.isEmpty();
    }


    @Override
    public Date getSigningTime ( SignerInfo signerInfo ) {
        return this.signedContent.getSigningTime(signerInfo);
    }


    @Override
    public SignerInfo getTSASignerInfo ( SignerInfo signerInfo ) {
        return this.signedContent.getTSASignerInfo(signerInfo);
    }


    @Override
    public void checkValidity ( SignerInfo signerInfo ) throws CertificateExpiredException, CertificateNotYetValidException {
        this.signedContent.checkValidity(signerInfo);
    }

}
