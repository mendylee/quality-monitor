package com.xrk.quality.es.plugin.jobs;

import java.util.Arrays;
import java.util.Properties;
import java.util.concurrent.Callable;
import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.KafkaException;
import org.apache.kafka.common.errors.WakeupException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.xrk.quality.es.plugin.KafkaConsumerConfig;
import com.xrk.quality.es.plugin.MessageHandler;
import com.xrk.quality.es.plugin.vo.MessageVo;

/**
 * 索引处理作业，负责生成消息处理对象，获取Kafka消息信息等操作
 * IndexerJob: IndexerJob.java.
 *
 * <br>==========================
 * <br> 公司：广州向日葵信息科技有限公司
 * <br> 开发：shunchiguo<shunchiguo@xiangrikui.com>
 * <br> 版本：1.0
 * <br> 创建时间：2016年5月24日
 * <br> JDK版本：1.7
 * <br>==========================
 */
public class IndexerJob implements Callable<Boolean>
{
	private static final Logger logger = LoggerFactory.getLogger(IndexerJob.class);

	/**
	 * 任务运行状态
	 */
	private final AtomicBoolean closed = new AtomicBoolean(false);
	/**
	 * Kafka消费者对象
	 */
	private final KafkaConsumer<String, String> consumer;

	private String topic;
	private int consumerPullWaitTime;
	private Properties props;
	private final KafkaConsumerConfig consumerConfig;
	/**
	 * 消息处理类
	 */
	private MessageHandler msgHandler;

	/**
	 * 
	 * Creates a new instance of IndexerJob.  
	 *  
	 * @param config
	 * @throws Exception
	 */
	public IndexerJob(final KafkaConsumerConfig config) throws Exception {
		logger.info("Instantiating IndexerJob");
		this.consumerConfig = config;

		this.topic = this.consumerConfig.topic;
		this.consumerPullWaitTime = this.consumerConfig.consumerPullWaitTime;
		props = new Properties();
		props.put("bootstrap.servers", config.kafkaBrokersList);
		props.put("group.id", config.consumerGroupName);
		props.put("enable.auto.commit", config.enableAutoCommit);
		props.put("auto.commit.interval.ms", "1000");
		props.put("session.timeout.ms", "30000");
		props.put("auto.offset.reset", "latest");//"latest", "earliest", "none"
		props.put("key.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
		props.put("value.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");		
		consumer = new KafkaConsumer<>(props);
		createMessageHandler();
	}
	
	/**
	 * 
	 * 创建消息处理类  
	 *    
	 * @throws Exception
	 */
	private void createMessageHandler() throws Exception {
		try {
			logger.info("MessageHandler Class given in config is {}, topic is {}", consumerConfig.messageHandlerClass, this.topic);
			msgHandler = (MessageHandler) Class
					.forName(consumerConfig.messageHandlerClass)
					.getConstructor(KafkaConsumerConfig.class)
					.newInstance(consumerConfig);
			logger.debug("Created and initialized MessageHandler: {}", consumerConfig.messageHandlerClass);
		} catch (Exception e) {
			logger.error("Exception creating MessageHandler class: ", e);
			throw e;
		}
	}
	
	/**
	 * 执行任务，从Kafka中获取已推送的消息，并将其添加到待处理队列中
	 * @see java.util.concurrent.Callable#call()
	 */
	@Override
	public Boolean call() throws Exception
	{	
		consumer.subscribe(Arrays.asList(this.topic.split(",")));
		
		try {
			while (!closed.get()) {
				try {
					if(!msgHandler.canEnqueue()){
						logger.info("message handler not enqueue! sleep {} ms", this.consumerConfig.consumerSleepTime);
						Thread.sleep(this.consumerConfig.consumerSleepTime);
						continue;
					}
					
					ConsumerRecords<String, String> records = consumer.poll(this.consumerPullWaitTime);
					// Handle new records
					for (ConsumerRecord<String, String> record : records){
						logger.debug("topic = {}, partition = {}, offset = {}, value = {}", 
								record.topic(), record.partition(), record.offset(), record.value());	
						msgHandler.enqueue(new MessageVo(record.offset(), record.topic(), record.partition(), record.key(), record.value()));
					}
				}
				catch (WakeupException e) {
					logger.error(e.getMessage(), e);
					if (!closed.get()){
						//throw e;
					}
				}
				catch(KafkaException e){
					logger.error(e.getMessage(), e);
					if (!closed.get()){
						//throw e;
					}
				}
			}
		}
		finally {
			consumer.close();
			msgHandler.close();
		}
		return true;
	}

	// Shutdown hook which can be called from a separate thread
	public void shutdown()
	{
		closed.set(true);
		consumer.wakeup();
	}

}
