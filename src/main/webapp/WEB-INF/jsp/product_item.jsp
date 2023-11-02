<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="http://www.springframework.org/tags/form" prefix="form" %>
<%@ page contentType="text/html;charset=UTF-8" %>
<div class="thumbnail">
	<form:form action="add" method="POST" modelAttribute="item">
	<table><tr><td class ="productthumb">
		<form:input type='hidden' path="productId" value="${product.id}"/>
		
		</td> <%-- <div class="divider col-sm-1"></div> --%>
		<td class="divider"></td>
		<td class="description">
			<b>${product.name}</b> <br> 
			<span> Price: <fmt:formatNumber value="${product.price}" type="currency" currencySymbol="$" /></span> <br> 
			<span> Units: ${product.units} </span> <br> 
			<span>${product.description}</span><br>
			<span><form:label path="units">units: </form:label> <form:input type="number" min="0" path="units"/></span> 
			
		</td></tr></table><input name="add" class="btn" value="Add to Cart" type="submit">
	</form:form>
</div>