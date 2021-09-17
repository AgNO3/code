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
you have applied for an account. To complete the registration process <a href="${data.link}">click here</a>.
This verification link will be valid until ${data.expirationDate?datetime}.
</p>

<p>
If you believe you have recieved this message in error, you may ignore it.
</p>

[/#escape]
[#include "mail-footer.html.ftl"]
</body>
</html>