package com.freelister.service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.net.URLConnection;
import java.util.List;
import java.util.Map;

import org.apache.http.HttpEntity;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.methods.RequestBuilder;
import org.apache.http.cookie.Cookie;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;

public class HttpRequest {
	CloseableHttpClient httpclient = HttpClients.createDefault();
	public static final int TYPE_RETURN_STATUSLINE = 0;
	public static final int TYPE_RETURN_COOKIES = 1;
	public static final int TYPE_RETURN_HEADERS = 2;
	public static final int TYPE_RETURN_CONTENT = 3;
	public static final int TYPE_RETURN_CONTENT_COOKIES = 4;
	public static Integer what = new Integer(0);

	public HttpRequest() {
		super();
		this.httpclient = HttpClients.createDefault();
	}

	protected void finalize() throws IOException {
		httpclient.close();
	}

	public static String doGet(String url, List<Cookie> cookies) {
		String result = "";
		BufferedReader in = null;
		try {
			String urlNameString = url;
			URL realUrl = new URL(urlNameString);
			// 打开和URL之间的连接
			URLConnection connection = realUrl.openConnection();
			// 建立实际的连接
			if (cookies != null) {
				String cookies_s = new String();
				for (int i = 0; i < cookies.size(); i++) {
					cookies_s += cookies.get(i).getName() + "="
							+ cookies.get(i).getValue() + "; ";
				}
				connection.setRequestProperty("Cookie", cookies_s);
			}
			connection.connect();
			// 获取所有响应头字段
			// Map<String, List<String>> map = connection.getHeaderFields();
			// // 遍历所有的响应头字段
			// for (String key : map.keySet()) {
			// System.out.println(key + "--->" + map.get(key));
			// }
			// 定义 BufferedReader输入流来读取URL的响应
			in = new BufferedReader(new InputStreamReader(
					connection.getInputStream()));
			String line;
			while ((line = in.readLine()) != null) {
				result += line;
			}
			return result;
		} catch (Exception e) {
			e.printStackTrace();
		}
		// 使用finally块来关闭输入流
		finally {
			try {
				if (in != null) {
					in.close();
				}
			} catch (Exception e2) {
				e2.printStackTrace();
			}
		}
		return result;
	}

	public static String doGet(String url) {
		String result = "";
		BufferedReader in = null;
		try {
			String urlNameString = url;
			URL realUrl = new URL(urlNameString);
			// 打开和URL之间的连接
			URLConnection connection = realUrl.openConnection();
			// 建立实际的连接
			connection.connect();
			// 获取所有响应头字段
			// Map<String, List<String>> map = connection.getHeaderFields();
			// // 遍历所有的响应头字段
			// for (String key : map.keySet()) {
			// System.out.println(key + "--->" + map.get(key));
			// }
			// 定义 BufferedReader输入流来读取URL的响应
			in = new BufferedReader(new InputStreamReader(
					connection.getInputStream()));
			String line;
			while ((line = in.readLine()) != null) {
				result += line;
			}
			return result;
		} catch (Exception e) {
			e.printStackTrace();
		}
		// 使用finally块来关闭输入流
		finally {
			try {
				if (in != null) {
					in.close();
				}
			} catch (Exception e2) {
				e2.printStackTrace();
			}
		}
		return result;
	}

	public CloseableHttpResponse doPost(String url, Map<String, String> params)
			throws ClientProtocolException, IOException {

		HttpPost httppost = new HttpPost(url);
		MultipartEntityBuilder reqEntityBuilder = MultipartEntityBuilder
				.create();
		for (String s : params.keySet()) {
			reqEntityBuilder.addPart(s, new StringBody(params.get(s),
					ContentType.TEXT_PLAIN));
		}
		HttpEntity reqEntity = reqEntityBuilder.build();
		httppost.setEntity(reqEntity);
		return httpclient.execute(httppost);
	}

	public static Object postForm(String url, Map<String, String> params,
			List<Cookie> cookies, int returnType) throws Exception {
		CloseableHttpResponse response = null;
		BasicCookieStore cookieStore = new BasicCookieStore();
		CloseableHttpClient httpclient = HttpClients.custom()
				.setDefaultCookieStore(cookieStore).build();
		RequestBuilder rb;
		synchronized (what) {
			rb = RequestBuilder
					.post()
					.setUri(new URI(
							"https://cas.dgut.edu.cn/User/Login?ReturnUrl=%2f%3fappid%3dself&appid=self"));
			for (String s : params.keySet())
				rb.addParameter(s, params.get(s));
			HttpUriRequest post = rb.build();
			response = httpclient.execute(post);
		}
		try {
			switch (returnType) {
			case HttpRequest.TYPE_RETURN_STATUSLINE:
				return response.getStatusLine().toString();
			case HttpRequest.TYPE_RETURN_COOKIES:
				List<Cookie> responsecookies = cookieStore.getCookies();
				if (responsecookies.isEmpty())
					return null;
				return responsecookies;
			}
		} finally {
			response.close();
		}
		return null;
	}

	public static Object doGet(String url, List<Cookie> cookies,
			int returnType, String header) throws Exception {
		BasicCookieStore cookieStore = new BasicCookieStore();
		CloseableHttpClient httpclient = HttpClients.custom()
				.setDefaultCookieStore(cookieStore).build();
		HttpUriRequest httpget = new HttpGet(url);
		if (cookies != null) {
			String cookies_s = new String();
			for (int i = 0; i < cookies.size(); i++) {
				cookies_s += cookies.get(i).getName() + "="
						+ cookies.get(i).getValue() + "; ";
			}
			httpget.addHeader("Cookie", cookies_s);
		}
		CloseableHttpResponse response = httpclient.execute(httpget);
		try {
			switch (returnType) {
			case HttpRequest.TYPE_RETURN_HEADERS:
				return response.getHeaders(header);
			case HttpRequest.TYPE_RETURN_STATUSLINE:
				return response.getStatusLine();
			case HttpRequest.TYPE_RETURN_COOKIES:
				List<Cookie> responsecookies = cookieStore.getCookies();
				if (responsecookies.isEmpty())
					return null;
				return cookieStore.getCookies();
			case HttpRequest.TYPE_RETURN_CONTENT:
				return getResult(response);
			case HttpRequest.TYPE_RETURN_CONTENT_COOKIES:
				Object[] result_cookies = new Object[2];
				result_cookies[0] = getResult(response);
				result_cookies[1] = cookieStore.getCookies();
				return result_cookies;
			}
		} finally {
			response.close();
		}
		return null;
	}

	private static String getResult(CloseableHttpResponse response)
			throws UnsupportedOperationException, IOException {
		HttpEntity entity = response.getEntity();
		InputStreamReader isr = new InputStreamReader(entity.getContent());
		BufferedReader br = new BufferedReader(isr);
		String result = "";
		String tmp = br.readLine();
		while ((tmp = br.readLine()) != null)
			result += tmp;
		return result;
	}

	/**
	 * 
	 * @Title: doPost
	 * @return: the response content
	 */
	public static String doPost(String url, Map<String, String> params,
			List<Cookie> cookies) {
		PrintWriter out = null;
		BufferedReader in = null;
		String result = "";
		try {
			String urlNameString = url;
			URL realUrl = new URL(urlNameString);
			String params_s = "";
			for (String s : params.keySet()) {
				params_s += s + '=' + params.get(s) + '&';
			}
			String cookies_s = "";
			URLConnection connection = realUrl.openConnection();
			if (cookies != null) {
				for (int i = 0; i < cookies.size(); i++) {
					cookies_s += cookies.get(i).getName() + "="
							+ cookies.get(i).getValue() + "; ";
				}
				connection.addRequestProperty("Cookie", cookies_s);

			}
			// 发送POST请求必须设置如下两行
			connection.setDoOutput(true);
			connection.setDoInput(true);
			out = new PrintWriter(connection.getOutputStream());
			out.print(params_s);

			out.flush();
			// 获取所有响应头字段
			// Map<String, List<String>> map = connection.getHeaderFields();
			// // 遍历所有的响应头字段
			// for (String key : map.keySet()) {
			// System.out.println(key + "--->" + map.get(key));
			// }
			// 定义BufferedReader输入流来读取URL的响应
			in = new BufferedReader(new InputStreamReader(
					connection.getInputStream()));
			String line;
			while ((line = in.readLine()) != null) {
				result += line;
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				if (out != null) {
					out.close();
				}
				if (in != null) {
					in.close();
				}
			} catch (IOException ex) {
				ex.printStackTrace();
			}
		}
		return result;
	}

	public static HttpURLConnection doGet(String url, String cookies) {
		BufferedReader in = null;
		try {
			URL realUrl = new URL(url);
			// 打开和URL之间的连接
			HttpURLConnection connection = (HttpURLConnection) realUrl
					.openConnection();
			if (cookies != null)
				connection.setRequestProperty("Cookie", cookies);
			// 建立实际的连接
			connection.connect();
			// 获取所有响应头字段
			// Map<String, List<String>> map = connection.getHeaderFields();
			// // 遍历所有的响应头字段
			// for (String key : map.keySet()) {
			// System.out.println(key + ": " + map.get(key));
			// }
			return connection;
		} catch (Exception e) {
			e.printStackTrace();
		}
		// 使用finally块来关闭输入流
		finally {
			try {
				if (in != null) {
					in.close();
				}
			} catch (Exception e2) {
				e2.printStackTrace();
			}
		}
		return null;
	}
}
