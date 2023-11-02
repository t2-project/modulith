<%@include file="head.jsp"%>
<%@include file="header.jsp"%>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>
<div class="container" id="main">
	<div class="row">
		<div class="col-sm-9 col-md-6  col-lg-8">
			<h2 class="minipage-title">Shopping Cart</h2>
			<!--  -->
				<table class="table table-bordered">
					<thead>
						<tr>
							<th><b>Item ID</b></th>
							<th><b>Product Name</b></th>
							<th><b>Description</b></th>

							<th><b>Quantity</b></th>
							<th><b>List Price</b></th>
							<th><b>Total Cost</b></th>
							<th>Remove</th>
						</tr>
					</thead>
					<tbody>
						<c:set var="count" value="0" scope="page" />
						<c:forEach items="${OrderItems}" var="orderItem">
						<form:form action="delete" method="POST" modelAttribute="item">
							<tr>
								
								<td>${orderItem.id}
								<form:input type='hidden' path="productId" value="${orderItem.id}"/></td>
								<td>${orderItem.name}</td>
								<td>${orderItem.description}</td>
								<td>${orderItem.units}
								<form:input type='hidden' path="units" value="${orderItem.units}"/></td>
								<!-- 
								<td><input required min="1" name="orderitem_${orderItem.id}"
									type="number" class="quantity" value="${orderItem.units}"></td>
							    -->
								<td><fmt:formatNumber
										value="${orderItem.price}" type="currency"
										currencySymbol="$" /></td>
								<td><fmt:formatNumber
										value="${orderItem.price*orderItem.units}"
										type="currency" currencySymbol="$" /></td>
								<c:set var="count"
									value="${count+(orderItem.price*orderItem.units)}"
									scope="page" />
								<td><button type="submit" class="submit-with-icon"
										name="delete">
										<span class="glyphicon glyphicon-trash" aria-hidden="true"></span>
									</button></td>
							</tr>
						</form:form>
						</c:forEach>
						<c:if test="${empty OrderItems}">
							<tr>
								<td colspan="7"><b>Your cart is empty.</b></td>
							</tr>
						</c:if>
						<!-- tr>
							<td colspan="7">Total: <fmt:formatNumber value="${count}"
									type="currency" currencySymbol="$" /> <input
								name="update" class="btn" value="Update Cart" type="submit"></td>
						</tr-->
					</tbody>
				</table>
			<!--  -->
			<form action="confirm" method="GET">	
				<c:if test="${!empty OrderItems}">
					<input name="confirm" class="btn" value="Proceed to Checkout"
						type="submit">
				</c:if>
			</form>
		</div>
	</div>
</div>
<%@include file="footer.jsp"%>