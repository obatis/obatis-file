package com.obatis.file.upload.ftp.pool;

import java.io.IOException;

import com.obatis.file.upload.ftp.config.FtpConfig;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPReply;
import org.apache.commons.pool2.BasePooledObjectFactory;
import org.apache.commons.pool2.PooledObject;
import org.apache.commons.pool2.impl.DefaultPooledObject;

/**
 * FTP连接池工厂类，负责维护连接池
 * @author HuangLongPu
 */
public class FtpFactory extends BasePooledObjectFactory<FTPClient> {

	private FtpConfig ftpConfig;

	public FtpConfig getFtpConfig() {
		return ftpConfig;
	}

	public void setFtpConfig(FtpConfig ftpConfig) {
		this.ftpConfig = ftpConfig;
	}

	/**
	 * 创建ftp 连接
	 * @author HuangLongPu
	 * @return
	 * @throws Exception
	 */
	@Override
	public FTPClient create() throws Exception {
		FTPClient ftp = new FTPClient();
		try {
			ftp.connect(ftpConfig.getFtpHost(), ftpConfig.getFtpPort());
			// 设置超时时间
			ftp.setDefaultTimeout(ftpConfig.getConnectTimeOut());
			ftp.setSoTimeout(ftpConfig.getConnectTimeOut());
			ftp.setConnectTimeout(ftpConfig.getConnectTimeOut());
			ftp.setUseEPSVwithIPv4(ftpConfig.isUseEPSVwithIPv4());
			
			int reply = ftp.getReplyCode();
			if (!FTPReply.isPositiveCompletion(reply)) {
				ftp.logout();
				ftp.disconnect();
				return null;
			}
			boolean result = ftp.login(ftpConfig.getFtpUsername(), ftpConfig.getFtpPassword());
			if (!result) {
				throw new Exception("登录失败! Host：" + ftpConfig.getFtpHost() + " Port：" + ftpConfig.getFtpPort() + " username:"
						+ ftpConfig.getFtpUsername() + " Password:" + ftpConfig.getFtpPassword());
			}

			ftp.setControlEncoding(ftpConfig.getControlEncoding());
			ftp.setBufferSize(ftpConfig.getBufferSize());
			ftp.setFileType(ftpConfig.getFileType());
			ftp.setDataTimeout(ftpConfig.getDataTimeout());
			// 设置FTP 服务主/被动模式
			if (ftpConfig.isPassiveMode()) {
				// 设置被动模式
				ftp.enterLocalPassiveMode();// 进入被动模式
			} else {
				// 设置主动模式
				ftp.enterLocalActiveMode();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return ftp;
	}

	@Override
	public PooledObject<FTPClient> wrap(FTPClient client) {
		return new DefaultPooledObject<FTPClient>(client);
	}

	@Override
	public void destroyObject(PooledObject<FTPClient> p) throws Exception {
		FTPClient ftpClient = p.getObject();
		ftpClient.logout();
		super.destroyObject(p);
	}

	@Override
	public boolean validateObject(PooledObject<FTPClient> p) {
		FTPClient ftpClient = p.getObject();
		if (ftpClient == null) {
			return false;
		}
		try {
			boolean connect = ftpClient.sendNoOp();
			if (!connect) {
				return false;
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return super.validateObject(p);
	}

}
