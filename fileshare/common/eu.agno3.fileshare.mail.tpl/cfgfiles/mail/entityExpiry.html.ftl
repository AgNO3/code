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
is about to expire. If you don't take any action, it will be deleted on ${data.entity.expires?date}.


[/#escape]
</p>
[#include "mail-footer.html.ftl"]
</body>
</html>