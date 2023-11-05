console.log("this is script")

const toggleSidebar = () => {
	if ($('.sidebar').is(":visible")) {
		$(".sidebar").css("display", "none");
		$(".content").css("margin-left", "0%");

	}
	else {
		$(".sidebar").css("display", "block");
		$(".content").css("margin-left", "20%");



	}

};

//request to server to create order
const paymentStart = () => {
	console.log("payment started");
	let amount = $("#payment_field").val()
	console.log(amount);
	if (amount == '' || amount == null) {
		console.log(jQuery.fn.jquery);
		alert("amount is required ");
		return;
	}
	// now i am using jquery ajax
	console.log("before ajax");
	$.ajax({
		url: "/user/create_order",
		data: JSON.stringify({ amount: amount, info: "order_request" }),
		contentType: "application/json",
		type: "POST",
		dataType: "json",
		success: function(response) {
			//invoked when success
			console.log(response);
			if (response.status == 'created') {
				//open payment form
				let options = {
					key: "rzp_test_w14dnADMuDa97D",
					amount: response.amount,
					currency: "INR",
					name: "Smart contact manager",
					description: "Donation",
					image: "https://kkdial.infinityfreeapp.com/images/logo.jpg",
					order_id: response.id,
					handler: function(response) {
						console.log(response.razorpay_payment_id);
						console.log(response.razorpay_order_id);
						console.log(response.razorpay_signature);
						console.log("payment successful");
						
						updatePaymentOnServer(response.razorpay_payment_id,response.razorpay_order_id,"paid");
						
						
					},
					prefill: {
						name: "",
						email: "",
						contact: "",
					},
					notes: {
						address: "learn with kishlay",
					},
					theme: {
						color: "#3399cc",
					},

				};
				let rzp = new Razorpay(options);

				rzp.on('payment.failed', function(response) {
					console.log(response.error.code);
					console.log(response.error.description);
					console.log(response.error.source);
					console.log(response.error.step);
					console.log(response.error.reason);
					console.log(response.error.metadata.order_id);
					console.log(response.error.metadata.payment_id);
					alert("oops payment failed");
				});


				rzp.open();


			}

		},
		error: function(error) {
			//invokde when error
			console.log(error);
			alert("something went wrong");
		},

	});
	console.log("after ajax");
};

function updatePaymentOnServer(payment_id,order_id,status)
{
	$.ajax({
		url: "/user/update_order",
		data: JSON.stringify({payment_id: payment_id, order_id: order_id,status:status}),
		contentType: "application/json",
		type: "POST",
		dataType: "json",
		success:function(response){
			alert("congratulation: thankyou for donation");
		},
		error:function(error){
			alert("You payment is successfull But we didnot capture it on server, We will contact you ASAP...");
		},
	});
}