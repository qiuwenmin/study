import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.net.URL;
import java.rmi.ConnectException;
import java.security.KeyStore;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.servlet.http.HttpServletRequest;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.SSLContexts;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;


import com.alibaba.dubbo.common.utils.StringUtils;

public class WechatPayUtils {
	
	private static final Log log = LogFactory.getLog(WechatPayUtils.class);
	// 微信统一支付接口
	public static final String PAY_URL = "https://api.mch.weixin.qq.com/pay/unifiedorder";
	public static final String WECHAT_APPID = "wx624ff6dce1c2965f";
	public static final String MCH_ID = "1389234902";
	public static final String WECHAT_TRADE_TYPE = "NATIVE";
	public static final String WECHAT_PAY_KEY = "SKLJX5621uujKJAK154dasDADF1200AD";
	public static final String BAR_PAY_URL = "https://api.mch.weixin.qq.com/pay/micropay";
	public static final String QUERY_URL = "https://api.mch.weixin.qq.com/pay/orderquery";
	public static final String wechat_pay_notify_url = "wechat_pay_notify_url";
	public static final String BAR_PAY_CANCLE_URL = "https://api.mch.weixin.qq.com/secapi/pay/reverse";
	public static final String REFUND_URL = "https://api.mch.weixin.qq.com/secapi/pay/refund";

	/**
	 * 微信统一支付,生成二维码链接
	 * 
	 * @Title: unifiedorder
	 * @Description: TODO
	 * @Param @param money 订单金额
	 * @Param @param prepay_id 微信预支付订单号(如果必须传)
	 * @Param @param attach (orders[订单支付]、recharge[充值] guarantee[消费保证金])
	 * @Throws
	 */

	public static JsonResponse unifiedorder(String orderSerial, String money, String attach, int userId) {
		JsonResponse json = new JsonResponse();
		json.setSuccess(true);
		String callBack = wechat_pay_notify_url;
		if (StringUtils.isBlank(callBack)) {
			json.setSuccess(false);
			json.setErrorMessage("请配置回调地址");
			return json;
		}
		if (!"orders".equals(attach) && !"recharge".equals(attach) && !"knowledge".equals(attach)) {
			json.setSuccess(false);
			json.setErrorMessage("请传入支付类型");
			return json;
		}
		int _money = new BigDecimal(money.replace(",", "")).multiply(new BigDecimal(100)).intValue();
		String body = "杭州淘艺术网-充值";
		if ("orders".equals(attach) || "knowledge".equals(attach)) {
			body = "杭州淘艺术网-支付";
		}
		String time_start = ""; 
				//DateUtils.dateToStr(new Date(), "yyyyMMddHHmmss");
		//Calendar c = Calendar.getInstance();
		//c.add(Calendar.MINUTE, 20);
		String time_expire = "123456";
		String nonce_str = UUID.randomUUID().toString().replace("-", "");
		attach += ("#" + userId);
		String md5 = "appid=" + WECHAT_APPID + "&attach=" + attach + "&body=" + body + "&mch_id=" + MCH_ID
				+ "&nonce_str=" + nonce_str + "&notify_url=" + callBack + "&out_trade_no=" + orderSerial
				+ "&time_expire=" + time_expire + "&time_start=" + time_start + "&total_fee=" + _money + "&trade_type="
				+ WECHAT_TRADE_TYPE + "&key=" + WECHAT_PAY_KEY;
		String sign = MD5.encode(md5).toUpperCase();

		String param = "<xml><appid>" + WECHAT_APPID + "</appid>" // appid
				+ "<attach>" + attach + "</attach>" // attach
				+ "<body>" + body + "</body>" // body
				+ "<mch_id>" + MCH_ID + "</mch_id>" // 商户名称
				+ "<nonce_str>" + nonce_str + "</nonce_str>" // 随机字符串，不长于32位
				+ "<notify_url>" + callBack + "</notify_url>" // 回调地址
				+ "<out_trade_no>" + orderSerial + "</out_trade_no>" // 订单流水
				+ "<total_fee>" + _money + "</total_fee>" // 总金额 单位分
				+ "<trade_type>" + WECHAT_TRADE_TYPE + "</trade_type>" // 交易类型
				+ "<time_start>" + time_start + "</time_start>" // 开始时间
				+ "<time_expire>" + time_expire + "</time_expire>" // 开始时间
				+ "<sign>" + sign + "</sign>" + "</xml>";
		// 解析xml
		try {
			Map<String, Object> map = new HashMap<String, Object>();
			String xml = payRequest(PAY_URL, "POST", param);
			log.info(xml);
			map = getMapFromXML(xml);
			log.info(map);
			if (null != map.get("code_url") && !"".equals(map.get("code_url").toString())) {
				json.setResult(map.get("code_url"));
			} else {
				json.setErrorMessage(map.get("return_msg").toString());
				json.setSuccess(false);
			}
			return json;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return json;
	}
	
	/**
	 * 发送https请求
	 * 
	 * @param requestUrl
	 *            请求地址
	 * @param requestMethod
	 *            请求方式（GET、POST）
	 * @param outputStr
	 *            提交的数据
	 * @return JSONObject(通过JSONObject.get(key)的方式获取json对象的属性值)
	 */
	public static String payRequest(String requestUrl, String requestMethod, String outputStr) {
		try {
			URL url = new URL(requestUrl);
			HttpsURLConnection conn = (HttpsURLConnection) url.openConnection();
			conn.setDoOutput(true);
			conn.setDoInput(true);
			conn.setUseCaches(false);
			// 设置请求方式（GET/POST）
			conn.setRequestMethod(requestMethod);
			// 当outputStr不为null时向输出流写数据
			if (null != outputStr) {
				OutputStream outputStream = conn.getOutputStream();
				// 注意编码格式
				outputStream.write(outputStr.getBytes("UTF-8"));
				outputStream.close();
			}
			// 从输入流读取返回内容
			InputStream inputStream = conn.getInputStream();
			InputStreamReader inputStreamReader = new InputStreamReader(inputStream, "utf-8");
			BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
			String str = null;
			StringBuffer buffer = new StringBuffer();
			while ((str = bufferedReader.readLine()) != null) {
				buffer.append(str);
			}
			// 释放资源
			bufferedReader.close();
			inputStreamReader.close();
			inputStream.close();
			inputStream = null;
			conn.disconnect();
			return buffer.toString();
		} catch (ConnectException ce) {
			log.error("连接超时：{}", ce);
		} catch (Exception e) {
			log.error("https请求异常：{}", e);
		}
		return null;
	}

	public static Map<String, Object> getMapFromXML(String xmlString)
			throws ParserConfigurationException, IOException, SAXException {

		// 这里用Dom的方式解析回包的最主要目的是防止API新增回包字段
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		DocumentBuilder builder = factory.newDocumentBuilder();
		InputStream is = getStringStream(xmlString);
		Document document = builder.parse(is);

		// 获取到document里面的全部结点
		NodeList allNodes = document.getFirstChild().getChildNodes();
		Node node;
		Map<String, Object> map = new HashMap<String, Object>();
		int i = 0;
		while (i < allNodes.getLength()) {
			node = allNodes.item(i);
			if (node instanceof Element) {
				map.put(node.getNodeName(), node.getTextContent());
			}
			i++;
		}
		return map;
	}

	public static InputStream getStringStream(String sInputString) {
		ByteArrayInputStream tInputStringStream = null;
		if (sInputString != null && !sInputString.trim().equals("")) {
			tInputStringStream = new ByteArrayInputStream(sInputString.getBytes());
		}
		return tInputStringStream;
	}
	
	
	/**
	 * 微信统一支付,生成二维码链接
	 * 
	 * @Title: unifiedorder
	 * @Description: TODO
	 * @Param @param money 订单金额
	 * @Param @param prepay_id 微信预支付订单号(如果必须传)
	 * @Param @param attach (orders[订单支付]、recharge[充值] guarantee[消费保证金])
	 * @Throws
	 */

	public static JsonResponse refund(String orderSerial, String totalMoney,String refundMoney, String refundNo) {
		log.info("【微信退款】订单编号"+orderSerial + "申请退款,金额为" + refundMoney);
		JsonResponse json = new JsonResponse();
		json.setSuccess(true);
		int _refundMoney = new BigDecimal(refundMoney.replace(",", "")).multiply(new BigDecimal(100)).intValue();
		int _totalMoney = new BigDecimal(totalMoney.replace(",", "")).multiply(new BigDecimal(100)).intValue();
		Calendar c = Calendar.getInstance();
		c.add(Calendar.MINUTE, 20);
		String nonce_str = UUID.randomUUID().toString().replace("-", "");
		String md5 = "appid=" + WECHAT_APPID + "&mch_id=" + MCH_ID
				+ "&nonce_str=" + nonce_str + "&op_user_id=" + MCH_ID + "&out_refund_no=" + refundNo + "&out_trade_no=" + orderSerial
				+ "&refund_fee=" + _refundMoney + "&total_fee=" + _totalMoney + "&key=" + WECHAT_PAY_KEY;
		String sign = MD5.encode(md5).toUpperCase();

		String param = "<xml><appid>" + WECHAT_APPID + "</appid>" // appid
				+ "<mch_id>" + MCH_ID + "</mch_id>" // 商户名称
				+ "<nonce_str>" + nonce_str + "</nonce_str>" // 随机字符串，不长于32位
				+ " <op_user_id>"+MCH_ID+"</op_user_id>" //操作员账号 默认商户号
				+ "<out_refund_no>" + refundNo + "</out_refund_no>" // 退款流水
				+ "<out_trade_no>" + orderSerial + "</out_trade_no>" // 订单流水
				+ "<refund_fee>" + _refundMoney + "</refund_fee>" // 退款金额 单位分
				+ "<total_fee>" + _totalMoney + "</total_fee>" // 总金额 单位分
				+ "<sign>" + sign + "</sign>" + "</xml>";
		// 解析xml
		try {
			Map<String, Object> map = new HashMap<String, Object>();
			//String xml = refundRequest(REFUND_URL, "POST", param);
			String xml = doRefund(REFUND_URL,param);
			log.info(xml);
			map = getMapFromXML(xml);
			log.info(map);
			if (null != map.get("result_code") && !"".equals(map.get("result_code").toString())) {
				json.setResult(map.get("transaction_id"));
			} else {
				log.info("【微信退款】订单编号"+orderSerial + "退款失败");
				json.setSuccess(false);
				return json;
			}
			log.info("【微信退款】订单编号"+orderSerial + "退款成功");
			return json;
		} catch (Exception e) {
			e.printStackTrace();
		}
		log.info(json);
		return json;
	}
	
	/**
	 * 微信统一支付
	 * 
	 * @Title: barPay
	 * @Description: TODO
	 * @Param @param money 订单金额
	 * @Param @param prepay_id 微信预支付订单号(如果必须传)
	 * @Param @param attach (orders[订单支付]、recharge[充值] guarantee[消费保证金])
	 * @Throws
	 */

	public static JsonResponse cancleBarPay(String orderSerial,HttpServletRequest request) {
		JsonResponse json = new JsonResponse();
		json.setSuccess(true);
		String nonce_str = UUID.randomUUID().toString().replace("-", "");
		String md5 = "appid=" + WECHAT_APPID+ "&mch_id=" + MCH_ID
				+ "&nonce_str=" + nonce_str + "&out_trade_no=" + orderSerial
				+ "&key=" + WECHAT_PAY_KEY;
		String sign = MD5.encode(md5).toUpperCase();
		String param = "<xml><appid>" + WECHAT_APPID + "</appid>" // appid
				+ "<mch_id>" + MCH_ID + "</mch_id>" // 商户名称
				+ "<nonce_str>" + nonce_str + "</nonce_str>" // 随机字符串，不长于32位
				+ "<out_trade_no>" + orderSerial + "</out_trade_no>" // 订单流水
				+ "<sign>" + sign + "</sign>" + "</xml>";
		// 解析xml
		try {
			Map<String, Object> map = new HashMap<String, Object>();
			String xml = doCancle(BAR_PAY_CANCLE_URL, param,request);
			log.info(xml);
			map = getMapFromXML(xml);
			log.info(map);
			if("SUCCESS".equals(map.get("return_code")) && "SUCCESS".equals(map.get("result_code"))){
				
			}
			else{
				json.setErrorMessage("交易失败");
				json.setSuccess(false);
			}
			return json;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return json;
	}
	
	public static String doCancle(String url,String data,HttpServletRequest request) throws Exception {  
        /** 
         * 注意PKCS12证书 是从微信商户平台-》账户设置-》 API安全 中下载的 
         */  
        KeyStore keyStore  = KeyStore.getInstance("PKCS12");  
        FileInputStream instream = new FileInputStream(new File(request.getSession().getServletContext().getRealPath("/cer/apiclient_cert.p12")));//P12文件目录  
        try {  
            /** 
             * 此处要改 
             * */  
            keyStore.load(instream, MCH_ID.toCharArray());//这里写密码..默认是你的MCHID  
        } finally {  
            instream.close();  
        }  
  
        // Trust own CA and all self-signed certs  
        /** 
         * 此处要改 
         * */  
        SSLContext sslcontext = SSLContexts.custom()  
                .loadKeyMaterial(keyStore, MCH_ID.toCharArray())//这里也是写密码的    
                .build();  
        // Allow TLSv1 protocol only  
        SSLConnectionSocketFactory sslsf = new SSLConnectionSocketFactory(  
                sslcontext,  
                new String[] { "TLSv1" },  
                null,  
                SSLConnectionSocketFactory.BROWSER_COMPATIBLE_HOSTNAME_VERIFIER);  
        CloseableHttpClient httpclient = HttpClients.custom()  
                .setSSLSocketFactory(sslsf)  
                .build();  
        try {  
            HttpPost httpost = new HttpPost(url); // 设置响应头信息  
            httpost.addHeader("Connection", "keep-alive");  
            httpost.addHeader("Accept", "*/*");  
            httpost.addHeader("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8");  
            httpost.addHeader("Host", "api.mch.weixin.qq.com");  
            httpost.addHeader("X-Requested-With", "XMLHttpRequest");  
            httpost.addHeader("Cache-Control", "max-age=0");  
            httpost.addHeader("User-Agent", "Mozilla/4.0 (compatible; MSIE 8.0; Windows NT 6.0) ");  
            httpost.setEntity(new StringEntity(data, "UTF-8"));  
            CloseableHttpResponse response = httpclient.execute(httpost);  
            try {  
                HttpEntity entity = response.getEntity();  
  
                String jsonStr = EntityUtils.toString(response.getEntity(), "UTF-8");  
                EntityUtils.consume(entity);  
               return jsonStr;  
            } finally {  
                response.close();  
            }  
        } finally {  
            httpclient.close();  
        }  
    }  
	
	
	public static String doRefund(String url,String data) throws Exception {  
        /** 
         * 注意PKCS12证书 是从微信商户平台-》账户设置-》 API安全 中下载的 
         */  
        KeyStore keyStore  = KeyStore.getInstance("PKCS12");  
        FileInputStream instream = new FileInputStream(new File("/cer/apiclient_cert.p12"));//P12文件目录  
        try {  
            /** 
             * 此处要改 
             * */  
            keyStore.load(instream, MCH_ID.toCharArray());//这里写密码..默认是你的MCHID  
        } finally {  
            instream.close();  
        }  
  
        // Trust own CA and all self-signed certs  
        /** 
         * 此处要改 
         * */  
        SSLContext sslcontext = SSLContexts.custom()  
                .loadKeyMaterial(keyStore, MCH_ID.toCharArray())//这里也是写密码的    
                .build();  
        // Allow TLSv1 protocol only  
        SSLConnectionSocketFactory sslsf = new SSLConnectionSocketFactory(  
                sslcontext,  
                new String[] { "TLSv1" },  
                null,  
                SSLConnectionSocketFactory.BROWSER_COMPATIBLE_HOSTNAME_VERIFIER);  
        CloseableHttpClient httpclient = HttpClients.custom()  
                .setSSLSocketFactory(sslsf)  
                .build();  
        try {  
            HttpPost httpost = new HttpPost(url); // 设置响应头信息  
            httpost.addHeader("Connection", "keep-alive");  
            httpost.addHeader("Accept", "*/*");  
            httpost.addHeader("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8");  
            httpost.addHeader("Host", "api.mch.weixin.qq.com");  
            httpost.addHeader("X-Requested-With", "XMLHttpRequest");  
            httpost.addHeader("Cache-Control", "max-age=0");  
            httpost.addHeader("User-Agent", "Mozilla/4.0 (compatible; MSIE 8.0; Windows NT 6.0) ");  
            httpost.setEntity(new StringEntity(data, "UTF-8"));  
            CloseableHttpResponse response = httpclient.execute(httpost);  
            try {  
                HttpEntity entity = response.getEntity();  
  
                String jsonStr = EntityUtils.toString(response.getEntity(), "UTF-8");  
                EntityUtils.consume(entity);  
               return jsonStr;  
            } finally {  
                response.close();  
            }  
        } finally {  
            httpclient.close();  
        }  
    }  

}
