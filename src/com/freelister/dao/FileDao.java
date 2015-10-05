package com.freelister.dao;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class FileDao<T> extends Thread {
	private File file;

	public FileDao(File file) {
		super();
		this.file = file;
	}

	public boolean save(List<T> list) throws IOException {
		if (!file.exists())
			file.createNewFile();
		FileWriter fw = new FileWriter(file);
		BufferedWriter bw = new BufferedWriter(fw);
		for (T t : list) {
			bw.write(t.toString() + '\n');
		}
		bw.flush();
		bw.close();
		fw.close();
		return true;
	}

	public List<T> load(Creator<T> a) throws IOException,
			ClassNotFoundException {
		List<T> list = null;
		if (file.exists()) {
			FileReader fr = new FileReader(file);
			BufferedReader br = new BufferedReader(fr);
			list = new ArrayList<T>();
			String s = br.readLine();
			while (s != null) {
				T e = a.creat(s);
				list.add(e);
				s = br.readLine();
			}
			br.close();
			fr.close();
		}
		return list;
	}

	public interface Creator<T> {
		public T creat(String s);
	}
}
