package com.xrk.quality.es.plugin.messageHandlers;

import com.xrk.quality.es.plugin.KafkaConsumerConfig;
import com.xrk.quality.es.plugin.vo.MessageVo;
import com.xrk.quality.es.plugin.vo.QualityLogVo;

/**
 * 处理质量日志消息处理类，分解TraceID和TracePath
 * QualityIndexHandler: QualityIndexHandler.java.
 *
 * <br>==========================
 * <br> 公司：广州向日葵信息科技有限公司
 * <br> 开发：shunchiguo<shunchiguo@xiangrikui.com>
 * <br> 版本：1.0
 * <br> 创建时间：2016年5月25日
 * <br> JDK版本：1.7
 * <br>==========================
 */
public class QualityIndexHandler extends BasicIndexHandler
{
	/**
	 * 
	 * Creates a new instance of QualityIndexHandler.  
	 *  
	 * @param config
	 * @param parent
	 */
	public QualityIndexHandler(KafkaConsumerConfig config, BasicIndexHandler parent) {
	    super(config, "quality_msg", "quality_day-", "fluentd", parent);	    
    }

	@Override
    protected String processMessage(MessageVo message)
    {
	    String msg = message.getValue();
	    //process trace_id, trace_path
	    QualityLogVo log = gson.fromJson(msg, QualityLogVo.class);
	    this.processTraceInfo(log);
	    log.setDuration(log.getElapsed_time());
	    log.setTimestamp(log.getReqeust_time());
	    return gson.toJson(log);
    }

}
