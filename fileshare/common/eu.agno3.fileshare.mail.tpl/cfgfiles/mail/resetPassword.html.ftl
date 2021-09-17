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
you requested to reset your password. To complete the password reset process <a href="${data.link}">click here</a>.
This verification link will be valid until ${data.expirationDate?datetime}.
</p>


[/#escape]
[#include "mail-footer.html.ftl"]
</body>
</html>