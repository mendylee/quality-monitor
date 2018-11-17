package com.xrk.quality.es.plugin.vo;

import com.google.gson.annotations.Expose;

public class UserAgentVo
{
	@Expose
	private String os;
	@Expose
	private String os_type;
	@Expose
	private String browser;
	@Expose
	private String browser_type;
	@Expose
	private String browser_version;
	@Expose
	private String manufacturer;
	@Expose
	private String render_engine;
	
	public UserAgentVo(){		
	}
	
	public String getOs()
	{
		return os;
	}

	public void setOs(String os)
	{
		this.os = os;
	}

	public String getOs_type()
	{
		return os_type;
	}

	public void setOs_type(String os_type)
	{
		this.os_type = os_type;
	}

	public String getBrowser()
	{
		return browser;
	}

	public void setBrowser(String browser)
	{
		this.browser = browser;
	}

	public String getBrowser_type()
	{
		return browser_type;
	}

	public void setBrowser_type(String browser_type)
	{
		this.browser_type = browser_type;
	}
	
	public String getBrowser_version()
	{
		return browser_version;
	}

	public void setBrowser_version(String browser_version)
	{
		this.browser_version = browser_version;
	}

	public String getManufacturer()
	{
		return manufacturer;
	}

	public void setManufacturer(String manufacturer)
	{
		this.manufacturer = manufacturer;
	}

	public String getRender_engine()
	{
		return render_engine;
	}

	public void setRender_engine(String render_engine)
	{
		this.render_engine = render_engine;
	}
}
