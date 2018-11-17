package com.xrk.quality.es.plugin.vo;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.gson.annotations.Expose;

/**
 * 质量日志基类
 * LogBaseVo: LogBaseVo.java.
 *
 * <br>==========================
 * <br> 公司：广州向日葵信息科技有限公司
 * <br> 开发：shunchiguo<shunchiguo@xiangrikui.com>
 * <br> 版本：1.0
 * <br> 创建时间：2016年5月25日
 * <br> JDK版本：1.7
 * <br>==========================
 */
public abstract class LogBaseVo
{
	@Expose
	private long timestamp;
	@Expose
	private long duration;
	@Expose
	private String app_name;
	@Expose
	private String trace_id;
	@Expose
	private String trace_path;
	@Expose
	private int trace_depth;
	@Expose
	private String trace_parent;	
	@Expose(serialize = false) 
	private String query_data;
	@Expose
	private List<TracePathVo> trace_query;
	@Expose
	private Map<String, String> query_content;
	
	public LogBaseVo(){
		setTrace_query(new ArrayList<TracePathVo>());
		query_content = new HashMap<String, String>();
	}
	
	public String getApp_name()
	{
		return app_name;
	}

	public void setApp_name(String app_name)
	{
		this.app_name = app_name;
	}
	
	public String getTrace_id()
	{
		return trace_id;
	}

	public void setTrace_id(String trace_id)
	{
		this.trace_id = trace_id;
	}

	public String getTrace_path()
	{
		return trace_path;
	}

	public void setTrace_path(String trace_path)
	{
		this.trace_path = trace_path;
	}

	public String getTrace_parent()
	{
		return trace_parent;
	}

	public void setTrace_parent(String trace_parent)
	{
		this.trace_parent = trace_parent;
	}

	public String getQuery_data()
	{
		return query_data;
	}

	public void setQuery_data(String query_data)
	{
		this.query_data = query_data;
	}

	public Map<String, String> getQuery_content()
	{
		return query_content;
	}

	public void setQuery_content(Map<String, String> query_content)
	{
		this.query_content = query_content;
	}

	public List<TracePathVo> getTrace_query()
    {
	    return trace_query;
    }

	public void setTrace_query(List<TracePathVo> trace_query)
    {
	    this.trace_query = trace_query;
    }

	public long getTimestamp()
    {
	    return timestamp;
    }

	public void setTimestamp(long timestamp)
    {
	    this.timestamp = timestamp;
    }

	public long getDuration()
    {
	    return duration;
    }

	public void setDuration(long duration)
    {
	    this.duration = duration;
    }

	public int getTrace_depth()
    {
	    return trace_depth;
    }

	public void setTrace_depth(int trace_depth)
    {
	    this.trace_depth = trace_depth;
    }
}
