import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.UUID;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import com.alipay.api.AlipayClient;
import com.alipay.api.DefaultAlipayClient;
import com.alipay.api.internal.util.AlipaySignature;
import com.alipay.api.request.AlipayTradePrecreateRequest;
import com.alipay.api.request.AlipayTradeRefundRequest;
import com.alipay.api.response.AlipayTradePrecreateResponse;
import com.alipay.api.response.AlipayTradeRefundResponse;
import com.alipay.util.AlipayNotify;

public class AlipayUtils {
	
	
	final public static String appId = "";
	final public static String key = "";
	final public static String publicKey = "";
	public static String getWay = "https://openapi.alipay.com/gateway.do";
	private static final Log log = LogFactory.getLog(AlipayUtils.class);
	/**
	 * 前往支付
	 * 
	 * @Title: payOrder
	 * @Description: TODO
	 * @Param @param no 订单号
	 * @Param @param orderName 订单名称
	 * @Param @param money 价格
	 * @Param @return
	 * @Return ModelAndView
	 * @Throws
	 */
	public static JsonResponse doPay(@RequestParam(defaultValue = "") String no,
			@RequestParam(defaultValue = "") String orderName, @RequestParam String money, String _md5Str) {
		JsonResponse json = new JsonResponse();
		json.setSuccess(true);
		AlipayClient alipayClient = new DefaultAlipayClient("https://openapi.alipay.com/gateway.do", appId, key, "json",
				"utf-8", publicKey, "RSA");
		AlipayTradePrecreateRequest request = new AlipayTradePrecreateRequest();
		request.setNotifyUrl("notify_url");
		request.setBizContent("{" + "\"out_trade_no\":\"" + no + "\"," + "\"total_amount\":" + money + ","
				+ "\"subject\":\"" + orderName + "\"" + "  }");
		AlipayTradePrecreateResponse response = null;
		try {
			response = alipayClient.execute(request);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			json.setErrorMessage("系统出错了");
		}
		if (response.isSuccess()) {
			json.setResult(response.getQrCode());
		} else {
			json.setErrorMessage(response.getMsg());
		}
		return json;
	}
	
	/**
	 * 退款接口
	 * @param tradeNo 订单号
	 * @param refundMoney 退款金额
	 * @return
	 */
	public static JsonResponse reufnd(String tradeNo, String refundMoney) {

		JsonResponse jp = new JsonResponse();
		log.info("【支付宝退款】订单编号" + tradeNo + "申请退款,金额为" + refundMoney);
		AlipayClient alipayClient = new DefaultAlipayClient("https://openapi.alipay.com/gateway.do", appId, key, "json",
				"utf-8", publicKey, "RSA");
		AlipayTradeRefundRequest request = new AlipayTradeRefundRequest();

		request.setBizContent("{" + "    \"out_trade_no\":\"" + tradeNo + "\"," + "    \"refund_amount\":" + refundMoney
				+ "," + "    \"refund_reason\":\"正常退款\"" + "," + "    \"out_request_no\":\""
				+ UUID.randomUUID().toString() + "\"" + "  }");
		AlipayTradeRefundResponse response = null;
		try {
			response = alipayClient.execute(request);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			log.info("【支付宝退款】订单编号" + tradeNo + "退款失败");
		}
		if (response.isSuccess()) {
			log.info("【支付宝退款】订单编号" + tradeNo + "退款成功");
			jp.setSuccess(true);
			jp.setResult(response.getTradeNo());
			return jp;
		} else {
			log.info("【支付宝退款】订单编号" + tradeNo + "退款失败");
			jp.setSuccess(false);
			return jp;
		}
	}
	
	
	/**
	 * 购票-PC-支付宝支付-跳到支付页面1.0，为空的参数需要改成自己的参数
	 * 2.0网站支付请参考支付宝官方文档与二维码支付等类似
	 */
	@RequestMapping("doPay")
	public ModelAndView payOrder(@RequestParam(defaultValue = "") String no,
			@RequestParam(defaultValue = "") String orderName, @RequestParam String money, String _md5Str) {
		//String md5Str = MD5.encode(Constants.MD5_STR + money + no);
		log.info("【淘艺支付】支付宝支付，订单编号" + no);
//		if (!_md5Str.equals(md5Str)) {
//			return new ModelAndView("redirect:order/list-user-order.htm");
//		}
		ModelAndView mav = new ModelAndView("alipay/sendPayInfo").addObject("orderSerial", no);
		// 商户网站订单系统中唯一订单号，必填
		// 订单名称
		String subject = orderName;
		// 付款金额
		String total_fee = money;
		// 必填
		// 订单描述
		String body = "淘艺术网";
		// 商品展示地址
		String show_url = "";
		// 需以http://开头的完整路径，例如：http://www.商户网址.com/myorder.html
		// 防钓鱼时间戳
		String anti_phishing_key = "";
		// 若要使用请调用类文件submit中的query_timestamp函数
		// 客户端的IP地址
		String exter_invoke_ip = "";
		// 把请求参数打包成数组
		Map<String, String> sParaTemp = new HashMap<String, String>();
		sParaTemp.put("service", "create_direct_pay_by_user");
		sParaTemp.put("partner", "");
		sParaTemp.put("_input_charset", "");
		sParaTemp.put("payment_type", "");
		sParaTemp.put("notify_url", "");
		sParaTemp.put("return_url", "");
		sParaTemp.put("seller_email", "");
		sParaTemp.put("out_trade_no", no);
		sParaTemp.put("subject", subject);
		sParaTemp.put("total_fee", total_fee);
		sParaTemp.put("body", body);
		sParaTemp.put("show_url", show_url);
		sParaTemp.put("anti_phishing_key", anti_phishing_key);
		sParaTemp.put("exter_invoke_ip", exter_invoke_ip);
		sParaTemp.put("it_b_pay", "");
		// 建立请求 需要引入aplipay1.0jar
		String html = "";
				//AlipaySubmit.buildRequest(sParaTemp, "get", "确认");
		return mav.addObject("html", html);
	}
	
	/**
	 * 支付支付回调-支付宝扫码支付、条页面支付
	 */
	@RequestMapping("alipay/notifyPay")
	@ResponseBody
	public String notifyPay(HttpServletRequest request, HttpServletResponse response) {
		try {
			// 获取支付宝GET过来反馈信息
			Map<String, String> params = new HashMap<String, String>();
			Map<String, String[]> requestParams = request.getParameterMap();
			for (Iterator<String> iter = requestParams.keySet().iterator(); iter.hasNext();) {
				String name = (String) iter.next();
				String[] values = (String[]) requestParams.get(name);
				String valueStr = "";
				for (int i = 0; i < values.length; i++) {
					valueStr = (i == values.length - 1) ? valueStr + values[i] : valueStr + values[i] + ",";
				}
				// 乱码解决，这段代码在出现乱码时使用
				// valueStr = new String(valueStr.getBytes("ISO-8859-1"),
				// "utf-8");
				params.put(name, valueStr);
			}

			// 商户订单号
			String out_trade_no = new String(request.getParameter("out_trade_no").getBytes("ISO-8859-1"), "UTF-8");
			// 支付宝交易号
			String alipayNo = new String(request.getParameter("trade_no").getBytes("ISO-8859-1"), "UTF-8");
			// 买家支付宝帐号
			// 交易状态
			String trade_status = new String(request.getParameter("trade_status").getBytes("ISO-8859-1"), "UTF-8");
			boolean verify_result = false;
			// 页面支付方式和扫码支付不同
			try {
				verify_result = AlipayNotify.verify(params, 1);
			} catch (Exception e) {
				verify_result = false;
			}
			if (!verify_result) {
				try {
					verify_result = AlipayNotify.verify(params, 2);
				} catch (Exception e) {
					verify_result = false;
				}
			}
			if (!verify_result) {
				try {
					verify_result = AlipaySignature.rsaCheckV1(params, publicKey, "utf-8", "RSA"); // 调用SDK验证签名
				} catch (Exception e) {
					verify_result = false;
				}
			}
			log.info("支付宝回调参数" + params);
			log.info("trade_status:" + trade_status + " , " + "verify_result:" + verify_result);
			if (trade_status.equals("TRADE_FINISHED") || trade_status.equals("TRADE_SUCCESS") && verify_result) {
				//业务处理逻辑
				//tickerOrderService.callBackPay(out_trade_no, 2, alipayNo);
			}
			return "success";
		} catch (Exception e) {
			e.printStackTrace();
		}
		return "fail";
	}

}
