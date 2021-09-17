[#ftl attributes={"language":"eng"}]
<!DOCTYPE html>
<html>
<head>
</head>
<body>
[#escape x as x?html]
<p>
Dear ${recipient.callingName!recipient.fullName!"user"},<br />
<br />

The user ${data.expiringUser.userDisplayName} (${data.expiringUser.principal.userName}) is about to expire. 
If you don't take any action, it will be deleted on ${data.expiringUser.expiration?date}.

[#if data.extensionLink?has_content]
Click <a href="${data.extensionLink}">here</a> to extend the user's expiration period.
[#else]
You are not allowed to extend the user's expiration period yourself. 
Contact an administrator if the account is still needed.
[/#if]

[/#escape]
</p>
[#include "mail-footer.html.ftl"]
</body>
</html>