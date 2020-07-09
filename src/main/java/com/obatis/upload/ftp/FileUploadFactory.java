package com.obatis.upload.ftp;

import com.obatis.tools.ValidateTool;
import com.obatis.upload.ftp.config.FtpConstant;
import com.obatis.upload.ftp.pool.FtpPool;
import com.obatis.convert.date.DateCommonConvert;
import org.apache.commons.net.ftp.FTPClient;

import java.io.*;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * ftp模块文件上传组件服务
 * @author HuangLongPu
 */
public class FileUploadFactory implements Serializable {
	
	private static Map<String, UploadPathTempData> uploadPathTempDataMap = new HashMap<>();

	/**
	 * 矩阵文件夹命名规则，字母在前，数字在后
	 */
	private static final String pathY = "ABCDEFGHJK";
	private static final String pathX = "0123456789";
	private static final int pathCompareSize = 9;

	private FileUploadFactory() {
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
	 * @author HuangLongPu
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
	 * 根据ftp服务器的文件路径删除文件
	 * @author HuangLongPu
 	 * @param filePath    传入文件ftp所在路径
	 * @return  true表示删除成功，false表示删除失败
	 */
	public static boolean delete(String filePath) {
		return deleteFile(filePath);
	}

	/**
	 * 删除文件,提供文件名包括后缀
	 * @author HuangLongPu
	 * @param filePath  文件ftp所在路径
	 * @return
	 */
	public static boolean deleteFile(String filePath) {
		boolean success = false;
		FTPClient ftpClient = null;
		try {
			ftpClient = FtpPool.borrowObject();
			ftpClient.deleteFile(filePath);
			success = true;
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				FtpPool.returnObject(ftpClient, 0);
			} catch (Exception ioe) {
				ioe.printStackTrace();
			}
		}
		return success;
	}

	/**
	 * 传入File 进行文件上传
	 * @author HuangLongPu
	 * @param file file文件
	 * @return
	 * @throws FileNotFoundException
	 */
	public static String upload(File file) throws FileNotFoundException {
		return upload(file, null);
	}

	/**
	 * 传入File 进行文件上传，同时规定文件类型
	 * @author HuangLongPu
	 * @param file         file wenj
	 * @param typeName     文件类型名称
	 * @return
	 * @throws FileNotFoundException
	 */
	public static String upload(File file, String typeName) throws FileNotFoundException {
		return upload(new FileInputStream(file), file.getName(), typeName);
	}

	/**
	 * 传入字节数组进行文件上传，同时传入文件名或者文件后缀
	 * @author HuangLongPu
	 * @param data       字节数组文件
	 * @param fileName   文件后缀名称
	 * @return
	 * @throws FileNotFoundException
	 */
	public static String upload(byte[] data, String fileName) {
		ByteArrayInputStream inputStream = new ByteArrayInputStream(data);
		return upload(inputStream, fileName, null);
	}

	/**
	 * 传入字节数组进行文件上传，同时传入文件名或者文件后缀，同时规定文件类型
	 * @author HuangLongPu
	 * @param data
	 * @param fileName
	 * @param typeName
	 * @return
	 * @throws FileNotFoundException
	 */
	public static String upload(byte[] data, String fileName, String typeName) {
		ByteArrayInputStream inputStream = new ByteArrayInputStream(data);
		return upload(inputStream, fileName, typeName);
	}

	/**
	 * 文件上传服务，以文件流的形式上传
	 * @author HuangLongPu
	 * @param inputStream
	 * @param fileName
	 * @return
	 */
	public static String upload(InputStream inputStream, String fileName) {
		return upload(inputStream, fileName, null);
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
		return upload(inputStream, fileSuffix, typeName, FtpConstant.UPLOAD_RETRY_TIMES_DEFAULT);
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

		// 获取当前年月 "yyyyMM" 格式的字符串
		String curYearMonth = DateCommonConvert.formatYearMonth();
		String topPath = null;
		if (ValidateTool.isEmpty(typeName)) {
			topPath = FtpConstant.UPLOAD_TOP_PATH;
		} else {
			// 默认目录+自定义文件夹
			topPath = FtpConstant.UPLOAD_TOP_PATH + "/" + typeName;
		}

		/**
		 * 获取Map中的对象
		 */
		UploadPathTempData uploadPathTempData = uploadPathTempDataMap.get(topPath);
		if (uploadPathTempData == null) {
			uploadPathTempData = new UploadPathTempData();
		}

		if(!ValidateTool.isEmpty(uploadPathTempData.getUploadYearMonth())){
			if (!curYearMonth.equals(uploadPathTempData.getUploadYearMonth())) {
				uploadPathTempDataMap.clear();
				uploadPathTempData = new UploadPathTempData();
				uploadPathTempData.setUploadYearMonth(curYearMonth);
			}
		}

		int topLevel = 0;
		String filePath = null;
		FTPClient ftpClient = null;

		// 标记文件上传成功标识，布尔类型表示上传成功与否状态
		boolean uploadSuccess = false;
		try {
			ftpClient = FtpPool.borrowObject();
			if(ftpClient == null) {
				return null;
			}

			fileSuffix = fileSuffix.indexOf(".") == -1 ? fileSuffix : fileSuffix.substring(fileSuffix.lastIndexOf(".")).replace(".", "").toLowerCase();
			String uploadFileName = getUploadFileName(fileSuffix);
			// 获取文件上传矩阵目录
			getUploadPath(uploadPathTempData);
			// 添加到缓存，方便下次取值判断
			uploadPathTempDataMap.put(topPath, uploadPathTempData);
			topPath += "/" + curYearMonth;

			// 二级矩阵目录
			topPath += "/" + pathY.charAt(uploadPathTempData.getPathSecondY()) + pathX.charAt(uploadPathTempData.getPathSecondX());
			// 三级矩阵目录
			topPath += "/" + pathY.charAt(uploadPathTempData.getPathThirdY()) + pathX.charAt(uploadPathTempData.getPathThirdX());

			if(retryTimes == FtpConstant.RETRY_TIMES_FLAG - 1) {
				// 说明重试最后一次，为保证能上传成功，更改连接模式
				if(FtpPool.DEFAULT_FTP_PASSIVE_MODE) {
					// 表示原先为被动模式，改为主动模式
					FtpPool.DEFAULT_FTP_PASSIVE_MODE = false;
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
			if(retryTimes == FtpConstant.RETRY_TIMES_FLAG - 1) {
				e.printStackTrace();
			}
		} finally {
			if(ftpClient != null) {
				try {
					FtpPool.returnObject(ftpClient, topLevel);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}

			retryTimes++;
			if ((uploadSuccess || retryTimes >= FtpConstant.RETRY_TIMES_FLAG) && inputStream != null) {
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
		if (!uploadSuccess && retryTimes < FtpConstant.RETRY_TIMES_FLAG) {
			filePath = upload(inputStream, fileSuffix, typeName, retryTimes);
		}
		/**
		 * 最后一次重试时，切换ftp连接的连接模式进行重试，重新初始化一次连接池
		 */
		if(uploadSuccess && retryTimes == FtpConstant.RETRY_TIMES_FLAG) {
			FtpConstant.DEFAULT_CONFIG.setPassiveMode(FtpPool.DEFAULT_FTP_PASSIVE_MODE);
			try {
				FtpPool.initPool(FtpConstant.DEFAULT_CONFIG);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return filePath;
	}

	/**
	 * 获取文件名，防止ftp文件服务器文件名重复，需要传入文件后缀名进行拼接
	 * @author HuangLongPu
	 * @param fileSuffix
	 * @return
	 */
	private static String getUploadFileName(String fileSuffix) {
		return DateCommonConvert.formatDateTimeMillis() + "_" + UUID.randomUUID().toString().replace("-", "01") + "." + fileSuffix;
	}
}