package app.view;

import app.utils.Utils;

import javax.swing.*;


public class DialogWindow extends JDialog {


    JButton ok;
    JButton cancel;
    JTextField input;
    String res = null;

    public DialogWindow(int width, int height, String title) {
        setModalExclusionType(ModalExclusionType.APPLICATION_EXCLUDE);
        setSize(width, height);
        setLocationRelativeTo(null);
        setLayout(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setResizable(false);
        setTitle(title);
        setView();
    }

    public String showAndWait() {
        setModal(true);
        setVisible(true);
        return res;
    }

    private void close() {
        setVisible(false);
        dispose();
    }

    public void setView() {
        ok = new JButton("OK");
        ok.setBounds(20, 80, 60,24);
        ok.setFocusPainted(false);
        ok.setContentAreaFilled(false);
        ok.addActionListener(e -> {
            res = input.getText();
            if (!checkStringValidation()) {
                setModal(false);
                Utils.showMessage(null, "String is not correct", "Input error", 1);
                setModal(true);
            } else {
                close();
            }
        });

        cancel = new JButton("CANCEL");
        cancel.setBounds(90, 80, 80,24);
        cancel.setFocusPainted(false);
        cancel.setContentAreaFilled(false);
        cancel.addActionListener(e -> {
            res = null;
            close();
        });

        input = new JTextField(70);
        input.setBounds(100,30, 70, 30);

        JLabel label = new JLabel("Enter client :");
        label.setBounds(20,30,120,30);

        add(label);
        add(input);
        add(ok);
        add(cancel);
    }

    private boolean checkStringValidation() {
        return res != null && !res.isEmpty();
    }
}
