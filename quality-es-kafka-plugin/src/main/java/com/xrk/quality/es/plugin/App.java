package com.xrk.quality.es.plugin;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.net.URISyntaxException;
import java.net.URLDecoder;
import java.security.CodeSource;

import org.apache.log4j.xml.DOMConfigurator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 
 * App: 应用程序入口
 *
 * <br>==========================
 * <br> 公司：广州向日葵信息科技有限公司
 * <br> 开发：shunchiguo<shunchiguo@xiangrikui.com>
 * <br> 版本：1.0
 * <br> 创建时间：2016年5月24日
 * <br> JDK版本：1.7
 * <br>==========================
 */

public class App
{	
	private static final Logger logger = LoggerFactory.getLogger(App.class);
	private static final String KAFKA_CONSUMER_SHUTDOWN_THREAD = "kafka-indexer-shutdown-thread";	
	
	public static void main(String[] args) {
		String appBasePath = "";
		try { 
			CodeSource codeSource = App.class.getProtectionDomain().getCodeSource();
	        appBasePath = URLDecoder.decode(codeSource.getLocation().toURI().getPath(), "UTF-8");
    		File jarFile = new File(appBasePath);
        	appBasePath = jarFile.getParentFile().getPath();
        }
        catch (URISyntaxException | UnsupportedEncodingException e) {
            e.printStackTrace();
        }
		UrlToMethodService.init(appBasePath);
		String configPath = String.format("%s/conf/", appBasePath);
		System.out.println("configPath="+configPath);
		String log4jPath = String.format("%slog4j.xml", configPath);
		DOMConfigurator.configureAndWatch(log4jPath, 60000);
		String configFile = String.format("%skafka-es-indexer.properties", configPath);
		
		QualityIndexerDriver driver = new QualityIndexerDriver();
    	Runtime.getRuntime().addShutdownHook(new Thread(KAFKA_CONSUMER_SHUTDOWN_THREAD) {
  	      public void run() {
  	        logger.info("Running Shutdown Hook .... ");
  	        try {
					driver.stop();
				} catch (Exception e) {
					logger.error("Error stopping the Consumer from the ShutdownHook: " + e.getMessage());
				}
  	      }
  	   });

    	try {
			driver.init(configFile);
			driver.start();
		} catch (Exception e) {
			logger.error("Exception from main() - exiting: " + e.getMessage());
		}
    	logger.debug("application exit");    	
    }
}
