package com.obatis.file.upload.ftp;

/**
 * 矩阵目录信息存储缓存类
 * @author HuangLongPu
 */
public class UploadPathTempData {

	/**
	 * 二级目录Y坐标
	 */
	private int pathSecondY = 0;
	/**
	 * 二级目录X坐标
	 */
	private int pathSecondX = 0;
	/**
	 * 三级目录Y坐标
	 */
	private int pathThirdY = 0;
	/**
	 * 三级目录X坐标
	 */
	private int pathThirdX = 0;
	/**
	 * 上传年月
	 */
	private String uploadYearMonth;
	
	public int getPathSecondX() {
		return pathSecondX;
	}

	public void setPathSecondX(int pathSecondX) {
		this.pathSecondX = pathSecondX;
	}

	public int getPathSecondY() {
		return pathSecondY;
	}

	public void setPathSecondY(int pathSecondY) {
		this.pathSecondY = pathSecondY;
	}

	public int getPathThirdY() {
		return pathThirdY;
	}

	public void setPathThirdY(int pathThirdY) {
		this.pathThirdY = pathThirdY;
	}

	public int getPathThirdX() {
		return pathThirdX;
	}

	public void setPathThirdX(int pathThirdX) {
		this.pathThirdX = pathThirdX;
	}

	public String getUploadYearMonth() {
		return uploadYearMonth;
	}

	public void setUploadYearMonth(String uploadYearMonth) {
		this.uploadYearMonth = uploadYearMonth;
	}
}
