[#ftl attributes={"language":"eng"}]
Dear ${recipient.callingName!recipient.fullName!"user"},

The user ${data.user.userDisplayName} (${data.user.principal.userName}) has accepted your invitation.

[#include "mail-footer.plain.ftl"]