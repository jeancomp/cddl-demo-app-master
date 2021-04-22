package br.ufma.lsdi.digialphenotyping.datacontroller;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;
import android.util.Log;
import androidx.core.app.ActivityCompat;
import java.io.FileNotFoundException;
import java.util.List;
import br.ufma.lsdi.cddl.CDDL;
import br.ufma.lsdi.cddl.ConnectionFactory;
import br.ufma.lsdi.cddl.listeners.IConnectionListener;
import br.ufma.lsdi.cddl.network.ConnectionImpl;
import br.ufma.lsdi.cddl.network.SecurityService;
import br.ufma.lsdi.digialphenotyping.inferenceprocessormanager.services.InferenceProcessorManager;

public class DataControllerManager {
    CDDL cddl;
    private ConnectionImpl con;
    private Context context;
    private Activity activity;
    private static final String TAG = DataControllerManager.class.getName();

    private String nameCaCertificate = "rootCA.crt";
    private String nameClientCertificate = "client.crt";

    private String statusConnection = "";

    private void start(Context context){
        this.context = context;

        initPermissions();
        //initSecure();
        initSecureCDDL();
        // Mostra os sensores virtuais disponíveis
        listSensoresVirtuais();
        initVirtualSensor();
    }

    private synchronized void startService() {
        Intent cs = new Intent(getContext(), InferenceProcessorManager.class);
        getContext().startService(cs);
    }

    private synchronized void stopService() {
        Intent cs = new Intent(getContext(), InferenceProcessorManager.class);
        context.stopService(cs);
    }

    private Context getContext() {
        return context;
    }

    private void setContext(Context context) {
        this.context= context;
    }

    private void initCDDL(){
        String host = CDDL.startMicroBroker();
        //val host = "broker.hivemq.com";
        con = ConnectionFactory.createConnection();
        con.setClientId("lcmuniz@gmail.com");
        con.setHost(host);
        con.addConnectionListener(connectionListener);
        con.connect();
        cddl = CDDL.getInstance();
        cddl.setConnection(con);
        cddl.setContext(getContext());
        cddl.startService();

        cddl.startCommunicationTechnology(CDDL.INTERNAL_TECHNOLOGY_VIRTUAL_ID);
    }

    private void initSecureCDDL(){
        //String host = CDDL.startMicroBroker();
        String host = CDDL.startSecureMicroBroker(getContext(), true);
        //val host = "broker.hivemq.com";
        con = ConnectionFactory.createConnection();
        con.setClientId("lcmuniz@gmail.com");
        con.setHost(host);
        con.addConnectionListener(connectionListener);
        //con.connect();
        con.secureConnect( getContext() );
        cddl = CDDL.getInstance();
        cddl.setConnection(con);
        cddl.setContext(getContext());
        cddl.startService();

        cddl.startCommunicationTechnology(CDDL.INTERNAL_TECHNOLOGY_VIRTUAL_ID);
    }

    private void initSecure(){
        // Android 9-10
        // Versão 26
        //Parte de segurança - Certificados Digitais
        // Senha da Chave privada: 123456
        SecurityService securityService = new SecurityService(getContext());
        //securityService.generateCSR("jean","LSDi","ufma","slz","ma","br");
        try {
            securityService.setCaCertificate(nameCaCertificate);
            securityService.setCertificate(nameClientCertificate);

            securityService.grantReadPermissionByCDDLTopic("lcmuniz@gmail.com", SecurityService.ALL_TOPICS);
            securityService.grantWritePermissionByCDDLTopic("lcmuniz@gmail.com", SecurityService.ALL_TOPICS);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    private IConnectionListener connectionListener = new IConnectionListener() {
        @Override
        public void onConnectionEstablished() {
            statusConnection = "Established connection.";
        }

        @Override
        public void onConnectionEstablishmentFailed() {
            statusConnection = "Failed connection.";
        }

        @Override
        public void onConnectionLost() {
            statusConnection = "Lost connection.";
        }

        @Override
        public void onDisconnectedNormally() {
            statusConnection = "A normal disconnect has occurred.";
        }

    };

    private String getStatusCon(){ return statusConnection; }

    private void setNameCaCertificate(String name){
        this.nameCaCertificate = name;
    }

    private String getNameCaCertificate(){ return nameCaCertificate; }

    private void setnameClientCertificate(String name){
        this.nameClientCertificate = name;
    }

    private String getNameClientCertificate(){ return nameClientCertificate;}

    private void listSensoresVirtuais(){
        List<String> s = cddl.getSensorVirtualList();
        Log.i(TAG,"#### Sensores virtuais disponíveis: \n");
        for(int i=0; i < s.size(); i++){
            Log.i(TAG,"#### " + s.get(i).toString());
        }
    }

    private void initVirtualSensor(){
        //Start sensores virtuais pelo nome e delay
        //cddl.startSensor("SMS",0);
        cddl.startSensor("Call",0);
        //cddl.startSensor("ScreenOnOff",0);

        //checkDrawOverlayPermission();        // Solicita permissão de desenhar (canDrawOverlays) para Toque de Tela
        //cddl.startSensor("TouchScreen", 0);
    }

    private void setActivity(Activity a){
        activity = a;
    }

    private Activity getActivity(){
        return activity;
    }

    private void checkDrawOverlayPermission() {
        Log.i(TAG, "#### permissao");
        // check if we already  have permission to draw over other apps
        if (Build.VERSION.SDK_INT >= 23) {
            if (!Settings.canDrawOverlays(getContext()) ){
                // if not construct intent to request permission
                Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                        Uri.parse("package:" + getContext().getPackageName()));
                //ac.startService(intent);
                // request permission via start activity for result
                Log.i(TAG, "#### permissao dada pelo usuário");

                getActivity().startActivityForResult(intent, 1);
            }
        }
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

    private void initPermissions() {
        // Checa as permissões para rodar os sensores virtuais
        int PERMISSION_ALL = 1;
        String[] PERMISSIONS = {
                // SMS
                android.Manifest.permission.SEND_SMS,
                android.Manifest.permission.RECEIVE_SMS,
                android.Manifest.permission.READ_SMS,

                //Call
                android.Manifest.permission.READ_PHONE_STATE,
                android.Manifest.permission.CALL_PHONE,
                android.Manifest.permission.READ_CALL_LOG,
                android.Manifest.permission.WRITE_CALL_LOG,
                android.Manifest.permission.ADD_VOICEMAIL,

                // SMS saída
                android.Manifest.permission.READ_EXTERNAL_STORAGE,

                // Escrita no storage Certificado Digital
                android.Manifest.permission.WRITE_EXTERNAL_STORAGE,

                // Para usar o GPS
                android.Manifest.permission.ACCESS_FINE_LOCATION
        };

        if (!hasPermissions(getActivity(), PERMISSIONS)) {
            Log.i(TAG,"##### Permissão Ativada");
            ActivityCompat.requestPermissions(getActivity(), PERMISSIONS, PERMISSION_ALL);
        }
    }
}
