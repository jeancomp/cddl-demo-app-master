package br.ufma.lsdi.digitalphenotyping;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.util.Log;
import androidx.core.app.ActivityCompat;
import br.ufma.lsdi.digitalphenotyping.dataprovider.services.ContextDataProvider;
import br.ufma.lsdi.digitalphenotyping.processormanager.services.InferenceProcessorManager;

public class DigitalPhenotypingManager{
    public static final String ACTIVE_SENSOR = "activesensor";
    public static final String DEACTIVATE_SENSOR = "deactivatesensor";
    private String statusCon = "undefined";
    //private final BusSystem busSystem = BusSystem.getInstance();
    private static final String TAG = DigitalPhenotypingManager.class.getName();
    private static DigitalPhenotypingManager instance = null;
    //private StartBusSystem startBusSystem;

    Context context;
    Activity activity;
    String clientID;
    int communicationTechnology;
    Boolean secure;
    Bus myService;


    /**
     *
     */
    public DigitalPhenotypingManager(){ }


    /**
     *
     * @param context
     * @param activity
     * @param clientID
     * @param communicationTechnology
     * @param secure
     */
    public DigitalPhenotypingManager(Context context, Activity activity, String clientID, int communicationTechnology, Boolean secure){
        try {
            Log.i(TAG, "#### INICIANDO FRAMEWORK");
            this.context = context;
            this.activity = activity;
            this.clientID = clientID;
            this.communicationTechnology = communicationTechnology;
            this.secure = secure;

            DPApplication dpApplication = new DPApplication(activity);

            initPermissionsRequired();
        }catch (Exception e){
            Log.e(TAG,"Error: " + e.toString());
        }
    }


    public static DigitalPhenotypingManager getInstance() {
        if (instance == null) {
            instance = new DigitalPhenotypingManager();
        }
        return instance;
    }


    public void start(){
        startService();
    }


    public void stop(){
        // PARA O SERVICE PRIMEIRO PLANO
        myService.stopForeground(true);

        stopService();
    }


//    public void startBus(){
//        startBusSystem = new StartBusSystem(instance, this.activity);
//        startBusSystem.onStart();
//    }


    private synchronized void startService() {
        try{
            Log.i(TAG,"#### Starts all framework services.");
            Intent intent = new Intent(getActivity(), Bus.class);
            intent.putExtra("clientID",getClientID());
            intent.putExtra("communicationTechnology",4);
            intent.putExtra("secure", getSecure());

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                getActivity().startForegroundService(intent);
            }
            else {
                getActivity().startService(intent);
            }
            //Intent ipm = new Intent(getContext(), InferenceProcessorManager.class);
            //getActivity().startService(ipm);

            Intent cdp = new Intent(getContext(), ContextDataProvider.class);
            getActivity().startService(cdp);
        }catch (Exception e){
            Log.e(TAG, "#### Error: " + e.getMessage());
        }
    }


    private synchronized void stopService() {
        Log.i(TAG,"#### Stop all framework services.");
        try {
            Intent intent = new Intent(getContext(), Bus.class);
            getActivity().stopService(intent);

            Intent ipm = new Intent(getContext(), InferenceProcessorManager.class);
            getActivity().stopService(ipm);

            Intent cdp = new Intent(getContext(), ContextDataProvider.class);
            getActivity().stopService(cdp);
        }catch (Exception e){
            Log.e(TAG,e.getMessage());
        }
    }


    public void publishMessage(String service, String message){
        Log.i(TAG,"#### Publicando mensagens");
        myService.publishMessage(service, message);
    }


    public Context getContext() {
        return context;
    }


    public void setContext(Context context) {
        this.context= context;
    }


    public void setClientID(String clientID){
        this.clientID = clientID;
    }


    public String getClientID(){
        return clientID;
    }


    public void setActivity(Activity a){
        this.activity = a;
    }


    public Activity getActivity(){
        return activity;
    }


    public void setCommunicationTechnology(int communicationTechnology) {
        this.communicationTechnology = communicationTechnology;
    }


    public int getCommunicationTechnology() {
        return communicationTechnology;
    }


    public void setSecure(Boolean secure) {
        this.secure = secure;
    }

    public Boolean getSecure() {
        return this.secure;
    }


    public void setStatusCon(String statusCon){
        this.statusCon = statusCon;
    }


    public String getStatusCon(){
        return statusCon;
    }


    public void setBusSystem(Bus bus){
        this.myService = bus;
    }


    public Bus getBusSystem(){
        return myService;
    }


    private static boolean hasPermissions(Context context, String... permissions) {
        if (context != null && permissions != null) {
            for (String permission : permissions) {
                if (ActivityCompat.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED) {
                    return false;
                }
            }
        }
        return true;
    }


    private void initPermissionsRequired() {
        // Checa as permissões para rodar os sensores virtuais
        int PERMISSION_ALL = 1;

        if (true) {
            String[] PERMISSIONS = {
                    // Service location
                    android.Manifest.permission.ACCESS_FINE_LOCATION,
                    android.Manifest.permission.ACCESS_COARSE_LOCATION,
                    android.Manifest.permission.FOREGROUND_SERVICE

                    // Outros services
            };

            if (!hasPermissions(getActivity(), PERMISSIONS)) {
                Log.i(TAG, "##### Permission enabled for framework");
                ActivityCompat.requestPermissions(getActivity(), PERMISSIONS, PERMISSION_ALL);
            }
        }
    }

//    public class StartBusSystem {
//        private DigitalPhenotypingManager digitalP;
//        private Activity activity;
//
//        public StartBusSystem(DigitalPhenotypingManager dP, Activity act){
//            this.digitalP = dP;
//            this.activity = act;
//        }
//
//        protected void onStart() {
//            try{
//                Log.i(TAG,"#### TTTTTTTTTT ClientID: " + getClientID());
//                Log.i(TAG,"#### TTTTTTTTTT CommunicationTechnology: " + getCommunicationTechnology());
//                Intent intent = new Intent(activity, BusSystem.class);
//                intent.putExtra("clientID","l");
//                intent.putExtra("communicationTechnology",4);
//
//                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
//                    getActivity().bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE);
//                }
//                else {
//                    getActivity().startService(intent);
//                }
//            }catch (Exception e){
//                Log.e(TAG, "#### Error: " + e.getMessage());
//            }
//        }
//
//
//        protected void onStop() {
//            activity.unbindService(serviceConnection);
//        }
//
//        ServiceConnection serviceConnection = new ServiceConnection() {
//            @Override
//            public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
//                Log.i(TAG,"#### Connection service busSystem.");
//                BusSystem.LocalBinder binder = (BusSystem.LocalBinder) iBinder;
//                myService = binder.getService();
//
//                digitalP.getInstance().setBusSystem(myService);
//            }
//
//            @Override
//            public void onServiceDisconnected(ComponentName componentName) {
//                Log.i(TAG,"#### Disconnection service busSystem");
//            }
//        };
//    }
}
