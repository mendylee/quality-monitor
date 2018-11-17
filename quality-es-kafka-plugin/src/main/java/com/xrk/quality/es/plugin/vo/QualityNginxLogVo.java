package com.xrk.quality.es.plugin.vo;

import com.google.gson.annotations.Expose;

/**
 * Nginx日志对象
 * QualityNginxLogVo: QualityNginxLogVo.java.
 *
 * <br>==========================
 * <br> 公司：广州向日葵信息科技有限公司
 * <br> 开发：shunchiguo<shunchiguo@xiangrikui.com>
 * <br> 版本：1.0
 * <br> 创建时间：2016年5月25日
 * <br> JDK版本：1.7
 * <br>==========================
 */
public class QualityNginxLogVo extends LogBaseVo
{
	@Expose(serialize=false)
	private float request_time;
	@Expose(serialize=false)
	private float elapsed_time;	
	@Expose
	private String client_ip;
	@Expose
	private String server_ip;
	@Expose
	private String status_code;
	@Expose
	private String method;
	@Expose
	private long req_length;
	@Expose
	private long resp_length;
	@Expose
	private String path;
	@Expose
	private String referer;
	@Expose
	private String url;
	@Expose
	private String query_entity;
	@Expose
	private String call_method;
	@Expose(serialize=false)
	private String user_agent;
	@Expose
	private UserAgentVo ua;
	
	public QualityNginxLogVo(){
		ua = new UserAgentVo();
	}

	public float getRequest_time()
	{
		return request_time;
	}

	public void setRequest_time(float request_time)
	{
		this.request_time = request_time;
	}

	public float getElapsed_time()
	{
		return elapsed_time;
	}

	public void setElapsed_time(float elapsed_time)
	{
		this.elapsed_time = elapsed_time;
	}
	
	public String getClient_ip()
	{
		return client_ip;
	}

	public void setClient_ip(String client_ip)
	{
		this.client_ip = client_ip;
	}

	public String getServer_ip()
	{
		return server_ip;
	}

	public void setServer_ip(String server_ip)
	{
		this.server_ip = server_ip;
	}

	public String getStatus_code()
	{
		return status_code;
	}

	public void setStatus_code(String status_code)
	{
		this.status_code = status_code;
	}

	public String getMethod()
	{
		return method;
	}

	public void setMethod(String method)
	{
		this.method = method;
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

	public String getPath()
	{
		return path;
	}

	public void setPath(String path)
	{
		this.path = path;
	}

	public String getReferer()
	{
		return referer;
	}

	public void setReferer(String referer)
	{
		this.referer = referer;
	}

	public String getUrl()
	{
		return url;
	}

	public void setUrl(String url)
	{
		this.url = url;
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

	public String getUser_agent()
	{
		return user_agent;
	}

	public void setUser_agent(String user_agent)
	{
		this.user_agent = user_agent;
	}

	public UserAgentVo getUa()
	{
		return ua;
	}

	public void setUa(UserAgentVo ua)
	{
		this.ua = ua;
	}
}
