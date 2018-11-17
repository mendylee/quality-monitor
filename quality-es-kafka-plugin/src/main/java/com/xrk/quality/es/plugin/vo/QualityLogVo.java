package com.xrk.quality.es.plugin.vo;

import com.google.gson.annotations.Expose;

/**
 * 质量日志对象
 * QualityLogVo: QualityLogVo.java.
 *
 * <br>==========================
 * <br> 公司：广州向日葵信息科技有限公司
 * <br> 开发：shunchiguo<shunchiguo@xiangrikui.com>
 * <br> 版本：1.0
 * <br> 创建时间：2016年5月25日
 * <br> JDK版本：1.7
 * <br>==========================
 */
public class QualityLogVo extends LogBaseVo
{
	@Expose
	private long request_time;
	@Expose
	private String log_type;
	@Expose
	private String client_ip;
	@Expose
	private int client_port;
	@Expose
	private String server_ip;
	@Expose
	private int server_port;
	@Expose
	private long elapsed_time;
	@Expose
	private String communication_type;
	@Expose
	private long req_length;
	@Expose
	private long resp_length;
	@Expose
	private String method;
	@Expose
	private String url;
	@Expose
	private String status_code;
	@Expose
	private String query_entity;
	@Expose
	private String call_method;
	@Expose
	private String business_code;
	@Expose
	private String return_content;
		
	public QualityLogVo(){		
	}
	
	public long getReqeust_time()
	{
		return request_time;
	}

	public void setReqeust_time(long reqeust_time)
	{
		this.request_time = reqeust_time;
	}

	public String getLog_type()
	{
		return log_type;
	}

	public void setLog_type(String log_type)
	{
		this.log_type = log_type;
	}

	public String getClient_ip()
	{
		return client_ip;
	}

	public void setClient_ip(String client_ip)
	{
		this.client_ip = client_ip;
	}

	public int getClient_port()
	{
		return client_port;
	}

	public void setClient_port(int client_port)
	{
		this.client_port = client_port;
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

	public long getElapsed_time()
	{
		return elapsed_time;
	}

	public void setElapsed_time(long elapsed_time)
	{
		this.elapsed_time = elapsed_time;
	}

	public String getCommunication_type()
	{
		return communication_type;
	}

	public void setCommunication_type(String communication_type)
	{
		this.communication_type = communication_type;
	}

	public long getReq_length()
	{
		return req_length;
	}

	public void setReq_length(long req_length)
	{
		this.req_length = req_length;
	}

	public long getResp_length()
	{
		return resp_length;
	}

	public void setResp_length(long resp_length)
	{
		this.resp_length = resp_length;
	}

	public String getMethod()
	{
		return method;
	}

	public void setMethod(String method)
	{
		this.method = method;
	}

	public String getUrl()
	{
		return url;
	}

	public void setUrl(String url)
	{
		this.url = url;
	}

	public String getStatus_code()
	{
		return status_code;
	}

	public void setStatus_code(String status_code)
	{
		this.status_code = status_code;
	}

	public String getQuery_entity()
	{
		return query_entity;
	}

	public void setQuery_entity(String query_entity)
	{
		this.query_entity = query_entity;
	}

	public String getCall_method()
	{
		return call_method;
	}

	public void setCall_method(String call_method)
	{
		this.call_method = call_method;
	}

	public String getBusiness_code()
	{
		return business_code;
	}

	public void setBusiness_code(String business_code)
	{
		this.business_code = business_code;
	}

	public String getReturn_content()
	{
		return return_content;
	}

	public void setReturn_content(String return_content)
	{
		this.return_content = return_content;
	}
}
