[#ftl attributes={"language":"eng"}]
[#if recipient.salutation?has_content]
${recipient.salutation}

[#else]
Dear ${recipient.callingName!recipient.fullName!"user"},

[/#if]

[#if data.hideSensitive]
A file
[#else]
The file "${data.fullPath}"
[/#if] 
[#if data.ownerIsGroup]
in your group ${data.entity.owner.name}
[/#if]
has been uploaded for you
[#if data.uploadingUser?has_content]
by ${data.uploadingUser.userDisplayName}
[#else]
by ${data.uploadingGrant}
[/#if]


[#include "mail-footer.plain.ftl"]