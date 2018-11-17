package com.xrk.quality.es.plugin;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.xrk.quality.es.plugin.jobs.IndexerJobManager;

/**
 * 质量日志服务处理入口类，
 * QualityIndexerDriver: QualityIndexerDriver.java.
 *
 * <br>==========================
 * <br> 公司：广州向日葵信息科技有限公司
 * <br> 开发：shunchiguo<shunchiguo@xiangrikui.com>
 * <br> 版本：1.0
 * <br> 创建时间：2016年5月25日
 * <br> JDK版本：1.7
 * <br>==========================
 */
public class QualityIndexerDriver
{
	boolean stopped = false;
	public IndexerJobManager indexerJobManager = null;
	private KafkaConsumerConfig kafkaConsumerConfig;
	private static final Logger logger = LoggerFactory.getLogger(QualityIndexerDriver.class);

	public QualityIndexerDriver() {
	}

	/**
	 * 
	 * 初始化服务相关类信息  
	 *    
	 * @param configFile
	 * @throws Exception
	 */
	public void init(String configFile) throws Exception
	{
		logger.info("Initializing Kafka ES Indexer, arguments passed to the Driver: ");
		kafkaConsumerConfig = new KafkaConsumerConfig(configFile);
		logger.info("Created kafka consumer config OK");
		indexerJobManager = new IndexerJobManager(kafkaConsumerConfig);
	}

	/**
	 * 
	 * 启动服务  
	 *    
	 * @throws Exception
	 */
	public void start() throws Exception
	{
		indexerJobManager.startAll();
	}

	/**
	 * 
	 * 停止服务  
	 *    
	 * @throws Exception
	 */
	public void stop() throws Exception
	{
		logger.info("Received the stop signal, trying to stop all indexer jobs...");
		stopped = true;

		indexerJobManager.stop();
		logger.info("Stopped all indexer jobs OK");
	}
}
