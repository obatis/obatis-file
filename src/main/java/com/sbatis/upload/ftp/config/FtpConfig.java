package com.sbatis.upload.ftp.config;

import com.sbatis.constant.DefaultNormalConstant;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.pool2.impl.GenericObjectPoolConfig;

/**
 * ftp配置参数对象 继承自GenericObjectPoolConfig，基于commons-pool2
 * @author HuangLongPu
 */
public class FtpConfig extends GenericObjectPoolConfig<FTPClient> {

	private String ftpHost;// ftp主机名(IP地址)
	private int ftpPort = 21; // 默认设置21端口
	private String ftpUsername;// ftp用户名
	private String ftpPassword;// ftp密码

	private int connectTimeOut = FtpConstant.TIME_OUT;// 默认ftp连接超时时间 毫秒
	private String controlEncoding = DefaultNormalConstant.CHARSET_UTF8;
	private int retryTimes; // 重试次数
	private int bufferSize = 1024 * 1024;// 缓冲区大小
	private int fileType = 2;// 传输数据格式 2表binary二进制数据
	private int dataTimeout = 30000;
	private boolean useEPSVwithIPv4 = false;  // 只允许用IPv4的方式连接
	private boolean passiveMode = true;// 是否启用被动模式

	public String getFtpHost() {
		return ftpHost;
	}

	public void setFtpHost(String ftpHost) {
		this.ftpHost = ftpHost;
	}

	public int getFtpPort() {
		return ftpPort;
	}

	public void setFtpPort(int ftpPort) {
		this.ftpPort = ftpPort;
	}

	public String getFtpUsername() {
		return ftpUsername;
	}

	public void setFtpUsername(String ftpUsername) {
		this.ftpUsername = ftpUsername;
	}

	public String getFtpPassword() {
		return ftpPassword;
	}

	public void setFtpPassword(String ftpPassword) {
		this.ftpPassword = ftpPassword;
	}

	public int getConnectTimeOut() {
		return connectTimeOut;
	}
	public void setConnectTimeOut(int connectTimeOut) {
		this.connectTimeOut = connectTimeOut;
	}
	public String getControlEncoding() {
		return controlEncoding;
	}
	public void setControlEncoding(String controlEncoding) {
		this.controlEncoding = controlEncoding;
	}
	public int getRetryTimes() {
		return retryTimes;
	}
	public void setRetryTimes(int retryTimes) {
		this.retryTimes = retryTimes;
	}
	public int getBufferSize() {
		return bufferSize;
	}
	public void setBufferSize(int bufferSize) {
		this.bufferSize = bufferSize;
	}
	public int getFileType() {
		return fileType;
	}
	public void setFileType(int fileType) {
		this.fileType = fileType;
	}
	public int getDataTimeout() {
		return dataTimeout;
	}
	public void setDataTimeout(int dataTimeout) {
		this.dataTimeout = dataTimeout;
	}
	public boolean isUseEPSVwithIPv4() {
		return useEPSVwithIPv4;
	}
	public void setUseEPSVwithIPv4(boolean useEPSVwithIPv4) {
		this.useEPSVwithIPv4 = useEPSVwithIPv4;
	}
	public boolean isPassiveMode() {
		return passiveMode;
	}
	public void setPassiveMode(boolean passiveMode) {
		this.passiveMode = passiveMode;
	}

}
