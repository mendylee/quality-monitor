package com.xrk.quality.es.plugin.vo;

import com.google.gson.annotations.Expose;

public class QualityErrorVo extends LogBaseVo
{
	@Expose
	private String server_ip;
	@Expose
	private int server_port;
	@Expose
	private String error_code;
	@Expose
	private String error_object;
	@Expose
	private String error_message;
	@Expose
	private String stack;
	
	public QualityErrorVo(){
		
	}
	
	public String getServer_ip()
	{
		return server_ip;
	}
	public void setServer_ip(String server_ip)
	{
		this.server_ip = server_ip;
	}
	public int getServer_port()
	{
		return server_port;
	}
	public void setServer_port(int server_port)
	{
		this.server_port = server_port;
	}
	public String getError_code()
	{
		return error_code;
	}
	public void setError_code(String error_code)
	{
		this.error_code = error_code;
	}
	public String getError_object()
	{
		return error_object;
	}
	public void setError_object(String error_object)
	{
		this.error_object = error_object;
	}
	public String getError_message()
	{
		return error_message;
	}
	public void setError_message(String error_message)
	{
		this.error_message = error_message;
	}
	public String getStack()
	{
		return stack;
	}
	public void setStack(String stack)
	{
		this.stack = stack;
	}	
}
