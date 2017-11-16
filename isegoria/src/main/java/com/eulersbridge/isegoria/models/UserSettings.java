package com.eulersbridge.isegoria.models;

@SuppressWarnings("CanBeFinal")
public class UserSettings {

    public boolean trackingOff;
    public boolean optOutDataCollection;

    public UserSettings(boolean trackingOff, boolean optOutDataCollection) {
        this.trackingOff = trackingOff;
        this.optOutDataCollection = optOutDataCollection;
    }

}
