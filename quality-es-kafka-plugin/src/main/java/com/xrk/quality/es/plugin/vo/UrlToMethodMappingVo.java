package com.xrk.quality.es.plugin.vo;

import java.util.regex.Pattern;

public class UrlToMethodMappingVo
{
	private String appName;
	private String pathRegex;
	private String method;
	private String methodName;
	private Pattern pattern;
	
	public UrlToMethodMappingVo(String appName, String pathRegex, 
	                            String method, String methodName){
		this.setAppName(appName);
		this.setPathRegex(pathRegex);
		this.setMethod(method);
		this.setMethodName(methodName);
		pattern = Pattern.compile(pathRegex, Pattern.CASE_INSENSITIVE);
	}

	public String getAppName()
    {
	    return appName;
    }

	public void setAppName(String appName)
    {
	    this.appName = appName;
    }

	public String getPathRegex()
    {
	    return pathRegex;
    }

	public void setPathRegex(String pathRegex)
    {
	    this.pathRegex = pathRegex;
    }

	public String getMethod()
    {
	    return method;
    }

	public void setMethod(String method)
    {
	    this.method = method;
    }

	public String getMethodName()
    {
	    return methodName;
    }

	public void setMethodName(String methodName)
    {
	    this.methodName = methodName;
    }
	
	public boolean match(String path){
		return pattern.matcher(path).find();
	}
}
