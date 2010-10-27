<%@ include file="/WEB-INF/jsp/includes.jsp" %>
<%@ include file="/WEB-INF/jsp/header.jsp" %>

<h2>Stack Devices:</h2>

<table>
  <tr>
  <thead><th>Name</th><th>H/W Address</th><th>MTU</th><th>IP Address</th></thead>
  </tr>
  <c:forEach var="device" items="${deviceList}">
    <tr>
      <td>${device.name} </td>
      <td>${device.address} </td>
      <td>${device.mtu} </td>
      <td>${device.protocolAddress} </td>
    </tr>
  </c:forEach>
</table>

<%@ include file="/WEB-INF/jsp/footer.jsp" %>