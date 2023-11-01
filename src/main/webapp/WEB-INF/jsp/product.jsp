<%@include file="head.jsp"%>
<%@include file="header.jsp"%>
<div class="container" id="main">
	<div class="row">
		<div class="col-sm-3 col-md-3 col-lg-2 sidebar"></div>
		<div id="MainImage" class="col-sm-6 col-lg-8">
			<h2>Added Product to Cart</h2>

			<div class="row">
				<input type="button" class="btn errorbtn" value="Back to Shop"
					onclick="location.href = '<c:url value='/ui/products' />';"> <input
					type="button" class="btn errorbtn" value="Go to Cart"
					onclick="location.href = '<c:url value='/ui/cart' />';">
			</div>
		</div>
	</div>
</div>
<%@include file="footer.jsp"%>