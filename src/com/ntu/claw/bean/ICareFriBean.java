package com.ntu.claw.bean;

public class ICareFriBean {

	private String username;   //显示的数据
	private String sortLetters;  //显示数据拼音的首字母
	private String mobile;
	private String avater;
	
	public String getAvater() {
		return avater;
	}
	public void setAvater(String avater) {
		this.avater = avater;
	}
	public String getMobile() {
		return mobile;
	}
	public void setMobile(String mobile) {
		this.mobile = mobile;
	}
	public String getUsername() {
		return username;
	}
	public void setUsername(String username) {
		this.username = username;
	}
	public String getSortLetters() {
		return sortLetters;
	}
	public void setSortLetters(String sortLetters) {
		this.sortLetters = sortLetters;
	}
	@Override
	public String toString() {
		return "ICareFriBean [username=" + username + ", sortLetters="
				+ sortLetters + ", mobile=" + mobile + ", avater=" + avater
				+ "]";
	}

	
}
