package com.smart.controller;

import java.security.Principal;
import java.util.Map;

import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseBody;

import com.razorpay.Order;
import com.razorpay.RazorpayClient;
import com.razorpay.RazorpayException;
import com.smart.dao.MyOrderRepository;
import com.smart.dao.UserRepository;
import com.smart.entities.MyOrder;

@Controller
public class PaymentController {
	
	@Autowired
	private UserRepository userRepository;
	
	@Autowired
	private MyOrderRepository myOrderRepository;
	
	@PostMapping("/user/create_order")
	@ResponseBody
	public String createOrder(@RequestBody Map<String, Object> data, Principal principal) throws Exception {

		try {
			System.out.println("Order function executed");
			int amt = Integer.parseInt(data.get("amount").toString());
			RazorpayClient client = new RazorpayClient("rzp_test_HQCbinlJcSivyn", "aqVxr7yZLiYpb7dSMUDSEDn1");
			JSONObject obj = new JSONObject();
			obj.put("amount", amt * 100);
			obj.put("currency", "INR");
			obj.put("receipt", "txn_123456");
			Order order = client.Orders.create(obj);
			System.out.println("Order created !!");
			
			// store order details to database
			MyOrder myOrder = new MyOrder();
			int amtInRupees = order.get("amount");
			myOrder.setAmount(amtInRupees/100);
			myOrder.setOrderId(order.get("id"));
			myOrder.setPaymentId(null);
			myOrder.setReceipt(order.get("receipt"));
			myOrder.setStatus("Created");
			myOrder.setUser(this.userRepository.getUserByUserName(principal.getName()));
			
			this.myOrderRepository.save(myOrder);
			System.out.println("Order details saved in DB successfully !!");
			
			return order.toString();

		} catch (Exception e) {
			e.printStackTrace();
			return "error";
		}
	}
	
	//Handler for updating payment details
	@PostMapping("/user/update-paymentDetais")
	public ResponseEntity<?> updatePaymentDetails(@RequestBody Map<String, Object> data){
		
		//get order details by order id
		MyOrder myOrder = this.myOrderRepository.findByOrderId(data.get("order_id").toString());
		myOrder.setPaymentId(data.get("payment_id").toString());
		myOrder.setStatus(data.get("status").toString());
		this.myOrderRepository.save(myOrder);
		System.out.println("Payment details updated successfully !!");
		return ResponseEntity.ok(Map.of("msg","Payment Details Updated "));
	}
}
