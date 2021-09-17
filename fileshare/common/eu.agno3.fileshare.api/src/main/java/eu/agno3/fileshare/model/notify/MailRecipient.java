/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 11.02.2015 by mbechler
 */
package eu.agno3.fileshare.model.notify;


import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.Locale;

import eu.agno3.runtime.util.serialization.SafeSerialization;


/**
 * @author mbechler
 *
 */
@SafeSerialization
public class MailRecipient implements Externalizable {

    /**
     * 
     */
    private static final long serialVersionUID = -8633018496413599975L;

    private String mailAddress;
    private Locale desiredLocale;
    private boolean noHtml;

    private String fullName;
    private String callingName;
    private String salutation;


    /**
     * @return the mailAddress
     */
    public String getMailAddress () {
        return this.mailAddress;
    }


    /**
     * @param mailAddress
     *            the mailAddress to set
     */
    public void setMailAddress ( String mailAddress ) {
        this.mailAddress = mailAddress;
    }


    /**
     * @return the salutation
     */
    public String getSalutation () {
        return this.salutation;
    }


    /**
     * @param salutation
     *            the salutation to set
     */
    public void setSalutation ( String salutation ) {
        this.salutation = salutation;
    }


    /**
     * @return the callingName
     */
    public String getCallingName () {
        return this.callingName;
    }


    /**
     * @param callingName
     *            the callingName to set
     */
    public void setCallingName ( String callingName ) {
        this.callingName = callingName;
    }


    /**
     * @return the fullName
     */
    public String getFullName () {
        return this.fullName;
    }


    /**
     * @param fullName
     *            the fullName to set
     */
    public void setFullName ( String fullName ) {
        this.fullName = fullName;
    }


    /**
     * @return the desiredLocale
     */
    public Locale getDesiredLocale () {
        return this.desiredLocale;
    }


    /**
     * @param desiredLocale
     *            the desiredLocale to set
     */
    public void setDesiredLocale ( Locale desiredLocale ) {
        this.desiredLocale = desiredLocale;
    }


    /**
     * @param noHtml
     *            the noHtml to set
     */
    public void setNoHtml ( boolean noHtml ) {
        this.noHtml = noHtml;
    }


    /**
     * @return the noHtml
     */
    public boolean isNoHtml () {
        return this.noHtml;
    }


    /**
     * {@inheritDoc}
     *
     * @see java.io.Externalizable#writeExternal(java.io.ObjectOutput)
     */
    @Override
    public void writeExternal ( ObjectOutput out ) throws IOException {
        // TODO Auto-generated method stub
        // private String mailAddress;
        // private Locale desiredLocale;
        // private boolean noHtml;

        // private String fullName;
        // private String callingName;
        // private String salutation;

        out.writeBoolean(this.mailAddress != null);
        if ( this.mailAddress != null ) {
            out.writeUTF(this.mailAddress);
        }
        out.writeBoolean(this.desiredLocale != null);
        if ( this.desiredLocale != null ) {
            out.writeUTF(this.desiredLocale.toLanguageTag());
        }
        out.writeBoolean(this.noHtml);
        out.writeBoolean(this.fullName != null);
        if ( this.fullName != null ) {
            out.writeUTF(this.fullName);
        }
        out.writeBoolean(this.callingName != null);
        if ( this.callingName != null ) {
            out.writeUTF(this.callingName);
        }
        out.writeBoolean(this.salutation != null);
        if ( this.salutation != null ) {
            out.writeUTF(this.salutation);
        }
    }


    /**
     * {@inheritDoc}
     *
     * @see java.io.Externalizable#readExternal(java.io.ObjectInput)
     */
    @Override
    public void readExternal ( ObjectInput in ) throws IOException, ClassNotFoundException {
        if ( in.readBoolean() ) {
            this.mailAddress = in.readUTF();
        }
        if ( in.readBoolean() ) {
            this.desiredLocale = Locale.forLanguageTag(in.readUTF());
        }
        this.noHtml = in.readBoolean();
        if ( in.readBoolean() ) {
            this.fullName = in.readUTF();
        }
        if ( in.readBoolean() ) {
            this.callingName = in.readUTF();
        }
        if ( in.readBoolean() ) {
            this.salutation = in.readUTF();
        }
    }


    /**
     * {@inheritDoc}
     *
     * @see java.lang.Object#hashCode()
     */
    // +GENERATED
    @Override
    public int hashCode () {
        final int prime = 31;
        int result = 1;
        result = prime * result + ( ( this.mailAddress == null ) ? 0 : this.mailAddress.hashCode() );
        return result;
    }


    // -GENERATED

    /**
     * {@inheritDoc}
     *
     * @see java.lang.Object#equals(java.lang.Object)
     */
    // +GENERATED
    @Override
    public boolean equals ( Object obj ) {
        if ( this == obj )
            return true;
        if ( obj == null )
            return false;
        if ( getClass() != obj.getClass() )
            return false;
        MailRecipient other = (MailRecipient) obj;
        if ( this.mailAddress == null ) {
            if ( other.mailAddress != null )
                return false;
        }
        else if ( !this.mailAddress.equals(other.mailAddress) )
            return false;
        return true;
    }
    // -GENERATED

}
