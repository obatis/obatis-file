package com.sbatis.upload.ftp;

public class PathRecordData {

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

//	private  int rowIndex3 = 0;//
	
	private  String fileDate = null;//
	


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

	//	public int getRowIndex3() {
//		return rowIndex3;
//	}
//	public void setRowIndex3(int rowIndex3) {
//		this.rowIndex3 = rowIndex3;
//	}
	public String getFileDate() {
		return fileDate;
	}
	public void setFileDate(String fileDate) {
		this.fileDate = fileDate;
	}




}
