package com.sbatis.upload.ftp;

import com.sbatis.convert.date.DateCommonConvert;
import com.sbatis.upload.ftp.config.FtpConfig;
import com.sbatis.upload.ftp.config.FtpConstant;
import com.sbatis.upload.ftp.pool.FTPFactory;
import com.sbatis.upload.ftp.pool.FTPPool;
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

	private static Map<String, PathRecordData> mapPathRecordData = new HashMap<String, PathRecordData>();

	/***
	 * 一级目录 二级目录 两位(0-9数字开头，字母结尾) 三级目录(字母开头，数字结尾)
	 */

	private static String pathY = "ABCDEFGHJK";
	private static String pathX = "0123456789";

	private static int pathCompareSize = 9;

	private static FTPPool ftpPool;

	private FtpUploadFactory() {
	}

	/**
	 * 获取存储目录
	 * @author HuangLongPu
	 * @param pathRecordData
	 * @return
	 */
	private static PathRecordData getPath(PathRecordData pathRecordData) {

		int pathSecondX = pathRecordData.getPathSecondX();
		int pathSecondY = pathRecordData.getPathSecondY();
		int pathThirdX = pathRecordData.getPathThirdX();
		int pathThirdY = pathRecordData.getPathThirdY();
		// 通过递归方式，得到矩阵目录
		comparePathSize(pathRecordData, pathSecondX, pathSecondY, pathThirdX, pathThirdY);
		return pathRecordData;
	}

	/**
	 * 判断目录索引，通过递归方式，得到当前的矩阵存储目录
	 * @param pathSecondX
	 * @param pathSecondY
	 * @param pathThirdX
	 * @param pathThirdY
	 * @return
	 */
	private static void comparePathSize(PathRecordData pathRecordData, int pathSecondX, int pathSecondY, int pathThirdX, int pathThirdY) {
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
			comparePathSize(pathRecordData, pathSecondX, pathSecondY, pathThirdX, pathThirdY);
		} else {
			pathRecordData.setPathSecondX(pathSecondX);
			pathRecordData.setPathSecondY(pathSecondY);
			pathRecordData.setPathThirdX(pathThirdX);
			pathRecordData.setPathThirdY(pathThirdY);
		}
	}

	/**
	 * 初始化ftp连接 表示连接超时为默认10000毫秒 表示连接模式为被动模式
	 * @param projectName
	 * @param ftpHost
	 * @param ftpPort
	 * @param ftpUsername
	 * @param ftpPassword
	 * @throws Exception
	 */
	public static void initConfig(String projectName, String ftpHost, int ftpPort, String ftpUsername, String ftpPassword) throws Exception {
		initConfig(projectName, ftpHost, ftpPort, ftpUsername, ftpPassword, FtpConstant.TIME_OUT);
	}

	/**
	 * 初始化ftp连接 自定义超时时间 表示连接模式为被动模式
	 * @param projectName
	 * @param host
	 * @param port
	 * @param username
	 * @param password
	 * @param clientTimeout
	 * @throws Exception
	 */
	public static void initConfig(String projectName, String host, int port, String username, String password, int clientTimeout) throws Exception {
		initConfig(projectName, host, port, username, password, clientTimeout, true);
	}

	/**
	 * 初始化ftp连接 自定义超时时间 自定义选择连接模式，被动为true，主动为false
	 * @param projectName
	 * @param host
	 * @param port
	 * @param username
	 * @param password
	 * @param clientTimeout
	 * @param passiveMode
	 * @throws Exception
	 */
	public static void initConfig(String projectName, String host, int port, String username, String password, int clientTimeout, boolean passiveMode)
			throws Exception {

		if (projectName == null || "".equals(projectName.trim())) {
			throw new Exception("initConfig error:project is empty");
		}

		FtpConstant.TOP_PATH = projectName;

		FtpConfig ftpConfig = new FtpConfig();
		ftpConfig.setFtpHost(host);
		ftpConfig.setFtpPort(port);
		ftpConfig.setFtpUsername(username);
		ftpConfig.setFtpPassword(password);

		ftpConfig.setMaxTotal(15); // 池中的最大连接数
		ftpConfig.setMinIdle(2); // 最少的空闲连接数
		ftpConfig.setMaxIdle(5); // 最多的空闲连接数
		ftpConfig.setMaxWaitMillis(-1); // 当连接池资源耗尽时,调用者最大阻塞的时间,超时时抛出异常 单位:毫秒数
		// ，-1位一直堵塞
		ftpConfig.setLifo(true); // 连接池存放池化对象方式,true放在空闲队列最前面,false放在空闲队列最后
		ftpConfig.setMinEvictableIdleTimeMillis(1000L * 60L * 30L); // 连接空闲的最小时间,达到此值后空闲连接可能会被移除,默认即为30分钟
		ftpConfig.setBlockWhenExhausted(true); // 连接耗尽时是否阻塞,默认为true
		ftpConfig.setConnectTimeOut(clientTimeout);
		ftpConfig.setPassiveMode(passiveMode);
		// 将默认值写入到常量，方便文件上传时重试判断
		DEFAULT_FTP_PASSIVE_MODE = passiveMode;
		initPool(ftpConfig);
	}

	/**
	 * 初始化配置ftp连接池
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
		
		// 临时保存config类
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
	 * @param fileType
	 * @return
	 * @throws FileNotFoundException
	 */
	public static String upload(File file, String fileType) throws FileNotFoundException {
		return upload(new FileInputStream(file), file.getName().substring(file.getName().lastIndexOf(".")), fileType);
	}

	/**
	 * 传入字节数组进行文件上传，同时传入文件名或者文件后缀
	 * @author HuangLongPu
	 * @param data
	 * @param fileName
	 * @return
	 * @throws FileNotFoundException
	 */
	public static String upload(byte[] data, String fileName) throws FileNotFoundException {
		fileName = fileName.indexOf(".") == -1 ? fileName : fileName.substring(fileName.lastIndexOf("."));
		ByteArrayInputStream inputStream = new ByteArrayInputStream(data);
		return upload(inputStream, fileName, null);
	}

	/**
	 * 传入字节数组进行文件上传，同时传入文件名或者文件后缀，同时规定文件类型
	 * @author HuangLongPu
	 * @param data
	 * @param fileName
	 * @param fileType
	 * @return
	 * @throws FileNotFoundException
	 */
	public static String upload(byte[] data, String fileName, String fileType) throws FileNotFoundException {
		fileName = fileName.indexOf(".") == -1 ? fileName : fileName.substring(fileName.lastIndexOf("."));
		ByteArrayInputStream inputStream = new ByteArrayInputStream(data);
		return upload(inputStream, fileName, fileType);
	}

	/**
	 * 文件上传服务，以文件流的形式上传
	 * @author HuangLongPu
	 * @param is
	 * @param fileName
	 * @return
	 */
	public static String upload(InputStream is, String fileName) {
		if(fileName.contains(".")) {
			fileName = fileName.substring(fileName.lastIndexOf("."));
		}
		return upload(is, fileName, null);
	}

	/**
	 * 文件上传
	 * @author HuangLongPu
	 * @param is
	 * @param suffix
	 * @param fileType
	 * @return
	 */
	public static String upload(InputStream is, String suffix, String fileType) {
		return upload(is, suffix, fileType, FtpConstant.RETRY_DEFAULT);
	}

	/**
	 * 加入重试机制
	 * @author HuangLongPu
	 * @param is
	 * @param suffix
	 * @param fileType
	 * @param retryCount
	 * @return
	 */
	private static String upload(InputStream is, String suffix, String fileType, int retryCount) {

		// 获取"yyyyMM"格式的字符串
		String fileDate = DateCommonConvert.formatCurYearMonth();
		String parentPath = null;
		if (fileType == null || fileType.trim().isEmpty()) {
			parentPath = FtpConstant.TOP_PATH;
		} else {
			// 默认目录+自定义文件夹
			parentPath = FtpConstant.TOP_PATH + "/" + fileType;
		}

		PathRecordData pathRecordData = mapPathRecordData.get(parentPath);// 获取Map中的对象
		if (null == pathRecordData) {
			pathRecordData = new PathRecordData();
		}

		if(null != pathRecordData.getFileDate()){
			if (!fileDate.equals(pathRecordData.getFileDate())) {// 获取日期比较
				mapPathRecordData.clear();
				pathRecordData = new PathRecordData();
				pathRecordData.setFileDate(fileDate);
			}
		}

		int parentLevel = 0;
		String filePath = null;
		FTPClient ftpClient = null;

		// 标记文件是否上传成功，true为上传成功，false表示失败
		boolean uploadFlag = false;
		try {
			ftpClient = ftpPool.borrowObject();
			if(ftpClient == null) {
				return null;
			}
			
			String fileSuffix = (suffix.indexOf(".") == -1 ? suffix : suffix.replace(".", "")).toLowerCase();
			String fileName = getUploadFileName(fileSuffix);
			// 判断是否指定了文件夹
			pathRecordData = getPath(pathRecordData);
			// 添加到map中
			mapPathRecordData.put(parentPath, pathRecordData);
			parentPath += "/" + fileDate;

			// 二级目录矩阵
			parentPath += "/" + pathY.charAt(pathRecordData.getPathSecondY()) + pathX.charAt(pathRecordData.getPathSecondX());
			// 三级目录矩阵
			parentPath += "/" + pathY.charAt(pathRecordData.getPathThirdY()) + pathX.charAt(pathRecordData.getPathThirdX());

			if(retryCount == FtpConstant.RETRY_BREAK_FLAG - 1) {
				// 说明是最后一次重试，更改连接模式
				if(DEFAULT_FTP_PASSIVE_MODE) {
					// 表示原先为被动模式，改为主动模式
					DEFAULT_FTP_PASSIVE_MODE = false;
					ftpClient.enterLocalActiveMode();
				} else {
					ftpClient.enterLocalPassiveMode();
				}
			}
			
			String[] pathArr = parentPath.split("/");
			parentLevel = pathArr.length;
			for (String path : pathArr) {
				ftpClient.makeDirectory(path);
				ftpClient.changeWorkingDirectory(path);
			}
			uploadFlag = ftpClient.storeFile(fileName, is);
			if (uploadFlag) {
				filePath = parentPath + "/" + fileName;
			}
			
		} catch (Exception e) {
			if(retryCount == FtpConstant.RETRY_BREAK_FLAG - 1) {
				e.printStackTrace();
			}
		} finally {
			if(ftpClient != null) {
				try {
					ftpPool.returnObject(ftpClient, parentLevel);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}

			retryCount++;
			if ((uploadFlag || retryCount >= FtpConstant.RETRY_BREAK_FLAG) && is != null) {
				try {
					is.close();
					is = null;
				} catch (Exception e2) {
					e2.printStackTrace();
				}
			}
		}
		
		/**
		 * 采用递归的形式进行上传重试
		 */
		if (!uploadFlag && retryCount < FtpConstant.RETRY_BREAK_FLAG) {
			filePath = upload(is, suffix, fileType, retryCount);
		}
		/**
		 * 最后一次重试时，切换ftp连接的连接模式进行重试，重新初始化一次连接池
		 */
		if(uploadFlag && retryCount == FtpConstant.RETRY_BREAK_FLAG) {
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
	 */
	private static String getUploadFileName(String suffix) {
		UUID randomUUID = UUID.randomUUID();
		return DateCommonConvert.formatCurDateTimeMillis() + "_" + randomUUID.toString().replace("-", "") + "." + suffix;
	}
}