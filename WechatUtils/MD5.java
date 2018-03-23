package com.taoart.common.utils;

import java.security.MessageDigest;
import java.util.UUID;

import org.apache.commons.codec.binary.Hex;
import org.apache.commons.codec.digest.Md5Crypt;
import org.apache.commons.lang.math.RandomUtils;

public class MD5 {
	private static final String CHARS = "0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ";

	public static String encode(String origin) {
		try {
			MessageDigest md = MessageDigest.getInstance("MD5");
			return Hex.encodeHexString(md.digest(origin.getBytes()));
		} catch (Exception ex) {
			throw new RuntimeException(ex.getMessage(), ex);
		}
	}

	public static String encodeUTF8(String origin) {
		try {
			MessageDigest md = MessageDigest.getInstance("MD5");
			return Hex.encodeHexString(md.digest(origin.getBytes("utf-8")));
		} catch (Exception ex) {
			throw new RuntimeException(ex.getMessage(), ex);
		}
	}

	public static String encode(String origin, String salt) {
		try {
			return Md5Crypt.md5Crypt(origin.getBytes(), "$1$" + salt);
		} catch (Exception ex) {
			throw new RuntimeException(ex.getMessage(), ex);
		}
	}

	public static String encode2(String origin) {
		try {
			String result = Md5Crypt.md5Crypt(origin.getBytes(), "$1$" + randomSalt(8));
			return Hex.encodeHexString(result.getBytes());
		} catch (Exception ex) {
			throw new RuntimeException(ex.getMessage(), ex);
		}
	}

	private static String randomSalt(int len) {
		StringBuffer buffer = new StringBuffer();
		for (int index = 0; index < len; index++) {
			buffer.append(CHARS.charAt(RandomUtils.nextInt(CHARS.length())));
		}
		return buffer.toString();
	}

	public static void main(String[] args) {
		System.err.println(encode("Message"));
		System.err.println(encode("Message", "GHYGGGFK"));
		System.err.println(encode(UUID.randomUUID().toString()));
		System.err.println(encode(UUID.randomUUID().toString()));
		byte[] b = new byte[] { 123 };

		System.out.println("==>" + Hex.encodeHexString(b));
		System.out.println(Md5Crypt.md5Crypt("aaa".getBytes(), "^1325293881573$"));
	}
}
