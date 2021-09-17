[#ftl attributes={"language":"eng"}]
[#if recipient.salutation?has_content]
${recipient.salutation}
[#else]
Dear ${recipient.callingName!recipient.fullName!"user"},

[/#if]
you have applied for an account. To complete the registration process go to

<${data.link}>

This verification link will be valid until ${data.expirationDate?datetime}.

If you believe you have recieved this message in error, you may ignore it.

[#include "mail-footer.plain.ftl"]