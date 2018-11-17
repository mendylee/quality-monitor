package com.xrk.quality.es.plugin;

import java.lang.reflect.InvocationTargetException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Iterator;
import java.util.Queue;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import org.elasticsearch.ElasticsearchException;
import org.elasticsearch.action.bulk.BulkItemResponse;
import org.elasticsearch.action.bulk.BulkRequestBuilder;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.index.IndexRequest.OpType;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.settings.Settings.Builder;
import org.elasticsearch.common.transport.InetSocketTransportAddress;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.xrk.quality.es.plugin.messageHandlers.BasicIndexHandler;
import com.xrk.quality.es.plugin.vo.MessageVo;

/**
 * 消息处理入口类，由IndexerJob类动态创建
 * MessageHandler: MessageHandler.java.
 *
 * <br>==========================
 * <br> 公司：广州向日葵信息科技有限公司
 * <br> 开发：shunchiguo<shunchiguo@xiangrikui.com>
 * <br> 版本：1.0
 * <br> 创建时间：2016年5月24日
 * <br> JDK版本：1.7
 * <br>==========================
 */
public class MessageHandler
{
	private static final Logger logger = LoggerFactory.getLogger(MessageHandler.class);
	/**
	 * ES客户端对象
	 */
	private TransportClient esClient;
	/**
	 * Kafka配置对象
	 */
	private KafkaConsumerConfig config;
	/**
	 * ES批量添加创建对象
	 */
	private BulkRequestBuilder bulkRequestBuilder;
	/**
	 * 日志索引处理对象，处理日志格式，以匹配ES索引结构
	 */
	private BasicIndexHandler indexHandler;
	/**
	 * 待处理消息队列
	 */
	private Queue<MessageVo> queueMessage;
	/**
	 * 调度服务线程对象
	 */
	private ScheduledExecutorService scheduledExecutorService = null;
	/**
	 * 停止对象
	 */
	private AtomicBoolean bStop = new AtomicBoolean();

	/**
	 * 
	 * Creates a new instance of MessageHandler.  
	 *  
	 * @param config
	 * @throws Exception
	 */
	public MessageHandler(KafkaConsumerConfig config) throws Exception {
		this.config = config;
		this.bulkRequestBuilder = null;
		queueMessage = new LinkedBlockingQueue<MessageVo>(); 
		// instantiate specified in the config IndexHandler class
		try {
			//加载配置的索引处理类
			String[] aryClass = config.indexHandlerClass.split(",");
			indexHandler = null;
			for(String s : aryClass){
				logger.info("Created IndexHandler: ", s);
				indexHandler = (BasicIndexHandler) Class.forName(s)
				        .getConstructor(KafkaConsumerConfig.class, BasicIndexHandler.class)
				        .newInstance(config, indexHandler);	
			}
		}
		catch (InstantiationException | IllegalAccessException | IllegalArgumentException
		        | InvocationTargetException | NoSuchMethodException | SecurityException
		        | ClassNotFoundException e) {
			logger.error("Exception creating IndexHandler: " + e.getMessage(), e);
			throw e;
		}
		logger.info("Created Message Handler");
		bStop.set(false);
		prepareEsClient();
		prepareScheduleTask();	
	}
	
	/**
	 * 
	 * 准备ES客户端对象  
	 *    
	 * @throws UnknownHostException
	 */
	private void prepareEsClient() throws UnknownHostException
    {
		Builder settings = Settings.builder();
		settings.put("cluster.name", this.config.esClusterName);
		
		esClient = TransportClient.builder().settings(settings).build();
		String[] esHostPortList = this.config.esHostPortList.trim().split(",");
		for (String eachHostPort : esHostPortList) {
			String[] ary = eachHostPort.split(":");
			String host = ary[0].trim();
			int port = 9300;
			if(ary.length > 1){
				port = Integer.parseInt(ary[1].trim());
			}
			esClient.addTransportAddress(new InetSocketTransportAddress(InetAddress.getByName(host), port));
		}		
		bulkRequestBuilder = esClient.prepareBulk();
    }

	/**
	 * 
	 * 准备调度任务对象  
	 *
	 */
	private void prepareScheduleTask(){
		scheduledExecutorService = Executors.newSingleThreadScheduledExecutor();
		scheduledExecutorService.scheduleAtFixedRate(new Runnable() {	
			@Override
			public void run()
			{
				MessageVo messageVo = queueMessage.poll();
				while(messageVo != null){
					//add bulk process
					addToBulk(messageVo);
					messageVo = queueMessage.poll();
				}
				postBulk();
			}
		},  1, 1, TimeUnit.SECONDS);
	}
	
	/**
	 * 
	 * 添加消息到批处理对象中  
	 *    
	 * @param msgVo
	 */
	private void addToBulk(MessageVo msgVo){
		try {
			String topic = msgVo.getTopic();
	        bulkRequestBuilder.add(esClient
	        		.prepareIndex(indexHandler.getIndexName(topic), indexHandler.getIndexType(topic), msgVo.getId())
	        		.setOpType(OpType.CREATE)
	        		.setSource(indexHandler.transformMessage(msgVo)));
        }
        catch (Exception e) {
	        logger.error("push message to es failed!key={}, topic={}, offset={}, value={}", 
	        		msgVo.getKey(), msgVo.getTopic(), msgVo.getOffset(), msgVo.getValue());
	        logger.error(e.getMessage(), e);
        }
		if(bulkRequestBuilder.numberOfActions() > config.esBulkSize){
			postBulk();
		}
	}
	
	/**
	 * 
	 * 提交批量数据到ES  
	 *
	 */
	private void postBulk(){
		if(bulkRequestBuilder.numberOfActions() < 0){
			return ;
		}
		
		try {
	        postToElasticSearch();
        }
        catch (Exception e) {
        	logger.error(e.getMessage(), e);
        }
		bulkRequestBuilder = esClient.prepareBulk();
	}
	
	/**
	 * 
	 * 提交数据到ES索引  
	 *    
	 * @return
	 * @throws Exception
	 */
	private boolean postToElasticSearch() throws Exception
	{
		BulkResponse bulkResponse = null;
		BulkItemResponse bulkItemResp = null;
		// Nothing/NoMessages to post to ElasticSearch
		if (bulkRequestBuilder.numberOfActions() <= 0) {
			//logger.debug("No messages to post to ElasticSearch - returning");
			return true;
		}

		int totalNum = bulkRequestBuilder.numberOfActions();
		int failedCount = 0;
		try {
			bulkResponse = bulkRequestBuilder.execute().actionGet();
		}
		catch (ElasticsearchException e) {
			logger.error("Failed to post messages to ElasticSearch: " + e.getMessage(), e);
			throw e;
		}
		logger.debug("Time to post messages to ElasticSearch: {} ms",
		        bulkResponse.getTookInMillis());
		if (bulkResponse.hasFailures()) {
			logger.error("Bulk Message Post to ElasticSearch has errors: {}",
			        bulkResponse.buildFailureMessage());
			
			Iterator<BulkItemResponse> bulkRespItr = bulkResponse.iterator();
			
			while (bulkRespItr.hasNext()) {
				bulkItemResp = bulkRespItr.next();
				if (bulkItemResp.isFailed()) {
					failedCount++;
					String errorMessage = bulkItemResp.getFailure().getMessage();
					String restResponse = bulkItemResp.getFailure().getStatus().name();
					logger.error("Failed Message #{}, REST response:{}; errorMessage:{}",
					        failedCount, restResponse, errorMessage);
				}
			}
			logger.info("# of failed to post messages to ElasticSearch: {} ", failedCount);
			return false;
		}
		logger.info("Bulk Post to ElasticSearch finished OK, total document:{}, failed:{}", totalNum, failedCount);
		bulkRequestBuilder = null;
		return true;
	}

	/**
	 * 
	 * 将获取到的消息入队  
	 *    
	 * @param message
	 */
	public void enqueue(MessageVo message)
    {
		queueMessage.offer(message);
    }

	/**
	 * 
	 * 检测当前队列是否允许入队  
	 *    
	 * @return
	 */
	public boolean canEnqueue()
    {
		if(bStop.get()){
			return false;
		}
		
		//检测队列大小以及ES客户端状态
	    if(queueMessage.size() > this.config.messageHandlerQueueNum){
	    	return false;
	    }
	    
	    return true;
    }

	/**
	 * 
	 * 停止处理服务  
	 *
	 */
	public void close()
    {
		bStop.set(true);
		//
		int retry = 0;
		while(retry < 5 && queueMessage.size() > 0){
			try {
	            Thread.sleep(1000);
            }
            catch (InterruptedException e) {	            
	            e.printStackTrace();
            }
			retry++;
		}
		
		try
		{
			scheduledExecutorService.shutdown();
		}
		catch(Exception e){
			logger.error(e.getMessage(), e);
		}
		scheduledExecutorService = null;
		
	    if(esClient != null){
	    	esClient.close();
	    }
	    esClient = null;
    }
}
