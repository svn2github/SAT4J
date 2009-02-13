package org.sat4j.apps.sudoku;

import java.net.URL;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

import javax.swing.ImageIcon;

public class SuDokuResources {

    public SuDokuResources() {
        try {
            resourceBundle = ResourceBundle
                    .getBundle("org.sat4j.apps.sudoku.sudoku");
        } catch (Exception e) {
            System.out.println(" Exception in ResourceBundle.getBundle() ");
            e.printStackTrace();
            System.exit(0);
        }
        iconsLoaded = false;
    }

    public java.net.URL getURLFromKey(String key) {
        String filename = resourceBundle.getString(key);
        return this.getClass().getResource(filename);
    }

    public String getStringFromKey(String key) {
        String result = resourceBundle.getString(key);
        return result == null ? "" : result;
    }

    public String getParsedProperty(String key) {
        String unParsed = System.getProperties().getProperty(key);
        if (unParsed == null) {
            return "";
        }
        return checkForEscapes(unParsed);
    }

    boolean isHex(char ch) {
        return (('0' <= ch) && (ch <= '9')) || (('A' <= ch) && (ch <= 'F'))
                || (('a' <= ch) && (ch <= 'f'));
    }

    int hexValue(char ch) {
        if (('0' <= ch) && (ch <= '9')) {
            return ch - '0';
        } else if (('A' <= ch) && (ch <= 'F')) {
            return 10 + ch - 'A';
        } else {
            return 10 + ch - 'a';
        }
    }

    String checkForEscapes(String s) {
        StringBuffer result = new StringBuffer();
        int i = 0;
        while (i < s.length()) {
            char ch = s.charAt(i);
            if (ch == '\\') {
                i++;
                if (i < s.length()) {
                    char tag = s.charAt(i);
                    switch (tag) {
                    case 'n':
                        result.append('\n');
                        i++;
                        break;

                    case 't':
                        result.append('\t');
                        i++;
                        break;

                    case 'u':
                        char unicode = 0;
                        boolean stillOK = true;
                        i++;
                        int j;
                        for (j = 0; stillOK && (j < 4); j++) {
                            stillOK = isHex(s.charAt(i + j));
                            if (stillOK) {
                                unicode = (char) (16 * unicode + hexValue(s
                                        .charAt(i + j)));
                            }
                        }
                        result.append(unicode);
                        i += j;
                        break;

                    default:
                        result.append(tag);
                        i++;
                        break;
                    }
                }
            } else {
                result.append(ch);
                i++;
            }
        }

        return result.toString();
    }

    ImageIcon getSat4jIcon() {
        ensureIcons();
        return sat4jIcon;
    }

    ImageIcon getECITIcon() {
        ensureIcons();
        return ecitIcon;
    }

    ImageIcon getCRILIcon() {
        ensureIcons();
        return crilIcon;
    }

    ImageIcon getObjectWebIcon() {
        ensureIcons();
        return objectWebIcon;
    }

    ImageIcon getEventIcon() {
        ensureIcons();
        return eventIcon;
    }

    void ensureIcons() {
        if (!iconsLoaded) {
            URL sat4jIconURL = getURLFromKey("ICON_SAT4J");
            sat4jIcon = new ImageIcon(sat4jIconURL);
            URL ECITIconURL = getURLFromKey("ICON_ECIT");
            ecitIcon = new ImageIcon(ECITIconURL);
            URL CRILIconURL = getURLFromKey("ICON_CRIL");
            crilIcon = new ImageIcon(CRILIconURL);
            URL objectWebIconURL = getURLFromKey("ICON_OBJECT_WEB");
            objectWebIcon = new ImageIcon(objectWebIconURL);
            URL scienceFestIconURL = getURLFromKey("ICON_EVENT");
            eventIcon = new ImageIcon(scienceFestIconURL);
            iconsLoaded = true;
        }

    }

    boolean iconsLoaded = false;

    ImageIcon sat4jIcon, ecitIcon, crilIcon, objectWebIcon, eventIcon;

    ResourceBundle resourceBundle;

    public ImageIcon getIconFromKey(String key) {
        try {
            URL url = getURLFromKey("ICON_" + key);
            if (url == null) {
                return null;
            }
            return new ImageIcon(url);
        } catch (MissingResourceException mre) {
            return null;
        }
    }

}
