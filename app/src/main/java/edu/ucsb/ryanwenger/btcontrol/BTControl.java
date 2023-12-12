package edu.ucsb.ryanwenger.btcontrol;

import android.app.Application;
import android.content.Context;

import java.util.UUID;

public class BTControl extends Application {
    private static BTControl instance;
    private static UUID myUUID;

    @Override
    public void onCreate() {
        instance = this;
        myUUID = new UUID(36456456, 565436);
        super.onCreate();
    }

    public static BTControl getInstance() {
        return instance;
    }

    public static Context getContext(){
        return instance.getApplicationContext();
    }

    public static UUID getUUID() {
        return myUUID;
    }
}
