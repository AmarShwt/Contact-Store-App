console.log("this is script file!!")

const toggleSideBar= () =>{
    
    if($(".sidebar").is(":visible")){
		//true >> Close sidebar
		$(".sidebar").css("display","none");
		$(".content").css("margin-left","0%");
	}else{
		//false >> show sidebar
		$(".sidebar").css("display","block");
		$(".content").css("margin-left","20%");
	}
};

const search = () => {
	//console.log("searching...");
	let query = $("#search-input").val();

	if (query == '') {
		$(".search-result").hide();
	} else {
		//search
		//console.log(query);
		let url = `http://localhost:8081/search/${query}`
		fetch(url).then((response) => {
			return response.json();
		}).then((data) => {
			//console.log(data);
			let text = `<div class="list-group">`
			data.forEach((contact) => {
				text += `<a href="/user/${contact.cId}/contact" class='list-group-item list-group-item-action'> ${contact.name} </a>`
			});
			text += `</div>`
			$(".search-result").html(text);
			$(".search-result").show();
		});
	}
}


// Payment gateway code

// First request to server to create order
const paymentStart=()=>{
	console.log("Payment starts...");
	var amount = $("#payment_feild").val();
	console.log(amount);
	if(amount == '' || amount == null){
		//alert("Amount is required !!");
		swal("Warning","Amount is required !!", "info");
		return;
	}
	// using ajax to send request to server to create order jquery
	$.ajax(
		{
			url:'/user/create_order',
			data:JSON.stringify({amount : amount,info:'order_request'}),
			contentType:'application/json',
			type:'POST',
			dataType:'json',
			success:function(response){
				console.log(response);
				if(response.status == "created"){
					let options = {
					"key":"rzp_test_HQCbinlJcSivyn",
					"amount":"response.amount",
					"currency":"INR",
					"name":"Smart Contact Manager",
					"description":"Service Charges",
					"image":"https://www.club4ca.com/formats/letter-formats/payment-release-request-letter/",
					"order_id":response.id,
					"handler":function(response){
						console.log(response.razorpay_payment_id);
						console.log(response.razorpay_order_id);
						console.log(response.razorpay_signature);
						//call method to update payment sucess details on server
						updatePaymentDetails(response.razorpay_payment_id, response.razorpay_order_id, 'Paid');
						
					},
					"prefill":{
						"name":"",
						"dscription":"",
						"contact":""
					},
					"notes":{
						"address":"App by Amar"
					},
					"theme":{
						"color":"#3399cc"
					}
				};
				
				var rzp = new Razorpay(options);
				rzp.on('payment.failed', function(response){
					console.log(response.error.code);
			        console.log(response.error.description);
			        console.log(response.error.source);
			        console.log(response.error.step);
			        console.log(response.error.reason);
			        console.log(response.error.metadata.order_id);
			        console.log(response.error.metadata.payment_id);
			        swal("Failed !!","Oops.. Payment Failed !!", "error");
				});
				rzp.open();
    			//e.preventDefault();
				}
				
			},
			error:function(error){
				console.log(error);
				alert("Something went wrong !!");
			}
		}
	)
}

// Updateing paymetn success details on server
function updatePaymentDetails(payment_id, order_id,status){
	$.ajax({
		url:"/user/update-paymentDetais",
		data:JSON.stringify({
			payment_id:payment_id, order_id:order_id, status:status
		}),
		dataType:'json',
		contentType:"application/json",
		type:"Post",
		success:function(response){
			console.log(response);
			swal("Success","Payment done successfully !!", "success");
		},
		error:function(error){
			console.log(error);
			swal("Info !!","You payment is successful but we did not get it on server. We will get bacj to you soon !!", "info");
		}
	})
}




















