/**   
 * @title	StatusServices.java 
 * @package	com.freelister.service 
 * @description	TODO(Describe the document in a sentence.) 
 * @author	ting 
 * @date	Jul 20, 2015 4:22:36 AM 
 * @version	V1.0   
 */
package com.freelister.service;

import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

import org.apache.http.cookie.Cookie;

/**
 * Project: freelister Comments: JDK version used: JDK1.8 Namespace:
 * com.freelister.service Author: ting Create Date: Jul 20, 2015 Version: V1.0
 */
public class StatusServices extends Thread {
	private static String action = "NONE";
	private static int activeCount;
	private static int accountCount;
	private static Queue<String> queue_message = new LinkedList<String>();
	private int sleeptime;
	private static int op = 0;
	private static String username_now;
	private static int time_used;
	private static int flow_used;
	public static final int OP_NONE = 0;
	public static final int OP_SCAN = 1;
	public static final int OP_STOPSCAN = 2;
	public static final int OP_SAVE = 4;
	public static final int OP_LOAD = 8;
	public static final int OP_UPDATE = 16;
	public static final int OP_STOPUPDATE = 32;
	

	public StatusServices(int sleeptime) {
		super();
		this.sleeptime = sleeptime;

	}

	public void run() {
		while (true) {
			StatusServices.setActiveCount(Thread.activeCount());
			if (System.currentTimeMillis() % 200 == 0)
			StatusServices.updateLoginStatus();
			try {
				Thread.sleep(sleeptime);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	public static boolean hasOp(int op) {
		if ((StatusServices.op & op) > 0)
			return true;
		return false;
	}

	public static String getSystemStatus() {
		String status = null;
		if (StatusServices.op == 0)
			status = "none";
		else {
			if (hasOp(StatusServices.OP_SCAN))
				status = new String("scan");
			else if (hasOp(StatusServices.OP_STOPSCAN))
				status = new String("stopScan");

			if (hasOp(StatusServices.OP_SAVE)) {
				if (status == null)
					status = new String("save");
				else
					status += "|save";
			} else if (hasOp(StatusServices.OP_LOAD)) {
				if (status == null)
					status = new String("load");
				else
					status += "|load";
			}
			
			if (hasOp(StatusServices.OP_UPDATE)) {
				if (status == null)
					status = new String("update");
				else
					status += "|update";
			} else if (hasOp(StatusServices.OP_STOPUPDATE)) {
				if (status == null)
					status = new String("stopUpdate");
				else
					status += "|stopUpdate";
			}
		}
		status = "accounts:" + accountCount + " threads:" + activeCount + " action:" + status;
		return status;
	}
	
	public static String pollMessage() {
		return queue_message.poll();
	}

	public static void updateLoginStatus() {
		String result = HttpRequest.doGet("http://192.168.252.8/", (List<Cookie>)null);
		int tmp_index = result.indexOf("uid='");
		if (tmp_index == -1) {
			username_now = "null";
			time_used = 0;
			flow_used = 0;
			return;
		}
		username_now = result.substring(tmp_index + 5, result.indexOf("';pwd"));
		time_used = Integer.parseInt(result.substring(
				result.indexOf("time='") + 6, result.indexOf("';flow=") - 1)
				.trim());
		flow_used = Integer.parseInt(result.substring(
				result.indexOf("flow='") + 6, result.indexOf("';fsele") - 1)
				.trim());
	}

	public static String getLoginStatus() {
		String time;
		String flow;
		time = "time:" + StatusServices.time_used/60 + "H" + StatusServices.time_used%60 + "M";
		flow = "flow:" + StatusServices.flow_used/1048576 + "G" + StatusServices.flow_used/1024%1024 + "M";
		return "user:" + StatusServices.username_now + " " + time + " " + flow;
	}

	public static void setAction(String action) {
		StatusServices.action = action;
	}

	public static String getAction() {
		return StatusServices.action;
	}

	public static int getActiveCount() {
		return activeCount;
	}

	public static void setActiveCount(int activeCount) {
		StatusServices.activeCount = activeCount;
	}

	public static void offerMessage(String s) {
		synchronized(queue_message) {
			StatusServices.queue_message.offer(s);
		}
	}

	public static int getOp() {
		return op;
	}

	public static void setOp(int op) {
		StatusServices.op = op;
	}

	public static void addOp(int op) {
		StatusServices.op |= op;
	}

	public static int getAccountCount() {
		return accountCount;
	}

	public static void setAccountCount(int accountCount) {
		StatusServices.accountCount = accountCount;
	}
}
