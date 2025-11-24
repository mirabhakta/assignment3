package model;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;

// Holds the current session state of the application
// Stores text, mode, and status. Notifies listeners (UI) when changes occur

public class SessionModel {
    private final PropertyChangeSupport pcs = new PropertyChangeSupport(this);

    private boolean loading = false;
    private String responseText = "";
    private String errorMessage = "";
    private WritingMode mode = WritingMode.PROFESSIONAL;

    // Listener registration methods
    public void addPropertyChangeListener(PropertyChangeListener l) { pcs.addPropertyChangeListener(l); }
    public void removePropertyChangeListener(PropertyChangeListener l) { pcs.removePropertyChangeListener(l); }

    // Getters for UI or controller to access session state
    public boolean isLoading() { return loading; }
    public String getResponseText() { return responseText; }
    public String getErrorMessage() { return errorMessage; }
    public WritingMode getMode() { return mode; }


    // Setters notify listeners when values change
    public void setLoading(boolean newVal) {
        boolean old = this.loading;
        this.loading = newVal;
        pcs.firePropertyChange("loading", old, newVal);
    }
    public void setResponseText(String newVal) {
        String old = this.responseText;
        this.responseText = newVal;
        pcs.firePropertyChange("responseText", old, newVal);
    }
    public void setErrorMessage(String newVal) {
        String old = this.errorMessage;
        this.errorMessage = newVal;
        pcs.firePropertyChange("errorMessage", old, newVal);
    }
    public void setMode(WritingMode newVal) {
        WritingMode old = this.mode;
        this.mode = newVal;
        pcs.firePropertyChange("mode", old, newVal);
    }
}
