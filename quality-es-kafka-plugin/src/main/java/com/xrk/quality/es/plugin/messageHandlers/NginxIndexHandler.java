package com.xrk.quality.es.plugin.messageHandlers;

import java.util.Date;

import com.xrk.quality.es.plugin.KafkaConsumerConfig;
import com.xrk.quality.es.plugin.UrlToMethodService;
import com.xrk.quality.es.plugin.vo.MessageVo;
import com.xrk.quality.es.plugin.vo.QualityNginxLogVo;

import eu.bitwalker.useragentutils.UserAgent;

/**
 * Nginx日志消息处理类，处理Nginx的日志，拆分UserAgent消息
 * NginxIndexHandler: NginxIndexHandler.java.
 *
 * <br>==========================
 * <br> 公司：广州向日葵信息科技有限公司
 * <br> 开发：shunchiguo<shunchiguo@xiangrikui.com>
 * <br> 版本：1.0
 * <br> 创建时间：2016年5月25日
 * <br> JDK版本：1.7
 * <br>==========================
 */
public class NginxIndexHandler extends BasicIndexHandler
{	
	/**
	 * 
	 * Creates a new instance of NginxIndexHandler.  
	 *  
	 * @param config
	 * @param parent
	 */
	public NginxIndexHandler(KafkaConsumerConfig config, BasicIndexHandler parent) {
	    super(config, "quality_nginx", "quality_nginx-", "nginx", parent);
    }	

	@Override
    protected String processMessage(MessageVo message)
    {
		String msg = message.getValue();
	    QualityNginxLogVo log = gson.fromJson(msg, QualityNginxLogVo.class);
	    this.processTraceInfo(log);
	    log.setDuration((long)(log.getElapsed_time() * 1000));
	    log.setTimestamp((long)(log.getRequest_time() * 1000));
	    
	    //处理path对象，拆分请求URL及查询请求参数
	    String path = log.getPath();
	    if(path != null && !path.isEmpty()){
	    	int idx = path.indexOf("?");
	    	String strQuery = "";
	    	String url = path;
	    	if(idx >= 0){
	    		strQuery = path.substring(idx+1);
	    		url = path.substring(0, idx);
	    	}
	    	log.setUrl(url);
	    	log.setQuery_entity(strQuery);
	    	//验证url对应的呼叫方法，如果没有对应配置，则默认使用URL
	    	log.setCall_method(UrlToMethodService.getMethod(log.getApp_name(), url, log.getMethod()));
	    }
	    
	    //处理UserAgent字段
	    UserAgent ua = UserAgent.parseUserAgentString(log.getUser_agent());
	    if(ua != null){
	    	if(ua.getBrowser() != null){
	    		log.getUa().setBrowser(ua.getBrowser().getName());
	    		
	    		if(ua.getBrowser().getBrowserType() != null){
	    			log.getUa().setBrowser_type(ua.getBrowser().getBrowserType().getName());
	    		}
	    		
	    		if(ua.getBrowser().getManufacturer() != null){
	    			log.getUa().setManufacturer(ua.getBrowser().getManufacturer().getName());
	    		}
	    		
	    		if(ua.getBrowser().getRenderingEngine() != null){
	    			log.getUa().setRender_engine(ua.getBrowser().getRenderingEngine().name());
	    		}
	    	}
	    	
	    	if(ua.getBrowserVersion() != null){
	    		log.getUa().setBrowser_version(ua.getBrowserVersion().getVersion());
	    	}
		    
	    	if(ua.getOperatingSystem() != null){
	    		log.getUa().setOs(ua.getOperatingSystem().getName());
	    		
	    		if(ua.getOperatingSystem().getDeviceType() != null){
	    			log.getUa().setOs_type(ua.getOperatingSystem().getDeviceType().getName());
	    		}
	    	}
	    }
	    
	    String json = gson.toJson(log);
	    return json;
    }

}
