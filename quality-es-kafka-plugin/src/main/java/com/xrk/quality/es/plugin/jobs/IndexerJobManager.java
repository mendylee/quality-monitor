package com.xrk.quality.es.plugin.jobs;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.xrk.quality.es.plugin.KafkaConsumerConfig;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

/**
 * 索引处理任务管理器
 * IndexerJobManager: IndexerJobManager.java.
 *
 * <br>==========================
 * <br> 公司：广州向日葵信息科技有限公司
 * <br> 开发：shunchiguo<shunchiguo@xiangrikui.com>
 * <br> 版本：1.0
 * <br> 创建时间：2016年5月25日
 * <br> JDK版本：1.7
 * <br>==========================
 */
public class IndexerJobManager
{
	private static final Logger logger = LoggerFactory.getLogger(IndexerJobManager.class);
	private static final String KAFKA_CONSUMER_STREAM_POOL_NAME_FORMAT = "kafka-indexer-consumer-thread-%d";
	private KafkaConsumerConfig consumerConfig;
	private ExecutorService executorService;
	private List<Future<Boolean>> indexerJobFutures;
	private List<IndexerJob> tasks;

	/**
	 * 
	 * Creates a new instance of IndexerJobManager.  
	 *  
	 * @param config
	 * @throws Exception
	 */
	public IndexerJobManager(KafkaConsumerConfig config) throws Exception {
		this.consumerConfig = config;
	}

	/**
	 * 
	 * 启动所有待处理任务  
	 *    
	 * @throws Exception
	 */
	public void startAll() throws Exception
	{
		ThreadFactory threadFactory = new ThreadFactoryBuilder().setNameFormat(
		        KAFKA_CONSUMER_STREAM_POOL_NAME_FORMAT).build();
		executorService = Executors.newFixedThreadPool(this.consumerConfig.consumerThreadNum,
		        threadFactory);
		tasks = new ArrayList<IndexerJob>();

		for (int i = 0; i < this.consumerConfig.consumerThreadNum; i++) {
			tasks.add(new IndexerJob(this.consumerConfig));
		}
		// now start them all
		indexerJobFutures = executorService.invokeAll(tasks);
	}

	/**
	 * 
	 * 停止所有任务  
	 *
	 */
	public void stop()
	{
		logger.info("About to stop all consumer jobs ...");
		if (this.tasks != null && this.tasks.size() > 0) {
			for (IndexerJob job : this.tasks) {
				job.shutdown();
			}
		}
		
		if (executorService != null && !executorService.isTerminated()) {
			try {
				executorService.awaitTermination(consumerConfig.appStopTimeoutSeconds,
				        TimeUnit.SECONDS);
			}
			catch (InterruptedException e) {
				logger.error(
				        "ERROR: failed to stop all consumer jobs due to InterruptedException: ", e);
			}
		}
		logger.info("Stop() finished OK");
	}

}
