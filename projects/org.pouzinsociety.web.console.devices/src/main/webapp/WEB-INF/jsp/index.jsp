<jsp:include page="/../top.jsp"/>

<h1>Devices</h1>
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

<jsp:include page="/../bottom.jsp"/>

