[#ftl attributes={"language":"eng"}]
Dear ${recipient.callingName!recipient.fullName!"user"},

The user ${data.expiringUser.userDisplayName} (${data.expiringUser.principal.userName}) is about to expire. 
If you don't take any action, it will be deleted on ${data.expiringUser.expiration?date}.

[#if data.extensionLink?has_content]
Click <${data.extensionLink}> to extend the user's expiration period.
[#else]
You are not allowed to extend the user's expiration period yourself. 
Contact an administrator if the account is still needed.
[/#if]

[#include "mail-footer.plain.ftl"]