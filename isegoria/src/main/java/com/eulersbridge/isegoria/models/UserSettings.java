package com.eulersbridge.isegoria.models;

/**
 * Created by Seb on 07/11/2017.
 */

public class UserSettings {

    public boolean trackingOff;
    public boolean optOutDataCollection;

    public UserSettings(boolean trackingOff, boolean optOutDataCollection) {
        this.trackingOff = trackingOff;
        this.optOutDataCollection = optOutDataCollection;
    }

}
