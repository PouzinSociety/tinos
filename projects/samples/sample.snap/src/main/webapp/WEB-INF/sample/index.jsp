<%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>

<jsp:include page="/../top.jsp"/>
<h1>Sample DAO Information</h1>
<table>
  <tr>
  <thead><th>Name</th></thead>
  </tr>
  <c:forEach var="dao" items="${daoList}">
    <tr>
      <td>${dao.text} </td>
          <spring:url value="/sample/delete.htm${textVal}" var="deleteUrl">
      <td>
            <spring:param name="textVal" value="${dao.text}"/>
          </spring:url>
          <a href="${fn:escapeXml(deleteUrl)}">Delete Me</a>
    </tr>
  </c:forEach>
</table>

<ul>
  <li><a href="<c:url value="/sample/addDaoForm.htm"/>">Add a new Sample DAO</a></li>
</ul>

<jsp:include page="/../bottom.jsp"/>

