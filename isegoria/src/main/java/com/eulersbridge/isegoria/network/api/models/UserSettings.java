package com.eulersbridge.isegoria.network.api.models;

@SuppressWarnings({"CanBeFinal", "WeakerAccess"})
public class UserSettings {

    public boolean trackingOff;
    public boolean optOutDataCollection;

    public UserSettings(boolean trackingOff, boolean optOutDataCollection) {
        this.trackingOff = trackingOff;
        this.optOutDataCollection = optOutDataCollection;
    }

}
