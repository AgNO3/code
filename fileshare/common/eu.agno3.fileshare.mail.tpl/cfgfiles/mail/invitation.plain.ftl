[#ftl attributes={"language":"eng"}]
[#if recipient.salutation?has_content]
${recipient.salutation}
[#else]
Dear ${recipient.callingName!recipient.fullName!"user"},

you have been invited to create an account to share files with ${data.sendingUser.userDisplayName}.
[/#if]

To create your account now go to

<${data.link}>

This verification link will be valid until ${data.expirationDate?datetime}.

If you believe you have recieved this message in error, you may ignore it.

[#include "mail-footer.plain.ftl"]