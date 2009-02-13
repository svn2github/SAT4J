package org.sat4j.apps.sudoku;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;

import javax.swing.JLabel;

@SuppressWarnings("serial")
public class ShowVersions extends JLabel {
    ShowVersions() {
        super();
        setFont(getFont().deriveFont(8));
        setText(" SuDoku " + getVersion("sudoku") + "; " + "sat4j "
                + getVersion("sat4j") + " ");
        // setBorder(BorderFactory.createEtchedBorder());
    }

    public String getVersion(String product) {
        String result;

        try {
            URL url = ShowVersions.class
                    .getResource("/" + product + ".version");
            if (url != null) {
                BufferedReader in = new BufferedReader(new InputStreamReader(
                        url.openStream()));
                result = in.readLine();
                in.close();
            } else {
                result = "0.0";
            }
        } catch (IOException e) {
            result = "0.0";
        }

        return result;
    }

}
