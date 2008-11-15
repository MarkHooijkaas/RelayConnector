package org.kisst.cordys.relay;

public class SoapFaultException extends RuntimeException {
	private static final long serialVersionUID = 1L;
	public final String code;
	public final String message;

	public SoapFaultException(String code, String message) {
		super(message);
		this.code=code;
		this.message=message;
	}

	public SoapFaultException(String code, String message, Throwable e) {
		super(message, e);
		this.code=code;
		this.message=message;
	}
}
