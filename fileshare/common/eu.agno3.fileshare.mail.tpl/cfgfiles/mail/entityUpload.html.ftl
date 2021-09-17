[#ftl attributes={"language":"eng"}]
<!DOCTYPE html>
<html>
<head>
</head>
<body>
[#escape x as x?html]
<p>
[#if recipient.salutation?has_content]
[#noescape]
${recipient.salutation?html?replace("\n", "<br>")}
<br />
[/#noescape]
[#else]
Dear ${recipient.callingName!recipient.fullName!"user"},<br />
<br />
[/#if]

[#if data.hideSensitive]
A file
[#else]
The file <q>${data.fullPath}</q>
[/#if]
[#if data.ownerIsGroup]
in your group <q>${data.entity.owner.name}</q>
[/#if]
has been uploaded for you
[#if data.uploadingUser?has_content]
by ${data.uploadingUser.userDisplayName}
[#else]
by ${data.uploadingGrant}
[/#if]


[/#escape]
</p>
[#include "mail-footer.html.ftl"]
</body>
</html>