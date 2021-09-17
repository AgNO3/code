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
is about to expire. If you don't take any action, it will be deleted on ${data.entity.expires?date}.



[#include "mail-footer.plain.ftl"]