package org.sat4j.sat;

import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.border.CompoundBorder;
import javax.swing.border.TitledBorder;

import org.sat4j.minisat.core.RestartStrategy;
import org.sat4j.minisat.core.SearchParams;
import org.sat4j.minisat.core.SolverStats;
import org.sat4j.minisat.restarts.LubyRestarts;
import org.sat4j.minisat.restarts.NoRestarts;
import org.sat4j.specs.ILogAble;

public class RestartCommandComponent extends CommandComponent {

    private static final long serialVersionUID = 1L;

    private JPanel restartPropertiesPanel;

    private JLabel chooseRestartStrategyLabel;
    private JLabel noParameterLabel;
    private JComboBox listeRestarts;
    private JButton restartButton;

    private JButton changeRestartMode;

    private JLabel factorLabel;
    private static final String FACTOR = "Factor: ";
    private JTextField factorField;

    private String currentRestart;

    private static final String RESTART = "Restart";
    private static final String CHOOSE_RESTART_STRATEGY = "Choose restart strategy: ";
    private static final String CHANGE_RESTART_STRATEGY = "Apply";
    private static final String MANUAL_RESTART = "Manual Restart";
    private static final String NO_PARAMETER_FOR_THIS_STRATEGY = "No paramaters for this strategy";
    private static final String RESTART_STRATEGY_CLASS = "org.sat4j.minisat.core.RestartStrategy";
    private static final String RESTART_PATH = "org.sat4j.minisat.restarts";

    private SolverController controller;

    private ILogAble logger;

    public RestartCommandComponent(String name, SolverController controller,
            String initialRestartStrategy, ILogAble logger) {
        this.setName(name);
        this.currentRestart = initialRestartStrategy;
        this.controller = controller;
        this.logger = logger;
        createPanel();
        initFactorParam();
    }

    @Override
    public void createPanel() {

        this.setLayout(new GridBagLayout());

        GridBagConstraints c = new GridBagConstraints();
        c.anchor = GridBagConstraints.PAGE_START;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.weightx = 1;
        c.gridx = 0;

        JPanel chooseRestartPanel = new JPanel();
        chooseRestartPanel.setLayout(new GridBagLayout());

        GridBagConstraints c1 = new GridBagConstraints();
        c1.anchor = GridBagConstraints.PAGE_START;
        c1.fill = GridBagConstraints.HORIZONTAL;
        c1.weightx = 1;
        c1.gridx = 0;

        chooseRestartPanel.setBorder(new CompoundBorder(new TitledBorder(null,
                this.getName(), TitledBorder.LEFT, TitledBorder.TOP),
                DetailedCommandPanel.BORDER5));

        JPanel tmpPanel1 = new JPanel();
        tmpPanel1.setLayout(new FlowLayout());

        this.chooseRestartStrategyLabel = new JLabel(CHOOSE_RESTART_STRATEGY);

        this.listeRestarts = new JComboBox(getListOfRestartStrategies()
                .toArray());

        this.listeRestarts.setSelectedItem(this.currentRestart);

        this.listeRestarts.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                modifyRestartParamPanel();
            }
        });

        tmpPanel1.add(this.chooseRestartStrategyLabel);
        tmpPanel1.add(this.listeRestarts);

        this.changeRestartMode = new JButton(CHANGE_RESTART_STRATEGY);

        this.changeRestartMode.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                hasClickedOnChange();
            }
        });

        tmpPanel1.add(this.changeRestartMode);

        this.noParameterLabel = new JLabel(NO_PARAMETER_FOR_THIS_STRATEGY);

        Font newLabelFont = new Font(this.noParameterLabel.getFont().getName(),
                Font.ITALIC, this.noParameterLabel.getFont().getSize());

        this.noParameterLabel.setFont(newLabelFont);

        this.restartPropertiesPanel = new JPanel();
        this.restartPropertiesPanel.add(this.noParameterLabel);

        this.restartButton = new JButton(RESTART);

        this.restartButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                hasClickedOnRestart();
            }
        });

        JPanel restartButtonPanel = new JPanel();
        restartButtonPanel.setName(MANUAL_RESTART);
        restartButtonPanel.setBorder(new CompoundBorder(new TitledBorder(null,
                restartButtonPanel.getName(), TitledBorder.LEFT,
                TitledBorder.TOP), DetailedCommandPanel.BORDER5));

        restartButtonPanel.add(this.restartButton);

        c1.gridy = 0;
        chooseRestartPanel.add(tmpPanel1, c1);

        c1.gridy = 1;
        chooseRestartPanel.add(this.restartPropertiesPanel, c1);

        c.gridy = 0;
        this.add(chooseRestartPanel, c);

        c.gridy = 1;
        this.add(restartButtonPanel, c);
    }

    public void initFactorParam() {

        this.factorLabel = new JLabel(FACTOR);
        this.factorField = new JTextField(
                LubyRestarts.DEFAULT_LUBY_FACTOR + "", 5);

    }

    public void modifyRestartParamPanel() {
        this.restartPropertiesPanel.removeAll();
        if (this.listeRestarts.getSelectedItem().equals("LubyRestarts")) {
            this.restartPropertiesPanel.add(this.factorLabel);
            this.restartPropertiesPanel.add(this.factorField);
        } else {
            this.restartPropertiesPanel.add(this.noParameterLabel);
        }
        setRestartPropertiesPanelEnabled(true);
        this.restartPropertiesPanel.repaint();
        this.repaint();
        this.paintAll(this.getGraphics());
        this.repaint();
    }

    public void setRestartPanelEnabled(boolean enabled) {
        this.listeRestarts.setEnabled(enabled);
        this.restartButton.setEnabled(enabled);
        this.chooseRestartStrategyLabel.setEnabled(enabled);
        setRestartPropertiesPanelEnabled(enabled);
        this.repaint();
    }

    public void setRestartPropertiesPanelEnabled(boolean enabled) {
        for (Component c : this.restartPropertiesPanel.getComponents()) {
            c.setEnabled(enabled);
        }
        this.restartPropertiesPanel.repaint();
    }

    public void updateRestartStrategyPanel() {
        this.listeRestarts.setSelectedItem(this.currentRestart);
    }

    public void hasClickedOnChange() {
        this.controller.shouldRestartNow();

        String choix = (String) this.listeRestarts.getSelectedItem();

        boolean isNotSameRestart = !choix.equals(this.currentRestart);
        boolean shouldInit = isNotSameRestart;

        RestartStrategy restart = new NoRestarts();
        SearchParams params = this.controller.getSearchParams();
        SolverStats stats = this.controller.getSolverStats();
        if (choix.equals("LubyRestarts")) {
            boolean factorChanged = false;
            int factor = LubyRestarts.DEFAULT_LUBY_FACTOR;
            if (this.factorField.getText() != null) {
                factor = Integer.parseInt(this.factorField.getText());
            }
            // if the current restart is a LubyRestart
            if (isNotSameRestart) {
                restart = new LubyRestarts(factor);
                this.controller.setRestartStrategy(restart);
            } else {
                factorChanged = !(factor == ((LubyRestarts) this.controller
                        .getRestartStrategy()).getFactor());
            }
            // if the factor has changed
            if (factorChanged) {
                restart = this.controller.getRestartStrategy();
                ((LubyRestarts) restart).setFactor(factor);
            }
            shouldInit = isNotSameRestart || factorChanged;

            if (shouldInit) {
                this.controller.init(params, stats);
            }

        } else {
            try {
                restart = (RestartStrategy) Class.forName(
                        RESTART_PATH + "." + choix).newInstance();
                assert restart != null;
                this.controller.setRestartStrategy(restart);
                this.controller.init(params, stats);

            } catch (ClassNotFoundException e) {
                logger.log(e.getMessage());
            } catch (IllegalAccessException e) {
                logger.log(e.getMessage());
            } catch (InstantiationException e) {
                logger.log(e.getMessage());
            }
        }

        this.currentRestart = choix;

    }

    public void hasClickedOnRestart() {
        this.controller.shouldRestartNow();
    }

    public List<String> getListOfRestartStrategies() {
        List<String> resultRTSI = RTSI.find(RESTART_STRATEGY_CLASS);
        List<String> finalResult = new ArrayList<String>();

        for (String s : resultRTSI) {
            if (!s.contains("Remote")) {
                finalResult.add(s);
            }
        }

        return finalResult;
    }

    public String getCurrentRestart() {
        return this.currentRestart;
    }

    public void setCurrentRestart(String currentRestart) {
        this.currentRestart = currentRestart;
        updateRestartStrategyPanel();
        modifyRestartParamPanel();
    }
}
