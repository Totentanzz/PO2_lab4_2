package app.web;

import app.utils.Utils;
import app.view.MainWindow;
import retrofit2.Call;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.scalars.ScalarsConverterFactory;

import javax.swing.*;
import java.io.IOException;

public class Client {
    private final String serverAddress = "http://localhost";
    private int serverPort;
    private FigureAPI restApi;

    private String clientName = null;
    private boolean isRegistered = false;

    public Client(int port) {
       this.serverPort = port;

       Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(serverAddress + ":" + serverPort)
                .addConverterFactory(ScalarsConverterFactory.create())
                .build();
       restApi = retrofit.create(FigureAPI.class);
    }

    public String getClientName() {
        return clientName;
    }

    public boolean isRegistered() {
        return isRegistered;
    }

    public void executeRegistration() {
        Call<String> call = restApi.registerClient();
        try {
            Response<String> response = call.execute();
            if (response.isSuccessful()) {
                clientName = response.body();
                isRegistered = true;
                Utils.showMessage(MainWindow.getInstance(),
                        "Registered successfully. Client name: " + clientName,
                        "Success",
                        JOptionPane.PLAIN_MESSAGE);
            } else {
                Utils.showMessage(MainWindow.getInstance(),
                        "Failed to register. Error:\n" + response.errorBody().string(),
                        "Error",
                        JOptionPane.PLAIN_MESSAGE);
            }
        } catch (IOException exc) {
            Utils.showMessage(MainWindow.getInstance(), exc.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    public void uploadFigures(String clientName, String xml) {
        Call<Void> call = restApi.createOrReplaceShape(clientName, xml);
        try {
            Response<Void> response = call.execute();
            if (response.isSuccessful()) {
                String message = "XML string with graphical objects was successfully uploaded";
                Utils.showMessage(MainWindow.getInstance(),
                        message,
                        "Success",
                        JOptionPane.PLAIN_MESSAGE);
            } else {
                String message = "Failed to upload graphical objects with xml string for client " +
                        clientName + ". Error:\n" + response.errorBody().string();
                Utils.showMessage(MainWindow.getInstance(),
                        message,
                        "Error",
                        JOptionPane.ERROR_MESSAGE);
            }
        } catch (IOException exc) {
            Utils.showMessage(MainWindow.getInstance(), exc.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    public String downloadFigures(String clientName) {
        Call<String> call = restApi.getShape(clientName);
        try {
            Response<String> response = call.execute();
            if (response.isSuccessful()) {
                return response.body();
            } else {
                Utils.showMessage(MainWindow.getInstance(),
                        "Can't download XML string. Error: " + response.errorBody().string(),
                        "Error",
                        JOptionPane.ERROR_MESSAGE);
                return null;
            }
        } catch (IOException exc) {
            Utils.showMessage(MainWindow.getInstance(), exc.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            return null;
        }
    }

    public void deleteClientInfo(boolean showMessageWindow) {
        Call<Void> call = restApi.deleteClient(clientName);
        try {
            Response<Void> response = call.execute();
            if (response.isSuccessful() && showMessageWindow) {
                Utils.showMessage(MainWindow.getInstance(),
                        "Figures were successfully deleted for client " + clientName,
                        "Success",
                        JOptionPane.PLAIN_MESSAGE);
            } else if (showMessageWindow) {
                Utils.showMessage(MainWindow.getInstance(),
                        "Failed to delete shape for client " + clientName + ". Error:\n" + response.errorBody().string(),
                        "Error",
                        JOptionPane.ERROR_MESSAGE);
            }
        } catch (IOException exc) {
            Utils.showMessage(MainWindow.getInstance(), exc.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

}
