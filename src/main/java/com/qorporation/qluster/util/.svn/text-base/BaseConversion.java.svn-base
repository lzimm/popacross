package com.qorporation.qluster.util;

import java.math.BigInteger;
import java.util.UUID;

public class BaseConversion {
	public static final String BASE62DIGITS = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz";
	
	public static String toBase(String base, BigInteger number) {  
        String tempVal = number.intValue() == 0 ? "0" : "";  
        BigInteger baseLength = BigInteger.valueOf(base.length());
        while(number.intValue() != 0) {  
            int mod = number.mod(baseLength).intValue();  
            tempVal = base.substring(mod, mod + 1) + tempVal;  
            number = number.divide(baseLength);  
        }  
  
        return tempVal;  
    }
	
	public static String toBase(String base, UUID uuid) {
		return BaseConversion.toBase(base, BigInteger.valueOf(uuid.getMostSignificantBits()).shiftLeft(64).add(BigInteger.valueOf(uuid.getLeastSignificantBits())));
	}
	
}
