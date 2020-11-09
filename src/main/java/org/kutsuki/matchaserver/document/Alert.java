package org.kutsuki.matchaserver.document;

public class Alert extends AbstractDocument {
    private String alert;

    public Alert(String alert) {
	this.alert = alert;
    }

    public String getAlert() {
	return alert;
    }

    public void setAlert(String alert) {
	this.alert = alert;
    }
}
