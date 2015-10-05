package com.freelister.model;

import java.util.ArrayList;
import java.util.List;

import org.apache.http.cookie.Cookie;

import com.freelister.dao.FileDao.Creator;

public class Account implements java.io.Serializable, Creator<Account> {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private String username;
	private String passwd;
	private String passwd_flow = "-";
	private String type = "unknown";
	private float money;
	private boolean online_ipv4 = false;
	private boolean online_ipv6 = false;
	private int timeUsed = 0;
	private float flowUsed = 0;
	private String getontime = "xx-xx xx:xx";
	private String ip = "xxx.xxx.xxx.xxx";
	private String remark="-";
	private List<Cookie> cookies = new ArrayList<Cookie>();
	public static final String[] TYPES = { "undefined", "FinanceDEP",
			"NETCenter2", "NETCenter3", "Student", "Department", "ScienReach",
			"Relation", "NETCenter_Special", "Teacher", "Employee",
			"GuanCheng", "undefined", "undefined", "WorkStudy", "Postgraduate",
			"SchoolLeader", "Headmaster", "XueJiao?", "unGuan", "undefined",
			"DEPCeiling", "NotOpenYet" };

	public boolean equals(Object a) {
		if (this.username.equals(((Account) a).getUsername()))
			return true;
		else
			return false;
	}

	public boolean isUpdated(Account a) {
		return !(a.getMoney() == this.money
				&& a.getPasswd().equals(this.passwd)
				&& a.getPasswd_flow().equals(this.passwd_flow)
				&& a.getType().equals(this.type)
				&& a.isOnline_ipv4() == this.isOnline_ipv4()
				&& a.isOnline_ipv6() == this.isOnline_ipv6()
				&& a.getTimeUsed() == this.timeUsed
				&& a.getFlowUsed() == this.flowUsed
				&& a.getGetontime() == this.getontime && a.getIp() == this.ip
				&& a.getRemark().equals(this.remark)
				&& a.getCookies().equals(cookies));
	}

	public void update(Account a) {
		this.money = a.getMoney();
		this.online_ipv4 = a.isOnline_ipv4();
		this.online_ipv6 = a.isOnline_ipv6();
		this.passwd = a.getPasswd();
		this.passwd_flow = a.getPasswd_flow();
		this.type = a.getType();
		this.timeUsed = a.getTimeUsed();
		this.flowUsed = a.getFlowUsed();
		this.getontime = a.getGetontime();
		this.ip = a.getIp();
		this.remark = a.getRemark();
		this.cookies = a.getCookies();
	}

	public String toString() {
		return username + '\t' + passwd + '\t' + passwd_flow + '\t' + type + '\t' + money + '\t'
				+ timeUsed + '\t' + flowUsed + '\t' + remark;
	}

	public static String getType(String gno) {
		return TYPES[Integer.parseInt(gno)];
	}

	public Account(String username, String passwd) {
		super();
		this.username = username;
		this.passwd = passwd;
	}
	
	public Account(String username, String passwd, String passwd_flow) {
		super();
		this.username = username;
		this.passwd = passwd_flow;
	}
	
	public Account() {
		super();
	}

	public Account(String username, String passwd, String passwd_flow, String type, float money,
			int timeUsed, float flowUsed, String remark) {
		super();
		this.username = username;
		this.passwd = passwd;
		this.passwd_flow = passwd_flow;
		this.type = type;
		this.money = money;
		this.timeUsed = timeUsed;
		this.flowUsed = flowUsed;
		this.remark = remark;
	}

	public Account(Account a) {
		super();
		this.username = a.getUsername();
		this.passwd = a.getPasswd();
		this.passwd_flow = a.getPasswd_flow();
		this.type = a.getType();
		this.money = a.getMoney();
		this.online_ipv4 = a.isOnline_ipv4();
		this.online_ipv6 = a.isOnline_ipv6();
		this.flowUsed = a.getFlowUsed();
		this.timeUsed = a.getTimeUsed();
		this.getontime = a.getGetontime();
		this.ip = a.getIp();
		this.remark = a.getRemark();
		this.cookies = a.getCookies();
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getPasswd() {
		return passwd;
	}

	public void setPasswd(String passwd) {
		this.passwd = passwd;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public void setType(int gno) {
		this.type = Account.TYPES[gno];
	}

	@Override
	public Account creat(String s) {
		// TODO Auto-generated method stub
		String params[] = s.split("\t");
		if (params.length == 8) {
			Account a = new Account(params[0], params[1], params[2], params[3],
					Float.parseFloat(params[4]), Integer.parseInt(params[5]),
					Float.parseFloat(params[6]), params[7]);
			return a;
		}
		return null;
	}

	public float getMoney() {
		return money;
	}

	public void setMoney(float money) {
		this.money = money;
	}

	public boolean isOnline_ipv4() {
		return online_ipv4;
	}

	public void setOnline_ipv4(boolean online) {
		this.online_ipv4 = online;
	}

	public boolean isOnline_ipv6() {
		return online_ipv6;
	}

	public void setOnline_ipv6(boolean online) {
		this.online_ipv6 = online;
	}

	public void setTimeUsed(int timeUsed) {
		this.timeUsed = timeUsed;
	}

	public float getFlowUsed() {
		return flowUsed;
	}

	public int getTimeUsed() {
		return timeUsed;
	}

	public String getFlowUsedString() {
		return (int) flowUsed / 1024 + "G" + (int) flowUsed % 1024 + "M";
	}

	public String getTimeUsedString() {
		return timeUsed / 60 + "H" + timeUsed % 60 + "M";
	}

	public void setFlowUsed(float flowUsed) {
		this.flowUsed = flowUsed;
	}

	public String getGetontime() {
		return getontime;
	}

	public void setGetontime(String getontime) {
		this.getontime = getontime;
	}

	public String getIp() {
		return ip;
	}

	public void setIp(String ip) {
		this.ip = ip;
	}

	public String getRemark() {
		return remark;
	}

	public void setRemark(String remark) {
		this.remark = remark;
	}

	public String getPasswd_flow() {
		return passwd_flow;
	}

	public void setPasswd_flow(String passwd_flow) {
		this.passwd_flow = passwd_flow;
	}

	public List<Cookie> getCookies() {
		return cookies;
	}

	public void setCookies(List<Cookie> cookies) {
		this.cookies = cookies;
	}
}
