package com.xrk.quality.es.plugin.messageHandlers;

import java.util.Base64;

import com.xrk.quality.es.plugin.KafkaConsumerConfig;
import com.xrk.quality.es.plugin.vo.MessageVo;
import com.xrk.quality.es.plugin.vo.QualityErrorVo;

public class QualityErrorIndexHandler extends BasicIndexHandler
{
	public QualityErrorIndexHandler(KafkaConsumerConfig config, String messageType,
            String indexName, String indexType, BasicIndexHandler parent) {
		super(config, "quality_error", "quality_error-", "error", parent);	    
	    // TODO Auto-generated constructor stub
    }

	@Override
	protected String processMessage(MessageVo message)
	{
		String msg = message.getValue();
	    //process trace_id, trace_path
	    QualityErrorVo log = gson.fromJson(msg, QualityErrorVo.class);
	    this.processTraceInfo(log);
	    //解码堆栈信息的Base64字符信息
	    log.setStack(new String(Base64.getDecoder().decode(log.getStack().getBytes())));
	    return gson.toJson(log);
	}

}
