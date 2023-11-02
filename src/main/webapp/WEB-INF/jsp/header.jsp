<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%-- static navbar --%>
<nav id="headnav" class="navbar navbar-default container">
	<div class="container-fluid">
		<div class="navbar-header">
			<button id="navbarbutton" type="button" class="navbar-toggle collapsed"
				data-toggle="collapse" data-target="#navbar" aria-controls="navbar">
				<span class="sr-only">Toggle navigation</span> <span
					class="icon-bar"></span> <span class="icon-bar"></span> <span
					class="icon-bar"></span>
			</button>
			<a class="navbar-brand" href="<c:url value="/ui/products"/>">
    <img src="<c:url value="/resources/images/front.png"/>" width="30" height="30" class="d-inline-block align-top" alt="">
    T2-Project</a>
		</div>
		<div id="navbar" class="navbar-collapse collapse">
			<ul class="nav navbar-nav navbar-right headnavbarlist">
				<li><a href="<c:url value="/ui/cart"/>"><span
						class="glyphicon glyphicon-shopping-cart" aria-hidden="true"></span></a></li>
			</ul>
		</div>
	</div>
	<c:if test="${not empty message}">
		<div class="alert alert-success alert-dismissable" role="alert">
			<a href="#" class="close" data-dismiss="alert" aria-label="close">&times;</a>
			<strong>Success!</strong> ${message}
		</div>
	</c:if>
	<c:if test="${not empty errormessage}">
		<div class="alert alert-warning alert-dismissable" role="alert">
			<a href="#" class="close" data-dismiss="alert" aria-label="close">&times;</a>
			<strong>Warning!</strong> ${errormessage}
		</div>
	</c:if><%--/.container-fluid --%>
</nav>