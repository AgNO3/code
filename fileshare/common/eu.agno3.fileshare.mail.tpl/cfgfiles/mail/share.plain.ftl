[#ftl attributes={"language":"eng"}]
[#if recipient.salutation?has_content]
${recipient.salutation}
[#else]
Dear ${recipient.callingName!recipient.fullName!"user"},

[/#if]
[#t]${senderDisplayName} has shared 
[#if data.hideSensitive]
[#t][#if data.entity.entityType == "FILE"]a file [/#if]
[#t][#if data.entity.entityType == "DIRECTORY"]a folder [/#if]
[#t]with you.
[#else]
[#t][#if data.entity.entityType == "FILE"]the file [/#if]
[#t][#if data.entity.entityType == "DIRECTORY"]the folder [/#if]
[#t]"${data.entity.localName}" with you.
[/#if]

[#if data.entity.entityType == "FILE"]
You can download the file [#if !data.hideSensitive](${data.formattedSize})[/#if] at

${data.viewURL}
[#else]
You can access the folder at

<${data.viewURL}>
[/#if]



[#include "mail-footer.plain.ftl"]