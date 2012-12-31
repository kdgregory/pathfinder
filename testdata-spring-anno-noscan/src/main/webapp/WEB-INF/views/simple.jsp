<%@page contentType="text/html"%>
<%@page pageEncoding="UTF-8"%>

<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>

<html>
<head>
    <title>Simple View JSP</title>
</head>
<body>

    The request URL was <code>${reqUrl}</code>
    <br/>
    It was handled by <code>${controller}</code>
    <br/>
    <br/>
    Values:
    <ul>
    <li>id      = ${id}
    <li>argle   = ${argle}
    <li>bargle  = ${bargle}
    <li>wargle  = ${wargle}
    </ul>

</body>
</html>
