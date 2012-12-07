package com.qorporation.qluster.util;

import java.io.File;
import java.net.JarURLConnection;
import java.net.URL;
import java.net.URLDecoder;
import java.util.Arrays;
import java.util.Collection;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import com.qorporation.qluster.Qluster;

public class ClassWalker implements Iterator<Class<?>> {
	public static final String ROOT_PACKAGE = Qluster.class.getPackage().getName();
	
	@SuppressWarnings("serial")
	private static final Collection<String> paths = new HashSet<String>() {{
		this.add(ROOT_PACKAGE);
	}};
	
	@SuppressWarnings("serial")
	private static final Collection<String> jarPaths = new HashSet<String>() {{
		this.add(ROOT_PACKAGE);
	}};
	
	public static final void addRootPackage(String path) {
		ClassWalker.jarPaths.add(path);
		
		if (path.contains(".")) {
			path = path.substring(0, path.indexOf('.'));
		}

		ClassWalker.paths.add(path);
	}
	
	private LinkedBlockingQueue<Class<?>> classes = null;
	
	public ClassWalker(ClassWalkerFilter ... filters) {
		this.classes = new LinkedBlockingQueue<Class<?>>();
		
		for (String path: paths) {
			this.walk(path, filters);
		}
	}
	
	private void walk(String path, ClassWalkerFilter ... filters) {		
		try {
			Enumeration<URL> res = Thread.currentThread().getContextClassLoader().getResources(path);
			while (res.hasMoreElements()) {
				URL url = res.nextElement();
				String proto = url.getProtocol();
				if (proto.equals("file")) {
					File file = new File(URLDecoder.decode(url.getPath(), "UTF-8"));				
					if (!file.isDirectory()) { continue; }
					walkDirectory(path, file, filters);
				} else if (proto.equals("jar")) {
					walkJar(path, url, filters);
				}
			}
		} catch (Exception e) {
			ErrorControl.logException(e);
		}
	}

	private void walkJar(String path, URL url, ClassWalkerFilter[] filters) {
		try {
			JarURLConnection con = (JarURLConnection) url.openConnection();
			JarFile archive = con.getJarFile();
			Enumeration<JarEntry> entries = archive.entries();
			while (entries.hasMoreElements()) {
				JarEntry entry = entries.nextElement();
				String name = entry.getName();
				
				if (!name.endsWith(".class")) continue;
								
				name = name.substring(0, name.length() - 6).replace(File.separatorChar, '.');
				
				boolean matchable = false;
				for (String p: jarPaths) {
					if (name.startsWith(p)) {
						matchable = true;
						break;
					}
				}
				
				if (!matchable) continue;
				
				try {
					Class<?> cls = Class.forName(name);
					
					boolean visit = true;
					for (ClassWalkerFilter filter: filters) {
						if (!filter.visit(cls)) {
							visit = false;
							break;
						}
					}
					
					if (visit) {
						this.classes.add(cls);
					}
				} catch (Error e) {
					continue;
				}
			}
		} catch (Exception e) {
			ErrorControl.logException(e);
		}
	}

	private void walkDirectory(String path, File file, ClassWalkerFilter[] filters) {		
		LinkedList<File> files = new LinkedList<File>();
		files.addAll(Arrays.asList(file.listFiles()));
		
		while(!files.isEmpty()) {
			File cur = files.poll();			
			if (cur.isDirectory()) {
				files.addAll(Arrays.asList(cur.listFiles()));
			} else {
				String name = cur.getAbsolutePath();
				if (!name.endsWith(".class")) continue;
				
				name = name.substring(0, name.length() - 6).replace(File.separatorChar, '.');
				name = name.substring(name.indexOf(path));
				
				try {
					Class<?> cls = Class.forName(name);
					
					boolean visit = true;
					for (ClassWalkerFilter filter: filters) {
						if (!filter.visit(cls)) {
							visit = false;
							break;
						}
					}
					
					if (visit) {
						this.classes.add(cls);
					}
				} catch (ClassNotFoundException e) {
					ErrorControl.logException(e);
					ErrorControl.fatal(cur.getAbsolutePath());
				}
			}
		}
	}

	public boolean hasNext() {
		return !this.classes.isEmpty();
	}

	public Class<?> next() {
		return this.classes.poll();
	}

	public void remove() {
		ErrorControl.fatal("Unsupported Operation");
	}
	
}
