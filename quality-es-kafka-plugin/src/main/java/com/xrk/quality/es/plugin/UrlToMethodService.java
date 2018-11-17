package com.xrk.quality.es.plugin;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.xrk.quality.es.plugin.vo.UrlToMethodMappingVo;

/**
 * 根据App对应的URL找到配置文件中对应的匹配方法
 * UrlToMethodService: UrlToMethodService.java.
 *
 * <br>==========================
 * <br> 公司：广州向日葵信息科技有限公司
 * <br> 开发：shunchiguo<shunchiguo@xiangrikui.com>
 * <br> 版本：1.0
 * <br> 创建时间：2016年5月30日
 * <br> JDK版本：1.7
 * <br>==========================
 */
public class UrlToMethodService
{
	/**
	 * 存储方法映射关系
	 */
	private static Map<String, List<UrlToMethodMappingVo>> methodMap;
	/**
	 * 
	 * 初始化映射方法，从配置文件中加载预设的URL配置信息，存储到缓存中  
	 *    
	 * @param appBasePath
	 */
	public static void init(String appBasePath)
    {		
	    methodMap = new ConcurrentHashMap<String, List<UrlToMethodMappingVo>>();	    
		String configPath = String.format("%s/mapping/", appBasePath);
		File f = new File(configPath);
		File[] matchingFiles = f.listFiles(new FilenameFilter() {
		    public boolean accept(File dir, String name) {
		        return name.endsWith(".txt");
		    }
		});
		
		if(matchingFiles == null){
			return ;
		}
		
		for(File file : matchingFiles){
			String name = file.getName().toLowerCase();
			name = name.substring(0, name.length()-4);
			List<UrlToMethodMappingVo> ls = new ArrayList<UrlToMethodMappingVo>();			
			try (BufferedReader br = new BufferedReader(new FileReader(file))) {
			    String line;
			    while ((line = br.readLine()) != null) {
			    	if(line == null || line.isEmpty()){
			    		continue;
			    	}
			    	String[] ary = line.split("\t");
			    	if(ary.length != 3){
			    		continue;
			    	}
			    	ls.add(new UrlToMethodMappingVo(name, ary[0], ary[1], ary[2]));
			    }
			}
            catch (Exception e) {	            
	            e.printStackTrace();
            }
			methodMap.put(name, ls);
		}
    }
	
	/**
	 * 
	 * 根据应用名称及URL查找对应的方法名称  
	 *    
	 * @param appName
	 * @param url
	 * @return
	 */
	public static String getMethod(String appName, String url, String httpMethod){
		//TODO:实现URL查询
		List<UrlToMethodMappingVo> ls = methodMap.get(appName);
		if(ls == null){
			return url;
		}
		
		String method = url;		
		for(UrlToMethodMappingVo mm : ls){
			if(mm.getMethod().equalsIgnoreCase(httpMethod) && mm.match(url)){
				method = mm.getMethodName();
				break;
			}
		}
		
		return method;
	}
}
