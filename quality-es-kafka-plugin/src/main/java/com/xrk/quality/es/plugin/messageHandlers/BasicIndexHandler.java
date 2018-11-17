package com.xrk.quality.es.plugin.messageHandlers;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.xrk.quality.es.plugin.KafkaConsumerConfig;
import com.xrk.quality.es.plugin.vo.LogBaseVo;
import com.xrk.quality.es.plugin.vo.MessageVo;
import com.xrk.quality.es.plugin.vo.TracePathVo;

/**
 * 索引处理基类，提供索引处理的公共实现方法以及定义子类需要实现的方法
 * BasicIndexHandler: BasicIndexHandler.java.
 *
 * <br>==========================
 * <br> 公司：广州向日葵信息科技有限公司
 * <br> 开发：shunchiguo<shunchiguo@xiangrikui.com>
 * <br> 版本：1.0
 * <br> 创建时间：2016年5月25日
 * <br> JDK版本：1.7
 * <br>==========================
 */
public abstract class BasicIndexHandler implements IndexHandler, IMessageHandler {	
	protected final KafkaConsumerConfig config;
	protected String indexName;
	protected String indexType;
	protected DateFormat dateFormat;
	/**
	 * 父节点消息处理类
	 */
	protected BasicIndexHandler parentMessageHandler;
	/**
	 * 当前节点处理的消息类型名称
	 */
	protected String messageType;
	/**
	 * 消息内容分割符
	 */
	protected final String splitChar = "\\$";
	/**
	 * 消息内容子消息分割符
	 */
	protected final String subSplitChar = "\\:";
	/**
	 * JSON序列化对象
	 */
	protected Gson gson;
	
	/**
	 * 
	 * Creates a new instance of BasicIndexHandler.  
	 *  
	 * @param config
	 * @param messageType
	 * @param indexName
	 * @param indexType
	 * @param parent
	 */
	public BasicIndexHandler(KafkaConsumerConfig config, String messageType, 
	                         String indexName, String indexType, BasicIndexHandler parent) {
		this.gson = new GsonBuilder()
		.enableComplexMapKeySerialization()
		.excludeFieldsWithoutExposeAnnotation().create();
		
		this.config = config;
		this.parentMessageHandler = parent;
		this.messageType = messageType;
		this.indexName = indexName;
		this.indexType = indexType;
//		indexName = config.esIndex;
//		if (indexName == null || indexName.trim().length() < 1)
//			indexName = DEFAULT_INDEX_NAME;
//		
//		indexType = config.esIndexType;
//		if (indexType == null || indexType.trim().length() < 1)
//			indexType =  DEFAULT_INDEX_TYPE;
		
		dateFormat = new SimpleDateFormat(this.config.esIndexDynamicName, Locale.CHINA);
	}
	
	/**
	 * 
	 * 处理消息函数，需要子类实现  
	 *    
	 * @param message
	 * @return
	 */
	protected abstract String processMessage(MessageVo message);
	
	/**
	 * 
	 * 处理日志跟踪消息，分拆出父应用以及日志索引查询对象和业务数据查询对象  
	 *    
	 * @param log
	 */
	protected void processTraceInfo(LogBaseVo log){
		//处理日志跟踪数据，日志格式：appName:appIP_appPort$appName2:appIP_appPort……
		if(log.getTrace_id() != null && !log.getTrace_id().isEmpty()){
	    	String tracePath = log.getTrace_path();
	    	String[] subPath = tracePath.split(splitChar);
	    	
	    	String parent_app_name = "";
	    	for(String sub : subPath){
	    		String[] ary = sub.split(subSplitChar);
	    		if(ary.length > 1){
		    		TracePathVo pathVo = new TracePathVo();
		    		pathVo.setApp_name(ary[0].trim());
		    		String[] aryIp = ary[1].trim().split("_");
		    		pathVo.setServer_ip(aryIp[0]);
		    		if(aryIp.length > 1){
		    			try{
		    				pathVo.setServer_port(Integer.parseInt(aryIp[1]));
		    			}catch(Exception e){
		    				pathVo.setServer_port(0);
		    			}
		    		}
		    		pathVo.setParent_app_name(parent_app_name);
		    		log.getTrace_query().add(pathVo);
		    		parent_app_name = pathVo.getApp_name();
	    		}
	    	}
	    	
	    	//设置父节点应用名称
	    	int traceDepth = log.getTrace_query().size();
	    	if(traceDepth > 1){
	    		int parentIdx = traceDepth - 2;
	    		log.setTrace_parent(log.getTrace_query().get(parentIdx).getApp_name());	    		
	    	}	    	
	    	log.setTrace_depth(traceDepth);
	    }
		
		//处理业务查询数据，日志格式：query_field1:query_data1$query_field2:query_data2$……
		if(log.getQuery_data() != null && !log.getQuery_data().isEmpty()){
			String queryData = log.getQuery_data();
			String[] subData = queryData.split(splitChar);
			for(String sub : subData){
				String[] ary = sub.split(subSplitChar);
				if(ary.length > 1){
					log.getQuery_content().put(ary[0].trim(), ary[1].trim());
				}
			}
		}
	}	
	
	/**
	 * 检测待处理的消息，如果主题是当前待处理的对象，则进行处理，否则将由上一个节点继续检测
	 * @see com.xrk.quality.es.plugin.messageHandlers.IMessageHandler#transformMessage(com.xrk.quality.es.plugin.vo.MessageVo)
	 */
	@Override
    public String transformMessage(MessageVo message) throws Exception
    {
	    if(this.messageType.equalsIgnoreCase(message.getTopic())){
	    	return this.processMessage(message);
	    }
	    
	    if(this.parentMessageHandler != null){
	    	return parentMessageHandler.transformMessage(message);
	    }
	    return null;
    }

	@Override
	public String getIndexName(String topic) {
		if(this.messageType.equalsIgnoreCase(topic)){
			return  String.format("%s%s",this.indexName, dateFormat.format(new Date()));
		}
		
		if(this.parentMessageHandler != null){
	    	return parentMessageHandler.getIndexName(topic);
	    }
	    return DEFAULT_INDEX_NAME;
	}

	@Override
	public String getIndexType(String topic) {
		if(this.messageType.equalsIgnoreCase(topic)){
			return  indexType;
		}
		
		if(this.parentMessageHandler != null){
	    	return parentMessageHandler.getIndexType(topic);
	    }
	    return DEFAULT_INDEX_TYPE;
	}

}
