package com.xrk.quality.es.plugin.messageHandlers;

import com.xrk.quality.es.plugin.KafkaConsumerConfig;
import com.xrk.quality.es.plugin.vo.MessageVo;

/**
 * 数据库日志对象处理类
 * DBIndexHandler: DBIndexHandler.java.
 *
 * <br>==========================
 * <br> 公司：广州向日葵信息科技有限公司
 * <br> 开发：shunchiguo<shunchiguo@xiangrikui.com>
 * <br> 版本：1.0
 * <br> 创建时间：2016年5月25日
 * <br> JDK版本：1.7
 * <br>==========================
 */
public class DBIndexHandler extends BasicIndexHandler
{

	public DBIndexHandler(KafkaConsumerConfig config, BasicIndexHandler parent) {
	    super(config, "quality_db", "quality_db-", "database", parent);
	    
    }

	@Override
	protected String processMessage(MessageVo message)
	{
		// TODO Auto-generated method stub
		return null;
	}

}
