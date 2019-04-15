package com.obatis.upload.ftp.config;

/**
 * ftp常量类
 * @author HuangLongPu
 */
public class FtpConstant {

	/**
	 * 文件上传顶级目录
	 */
	public static String UPLOAD_TOP_PATH = null;

	/**
	 * 超时时间
	 */
	public static int TIME_OUT = 10000;

	/**
	 * 文件上传客户端重试次数，默认值0
	 */
	public static final int UPLOAD_RETRY_TIMES_DEFAULT = 0;

	/**
	 * 重试中止边界值，表示重试3次
	 */
	public static final int RETRY_TIMES_FLAG = 3;

	/**
	 * 缓存config文件于内存，方便取值
	 */
	public static FtpConfig DEFAULT_CONFIG = null;

	/**
	 * ############ FTP连接池配置开始 ############
	 */
	/**
	 * 连接池最大数
	 */
	public static final int POOL_MAX_TOTAL = 5;
	/**
	 * 连接池最小空闲
	 */
	public static final int POOL_MIN_IDLE = 1;
	/**
	 * 连接池最大空闲
	 */
	public static final int POOL_MAX_IDLE = 2;
	/**
	 * 最大等待时间，-1表示一直堵塞，单位为毫秒
	 */
	public static final int POOL_MAX_WAIT = 30000;
	/**
	 * 池对象耗尽之后是否阻塞,maxWait小于0时一直等待
	 */
	public static final boolean POOL_BLOCK_WHENEXHAUSTED = true;
	/**
	 * 取对象是验证
	 */
	public static final boolean POOL_TEST_ON_BORROW = true;
	/**
	 * 回收验证
	 */
	public static final boolean POOL_TEST_ON_RETURN = false;
	/**
	 * 创建时验证
	 */
	public static final boolean POOL_TEST_ON_CREATE = true;
	/**
	 * 空闲验证
	 */
	public static final boolean POOL_TEST_WHILEIDLE = true;
	/**
	 * 空闲验证时间,单位毫米，默认为-1，表示不检测,以下设置表示一分钟检测一次是否可用
	 */
	public static final int POOL_TIME_BETWEEN_EVICTION_RUNS_MILLIS = 60000;
	/**
	 *  连接空闲的最小时间，达到此值后空闲链接将会被移除，且保留“minIdle”个空闲连接数。默认为-1表示不移除，以下设置表示空闲1分钟即移除空闲连接
	 */
	public static final int POOL_MIN_EVICTABLE_IDLE_TIMEMILLIS = 60000;
	/**
	 * 后进先出
	 */
	public static final boolean POOL_LIFO = false;
	/**
	 * ############ FTP连接池配置结束 ############
	 */
}
