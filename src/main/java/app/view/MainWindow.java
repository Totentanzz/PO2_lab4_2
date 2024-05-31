package app.view;

import com.thoughtworks.xstream.XStream;
import app.shapes.Circle;
import app.shapes.Figure;
import app.shapes.Rectangle;
import app.utils.RandomFigureFactory;
import app.utils.Utils;
import app.web.Client;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.*;
import java.util.ArrayList;

public class MainWindow extends JFrame {

    private static MainWindow instance;
    private JButton btnConnect;
    private JButton btnDisconnect;
    private JButton btnReceive;
    private JButton btnUpload;
    private JButton btnNew;
    private FigurePanel figurePanel;
    private Client client;
    private final ArrayList<Figure> figures = new ArrayList<>();
    public static int figuresCount;
    private JList<String> clients;
    private int selectedId;


    private MainWindow(int width, int height) {
        setSize(width, height);
        setLocationRelativeTo(null);
        setLayout(null);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setResizable(false);

        addMenuBar();
        addPanel(width, height);
        addConnectButton();
        addDisconnectButton();
        addUploadButton();
        addReceiveButton();
        addNewButton();
        implWindowClosingMethod();
    }

    public static void createInstance(int width, int height) {
        if (instance == null) {
            instance = new MainWindow(width, height);
        }
    }

    public static MainWindow getInstance() {
        return instance;
    }

    public ArrayList<Figure> getFigures() {
        return figures;
    }

    private void addMenuBar() {
        JMenuBar menuBar = new JMenuBar();
        JMenu menuFile = new JMenu("File");

        JMenuItem menuItemSave = new JMenuItem("Save XML");
        menuItemSave.addActionListener(this::saveActionPerformed);
        menuFile.add(menuItemSave);

        JMenuItem menuItemLoad = new JMenuItem("Load XML");
        menuItemLoad.addActionListener(this::loadActionPerformed);
        menuFile.add(menuItemLoad);

        menuBar.add(menuFile);
        setJMenuBar(menuBar);
    }

    private void saveActionPerformed(ActionEvent event) {
        FileDialog fd = createFileDialog(this, "Save file", FileDialog.SAVE);

        try (OutputStreamWriter osw = new OutputStreamWriter(new FileOutputStream(getPath(fd)), "Unicode")) {
            XStream xstream = new XStream();
            Utils.setXMLParameters(xstream);
            xstream.toXML(figures, osw);
            osw.flush();
        } catch (IOException ex) {
            Utils.showMessage(this, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void loadActionPerformed(ActionEvent event) {
        FileDialog fd = createFileDialog(this, "Load file", FileDialog.LOAD);

        try (FileInputStream fis = new FileInputStream(getPath(fd));
             BufferedReader br = new BufferedReader(new InputStreamReader(fis, "Unicode"))) {

            XStream xstream = new XStream();
            Utils.setXMLParameters(xstream);
            xstream.allowTypes(new Class[]{Circle.class, Rectangle.class});

            /* *
             * This forced conversion is not safe and may throw an exception and cause the program to crash:
             * ArrayList<lab1.Figure> receivedFigures = (ArrayList<lab1.Figure>) xstream.fromXML(br);
             * */
            ArrayList<Figure> receivedFigures = Utils.castArrayList(xstream.fromXML(br), Figure.class);
            Utils.restoreColor(receivedFigures);

            loadFigures(receivedFigures);
        } catch (IOException ex) {
            Utils.showMessage(this, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void addPanel(int width, int height) {
        figurePanel = new FigurePanel(width - 35, height - 200);
        add(figurePanel);
    }

    private void addConnectButton() {
        btnConnect = createButton("Connect", 10, 10, true);
        add(btnConnect);

        btnConnect.addActionListener(event -> {
            client = new Client(1235);
            client.executeRegistration();
            if (client.isRegistered()) {
                setTitle(client.getClientName());
                switchBtnsState(false);
            }
        });
    }

    private void addDisconnectButton() {
        btnDisconnect = createButton("Disconnect", 10, 70, false);
        add(btnDisconnect);

        btnDisconnect.addActionListener(event -> {
            if (client.isRegistered()) {
                client.deleteClientInfo(true);
                switchBtnsState(true);
            } else {
                Utils.showMessage(this, "Client is not registered on server!", "Error", JOptionPane.ERROR_MESSAGE);
            }
        });
    }

    private void addUploadButton() {
        btnUpload = createButton("Upload", 120, 40, false);
        add(btnUpload);

        btnUpload.addActionListener(event -> {
            if (client.isRegistered()) {
                XStream xstream = new XStream();
                Utils.setXMLParameters(xstream);
                String xmlString = xstream.toXML(MainWindow.getInstance().getFigures());
                client.uploadFigures(client.getClientName(), xmlString);
            } else {
                Utils.showMessage(this, "Client is not registered on server!", "Error", JOptionPane.ERROR_MESSAGE);
            }
        });
    }

    private void addReceiveButton() {
        btnReceive = createButton("Receive", 230, 40, false);
        add(btnReceive);

        btnReceive.addActionListener(event -> {
            if (client.isRegistered()) {
                DialogWindow dw = new DialogWindow(205, 160, "Input");
                String clientName = dw.showAndWait();
                String xmlString = client.downloadFigures(clientName);
                if (xmlString != null) {
                    if (xmlString.isEmpty()) {
                        Utils.showMessage(this, "Gotten xml string is empty", "Error", JOptionPane.ERROR_MESSAGE);
                        return;
                    }
                    System.out.println("Gotten xml string:\n" + xmlString);
                    XStream xstream = new XStream();
                    Utils.setXMLParameters(xstream);
                    xstream.allowTypes(new Class[]{Circle.class, Rectangle.class});

                    ArrayList<Figure> receivedFigures = Utils.castArrayList(xstream.fromXML(xmlString), Figure.class);
                    Utils.restoreColor(receivedFigures);
                    loadFigures(receivedFigures);
                }
            } else {
                Utils.showMessage(this, "Client is not registered on server!", "Error", JOptionPane.ERROR_MESSAGE);
            }
        });
    }

    private void addNewButton() {
        btnNew = createButton("New", 575,40, true);
        add(btnNew);

        btnNew.addActionListener(event -> {
            createFigures();
            repaint();
        });
    }

    private void implWindowClosingMethod() {
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                System.out.println("CLOSING");
                if (client != null && client.isRegistered()) {
                    client.deleteClientInfo(false);
                    System.out.println("client was unregistered");
                }
            }
        });
        Runtime.getRuntime().addShutdownHook(new Thread(()->{
            SwingUtilities.invokeLater(()->{
                MainWindow.getInstance().dispatchEvent(
                        new WindowEvent(MainWindow.getInstance(), WindowEvent.WINDOW_CLOSING)
                );
            });
        }));
    }

    private FileDialog createFileDialog(JFrame p, String title, int mode) {
        FileDialog fd = new FileDialog(p, title, mode);
        fd.setFile("data.xml");
        fd.setVisible(true);
        return fd;
    }

    private String getPath(FileDialog fd) {
        return fd.getDirectory() + fd.getFile();
    }

    private JButton createButton(String title, int x, int y, boolean isEnabled) {
        JButton button = new JButton(title);
        button.setBounds(x, y, 100, 50);
        button.setFocusPainted(false);
        button.setContentAreaFilled(false);
        button.setEnabled(isEnabled);
        return button;
    }

    public void createFigures() {
        figures.clear();

        for (int i = 0; i < MainWindow.figuresCount; i++) {
            if (i <= MainWindow.figuresCount / 2) {
                figures.add(RandomFigureFactory.createCircle(figurePanel));
            } else {
                figures.add(RandomFigureFactory.createRectangle(figurePanel));
            }
        }
        repaint();
    }

    public void loadFigures(ArrayList<Figure> newFigures) {
        figures.clear();
        figures.addAll(newFigures);
        MainWindow.figuresCount = figures.size();
        repaint();
    }

    private void switchBtnsState(boolean state) {
        btnConnect.setEnabled(state);
        btnDisconnect.setEnabled(!state);
        btnUpload.setEnabled(!state);
        btnReceive.setEnabled(!state);
    }
}
