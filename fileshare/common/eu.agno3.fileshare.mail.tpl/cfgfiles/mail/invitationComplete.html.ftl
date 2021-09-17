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

The user ${data.user.userDisplayName} (${data.user.principal.userName}) has accepted your invitation.

[/#escape]
</p>
[#include "mail-footer.html.ftl"]
</body>
</html>