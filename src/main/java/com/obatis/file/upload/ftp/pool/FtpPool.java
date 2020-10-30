package com.obatis.file.upload.ftp.pool;

import java.io.IOException;

import com.obatis.tools.ValidateTool;
import com.obatis.file.upload.ftp.config.FtpConfig;
import com.obatis.file.upload.ftp.config.FtpConstant;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.pool2.impl.GenericObjectPool;
import org.apache.commons.pool2.impl.GenericObjectPoolConfig;

/**
 * FTP连接池管理类
 * @author HuangLongPu
 */
public class FtpPool {

	private static FtpPool ftpPool;

	/**
	 * 默认设置ftp为被动模式
	 */
	public static boolean DEFAULT_FTP_PASSIVE_MODE = true;

	/**
	 * 初始化ftp连接 表示连接超时为默认10000毫秒 表示连接模式为被动模式
	 * @author HuangLongPu
	 * @param pathName
	 * @param ftpHost
	 * @param ftpPort
	 * @param ftpUsername
	 * @param ftpPassword
	 * @throws Exception
	 */
	public static void initConfig(String pathName, String ftpHost, int ftpPort, String ftpUsername, String ftpPassword) throws Exception {
		initConfig(pathName, ftpHost, ftpPort, ftpUsername, ftpPassword, FtpConstant.TIME_OUT);
	}

	/**
	 * 初始化ftp连接 表示连接超时为默认10000毫秒
	 * @author HuangLongPu
	 * @param pathName
	 * @param ftpHost
	 * @param ftpPort
	 * @param ftpUsername
	 * @param ftpPassword
	 * @throws Exception
	 */
	public static void initConfig(String pathName, String ftpHost, int ftpPort, String ftpUsername, String ftpPassword, boolean ftpPassiveMode) throws Exception {
		initConfig(pathName, ftpHost, ftpPort, ftpUsername, ftpPassword, FtpConstant.TIME_OUT, ftpPassiveMode);
	}

	/**
	 * 初始化ftp连接 自定义超时时间 表示连接模式为被动模式
	 * @author HuangLongPu
	 * @param pathName
	 * @param ftpHost
	 * @param ftpPort
	 * @param ftpUsername
	 * @param ftpPassword
	 * @param timeout
	 * @throws Exception
	 */
	public static void initConfig(String pathName, String ftpHost, int ftpPort, String ftpUsername, String ftpPassword, int timeout) throws Exception {
		initConfig(pathName, ftpHost, ftpPort, ftpUsername, ftpPassword, timeout, true);
	}

	/**
	 * 初始化ftp连接 自定义超时时间 自定义选择连接模式，被动为true，主动为false
	 * @author HuangLongPu
	 * @param pathName
	 * @param ftpHost
	 * @param ftpPort
	 * @param ftpUsername
	 * @param ftpPassword
	 * @param timeout
	 * @param ftpPassiveMode
	 * @throws Exception
	 */
	public static void initConfig(String pathName, String ftpHost, int ftpPort, String ftpUsername, String ftpPassword, int timeout, boolean ftpPassiveMode)
			throws Exception {

		if (ValidateTool.isEmpty(pathName)) {
			throw new Exception("ftp info initConfig error : pathName is empty!");
		}

		FtpConstant.UPLOAD_TOP_PATH = pathName;

		FtpConfig ftpConfig = new FtpConfig();
		ftpConfig.setFtpHost(ftpHost);
		ftpConfig.setFtpPort(ftpPort);
		ftpConfig.setFtpUsername(ftpUsername);
		ftpConfig.setFtpPassword(ftpPassword);

		/**
		 * 连接池最大数
		 */
		ftpConfig.setMaxTotal(FtpConstant.POOL_MAX_TOTAL);
		/**
		 * 连接池最小的空闲数
		 */
		ftpConfig.setMinIdle(FtpConstant.POOL_MIN_IDLE);
		/**
		 * 连接池最大的空闲数
		 */
		ftpConfig.setMaxIdle(FtpConstant.POOL_MAX_IDLE);
		/**
		 * 当连接池最大阻塞时间,超时则抛出异常
		 */
		ftpConfig.setMaxWaitMillis(FtpConstant.POOL_MAX_WAIT);
		/**
		 * 遵循队列先进先出原则
		 */
		ftpConfig.setLifo(FtpConstant.POOL_LIFO);
		/**
		 * 连接空闲的最小时间,达到此值后空闲连接可能会被移除
		 */
		ftpConfig.setMinEvictableIdleTimeMillis(FtpConstant.POOL_MIN_EVICTABLE_IDLE_TIMEMILLIS);
		/**
		 * 连接耗尽时是否阻塞
		 */
		ftpConfig.setBlockWhenExhausted(FtpConstant.POOL_BLOCK_WHENEXHAUSTED);
		/**
		 *  超时时间
		 */
		ftpConfig.setConnectTimeOut(timeout);
		/**
		 * 连接模式
		 */
		ftpConfig.setPassiveMode(ftpPassiveMode);
		/**
		 * 将默认值写入到常量，方便文件上传时重试判断
		 */
		DEFAULT_FTP_PASSIVE_MODE = ftpPassiveMode;
		initPool(ftpConfig);
	}

	/**
	 * 初始化配置ftp连接池
	 * @author HuangLongPu
	 * @param ftpConfig
	 * @throws Exception
	 */
	public synchronized static void initPool(FtpConfig ftpConfig) throws Exception {

		if (ftpPool != null) {
			ftpPool.closeObject();
		}

		// 初始化连接池
		FtpFactory ftpFactory = new FtpFactory();
		ftpFactory.setFtpConfig(ftpConfig);
		ftpPool = new FtpPool(ftpFactory);
		// 将config类信息保存在内存中
		FtpConstant.DEFAULT_CONFIG = ftpConfig;
	}

	/**
	 * 基于pool2的连接池对象
	 * @author HuangLongPu
	 */
	private static GenericObjectPool<FTPClient> pool;

	/**
	 * 传入 FTPClientFactory 对象，基于pool2，配置ftp连接池
	 * @author HuangLongPu
	 * @param clientFactory
	 * @throws Exception 
	 */
	public FtpPool(FtpFactory clientFactory) throws Exception {
		GenericObjectPoolConfig<FTPClient> poolConfig = new GenericObjectPoolConfig<FTPClient>();
		poolConfig.setBlockWhenExhausted(FtpConstant.POOL_BLOCK_WHENEXHAUSTED);
        poolConfig.setMaxWaitMillis(FtpConstant.POOL_MAX_WAIT);
        poolConfig.setMinIdle(FtpConstant.POOL_MIN_IDLE);
        poolConfig.setMaxIdle(FtpConstant.POOL_MAX_IDLE);
        poolConfig.setMaxTotal(FtpConstant.POOL_MAX_TOTAL);
        poolConfig.setTestOnCreate(FtpConstant.POOL_TEST_ON_CREATE);
        poolConfig.setTestWhileIdle(FtpConstant.POOL_TEST_WHILEIDLE);
        poolConfig.setTimeBetweenEvictionRunsMillis(FtpConstant.POOL_TIME_BETWEEN_EVICTION_RUNS_MILLIS);
        poolConfig.setSoftMinEvictableIdleTimeMillis(FtpConstant.POOL_MIN_EVICTABLE_IDLE_TIMEMILLIS);
        poolConfig.setLifo(FtpConstant.POOL_LIFO);
		pool = new GenericObjectPool<FTPClient>(clientFactory, poolConfig);
		
	}

	/**
	 * 获取一个新的ftp连接
	 * @author HuangLongPu
	 * @return
	 * @throws Exception
	 */
	public static FTPClient borrowObject() throws Exception {
		return borrowObject(FtpConstant.UPLOAD_RETRY_TIMES_DEFAULT);
	}
	
	/**
	 * 获取一个新的连接，如果失败，设置重试机制，设置为3次
	 * @author HuangLongPu
	 * @param retry
	 * @return
	 * @throws Exception
	 */
	private static FTPClient borrowObject(int retry) throws Exception {
		if(retry < FtpConstant.RETRY_TIMES_FLAG) {
			FTPClient ftp = pool.borrowObject();
			if(ftp == null) {
				retry++;
				return borrowObject(retry);
			} else {
				return ftp;
			}
		}
		
		return null;
	}

	/**
	 * 将ftp连接归还给连接池，同时将ftp目录重置到顶级目录，根据level设置向上返回次数
	 * @author HuangLongPu
	 * @param ftpClient
	 * @param level
	 * @throws IOException
	 */
	public static void returnObject(FTPClient ftpClient, int level) throws IOException {
		for (int i = 0; i < level; i++) {
			ftpClient.changeToParentDirectory();
		}
		if(ftpClient != null) {
			pool.returnObject(ftpClient);
		}
	}

	/**
	 * 关闭ftp连接池
	 * @author HuangLongPu
	 */
	public void closeObject() {
		if(pool != null) {
			pool.close();
		}
	}
	
}