[#ftl attributes={"language":"eng"}]
[#if recipient.salutation?has_content]
${recipient.salutation}
[#else]
Dear ${recipient.callingName!recipient.fullName!"user"},

[/#if]
you requested to reset your password. To complete the password reset process go to

<${data.link}>

This verification link will be valid until ${data.expirationDate?datetime}.

[#include "mail-footer.plain.ftl"]