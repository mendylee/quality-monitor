package com.xrk.quality.es.plugin.vo;

import com.google.gson.annotations.Expose;

/**
 * 日志跟踪路径信息
 * TracePathVo: TracePathVo.java.
 *
 * <br>==========================
 * <br> 公司：广州向日葵信息科技有限公司
 * <br> 开发：shunchiguo<shunchiguo@xiangrikui.com>
 * <br> 版本：1.0
 * <br> 创建时间：2016年5月25日
 * <br> JDK版本：1.7
 * <br>==========================
 */
public class TracePathVo
{
	@Expose
	private String app_name;
	@Expose
	private String server_ip;
	@Expose
	private int server_port;
	@Expose
	private String parent_app_name;
	public TracePathVo(){
		
	}
	
	public TracePathVo(String name, String ip){
		this.app_name = name;
		this.server_ip = ip;
	}
	
	public String getApp_name()
	{
		return app_name;
	}
	public void setApp_name(String app_name)
	{
		this.app_name = app_name;
	}
	public String getServer_ip()
	{
		return server_ip;
	}
	public void setServer_ip(String server_ip)
	{
		this.server_ip = server_ip;
	}

	public String getParent_app_name()
    {
	    return parent_app_name;
    }

	public void setParent_app_name(String parent_app_name)
    {
	    this.parent_app_name = parent_app_name;
    }

	public int getServer_port()
    {
	    return server_port;
    }

	public void setServer_port(int server_port)
    {
	    this.server_port = server_port;
    }
}
