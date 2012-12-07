package com.qorporation.popacross.logic;

import java.util.Map;

import com.paypal.sdk.core.nvp.NVPDecoder;
import com.paypal.sdk.core.nvp.NVPEncoder;
import com.paypal.sdk.profiles.APIProfile;
import com.paypal.sdk.profiles.CertificateAPIProfile;
import com.paypal.sdk.services.NVPCallerServices;
import com.qorporation.popacross.entity.definition.Item;
import com.qorporation.popacross.entity.definition.User;
import com.qorporation.qluster.entity.Entity;
import com.qorporation.qluster.logic.LogicController;
import com.qorporation.qluster.util.ErrorControl;

public class CommerceLogic extends LogicController {
	private static final String BILLING_PROPERTY_NAMESPACE = "ppacs_billing";
	private static final String PAYPAL_API_USERNAME = "";
	private static final String PAYPAL_API_PASSWORD = "";
	
	private ItemLogic itemLogic = null;
	
	@Override
	public void init() {
		this.itemLogic = this.logicService.get(ItemLogic.class);
	}
	
	public static class BillingParams {
		String paymentType = null;
		String ccType = null;
		String ccNum = null;
		String ccVerify = null;
		Integer expMonth = null;
		Integer expYear = null;
		String billingFirstName = null;
		String billingLastName = null;
		String billingEmail = null;
		String billingAddr1 = null;
		String billingAddr2 = null;
		String billingCity = null;
		String billingProvince = null;
		String billingPostal = null;
		String billingCountry = null;
		String billingPhone = null;
	}
	
	public boolean purchase(Entity<Item> item, Entity<User> user, BillingParams billing, int amount) {
		Map<String, String> itemParams = this.itemLogic.getPropertyMap(item, CommerceLogic.BILLING_PROPERTY_NAMESPACE);
		if (itemParams.size() == 0) return false;
		
		int remaining = Integer.parseInt(itemParams.get("count"));
		if (remaining == 0) {
			return false;
		}
		
		if (remaining > 0 && !this.itemLogic.updateProperty(item, CommerceLogic.BILLING_PROPERTY_NAMESPACE, "count", itemParams.get("count"), Integer.toString(remaining - amount))) {
			return false;
		}
		
		float price = Float.parseFloat(itemParams.get("price"));
		float total = price * (amount * 1.f);
		
		try {
			APIProfile apiProfile = new CertificateAPIProfile();
			apiProfile.setAPIUsername(CommerceLogic.PAYPAL_API_USERNAME);
			apiProfile.setAPIPassword(CommerceLogic.PAYPAL_API_PASSWORD);
			NVPCallerServices caller = new NVPCallerServices();
			caller.setAPIProfile(apiProfile);
			
			NVPEncoder encoder = new NVPEncoder();
			
			encoder.add("METHOD","DoDirectPayment");
			encoder.add("PAYMENTACTION", billing.paymentType);
			encoder.add("AMT", Float.toString(total));
			encoder.add("CREDITCARDTYPE", billing.ccType);		
			encoder.add("ACCT", billing.ccNum);						
			encoder.add("EXPDATE", billing.expMonth.toString() + billing.expYear.toString());
			encoder.add("CVV2", billing.ccVerify);
			encoder.add("FIRSTNAME", billing.billingFirstName);
			encoder.add("LASTNAME", billing.billingLastName);										
			encoder.add("STREET", billing.billingAddr1);
			encoder.add("CITY", billing.billingCity);	
			encoder.add("STATE", billing.billingProvince);			
			encoder.add("ZIP", billing.billingPostal);	
			encoder.add("COUNTRYCODE", billing.billingCountry);											
			encoder.add("CURRENCYCODE", itemParams.get("currency"));		
			String NVPString = encoder.encode();
	
			String res = caller.call(NVPString);	
			NVPDecoder decoder = new NVPDecoder();			
			decoder.decode(res);				
			String strAck = decoder.get("ACK"); 
			if(strAck != null && !(strAck.equals("Success") || strAck.equals("SuccessWithWarning"))) {
				return true;
			}
		} catch (Exception e) {
			ErrorControl.logException(e);
		}
		
		this.itemLogic.updateProperty(item, CommerceLogic.BILLING_PROPERTY_NAMESPACE, "count", Integer.toString(remaining - amount), itemParams.get("count"));
		
		return false;
	}
	
}
