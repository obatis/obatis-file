package com.sbatis.upload.ftp.pool;

import java.io.IOException;

import com.sbatis.upload.ftp.config.FtpConstant;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.pool2.impl.GenericObjectPool;
import org.apache.commons.pool2.impl.GenericObjectPoolConfig;

public class FTPPool {

	/**
	 * 基于pool2的连接池对象
	 */
	private GenericObjectPool<FTPClient> pool;

	/**
	 * 传入 FTPClientFactory 对象，基于pool2，配置ftp连接池
	 * @param clientFactory
	 * @throws Exception 
	 */
	public FTPPool(FTPFactory clientFactory) throws Exception {
		GenericObjectPoolConfig<FTPClient> poolConfig = new GenericObjectPoolConfig<FTPClient>();
		poolConfig.setBlockWhenExhausted(FtpConstant.POOL_BLOCK_WHENEXHAUSTED);
        poolConfig.setMaxWaitMillis(FtpConstant.POOL_MAX_WAIT);
        poolConfig.setMinIdle(FtpConstant.POOL_MIN_IDLE);
        poolConfig.setMaxIdle(FtpConstant.POOL_MAX_IDLE);
        poolConfig.setMaxTotal(FtpConstant.POOL_MAX_TOTAL);
        poolConfig.setTestOnCreate(FtpConstant.POOL_TEST_ON_CREATE);
        poolConfig.setTestWhileIdle(FtpConstant.POOL_TEST_WHILEIDLE);
        poolConfig.setTimeBetweenEvictionRunsMillis(FtpConstant.POOL_TIME_BETWEEN_EVICTION_RUNS_MILLIS);
        poolConfig.setSoftMinEvictableIdleTimeMillis(FtpConstant.POOL_SOFT_MIN_EVICTABLE_IDLE_TIMEMILLIS);
        poolConfig.setLifo(FtpConstant.POOL_LIFO);
		pool = new GenericObjectPool<FTPClient>(clientFactory, poolConfig);
		
	}

	/**
	 * 获取一个新的ftp连接
	 * @return
	 * @throws Exception
	 */
	public FTPClient borrowObject() throws Exception {
		return borrowObject(FtpConstant.RETRY_DEFAULT);
	}
	
	/**
	 * 获取一个新的连接，如果失败，设置重试机制，设置为3次
	 * @param retry
	 * @return
	 * @throws Exception
	 */
	private FTPClient borrowObject(int retry) throws Exception {
		if(retry < FtpConstant.RETRY_BREAK_FLAG) {
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
	 * @param ftpClient
	 * @param level
	 * @throws IOException
	 */
	public void returnObject(FTPClient ftpClient, int level) throws IOException {
		for (int i = 0; i < level; i++) {
			ftpClient.changeToParentDirectory();
		}
		if(ftpClient != null) {
			pool.returnObject(ftpClient);
		}
	}

	/**
	 * 关闭ftp连接池
	 */
	public void closeObject() {
		if(pool != null) {
			pool.close();
		}
	}
	
}