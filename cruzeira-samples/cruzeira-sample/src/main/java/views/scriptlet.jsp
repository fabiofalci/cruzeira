<html>
<head>
</head>
<body>

<%
    String username = request.getParameter("username");
    if ( username != null && username.length() > 0 ) {
%>
	<p>Hi then!</p>
<%
    } else { 
%>
	<p>Hi else!</p>
<%
    } 
%>

</body>
</html>