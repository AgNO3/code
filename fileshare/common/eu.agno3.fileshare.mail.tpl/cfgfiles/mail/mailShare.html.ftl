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
${senderDisplayName} has shared 
[#if data.hideSensitive]
[#if data.entity.entityType == "FILE"]a file [/#if]
[#if data.entity.entityType == "DIRECTORY"]a folder [/#if]
with you.
[#else]
[#if data.entity.entityType == "FILE"]the file [/#if]
[#if data.entity.entityType == "DIRECTORY"]the folder [/#if]
<q>${data.entity.localName}</q> with you.
[/#if]
</p>

<p>
[#if data.entity.entityType == "FILE"]
<a target="_blank" href="${data.viewURL}">Download the file</a> [#if !data.hideSensitive](${data.formattedSize})[/#if] now.
[#else]
<a target="_blank" href="${data.viewURL}">Open the folder</a> now.
[/#if]
</p>
[/#escape]
[#include "mail-footer.html.ftl"]
</body>
</html>