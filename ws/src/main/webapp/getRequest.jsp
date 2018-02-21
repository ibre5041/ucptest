<%@ page import="sandbox.ws.*" %>
<%@ page import="java.sql.Connection " %>
<%@ page import="java.sql.SQLException " %>
<%@ page import="java.sql.Statement " %>
<%@ page import="oracle.ucp.jdbc.PoolDataSourceFactory " %>
<%@ page import="oracle.ucp.jdbc.PoolDataSource " %>
<%@ page import="oracle.ucp.jdbc.ValidConnection " %>
<%@ page import="java.sql.ResultSet " %>
 
<html>
<head>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
    <title>Application Continuity with RAC 12c Example JSP</title>
</head>
<body bgcolor="white">
 
    <p>request started</p>
 
<%
    String message = request.getParameter("q");
 
    UCPHelper helper = new UCPHelper();
    Connection conn = helper.getConnection("jdbc/UCPPool");
 
    helper.executePLSQL(message, conn);
%>
 
    <p>request completed</p>
 
</body>
</html>
