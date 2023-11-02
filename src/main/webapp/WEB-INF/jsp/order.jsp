<%@include file="head.jsp"%>
<%@include file="header.jsp"%>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>
<%@ page contentType="text/html;charset=UTF-8" %>
<div class="container" id="main">
	<div class="row">
		<div class="col-sm-6 col-lg-8">
			<h2 class="minipage-title">Order</h2>
			<form:form action="confirm" method="POST" modelAttribute="details">
				<div class="row">
					<div class="col-sm-12 col-md-12 col-lg-4">
						<h4 class="minipage-title">Billing Address</h4>
						<div class="form-group row">
							<form:label path="cardOwner"
								class="col-sm-6 col-md-5 col-form-label col-form-label-lg">First
								Name</form:label>
							<div class="col-sm-6 col-md-7">
								<form:input type="text" class="form-control form-control-lg"
									path="cardOwner" placeholder="Joe" value="Jon"
									/>
							</div>
						</div>
						<div class="form-group row">
							<label for="lastname"
								class="col-sm-6 col-md-5 col-form-label col-form-label-lg">Last
								Name</label>
							<div class="col-sm-6 col-md-7">
								<input type="text" class="form-control form-control-lg"
									name="lastname" id="lastname" placeholder="Doe" value="Snow"
									required>
							</div>
						</div>
						<div class="form-group row">
							<label for="address1"
								class="col-sm-6 col-md-5 col-form-label col-form-label-lg">Address
								1</label>
							<div class="col-sm-6 col-md-7">
								<input type="text" class="form-control form-control-lg"
									name="address1" id="address1"
									placeholder="901 San Antonio Road" value="Winterfell" required>
							</div>
						</div>
					</div>
					<div class="col-sm-12 col-md-12 col-lg-4">
						<h4 class="minipage-title">Payment Details</h4>
						<div class="form-group row">
							<label for="cardtype"
								class="col-sm-6 col-md-5 col-form-label col-form-label-lg">Card
								Type</label>
							<div class="col-sm-6 col-md-7">
								<select class="form-control form-control-lg" name="cardtype"
									id="cardtype">
									<option value="volvo">Visa</option>
									<option value="saab">MasterCard</option>
									<option value="fiat">American Express</option>
								</select>
							</div>
						</div>
						<div class="form-group row">
							<form:label  path="cardNumber"
								class="col-sm-6 col-md-5 col-form-label col-form-label-lg">Card
								Number</form:label>
							<div class="col-sm-6 col-md-7">
								<form:input type="text" path="cardNumber"
									class="form-control form-control-lg" 
									placeholder="314159265359" value="314159265359"
									/>
							</div>
						</div>
						<div class="form-group row">
							<form:label  path="checksum" class="col-sm-6 col-md-5 col-form-label col-form-label-lg">Check Sum</form:label>
							<div class="col-sm-6 col-md-7">
								<form:input type="text" path="checksum" class="form-control form-control-lg" placeholder="299" value="299"/>
							</div>
						</div>
						<div class="form-group row">
							<label for="expirydate"
								class="col-sm-6 col-md-5 col-form-label col-form-label-lg">Expiry
								Date (MM/YYYY)</label>
							<div class="col-sm-6 col-md-7">
								<input type="text" class="form-control form-control-lg"
									name="expirydate" id="expirydate" placeholder="12/2025"
									value="12/2025" pattern="(0[1-9]|1[012])[/](19|20)\d\d"
									required>
							</div>
						</div>
					</div>
				</div>
				<input class="btn" name="confirm" value="Confirm" type="submit">
			</form:form>
		</div>
	</div>
</div>
<%@include file="footer.jsp"%>
