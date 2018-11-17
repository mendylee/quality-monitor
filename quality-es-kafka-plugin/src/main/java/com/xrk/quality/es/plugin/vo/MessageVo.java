package com.xrk.quality.es.plugin.vo;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Kafka消息对象
 * MessageVo: MessageVo.java.
 *
 * <br>==========================
 * <br> 公司：广州向日葵信息科技有限公司
 * <br> 开发：shunchiguo<shunchiguo@xiangrikui.com>
 * <br> 版本：1.0
 * <br> 创建时间：2016年5月25日
 * <br> JDK版本：1.7
 * <br>==========================
 */
public class MessageVo
{
	private long offset;
	private String topic;
	private int partition;
	private String key;
	private String value;
	
	public MessageVo(long offset, String topic, int partition, String key, String value){
		this.offset = offset;
		this.topic = topic;
		this.partition = partition;
		this.key = key;
		this.value = value;
	}
	
	public long getOffset()
	{
		return offset;
	}
	public void setOffset(long offset)
	{
		this.offset = offset;
	}
	public String getTopic()
	{
		return topic;
	}
	public void setTopic(String topic)
	{
		this.topic = topic;
	}
	public int getPartition()
	{
		return partition;
	}
	public void setPartition(int partition)
	{
		this.partition = partition;
	}
	public String getKey()
	{
		return key;
	}
	public void setKey(String key)
	{
		this.key = key;
	}
	public String getValue()
	{
		return value;
	}
	public void setValue(String value)
	{
		this.value = value;
	}

	public String getId()
    {
		String strDate = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
	    return String.format("%s_%s_%s_%s", this.topic, strDate, this.partition, this.offset);
    }
}
