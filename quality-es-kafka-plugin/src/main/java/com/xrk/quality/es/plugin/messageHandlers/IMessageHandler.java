package com.xrk.quality.es.plugin.messageHandlers;

import com.xrk.quality.es.plugin.vo.MessageVo;
/**
 * 消息处理接口定义
 * IMessageHandler: IMessageHandler.java.
 *
 * <br>==========================
 * <br> 公司：广州向日葵信息科技有限公司
 * <br> 开发：shunchiguo<shunchiguo@xiangrikui.com>
 * <br> 版本：1.0
 * <br> 创建时间：2016年5月25日
 * <br> JDK版本：1.7
 * <br>==========================
 */
public interface IMessageHandler
{
	/**
	 * 
	 * 处理送入的消息，将其转换为JSON字符串格式  
	 *    
	 * @param message
	 * @return
	 * @throws Exception
	 */
	String transformMessage(MessageVo message) throws Exception;
}
