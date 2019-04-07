package com.sbatis.upload.ftp;

import com.sbatis.convert.date.DateCommonConvert;
import com.sbatis.upload.ftp.config.FtpConfig;
import com.sbatis.upload.ftp.config.FtpConstant;
import com.sbatis.upload.ftp.pool.FTPFactory;
import com.sbatis.upload.ftp.pool.FTPPool;
import com.sbatis.validate.CommonValidate;
import org.apache.commons.net.ftp.FTPClient;

import java.io.*;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * ftp模块文件上传组件服务
 * @author HuangLongPu
 */
public class FtpUploadFactory implements Serializable {
	
	/**
	 * 默认设置ftp为被动模式
	 */
	private static boolean DEFAULT_FTP_PASSIVE_MODE = true;

	private static Map<String, UploadPathTempData> uploadPathTempDataMap = new HashMap<>();

	/***
	 * 一级目录 二级目录 两位(0-9数字开头，字母结尾) 三级目录(字母开头，数字结尾)
	 */

	private static final String pathY = "ABCDEFGHJK";
	private static final String pathX = "0123456789";
	private static final int pathCompareSize = 9;

	private static FTPPool ftpPool;

	private FtpUploadFactory() {
	}

	/**
	 * 获取存储目录
	 * @author HuangLongPu
	 * @param uploadPathTempData
	 * @return
	 */
	private static void getUploadPath(UploadPathTempData uploadPathTempData) {

		int pathSecondX = uploadPathTempData.getPathSecondX();
		int pathSecondY = uploadPathTempData.getPathSecondY();
		int pathThirdX = uploadPathTempData.getPathThirdX();
		int pathThirdY = uploadPathTempData.getPathThirdY();
		// 通过递归方式，得到矩阵目录
		comparePathSize(uploadPathTempData, pathSecondX, pathSecondY, pathThirdX, pathThirdY);
	}

	/**
	 * 判断目录索引，通过递归方式，得到当前的矩阵存储目录
	 * @param pathSecondX
	 * @param pathSecondY
	 * @param pathThirdX
	 * @param pathThirdY
	 * @return
	 */
	private static void comparePathSize(UploadPathTempData uploadPathTempData, int pathSecondX, int pathSecondY, int pathThirdX, int pathThirdY) {
		boolean flag = true;
		if (pathThirdX > pathCompareSize) {
			pathThirdX = 0;
			pathThirdY++;
			flag = false;
		}
		if(pathThirdY > pathCompareSize) {
			pathThirdX = 0;
			pathThirdY = 0;
			pathSecondX++;
			flag = false;
		}
		if(pathSecondX > pathCompareSize) {
			pathThirdX = 0;
			pathThirdY = 0;
			pathSecondX = 0;
			pathSecondY++;
			flag = false;
		}
		if(pathSecondY > pathCompareSize) {
			pathSecondY = 0;
			pathThirdX = 0;
			pathThirdY = 0;
			pathSecondX = 0;
			flag = false;
		}

		if(flag) {
			pathThirdX++;
		}
		if(pathThirdX > pathCompareSize) {
			comparePathSize(uploadPathTempData, pathSecondX, pathSecondY, pathThirdX, pathThirdY);
		} else {
			uploadPathTempData.setPathSecondX(pathSecondX);
			uploadPathTempData.setPathSecondY(pathSecondY);
			uploadPathTempData.setPathThirdX(pathThirdX);
			uploadPathTempData.setPathThirdY(pathThirdY);
		}
	}

	/**
	 * 初始化ftp连接 表示连接超时为默认10000毫秒 表示连接模式为被动模式
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
	 * 初始化ftp连接 自定义超时时间 表示连接模式为被动模式
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

		if (CommonValidate.isNull(pathName)) {
			throw new Exception("ftp info initConfig error : pathName is empty!");
		}

		FtpConstant.TOP_PATH = pathName;

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
		FTPFactory ftpFactory = new FTPFactory();
		ftpFactory.setFtpConfig(ftpConfig);
		ftpPool = new FTPPool(ftpFactory);
		// 将config类信息保存在内存中
		FtpConstant.DEFAULT_CONFIG = ftpConfig;
	}

	/**
	 * 根据ftp服务器的文件路径删除文件
	 * @author HuangLongPu
 	 * @param filePath
	 * @return
	 */
	public static boolean delete(String filePath) {
		return deleteFile(filePath);
	}

	/**
	 * 传入File 进行文件上传
	 * @author HuangLongPu
	 * @param file
	 * @return
	 * @throws FileNotFoundException
	 */
	public static String upload(File file) throws FileNotFoundException {
		return upload(file, null);
	}

	/**
	 * 传入File 进行文件上传，同时规定文件类型
	 * @author HuangLongPu
	 * @param file
	 * @param typeName
	 * @return
	 * @throws FileNotFoundException
	 */
	public static String upload(File file, String typeName) throws FileNotFoundException {
		return upload(new FileInputStream(file), file.getName().substring(file.getName().lastIndexOf(".")), typeName);
	}

	/**
	 * 传入字节数组进行文件上传，同时传入文件名或者文件后缀
	 * @author HuangLongPu
	 * @param data
	 * @param fileName
	 * @return
	 * @throws FileNotFoundException
	 */
	public static String upload(byte[] data, String fileName) {
		fileName = fileName.indexOf(".") == -1 ? fileName : fileName.substring(fileName.lastIndexOf("."));
		ByteArrayInputStream inputStream = new ByteArrayInputStream(data);
		return upload(inputStream, fileName, null);
	}

	/**
	 * 传入字节数组进行文件上传，同时传入文件名或者文件后缀，同时规定文件类型
	 * @author HuangLongPu
	 * @param data
	 * @param fileName
	 * @param typeName
	 * @author HuangLongPu
	 * @return
	 * @throws FileNotFoundException
	 */
	public static String upload(byte[] data, String fileName, String typeName) {
		String fileSuffix = fileName.indexOf(".") == -1 ? fileName : fileName.substring(fileName.lastIndexOf("."));
		ByteArrayInputStream inputStream = new ByteArrayInputStream(data);
		return upload(inputStream, fileSuffix, typeName);
	}

	/**
	 * 文件上传服务，以文件流的形式上传
	 * @author HuangLongPu
	 * @param inputStream
	 * @param fileName
	 * @return
	 */
	public static String upload(InputStream inputStream, String fileName) {
		String fileSuffix = fileName.indexOf(".") == -1 ? fileName : fileName.substring(fileName.lastIndexOf("."));
//		if(fileName.contains(".")) {
//			fileSuffix = fileName.substring(fileName.lastIndexOf("."));
//		} else {
//			fileSuffix = fileName;
//		}
		return upload(inputStream, fileSuffix, null);
	}

	/**
	 * 文件上传
	 * @author HuangLongPu
	 * @param inputStream
	 * @param fileSuffix
	 * @param typeName
	 * @return
	 */
	public static String upload(InputStream inputStream, String fileSuffix, String typeName) {
		return upload(inputStream, fileSuffix, typeName, FtpConstant.RETRY_DEFAULT);
	}

	/**
	 * 加入重试机制
	 * @author HuangLongPu
	 * @param inputStream
	 * @param fileSuffix
	 * @param typeName
	 * @param retryTimes
	 * @return
	 */
	private static String upload(InputStream inputStream, String fileSuffix, String typeName, int retryTimes) {

		// 获取"yyyyMM"格式的字符串
		String curYearMonth = DateCommonConvert.formatCurYearMonth();
		String topPath = null;
		if (CommonValidate.isNull(typeName)) {
			topPath = FtpConstant.TOP_PATH;
		} else {
			// 默认目录+自定义文件夹
			topPath = FtpConstant.TOP_PATH + "/" + typeName;
		}

		UploadPathTempData uploadPathTempData = uploadPathTempDataMap.get(topPath);// 获取Map中的对象
		if (uploadPathTempData == null) {
			uploadPathTempData = new UploadPathTempData();
		}

		if(!CommonValidate.isNull(uploadPathTempData.getUploadYearMonth())){
			if (!curYearMonth.equals(uploadPathTempData.getUploadYearMonth())) {
				uploadPathTempDataMap.clear();
				uploadPathTempData = new UploadPathTempData();
				uploadPathTempData.setUploadYearMonth(curYearMonth);
			}
		}

		int topLevel = 0;
		String filePath = null;
		FTPClient ftpClient = null;

		// 标记文件是否上传成功，true为上传成功，false表示失败
		boolean uploadSuccess = false;
		try {
			ftpClient = ftpPool.borrowObject();
			if(ftpClient == null) {
				return null;
			}
			
			fileSuffix = (fileSuffix.indexOf(".") == -1 ? fileSuffix : fileSuffix.replace(".", "")).toLowerCase();
			String uploadFileName = getUploadFileName(fileSuffix);
			// 获取文件上传目录
			getUploadPath(uploadPathTempData);
			// 添加到缓存，方便下次取值判断
			uploadPathTempDataMap.put(topPath, uploadPathTempData);
			topPath += "/" + curYearMonth;

			// 二级目录矩阵
			topPath += "/" + pathY.charAt(uploadPathTempData.getPathSecondY()) + pathX.charAt(uploadPathTempData.getPathSecondX());
			// 三级目录矩阵
			topPath += "/" + pathY.charAt(uploadPathTempData.getPathThirdY()) + pathX.charAt(uploadPathTempData.getPathThirdX());

			if(retryTimes == FtpConstant.RETRY_BREAK_FLAG - 1) {
				// 说明是最后一次重试，更改连接模式
				if(DEFAULT_FTP_PASSIVE_MODE) {
					// 表示原先为被动模式，改为主动模式
					DEFAULT_FTP_PASSIVE_MODE = false;
					ftpClient.enterLocalActiveMode();
				} else {
					ftpClient.enterLocalPassiveMode();
				}
			}
			
			String[] pathArray = topPath.split("/");
			topLevel = pathArray.length;
			for (String path : pathArray) {
				ftpClient.makeDirectory(path);
				ftpClient.changeWorkingDirectory(path);
			}
			uploadSuccess = ftpClient.storeFile(uploadFileName, inputStream);
			if (uploadSuccess) {
				filePath = topPath + "/" + uploadFileName;
			}
			
		} catch (Exception e) {
			if(retryTimes == FtpConstant.RETRY_BREAK_FLAG - 1) {
				e.printStackTrace();
			}
		} finally {
			if(ftpClient != null) {
				try {
					ftpPool.returnObject(ftpClient, topLevel);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}

			retryTimes++;
			if ((uploadSuccess || retryTimes >= FtpConstant.RETRY_BREAK_FLAG) && inputStream != null) {
				try {
					inputStream.close();
					inputStream = null;
				} catch (Exception e2) {
					e2.printStackTrace();
				}
			}
		}
		
		/**
		 * 采用递归的形式进行上传重试
		 */
		if (!uploadSuccess && retryTimes < FtpConstant.RETRY_BREAK_FLAG) {
			filePath = upload(inputStream, fileSuffix, typeName, retryTimes);
		}
		/**
		 * 最后一次重试时，切换ftp连接的连接模式进行重试，重新初始化一次连接池
		 */
		if(uploadSuccess && retryTimes == FtpConstant.RETRY_BREAK_FLAG) {
			FtpConstant.DEFAULT_CONFIG.setPassiveMode(DEFAULT_FTP_PASSIVE_MODE);
			try {
				initPool(FtpConstant.DEFAULT_CONFIG);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return filePath;
	}


	/**
	 * 删除文件,提供文件名包括后缀
	 * @author HuangLongPu
	 * @param filePath
	 * @return
	 */
	public static boolean deleteFile(String filePath) {
		boolean success = false;
		FTPClient ftpClient = null;
		try {
			ftpClient = ftpPool.borrowObject();
			ftpClient.deleteFile(filePath);
			success = true;
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				ftpPool.returnObject(ftpClient, 0);
			} catch (Exception ioe) {
				ioe.printStackTrace();
			}
		}
		return success;
	}

	/**
	 * 获取文件名，防止ftp文件服务器文件名重复，需要传入文件后缀名进行拼接
	 * @author HuangLongPu
	 * @param fileSuffix
	 * @return
	 */
	private static String getUploadFileName(String fileSuffix) {
		return DateCommonConvert.formatCurDateTimeMillis() + "_" + UUID.randomUUID().toString().replace("-", "01") + "." + fileSuffix;
	}
}