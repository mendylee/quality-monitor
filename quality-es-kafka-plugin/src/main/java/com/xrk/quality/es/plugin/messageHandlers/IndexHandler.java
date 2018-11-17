package com.xrk.quality.es.plugin.messageHandlers;

/**
 * 索引处理接口定义，用以获取指定主题的索引名和索引类型
 * IndexHandler: IndexHandler.java.
 *
 * <br>==========================
 * <br> 公司：广州向日葵信息科技有限公司
 * <br> 开发：shunchiguo<shunchiguo@xiangrikui.com>
 * <br> 版本：1.0
 * <br> 创建时间：2016年5月25日
 * <br> JDK版本：1.7
 * <br>==========================
 */
public interface IndexHandler {
	
	// default index name, if not specified/calculated otherwise
	public static final String DEFAULT_INDEX_NAME = "quality_day-yyyyMMdd";
	// default index type, if not specified/calculated otherwise
	public static final String DEFAULT_INDEX_TYPE = "fluentd";

	/**
	 * 
	 * 获取索引名称  
	 *    
	 * @param topic 当前消息的主题
	 * @return
	 */
	public String getIndexName (String topic);
	/**
	 * 
	 * 获取索引类型  
	 *    
	 * @param topic 当前消息的主题
	 * @return
	 */
	public String getIndexType (String topic);
}
