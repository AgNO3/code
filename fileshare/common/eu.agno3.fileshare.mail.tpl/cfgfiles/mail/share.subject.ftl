[#t]${senderDisplayName} has shared [#if data.hideSensitive]
[#t][#if data.entity.entityType == "FILE"]a file [/#if]
[#t][#if data.entity.entityType == "DIRECTORY"]a folder [/#if]
[#t]with you.
[#else]
[#t][#if data.entity.entityType == "FILE"]the file [/#if]
[#t][#if data.entity.entityType == "DIRECTORY"]the folder [/#if]
[#t]"${data.entity.localName}" with you.
[/#if]