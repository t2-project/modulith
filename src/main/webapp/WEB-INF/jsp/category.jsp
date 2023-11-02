<%@include file="head.jsp"%>
<%@include file="header.jsp"%>
<div class="container" id="main">
	<div class="row">
		<div class="col-md-9 col-lg-10 col-sm-12">
			<h2 class="minipage-title">${category}</h2>
			<div class="row">
				<c:forEach items="${productslist}" var="product" varStatus="loop">
					<div class="col-sm-6 col-md-4 col-lg-3 placeholder">
						<%@include file="product_item.jsp"%>
					</div>
				</c:forEach>
			</div>
		</div>
	</div>
</div>
<%@include file="footer.jsp"%>