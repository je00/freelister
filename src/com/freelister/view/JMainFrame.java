package com.freelister.view;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
//import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JEditorPane;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.SpinnerNumberModel;
import javax.swing.border.TitledBorder;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableColumn;
import javax.swing.table.TableRowSorter;

import com.freelister.model.Account;
import com.freelister.service.AccountServices;
import com.freelister.service.AccountServices.Account_withFlags;
import com.freelister.service.StatusServices;

public class JMainFrame extends JFrame implements Runnable, ActionListener {
	private static final long serialVersionUID = 7975445398629255082L;
	private final static int WIDTH = 1000;
	private final static int HEIGHT = 500;
	private JSpinner[] spin_year = new JSpinner[2];
	private JSpinner[] spin_digit = new JSpinner[3];
	private JTextArea text_passwd;
	private JTable jtable;
	private JLabel jlSystemStatus;
	private JLabel jlLoginStatus;
	private JTextArea jtaMessage;
	private JButton jbStopScan = new JButton("stopScan");
	private JButton jbScan = new JButton("scan");;
	private JButton jbSave = new JButton("save");
	private JButton jbLoad = new JButton("load");
	private JButton jbUpdate = new JButton("update");
	private JButton jbStopUpdate = new JButton("stopUpdate");
	private JButton jbLogout = new JButton("logout");

	private JPopupMenu popupmenu_table = new JPopupMenu();
	private JPopupMenu popupmenu_loginStatus = new JPopupMenu();

	private JMainFrame self = this;
	private AccountServices accountServices = new AccountServices(
			Main.SLEEPTIME_SERVICE_ACCOUNT);
	private StatusServices statusServices = new StatusServices(
			Main.SLEEPTIME_SERVICE_STATUS);
	private TableRowSorter<DefaultTableModel> sorter;

	public JMainFrame() throws ClassNotFoundException, IOException {
		this.setTitle("freelister");
		this.setBackground(java.awt.Color.lightGray);
		this.setSize(WIDTH, HEIGHT);
		// this.setCenter();
		this.setDefaultCloseOperation(EXIT_ON_CLOSE);
		this.setVisible(true);
		jlLoginStatus = new JLabel("login status...");
		jlSystemStatus = new JLabel("operation status...");
		jtaMessage = new JTextArea();
		jbScan.addActionListener(this);
		jbStopScan.addActionListener(this);
		jbSave.addActionListener(this);
		jbLoad.addActionListener(this);
		jbUpdate.addActionListener(this);
		jbStopUpdate.addActionListener(this);
		jbLogout.addActionListener(this);

		String menuitems_cut[] = { "update", "login", "remark", "chpwd",
				"records" };
		JMenuItem popmenuitem[] = new JMenuItem[menuitems_cut.length];
		for (int i = 0; i < popmenuitem.length; i++) {
			popmenuitem[i] = new JMenuItem(menuitems_cut[i]);
			popmenuitem[i].addActionListener(new ActionListener_PopupMenu());
			this.popupmenu_table.add(popmenuitem[i]);
		}
		String menuitems_cut2[] = { "logout", "newlogin" };
		JMenuItem popmenuitem2[] = new JMenuItem[menuitems_cut2.length];
		for (int i = 0; i < popmenuitem2.length; i++) {
			popmenuitem2[i] = new JMenuItem(menuitems_cut2[i]);
			popmenuitem2[i].addActionListener(new ActionListener_PopupMenu());
			this.popupmenu_loginStatus.add(popmenuitem2[i]);
		}
		this.add(getJPMain());
		this.accountServices.setPriority(Main.PRIORITY_SERVICE_ACCOUNT);
		this.statusServices.setPriority(Main.PRIORITY_SERVICE_STATUS);
		this.accountServices.start();
		this.statusServices.start();
		this.accountServices.load();

	}

	private void getJEPHtml(Account a) {

		JFrame frame = new JFrame();
		frame.setTitle("records - " + a.getUsername());
		frame.setLocation(this.getLocation().x + 200,
				this.getLocation().y + 100);
		frame.setDefaultCloseOperation(DISPOSE_ON_CLOSE);
		JPanel jpanel = new JPanel(new BorderLayout());
		JEditorPane jeditorpane = new JEditorPane();
		jeditorpane.setContentType("text/html");
		JScrollPane jscpane = new JScrollPane(jeditorpane);
		JPanel jpNorth = new JPanel();
		JButton jbutton = new JButton("show");
		String types[] = { "上网日志", "收费日志", "流量包日志" };
		String years[] = { "2015", "2014" };
		String months[] = { "01", "02", "03", "04", "05", "06", "07", "08",
				"09", "10", "11", "12" };
		JComboBox<String> combox_type, combox_year, combox_month;
		combox_type = new JComboBox<String>(types);
		combox_year = new JComboBox<String>(years);
		combox_month = new JComboBox<String>(months);
		jpNorth.setLayout(new FlowLayout(FlowLayout.LEFT));
		jpNorth.add(new JLabel("type:"));
		jpNorth.add(combox_type);
		jpNorth.add(new JLabel(" month:"));
		jpNorth.add(combox_year);
		jpNorth.add(combox_month);
		jpNorth.add(new JLabel("  "));
		jpNorth.add(jbutton);
		jpanel.add(jscpane, BorderLayout.CENTER);
		jpanel.add(jpNorth, BorderLayout.NORTH);
		frame.add(jpanel);
		frame.setSize(WIDTH, HEIGHT);
		frame.setVisible(true);
		jbutton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				try {
					jeditorpane.setText("");
					String result = accountServices.getRecord(a,
							(String) combox_type.getSelectedItem(),
							(String) combox_year.getSelectedItem() + "-"
									+ (String) combox_month.getSelectedItem());
					jeditorpane.setText(result);
				} catch (Exception e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
			}

		});

	}

	public void FitTableColumns(JTable myTable) {
		JTableHeader header = myTable.getTableHeader();
		int rowCount = myTable.getRowCount();

		@SuppressWarnings("rawtypes")
		Enumeration columns = myTable.getColumnModel().getColumns();
		while (columns.hasMoreElements()) {
			TableColumn column = (TableColumn) columns.nextElement();
			int col = header.getColumnModel().getColumnIndex(
					column.getIdentifier());
			int width = (int) myTable
					.getTableHeader()
					.getDefaultRenderer()
					.getTableCellRendererComponent(myTable,
							column.getIdentifier(), false, false, -1, col)
					.getPreferredSize().getWidth();
			for (int row = 0; row < rowCount; row++) {
				int preferedWidth = (int) myTable
						.getCellRenderer(row, col)
						.getTableCellRendererComponent(myTable,
								myTable.getValueAt(row, col), false, false,
								row, col).getPreferredSize().getWidth();
				width = Math.max(width, preferedWidth);
			}
			header.setResizingColumn(column); // 此行很重要
			column.setWidth(width + myTable.getIntercellSpacing().width);
		}
	}

	public void showJlSystemStatus(String s) {
		jlSystemStatus.setText(s);
	}

	public void showJTAMessage(String s) {
		if (jtaMessage.getRows() > 100)
			jtaMessage.removeAll();
		jtaMessage.insert(s + "\n", 0);
	}

	public void showJlLoginStatus(String s) {
		jlLoginStatus.setText(s);
	}

	public void pushJTResult(Account_withFlags a) {
		if (a == null)
			return;
		if (a.isUpdate()) {
			int row = a.getIndex();
			this.jtable.getModel().setValueAt(a.getPasswd(), row, 1);
			this.jtable.getModel().setValueAt(a.getPasswd_flow(), row, 2);
			this.jtable.getModel().setValueAt(a.getType(), row, 3);
			this.jtable.getModel().setValueAt(a.getMoney(), row, 4);
			this.jtable.getModel().setValueAt(a.getTimeUsedString(), row, 5);
			this.jtable.getModel().setValueAt(a.getFlowUsedString(), row, 6);
			this.jtable.getModel().setValueAt(a.isOnline_ipv4(), row, 7);
			this.jtable.getModel().setValueAt(a.getGetontime(), row, 8);
			this.jtable.getModel().setValueAt(a.getIp(), row, 9);
			// this.jtable.getModel().setValueAt(a.isOnline_ipv6(), row, 10);
			this.jtable.getModel().setValueAt(a.getRemark(), row, 10);

		} else if (a.isDelete()) {
			int row = a.getIndex();
			((DefaultTableModel) this.jtable.getModel()).removeRow(row);
		} else {
			Object[] data = { a.getUsername(), a.getPasswd(),
					a.getPasswd_flow(), a.getType(), a.getMoney(),
					a.getTimeUsedString(), a.getFlowUsedString(),
					a.isOnline_ipv4(), a.getGetontime(), a.getIp(),
					a.getRemark() };
			((DefaultTableModel) this.jtable.getModel()).addRow(data);
		}
	}

//	private void setCenter() {
//		Toolkit kit = Toolkit.getDefaultToolkit();
//		Dimension screenSize = kit.getScreenSize();
//		this.setLocation((screenSize.width - WIDTH) / 2,
//				(screenSize.height - HEIGHT) / 2);
//	}

	private JPanel getJPMain() {
		JPanel jpMain = new JPanel();
		jpMain.setBorder(BorderFactory
				.createMatteBorder(5, 5, 5, 5, Color.gray));

		jpMain.setLayout(new BorderLayout());
		jpMain.add(getJPTop(), BorderLayout.NORTH);
		jpMain.add(getJPResult(), BorderLayout.CENTER);
		jpMain.add(getJPBottom(), BorderLayout.SOUTH);
		return jpMain;
	}

	private JPanel getJPTop() {
		JPanel jpTop = new JPanel();
		jpTop.add(getJPScan());
		return jpTop;
	}

	private JPanel getJPScan() {
		int[] scanRange_year = this.accountServices.getScanRange_year();
		int[] scanRange_digit = this.accountServices.getScanRange_digit();
		String[] scanRange_passwd = this.accountServices.getScanRange_passwd();
		this.spin_year[0] = new JSpinner(new SpinnerNumberModel(
				scanRange_year[0], scanRange_year[0], scanRange_year[1], 1));
		this.spin_year[1] = new JSpinner(new SpinnerNumberModel(
				scanRange_year[1], scanRange_year[0], scanRange_year[1], 1));

		this.spin_digit[0] = new JSpinner(new SpinnerNumberModel(
				scanRange_digit[0], 0, 9, 1));
		this.spin_digit[1] = new JSpinner(new SpinnerNumberModel(
				scanRange_digit[1], 0, 9, 1));
		this.spin_digit[2] = new JSpinner(new SpinnerNumberModel(
				scanRange_digit[2], 0, 9, 1));

		JPanel jpanel = new JPanel(new BorderLayout());

		this.text_passwd = new JTextArea(null, 2, 20);
		this.text_passwd.setLineWrap(true);
		for (int i = 0; i < scanRange_passwd.length; i++) {
			this.text_passwd.append(scanRange_passwd[i] + '\n');
		}
		JScrollPane jpPasswd = new JScrollPane(this.text_passwd);
		jpPasswd.setBorder(new TitledBorder("passwds"));
		jpanel.add(jpPasswd);
		JPanel jpSouth = new JPanel();
		jpSouth.add(jbScan);
		jpSouth.add(jbStopScan);
		JPanel jpButton = new JPanel(new GridLayout(4, 1));
		jpButton.setBorder(BorderFactory.createMatteBorder(0, 0, 2, 0,
				Color.blue));
		jpButton.add(new JLabel());
		jpButton.add(new JLabel());
		jpButton.add(jbScan, BorderLayout.SOUTH);

		jpButton.add(jbStopScan, BorderLayout.NORTH);

		JPanel jpYear = new JPanel();
		jpYear.setBorder(new TitledBorder("year"));
		jpYear.add(new JLabel("from"));
		jpYear.add(this.spin_year[0]);
		jpYear.add(new JLabel("to"));
		jpYear.add(this.spin_year[1]);
		jpYear.add(new JLabel("  "));
		JPanel jpDigit = new JPanel();
		jpDigit.setBorder(new TitledBorder("digit"));
		jpDigit.add(new JLabel("from"));
		jpDigit.add(this.spin_digit[0]);
		jpDigit.add(new JLabel("to"));
		jpDigit.add(this.spin_digit[1]);
		jpDigit.add(new JLabel("      tail"));
		jpDigit.add(this.spin_digit[2]);
		jpDigit.add(new JLabel("  "));
		JPanel jpRange = new JPanel(new GridLayout(2, 1));
		jpRange.add(jpYear);
		jpRange.add(jpDigit);
		JPanel jpRight = new JPanel(new BorderLayout());
		jpRight.add(jpRange, BorderLayout.WEST);
		jpRight.add(jpButton, BorderLayout.EAST);

		jpanel.add(jpPasswd, BorderLayout.WEST);
		jpanel.add(jpRight, BorderLayout.EAST);
		return jpanel;
	}

	private JPanel getJPBottom() {
		JPanel jpBottom = new JPanel(new BorderLayout());
		JPanel jpStatus = new JPanel(new BorderLayout());
		jpStatus.add(getJPSystemStatus(), BorderLayout.WEST);
		jpStatus.add(getJPLoginStatus(), BorderLayout.EAST);
		jpBottom.add(jpStatus, BorderLayout.SOUTH);
		jpBottom.add(getJSPMessage(), BorderLayout.NORTH);
		return jpBottom;
	}

	private JScrollPane getJSPMessage() {
		JScrollPane jpMessage = new JScrollPane(jtaMessage);
		jpMessage.setPreferredSize(new Dimension(10, 50));
		return jpMessage;
	}

	private JPanel getJPSystemStatus() {
		JPanel jpSystemStatus = new JPanel(new BorderLayout());
		jpSystemStatus.setLayout(new FlowLayout(FlowLayout.LEFT));
		jpSystemStatus.add(jlSystemStatus);
		return jpSystemStatus;
	}

	private JPanel getJPLoginStatus() {
		JPanel jpLoginStatus = new JPanel();
		jpLoginStatus.add(jlLoginStatus);
		jpLoginStatus.add(this.popupmenu_loginStatus);
		jpLoginStatus.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				if (e.getButton() == MouseEvent.BUTTON3) {
					popupmenu_loginStatus.show(jpLoginStatus, e.getX(),
							e.getY());
				}
			}
		});
		return jpLoginStatus;
	}

	private JPanel getJPResult() {
		JPanel jpanel = new JPanel(new BorderLayout());
		// jpanel.setBorder(new TitledBorder("results"));
		String titles[] = { "     username     ", "      passwd      ",
				"  passwd_flow  ", "     type     ", "money ", " timeUsed ",
				" flowUsed  ", "isOnline", "  getontime  ",
				"            ip            ", "     remark     " };
		DefaultTableModel tablemodel = new DefaultTableModel(titles, 0);

		this.jtable = new JTable(tablemodel) {
			private static final long serialVersionUID = -3815785656685560409L;

			public boolean isCellEditable(int row, int column) {
				return false;
			}
		};
		FitTableColumns(this.jtable);
		sorter = new TableRowSorter<DefaultTableModel>(tablemodel);
		Comparator<String> usernameComparator = new Comparator<String>() {
			@Override
			public int compare(String o1, String o2) {
				// TODO Auto-generated method stub
				return (int) (Long.parseLong(o1) - Long.parseLong(o2));
			}
		};
		Comparator<Float> moneyComparator = new Comparator<Float>() {
			@Override
			public int compare(Float o1, Float o2) {
				// TODO Auto-generated method stub
				return (int) (o1 - o2);
			}
		};
		sorter.setComparator(0, usernameComparator);
		sorter.setComparator(4, moneyComparator);
		this.jtable.setRowSorter(sorter);
		this.jtable.add(this.popupmenu_table);
		this.jtable.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				if (e.getButton() == MouseEvent.BUTTON3) {
					popupmenu_table.show(jtable, e.getX(), e.getY());
					// int row = jtable.rowAtPoint(e.getPoint());
					// if(row>=0)
					// jtable.setRowSelectionInterval(row,row);
				}
			}
		});
		JScrollPane jspTable = new JScrollPane(jtable);
		JPanel jpSouth = new JPanel();
		jpSouth.add(jbSave);
		jpSouth.add(jbLoad);
		jpSouth.add(jbUpdate);
		jpSouth.add(jbStopUpdate);
		jpanel.add(jspTable, BorderLayout.CENTER);
		jpanel.add(jpSouth, "South");
		return jpanel;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
	 */
	@Override
	public void actionPerformed(ActionEvent e) {
		// TODO Auto-generated method stub
		switch (e.getActionCommand()) {
		case "scan":
			int[] scanRange_year = new int[2];
			int[] scanRange_digit = new int[3];
			String[] passwd;
			for (int i = 0; i < 2; i++)
				scanRange_year[i] = (int) spin_year[i].getValue();
			for (int i = 0; i < 3; i++)
				scanRange_digit[i] = (int) spin_digit[i].getValue();
			passwd = text_passwd.getText().split("\n");
			try {
				accountServices.setAll(scanRange_year, scanRange_digit, passwd);
				accountServices.scan();
			} catch (Exception e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			break;
		case "stopScan":
			accountServices.stopScan();
			break;
		case "update":
			self.accountServices.updateAll();
			break;
		case "stopUpdate":
			self.accountServices.stopUpdate();
			break;
		case "save":
			self.accountServices.save();
			break;
		case "load":
			try {
				self.accountServices.load();
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
		case "logout":
			self.accountServices.logout();
			break;
		}
	}

	private class ActionListener_PopupMenu implements ActionListener {
		@Override
		public void actionPerformed(ActionEvent e) {

			if (e.getActionCommand().equals("update")) {
				new Thread() {
					public void run() {
						int[] rows = jtable.getSelectedRows();
						List<Account> alist = new ArrayList<Account>();
						for (int i = 0; i < rows.length; i++) {
							String userName = (String) jtable.getValueAt(
									rows[i], 0);
							String passwd = (String) jtable.getValueAt(rows[i],
									1);
							alist.add(new Account(userName, passwd));
						}
						accountServices.updateAccounts(alist);
					}
				}.start();
			} else if (e.getActionCommand().equals("login")) {
				new Thread() {
					public void run() {
						try {
							String userName = (String) jtable.getValueAt(
									jtable.getSelectedRow(), 0);
							String passwd = (String) jtable.getValueAt(
									jtable.getSelectedRow(), 1);
							String passwd_flow = (String) jtable.getValueAt(
									jtable.getSelectedRow(), 2);
							self.accountServices.login(new Account(userName,
									passwd, passwd_flow));
						} catch (Exception e1) {
							// TODO Auto-generated catch block
							e1.printStackTrace();
						}
					}
				}.start();
			} else if (e.getActionCommand().equals("remark")) {
				new Thread() {
					public void run() {
						String userName = (String) jtable.getValueAt(
								jtable.getSelectedRow(), 0);
						String passwd = (String) jtable.getValueAt(
								jtable.getSelectedRow(), 1);
						Account a = new Account(userName, passwd);
						String remark = JOptionPane.showInputDialog(self,
								"Please input remark",
								jtable.getValueAt(jtable.getSelectedRow(), 10));
						if (remark != null)
							self.accountServices.setRemark(a, remark);
					}
				}.start();
			} else if (e.getActionCommand().equals("chpwd")) {
				new Thread() {
					public void run() {
						Object[] possibleValues = { "Synchronized",
								"FlowAccountOnly", "Cancel" }; // 用户的选择项目
						int response = JOptionPane.showOptionDialog(self,
								"Choose one", "Input", JOptionPane.YES_OPTION,
								JOptionPane.QUESTION_MESSAGE, null,
								possibleValues, possibleValues[0]);
						boolean flowOnly = false;
						switch (response) {
						case 0:
							flowOnly = false;
							break;
						case 1:
							flowOnly = true;
							break;
						case 2:
							return;
						}
						String str = (String) JOptionPane.showInputDialog(self,
								"Please input newpasswd,newpasswd",
								"Change Password",
								JOptionPane.INFORMATION_MESSAGE, null, null,
								null);
						if (str == null)
							return;
						String[] newpasswd = str.split(",");
						if (newpasswd.length == 2) {
							try {
								String userName = (String) jtable.getValueAt(
										jtable.getSelectedRow(), 0);
								String passwd = (String) jtable.getValueAt(
										jtable.getSelectedRow(), 1);
								Account a = new Account(userName, passwd);
								accountServices.changePasswd(a, newpasswd[0],
										newpasswd[1], flowOnly);
							} catch (Exception e1) {
								// TODO Auto-generated catch block
								e1.printStackTrace();
							}
						}
					}
				}.start();
			} else if (e.getActionCommand().equals("records")) {
				new Thread() {
					public void run() {
						int row = jtable.getSelectedRow();
						String userName = (String) jtable.getValueAt(row, 0);
						String passwd = (String) jtable.getValueAt(row, 1);
						try {
							getJEPHtml(new Account(userName, passwd));
						} catch (Exception e1) {
							// TODO Auto-generated catch block
							e1.printStackTrace();
						}
					}
				}.start();
			} else if (e.getActionCommand().equals("logout")) {
				new Thread() {
					public void run() {
						self.accountServices.logout();
					}
				}.start();
			} else if (e.getActionCommand().equals("newlogin")) {
				new Thread() {
					public void run() {
						String str = (String) JOptionPane.showInputDialog(self,
								"Please input username,passwd", "New Login",
								JOptionPane.INFORMATION_MESSAGE, null, null,
								null);
						if (str != null) {
							String[] params = str.split(",");
							if (params.length == 2) {
								Account a = new Account(params[0], "-");
								a.setPasswd_flow(params[1]);
								try {
									accountServices.login(a);
								} catch (Exception e1) {
									// TODO Auto-generated catch block
									e1.printStackTrace();
								}
							}
						}
					}
				}.start();
			}
		}
	}

	public void run() {
		while (true) {
			Account_withFlags tmp_account;
			tmp_account = self.accountServices.pollAccount();
			self.showJlSystemStatus(StatusServices.getSystemStatus());
			String message = StatusServices.pollMessage();
			if (message != null)
				self.showJTAMessage(message);
			if (tmp_account != null)
				self.pushJTResult(tmp_account);
			self.showJlLoginStatus(StatusServices.getLoginStatus());

			if (StatusServices.hasOp(StatusServices.OP_SCAN)) {
				jbScan.setEnabled(false);
				jbStopScan.setEnabled(true);
			} else if (StatusServices.hasOp(StatusServices.OP_STOPSCAN)) {
				jbScan.setEnabled(false);
				jbStopScan.setEnabled(false);
			} else {
				jbScan.setEnabled(true);
				jbStopScan.setEnabled(false);
			}
			if (StatusServices.hasOp(StatusServices.OP_SAVE)
					|| StatusServices.hasOp(StatusServices.OP_LOAD)) {
				jbSave.setEnabled(false);
				jbLoad.setEnabled(false);
			} else {
				jbSave.setEnabled(true);
				jbLoad.setEnabled(true);
			}
			if (StatusServices.hasOp(StatusServices.OP_UPDATE)) {
				jbUpdate.setEnabled(false);
				jbStopUpdate.setEnabled(true);
			} else if (StatusServices.hasOp(StatusServices.OP_STOPUPDATE)) {
				jbUpdate.setEnabled(false);
				jbStopUpdate.setEnabled(false);
			} else {
				jbUpdate.setEnabled(true);
				jbStopUpdate.setEnabled(false);
			}
			try {
				Thread.sleep(Main.SLEEPTIME_VIEW_FRAME);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
}
