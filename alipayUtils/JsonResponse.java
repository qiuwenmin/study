
import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 
 * @ClassName: JsonResponse
 * @Description: TODO
 * @Author qwm
 * @Date 2015年8月11日 下午6:16:03
 * @Modify
 * @CopyRight qiuwenmin
 */
public class JsonResponse implements Serializable {

	private static final long serialVersionUID = 8233772087554568910L;

	private boolean success;

	private String message;

	private List<?> list;

	private Object result;


	private String errorMessage;

	private String errorCode;

	private Map<String, Object> returnMap = new HashMap<String, Object>();
	
	private Object exports;
	
	private Object exports2;
	
	private Object exports3;

	public boolean isSuccess() {
		return success;
	}

	public void setSuccess(boolean success) {
		this.success = success;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public List<?> getList() {
		return list;
	}

	public void setList(List<?> list) {
		this.list = list;
	}

	public Object getResult() {
		return result;
	}

	public void setResult(Object result) {
		this.result = result;
	}


	public String getErrorMessage() {
		return errorMessage;
	}

	public void setErrorMessage(String errorMessage) {
		this.errorMessage = errorMessage;
	}

	public String getErrorCode() {
		return errorCode;
	}

	public void setErrorCode(String errorCode) {
		this.errorCode = errorCode;
	}

	public Map<String, Object> getReturnMap() {
		return returnMap;
	}

	public void setReturnMap(Map<String, Object> returnMap) {
		this.returnMap = returnMap;
	}

	public Object getExports() {
		return exports;
	}

	public void setExports(Object exports) {
		this.exports = exports;
	}

	public Object getExports2() {
		return exports2;
	}

	public void setExports2(Object exports2) {
		this.exports2 = exports2;
	}

	public Object getExports3() {
		return exports3;
	}

	public void setExports3(Object exports3) {
		this.exports3 = exports3;
	}
	
}
