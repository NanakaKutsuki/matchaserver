package org.kutsuki.matchaserver.document;

public class Alert extends AbstractDocument {
    private String alertId;

    public String getAlertId() {
	return alertId;
    }

    public void setAlertId(String alertId) {
	this.alertId = alertId;
    }
}
