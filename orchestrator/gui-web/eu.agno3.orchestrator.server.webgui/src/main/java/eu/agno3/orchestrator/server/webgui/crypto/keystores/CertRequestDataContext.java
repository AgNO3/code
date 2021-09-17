/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 16.04.2015 by mbechler
 */
package eu.agno3.orchestrator.server.webgui.crypto.keystores;


import java.io.Serializable;
import java.security.cert.CertificateParsingException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.faces.view.ViewScoped;
import javax.validation.ValidationException;

import org.apache.commons.lang3.StringUtils;
import org.bouncycastle.asn1.ASN1ObjectIdentifier;
import org.bouncycastle.asn1.x509.BasicConstraints;
import org.bouncycastle.asn1.x509.ExtendedKeyUsage;
import org.bouncycastle.asn1.x509.Extension;
import org.bouncycastle.asn1.x509.GeneralName;
import org.bouncycastle.asn1.x509.GeneralNames;
import org.bouncycastle.asn1.x509.KeyPurposeId;
import org.bouncycastle.asn1.x509.KeyUsage;
import org.joda.time.Duration;

import eu.agno3.orchestrator.config.model.base.exceptions.ModelObjectValidationException;
import eu.agno3.orchestrator.types.net.AbstractIPAddress;
import eu.agno3.orchestrator.types.validation.ValidDN;
import eu.agno3.runtime.crypto.x509.CertExtension;
import eu.agno3.runtime.crypto.x509.CertExtensionImpl;


/**
 * @author mbechler
 *
 */
@ViewScoped
public class CertRequestDataContext implements Serializable {

    /**
     * 
     */
    private static final long serialVersionUID = 2658451086968702000L;
    private Duration lifetime = Duration.standardDays(2 * 365);
    private String subject;
    private String requestPassword;

    private boolean ekusCritical;
    private boolean sansCritical;

    private boolean ca;
    private int caPathLength;

    private Set<String> keyUsages;
    private Set<String> ekus;
    private List<String> sans;


    /**
     * @return the lifetime
     */
    public Duration getLifetime () {
        return this.lifetime;
    }


    /**
     * @param lifetime
     *            the lifetime to set
     */
    public void setLifetime ( Duration lifetime ) {
        this.lifetime = lifetime;
    }


    /**
     * @return the subject
     */
    @ValidDN
    public String getSubject () {
        return this.subject;
    }


    /**
     * @param subject
     *            the subject to set
     */
    public void setSubject ( String subject ) {
        this.subject = subject;
    }


    /**
     * @return the request password
     */
    public String getRequestPassword () {
        return this.requestPassword;
    }


    /**
     * @param requestPassword
     *            the requestPassword to set
     */
    public void setRequestPassword ( String requestPassword ) {
        this.requestPassword = requestPassword;
    }


    /**
     * @return the ca
     */
    public boolean getCa () {
        return this.ca;
    }


    /**
     * @param ca
     *            the ca to set
     */
    public void setCa ( boolean ca ) {
        this.ca = ca;
    }


    /**
     * @return the caPathLength
     */
    public int getCaPathLength () {
        return this.caPathLength;
    }


    /**
     * @param caPathLength
     *            the caPathLength to set
     */
    public void setCaPathLength ( int caPathLength ) {
        this.caPathLength = caPathLength;
    }


    /**
     * @return the keyUsages
     */
    public Set<String> getKeyUsages () {
        return this.keyUsages;
    }


    /**
     * @param keyUsages
     *            the keyUsages to set
     */
    public void setKeyUsages ( Set<String> keyUsages ) {
        this.keyUsages = keyUsages;
    }


    /**
     * @return the sans
     */
    public List<String> getSans () {
        return this.sans;
    }


    /**
     * @param sans
     *            the sans to set
     */
    public void setSans ( List<String> sans ) {
        this.sans = sans;
    }


    /**
     * @return the sansCritical
     */
    public boolean getSansCritical () {
        return this.sansCritical;
    }


    /**
     * @param sansCritical
     *            the sansCritical to set
     */
    public void setSansCritical ( boolean sansCritical ) {
        this.sansCritical = sansCritical;
    }


    /**
     * @return the ekus
     */
    public Set<String> getEkus () {
        return this.ekus;
    }


    /**
     * @param ekus
     *            the ekus to set
     */
    public void setEkus ( Set<String> ekus ) {
        this.ekus = ekus;
    }


    /**
     * @return the ekusCritical
     */
    public boolean getEkusCritical () {
        return this.ekusCritical;
    }


    /**
     * @param ekusCritical
     *            the ekusCritical to set
     */
    public void setEkusCritical ( boolean ekusCritical ) {
        this.ekusCritical = ekusCritical;
    }


    /**
     * @return the cert extensions
     * @throws ModelObjectValidationException
     */
    public Set<CertExtension> getExtensions () throws ModelObjectValidationException {
        Set<CertExtension> res = new HashSet<>();

        if ( this.ca && this.caPathLength != 0 ) {
            res.add(new CertExtensionImpl(Extension.basicConstraints, true, new BasicConstraints(this.caPathLength)));
        }
        else {
            res.add(new CertExtensionImpl(Extension.basicConstraints, true, new BasicConstraints(this.ca)));
        }

        res.add(new CertExtensionImpl(Extension.keyUsage, true, new KeyUsage(makeKeyUsage())));

        KeyPurposeId[] kpis = makeKPIs();
        if ( kpis != null && kpis.length > 0 ) {
            res.add(new CertExtensionImpl(Extension.extendedKeyUsage, this.ekusCritical, ( new ExtendedKeyUsage(kpis) ).toASN1Primitive()));
        }

        if ( this.sans != null && !this.sans.isEmpty() ) {
            res.add(new CertExtensionImpl(Extension.subjectAlternativeName, this.sansCritical, makeSANs()));
        }

        return res;
    }


    /**
     * @return
     */
    private int makeKeyUsage () {
        int res = 0;
        for ( String i : this.keyUsages ) {
            res |= Integer.parseInt(i);
        }
        return res;
    }


    /**
     * @return
     * @throws ModelObjectValidationException
     */
    private GeneralNames makeSANs () throws ModelObjectValidationException {
        GeneralName[] names = new GeneralName[this.sans.size()];
        int i = 0;
        for ( String name : this.sans ) {
            if ( !StringUtils.isBlank(name) ) {
                names[ i++ ] = makeGeneralName(name);
            }
        }
        return new GeneralNames(names);
    }


    /**
     * @param name
     * @return
     * @throws ModelObjectValidationException
     */
    private static GeneralName makeGeneralName ( String name ) throws ModelObjectValidationException {
        int lastSepPos = name.lastIndexOf('.');
        if ( StringUtils.isNumeric(name)
                || ( lastSepPos >= 0 && lastSepPos != ( name.length() - 1 ) && StringUtils.isNumeric(name.substring(lastSepPos + 1)) )
                || ( name.charAt(0) == '[' && name.charAt(name.length() - 1) == ']' ) ) {
            return new GeneralName(GeneralName.iPAddress, canonicalizeAddress(name));
        }
        return new GeneralName(GeneralName.dNSName, name);
    }


    /**
     * @param name
     * @return
     * @throws ModelObjectValidationException
     */
    private static String canonicalizeAddress ( String name ) throws ModelObjectValidationException {
        try {
            return AbstractIPAddress.parse(name).getCanonicalForm();
        }
        catch ( IllegalArgumentException e ) {
            throw new ValidationException("Invalid IP Address"); //$NON-NLS-1$
        }
    }


    /**
     * @return
     */
    private KeyPurposeId[] makeKPIs () {
        KeyPurposeId[] kpis = new KeyPurposeId[this.ekus.size()];
        int i = 0;
        for ( String eku : this.ekus ) {
            ASN1ObjectIdentifier asn1ObjectIdentifier = new ASN1ObjectIdentifier(eku);
            kpis[ i++ ] = KeyPurposeId.getInstance(asn1ObjectIdentifier);
        }

        return kpis;
    }


    /**
     * 
     * @param cert
     * @return SANs as string
     * @throws CertificateParsingException
     */
    public static List<String> sansToString ( X509Certificate cert ) throws CertificateParsingException {
        List<String> stringSans = new ArrayList<>();
        for ( List<?> san : cert.getSubjectAlternativeNames() ) {
            int type = (int) san.get(0);
            if ( type == 2 || type == 7 ) {
                stringSans.add((String) san.get(1));
            }
        }
        return stringSans;
    }


    /**
     * @param cert
     * @return key usages
     */
    public static Set<String> keyUsageToString ( X509Certificate cert ) {
        boolean[] keyUsage = cert.getKeyUsage();
        Set<String> kus = new HashSet<>();

        int i = 0;
        for ( boolean b : keyUsage ) {
            if ( b ) {
                kus.add(String.valueOf(1 << i));
            }
            i++;
        }
        return kus;
    }


    /**
     * @param cert
     * @return ext key usage OIDs as string
     * @throws CertificateParsingException
     */
    public static Set<String> extKeyUsageToString ( X509Certificate cert ) throws CertificateParsingException {
        return new HashSet<>(cert.getExtendedKeyUsage());
    }
}
