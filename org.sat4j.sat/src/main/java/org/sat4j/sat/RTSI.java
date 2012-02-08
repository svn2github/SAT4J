package org.sat4j.sat;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.lang.reflect.Modifier;
import java.net.JarURLConnection;
import java.net.URL;
import java.util.Enumeration;
import java.util.Vector;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.zip.ZipEntry;

public class RTSI {

	public static Vector<String> alreadySeenPckges;

	public static Vector<String> find(String tosubclassname) {
		alreadySeenPckges = new Vector<String>();
		Vector<String> v = new Vector<String>();
		Vector<String> tmp;
		try {
			// ClassLoader.getSystemClassLoader().setPackageAssertionStatus("org.sat4j",
			// true);
			Class<?> tosubclass = Class.forName(tosubclassname);
			Package[] pcks = Package.getPackages();
			for (int i = 0; i < pcks.length; i++) {
				tmp = find(pcks[i].getName(), tosubclass);
				if (tmp != null)
					v.addAll(tmp);
			}
		} catch (ClassNotFoundException ex) {
			System.err.println("Class " + tosubclassname + " not found!");
		}
		return v;
	}

	public static Vector<String> find(String pckname, String tosubclassname) {
		Vector<String> v = new Vector<String>();
		try {
			Class<?> tosubclass = Class.forName(tosubclassname);
			v = find(pckname, tosubclass);
		} catch (ClassNotFoundException ex) {
			System.err.println("Class " + tosubclassname + " not found!");
		}
		return v;
	}

	public static Vector<String> find(String pckgname, Class<?> tosubclass) {
		if (alreadySeenPckges.contains(pckgname)) {
			return new Vector<String>();
		} else {
			alreadySeenPckges.add(pckgname);
			return findnames(pckgname, tosubclass);
		}
	}

	public static Vector<String> findnames(String pckgname, Class<?> tosubclass) {
		Vector<String> v = new Vector<String>();
		// Code from JWhich
		// ======
		// Translate the package name into an absolute path
		String name = new String(pckgname);
		if (!name.startsWith("/")) {
			name = "/" + name;
		}
		name = name.replace('.', '/');

		// Get a File object for the package
		URL url = RTSI.class.getResource(name);
		// URL url = tosubclass.getResource(name);
		// URL url = ClassLoader.getSystemClassLoader().getResource(name);
		// System.out.println(name+"->"+url);

		// Happens only if the jar file is not well constructed, i.e.
		// if the directories do not appear alone in the jar file like here:
		//
		// meta-inf/
		// meta-inf/manifest.mf
		// commands/ <== IMPORTANT
		// commands/Command.class
		// commands/DoorClose.class
		// commands/DoorLock.class
		// commands/DoorOpen.class
		// commands/LightOff.class
		// commands/LightOn.class
		// RTSI.class
		//
		if (url == null)
			return null;

		File directory = new File(url.getFile());

		// New code
		// ======
		if (directory.exists()) {
			// Get the list of the files contained in the package
			String[] files = directory.list();
			for (int i = 0; i < files.length; i++) {

				// we are only interested in .class files
				if (files[i].endsWith(".class")) {
					// removes the .class extension
					String classname = files[i].substring(0,
							files[i].length() - 6);
					try {
						// Try to create an instance of the object
						Class<?> o = Class.forName(pckgname + "." + classname);

						if (tosubclass.isAssignableFrom(o) && !o.isInterface()
								&& !Modifier.isAbstract(o.getModifiers())) {
							// System.out.println(classname);
							v.add(classname);
						}
					} catch (NoClassDefFoundError cnfex) {
						// System.out.println("Warning : no classDefFoundError : "
						// + classname);
					} catch (ClassNotFoundException cnfex) {
						System.err.println(cnfex);
					}
					// catch (InstantiationException iex) {
					// // We try to instanciate an interface
					// // or an object that does not have a
					// // default constructor
					// } catch (IllegalAccessException iaex) {
					// // The class is not public
					// }
				}
			}
			File[] dirs = directory.listFiles(new FilenameFilter() {
				public boolean accept(File dir, String name) {
					return (new File(dir.getAbsolutePath() + "/" + name))
							.isDirectory();
				}
			});
			Vector<String> tmp;
			for (int i = 0; i < dirs.length; i++) {
				String newName = pckgname + "." + dirs[i].getName();
				tmp = find(newName, tosubclass);
				if (tmp != null)
					v.addAll(tmp);

			}

		} else {
			try {
				// It does not work with the filesystem: we must
				// be in the case of a package contained in a jar file.
				JarURLConnection conn = (JarURLConnection) url.openConnection();
				String starts = conn.getEntryName();
				JarFile jfile = conn.getJarFile();
				Enumeration<JarEntry> e = jfile.entries();
				while (e.hasMoreElements()) {
					ZipEntry entry = (ZipEntry) e.nextElement();
					String entryname = entry.getName();
					if (entryname.startsWith(starts)
					// &&(entryname.lastIndexOf('/')<=starts.length())
							&& entryname.endsWith(".class")) {
						String classname = entryname.substring(0,
								entryname.length() - 6);
						// System.out.println(classname);
						if (classname.startsWith("/"))
							classname = classname.substring(1);
						classname = classname.replace('/', '.');
						try {
							// Try to create an instance of the object
							// Object o =
							// Class.forName(classname).newInstance();
							// if (tosubclass.isInstance(o)) {

							Class<?> o = Class.forName(classname);

							if (tosubclass.isAssignableFrom(o)
									&& !o.isInterface()
									&& !Modifier.isAbstract(o.getModifiers())) {
								// System.out.println(classname.substring(classname.lastIndexOf('.')+1));
								v.add(classname.substring(classname
										.lastIndexOf('.') + 1));
							}
						} catch (NoClassDefFoundError cnfex) {
							// System.out.println("Warning : no classDefFoundError : "
							// + classname);
						} catch (ClassNotFoundException cnfex) {
							System.err.print(cnfex);
						}
						// catch (InstantiationException iex) {
						// // We try to instanciate an interface
						// // or an object that does not have a
						// // default constructor
						// } catch (IllegalAccessException iaex) {
						// // The class is not public
						// }
					}
				}
			} catch (IOException ioex) {
				System.err.println(ioex);
			}
		}

		return v;
	}

	public static void displayResultOfFind(String tosubclassname) {
		System.out.println(find(tosubclassname));
	}

	public static void displayResultOfFind(String pckname, String tosubclassname) {
		System.out.println(find(pckname, tosubclassname));
	}

	public static void displayResultOfFind(String pckgname, Class<?> tosubclass) {
		System.out.println(findnames(pckgname, tosubclass));

	}

	public static void main(String[] args) {
		if (args.length == 2) {
			displayResultOfFind(args[0], args[1]);
		} else {
			if (args.length == 1) {
				displayResultOfFind(args[0]);
			} else {
				System.out.println("Usage: java RTSI [<package>] <subclass>");
			}
		}
	}
}// RTSI
