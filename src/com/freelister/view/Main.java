package com.freelister.view;

public class Main {
	public static final int SLEEPTIME_VIEW_FRAME = 20;
	public static final int SLEEPTIME_SERVICE_ACCOUNT = 20;
	public static final int SLEEPTIME_SERVICE_STATUS = 20;
	public static final int PRIORITY_VIEW_FRAME = 5;
	public static final int PRIORITY_SERVICE_ACCOUNT = 5;
	public static final int PRIORITY_SERVICE_STATUS = 5;

	public static void main(String args[]) throws Exception {
		JMainFrame frame = new JMainFrame();
		frame.setVisible(true);
		Thread thread_view_frame = new Thread(frame);
		thread_view_frame.setPriority(Main.PRIORITY_VIEW_FRAME);
		thread_view_frame.start();
	}
}
