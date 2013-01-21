/*******************************************************************************
 * SAT4J: a SATisfiability library for Java Copyright (C) 2004, 2012 Artois University and CNRS
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 *  http://www.eclipse.org/legal/epl-v10.html
 *
 * Alternatively, the contents of this file may be used under the terms of
 * either the GNU Lesser General Public License Version 2.1 or later (the
 * "LGPL"), in which case the provisions of the LGPL are applicable instead
 * of those above. If you wish to allow use of your version of this file only
 * under the terms of the LGPL, and not to allow others to use your version of
 * this file under the terms of the EPL, indicate your decision by deleting
 * the provisions above and replace them with the notice and other provisions
 * required by the LGPL. If you do not delete the provisions above, a recipient
 * may use your version of this file under the terms of the EPL or the LGPL.
 *
 * Based on the original MiniSat specification from:
 *
 * An extensible SAT solver. Niklas Een and Niklas Sorensson. Proceedings of the
 * Sixth International Conference on Theory and Applications of Satisfiability
 * Testing, LNCS 2919, pp 502-518, 2003.
 *
 * See www.minisat.se for the original solver in C++.
 *
 * That class was initially written in 2001 by Daniel Le Berre for JavaWorld JavaTips 113.
 * http://www.javaworld.com/javaworld/javatips/jw-javatip113.html
 * 
 * Contributors:
 *   CRIL - initial API and implementation
 *******************************************************************************/
package org.sat4j.sat;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.lang.reflect.Modifier;
import java.net.JarURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.zip.ZipEntry;

/**
 * 
 * This class allows dynamic search for classes.
 * 
 * That code appeared in Java World 113 in an article written by Daniel Le Berre
 * http://www.javaworld.com/javatips/jw-javatip113.html
 * 
 * The initial code of JWhich, on which is based this code, was published in
 * JavaWorld 105 by Mike Clark
 * http://www.javaworld.com/javaworld/javatips/jw-javatip105.html
 * 
 * @author sroussel and dleberre
 * 
 */
public class RTSI {

    private static List<String> alreadySeenPckges;

    private RTSI() {
        // prevent creation of an instance of that class
    }

    public static List<String> find(String tosubclassname, boolean fullname) {
        alreadySeenPckges = new ArrayList<String>();
        Set<String> v = new HashSet<String>();
        Set<String> tmp;
        try {
            Class<?> tosubclass = Class.forName(tosubclassname);
            Package[] pcks = Package.getPackages();
            for (Package pck : pcks) {
                tmp = find(pck.getName(), tosubclass, fullname);
                if (tmp != null) {
                    v.addAll(tmp);
                }
            }
        } catch (ClassNotFoundException ex) {
            System.err.println("Class " + tosubclassname + " not found!");
        }
        return new ArrayList<String>(v);
    }

    public static Set<String> find(String pckname, String tosubclassname,
            boolean fullname) {
        Set<String> v = new HashSet<String>();
        try {
            Class<?> tosubclass = Class.forName(tosubclassname);
            v = find(pckname, tosubclass, fullname);
        } catch (ClassNotFoundException ex) {
            System.err.println("Class " + tosubclassname + " not found!");
        }
        return v;
    }

    public static Set<String> find(String pckgname, Class<?> tosubclass,
            boolean fullname) {
        if (alreadySeenPckges.contains(pckgname)) {
            return new HashSet<String>();
        } else {
            alreadySeenPckges.add(pckgname);
            return findnames(pckgname, tosubclass, fullname);
        }
    }

    public static List<String> find(String tosubclassname) {
        return find(tosubclassname, false);
    }

    public static Set<String> find(String pckname, String tosubclassname) {
        return find(pckname, tosubclassname, false);
    }

    public static Set<String> find(String pckgname, Class<?> tosubclass) {
        return find(pckgname, tosubclass, false);
    }

    public static Set<String> findnames(String pckgname, Class<?> tosubclass) {
        return findnames(pckgname, tosubclass, false);
    }

    public static Set<String> findnames(String pckgname, Class<?> tosubclass,
            boolean fullname) {
        Set<String> v = new HashSet<String>();

        // Code from JWhich
        // ======
        // Translate the package name into an absolute path
        String name = pckgname;
        if (!name.startsWith("/")) {
            name = "/" + name;
        }
        name = name.replace('.', '/');

        // Get a File object for the package
        URL url = RTSI.class.getResource(name);

        if (url == null) {
            return null;
        }

        File directory = new File(url.getFile());

        // New code
        // ======
        if (directory.exists()) {
            // Get the list of the files contained in the package
            String[] files = directory.list();
            for (String file : files) {

                // we are only interested in .class files
                if (file.endsWith(".class")) {

                    String classname = file.substring(0, file.length() - 6);
                    try {
                        // Try to create an instance of the object
                        Class<?> o = Class.forName(pckgname + "." + classname);

                        if (tosubclass.isAssignableFrom(o) && !o.isInterface()
                                && !Modifier.isAbstract(o.getModifiers())) {
                            if (fullname) {
                                v.add(pckgname + "." + classname);
                            } else {
                                v.add(classname);
                            }
                        }
                    } catch (NoClassDefFoundError cnfex) {

                    } catch (ClassNotFoundException cnfex) {
                        System.err.println(cnfex);
                    }

                }
            }
            File[] dirs = directory.listFiles(new FilenameFilter() {
                public boolean accept(File dir, String name) {
                    return new File(dir.getAbsolutePath() + "/" + name)
                            .isDirectory();
                }
            });
            Set<String> tmp;
            for (File dir : dirs) {
                String newName = pckgname + "." + dir.getName();
                tmp = find(newName, tosubclass, fullname);
                if (tmp != null) {
                    v.addAll(tmp);
                }

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
                    ZipEntry entry = e.nextElement();
                    String entryname = entry.getName();
                    if (entryname.startsWith(starts)
                            && entryname.endsWith(".class")) {
                        String classname = entryname.substring(0,
                                entryname.length() - 6);
                        if (classname.startsWith("/")) {
                            classname = classname.substring(1);
                        }
                        classname = classname.replace('/', '.');
                        try {
                            // Try to create an instance of the object
                            Class<?> o = Class.forName(classname);

                            if (tosubclass.isAssignableFrom(o)
                                    && !o.isInterface()
                                    && !Modifier.isAbstract(o.getModifiers())) {
                                if (fullname) {
                                    v.add(classname);
                                } else {
                                    v.add(classname.substring(classname
                                            .lastIndexOf('.') + 1));
                                }
                            }
                        } catch (NoClassDefFoundError cnfex) {
                        } catch (ClassNotFoundException cnfex) {
                            System.err.print(cnfex);
                        }
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
