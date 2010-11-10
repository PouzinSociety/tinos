<%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<jsp:include page="/../top.jsp"/>

<br>
<br>
<form:form modelAttribute="dao">
  <table>
    <tr>
      <th>
        Text: <form:errors path="text" cssClass="errors"/>
        <br/>
        <form:input path="text" size="20" maxlength="30"/>
      </th>
    </tr>
    <tr>
      <td>
      	<br>
        <p class="submit"><input type="submit" value="Add Dao Now!"/></p>
      </td>
    </tr>
  </table>
</form:form>
<br>
<br>

<jsp:include page="/../bottom.jsp"/>