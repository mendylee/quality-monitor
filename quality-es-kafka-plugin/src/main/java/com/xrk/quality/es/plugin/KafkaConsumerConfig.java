package com.xrk.quality.es.plugin;

import java.io.FileInputStream;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Kafka消息服务配置类
 * 
 * KafkaConsumerConfig: KafkaConsumerConfig.java.
 *
 * <br>==========================
 * <br> 公司：广州向日葵信息科技有限公司
 * <br> 开发：shunchiguo<shunchiguo@xiangrikui.com>
 * <br> 版本：1.0
 * <br> 创建时间：2016年5月24日
 * <br> JDK版本：1.7
 * <br>==========================
 */
public class KafkaConsumerConfig {

	private static final Logger logger = LoggerFactory.getLogger(KafkaConsumerConfig.class);	
	private Properties prop = new Properties();

	// Full class path and name for the concrete message handler class 
	public final String messageHandlerClass;
	// Full class name of a custom IndexHandler implementation class
	public final String indexHandlerClass;
	// Kafka Broker's IP Address/HostName : port list
	public final String kafkaBrokersList;
	// Kafka Topic from which the message has to be processed
	public final String topic;
	// Name of the Kafka Consumer Group
	public final String consumerGroupName;	
	public final int consumerPullWaitTime;
	public final String enableAutoCommit;
	public final Integer messageHandlerQueueNum;
	// Name of the ElasticSearch Cluster
	public final String esClusterName;
	// Name of the ElasticSearch Host Port List
	public final String esHostPortList;
	// IndexName in ElasticSearch to which the processed Message has to be posted
	public final String esIndex;
	public final String esIndexDynamicName;
	// IndexType in ElasticSearch to which the processed Message has to be posted
	public final String esIndexType;
	public final int esBulkSize;
	// sleep time in ms between attempts to index data into ES again
	public final int esIndexingRetrySleepTimeMs;
	// number of times to try to index data into ES if ES cluster is not reachable
	public final int numberOfEsIndexingRetryAttempts;
	
	public final int consumerThreadNum;
	
	public final int appStopTimeoutSeconds;
	
	// Log property file for the consumer instance
	public final String logPropertyFile;

	// Wait time in seconds between consumer job rounds
	public final int consumerSleepTime;
	/**
	 * 
	 * Creates a new instance of KafkaConsumerConfig.  
	 *  
	 * @param configFile
	 * @throws Exception
	 */
	public KafkaConsumerConfig(String configFile) throws Exception {
		try {
			logger.info("configFile : " + configFile);
			prop.load(new FileInputStream(configFile));
	
			logger.info("Properties : " + prop);
		} catch (Exception e) {
			logger.error("Error reading/loading configFile: " + e.getMessage(), e);
			throw e;
		}

		messageHandlerClass = prop.getProperty("messageHandlerClass",
						"com.xrk.quality.es.plugin.messageHandlers.RawMessageStringHandler");
		messageHandlerQueueNum = Integer.parseInt(prop.getProperty(
				"messageHandlerQueueNum", "10000"));
		indexHandlerClass = prop.getProperty("indexHandlerClass",
						"com.xrk.quality.es.plugin.BasicIndexHandler");
		kafkaBrokersList = prop.getProperty("kafkaBrokersList", "localhost:9092");
		topic = prop.getProperty("topic", "");
		consumerGroupName = prop.getProperty("consumerGroupName", "ESKafkaConsumerClient");
		
		esClusterName = prop.getProperty("esClusterName", "");
		esIndexDynamicName = prop.getProperty("esIndexDynamicName", "");
		esHostPortList = prop.getProperty("esHostPortList", "localhost:9300");
		esIndex = prop.getProperty("esIndex", "kafkaConsumerIndex");
		esIndexType = prop.getProperty("esIndexType", "kafka");
		logPropertyFile = prop.getProperty("logPropertyFile",
				"log4j.properties");
		consumerSleepTime = Integer.parseInt(prop.getProperty(
				"consumerSleepTime", "500"));
		consumerThreadNum = Integer.parseInt(prop.getProperty(
				"consumerThreadNum", "3"));
		consumerPullWaitTime = Integer.parseInt(prop.getProperty(
				"consumerPullWaitTime", "1000"));
		enableAutoCommit = prop.getProperty("enableAutoCommit", "true");
		esIndexingRetrySleepTimeMs = Integer.parseInt(prop.getProperty(
				"esIndexingRetrySleepTimeMs", "1000"));
		esBulkSize = Integer.parseInt(prop.getProperty(
				"esBulkSize", "1000"));
		numberOfEsIndexingRetryAttempts = Integer.parseInt(prop.getProperty(
				"numberOfEsIndexingRetryAttempts", "2"));
		
		appStopTimeoutSeconds = Integer.parseInt(prop.getProperty(
				"appStopTimeoutSeconds", "10"));
		
		logger.info("Config reading complete !");
	}

	public Properties getProperties() {
		return prop;
	}

	public String getEsIndexType() {
		return esIndexType;
	}

	public int getEsIndexingRetrySleepTimeMs() {
		return esIndexingRetrySleepTimeMs;
	}

	public int getNumberOfEsIndexingRetryAttempts() {
		return numberOfEsIndexingRetryAttempts;
	}

}
