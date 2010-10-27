<%@ include file="/WEB-INF/jsp/includes.jsp" %>
<%@ include file="/WEB-INF/jsp/header.jsp" %>

<h1>TINOS Status</h2>
<h2>Hardware Interfaces</h2>
<table>
  <tr>
  <thead><th>Interface</th><th>MAC-Address</th><th>MTU-Size</th></thead>
  </tr>
  <c:forEach var="rowlist" items="${machine}">
  	<tr>
   		<c:forEach var="data" items="${rowlist}">
      		<td>${data} </td>
    	</c:forEach>
    </tr>
  </c:forEach>
</table>

<%@ include file="/WEB-INF/jsp/footer.jsp" %>
