package kg.skureganov.netquality;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.telephony.CellIdentityGsm;
import android.telephony.CellIdentityLte;
import android.telephony.CellIdentityWcdma;
import android.telephony.CellInfo;
import android.telephony.CellInfoGsm;
import android.telephony.CellInfoLte;
import android.telephony.CellInfoWcdma;
import android.telephony.CellSignalStrengthGsm;
import android.telephony.CellSignalStrengthLte;
import android.telephony.CellSignalStrengthWcdma;
import android.telephony.PhoneStateListener;
import android.telephony.SignalStrength;
import android.telephony.TelephonyManager;
import android.telephony.gsm.GsmCellLocation;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;



import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.List;
import java.util.Locale;

import fr.bmartel.speedtest.SpeedTestReport;
import fr.bmartel.speedtest.SpeedTestSocket;
import fr.bmartel.speedtest.inter.ISpeedTestListener;
import fr.bmartel.speedtest.model.SpeedTestError;

public class MainActivity extends AppCompatActivity {


    private static final int REQUEST_CODE_PERMISSION_ACCESS_LOCATION = 22;
    private static final int REQUEST_CODE_PERMISSION_READ_PHONE_STATE = 33;
    private static final int REQUEST_CODE = 44;
    private static final String O_OPERATOR_ID = "43709";
    private static final int O_OPERANOR_MNC = 9;


    private SimInfo simInfo;
    private TelephonyManager telephonyManager;
    private GsmCellLocation gsmCellLocation;
    private MyPhoneStateListener myPhoneStateListener;
    private LocationManager locationManager;
    private LocationListener locationListener;
    private Location location;
    private int mSignalStrength;
    private List<CellInfo> allCells;
    private Handler handler;


    private Button startSpeedTestBtn;
    private TextView tvImeiValue, tvOperatorIdValue, tvNetworkTypeValue, tvCidValue, tvLacValue,
            tvSignalValue, tvLatitudeValue, tvLongitudeValue, tvDownLoadSpeedValue, tvUploadSpeedValue;

    private int oOperator = -1;
    private int anotherOperator = -1;
    private int oOperatorSimSlot = -1;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        telephonyManager = (TelephonyManager) this.getSystemService(Context.TELEPHONY_SERVICE);
        initViews();

        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);


//        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED &&
//                ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
//            ActivityCompat.requestPermissions(this,
//                    new String[]{Manifest.permission.READ_PHONE_STATE,
//                            Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_CODE);
//        } else {
//
//            allCells = telephonyManager.getAllCellInfo();
//            checkOSimCard();
//
//        }


    }


    @Override
    protected void onStart() {
        super.onStart();

//        if (oOperator == -1) {
//            Toast.makeText(getApplicationContext(), R.string.noSimCard, Toast.LENGTH_LONG).show();
//        } else {
//            final SimInfo simInfo = getSimInfo();
//
//
//            locationListener = new LocationListener() {
//                @Override
//                public void onLocationChanged(Location location) {
//                    simInfo.setLatitude(location.getLatitude());
//                    simInfo.setLongitude(location.getLongitude());
//                    setDataOnViews(simInfo);
//                }
//
//                @Override
//                public void onStatusChanged(String provider, int status, Bundle extras) {
//
//                }
//
//                @Override
//                public void onProviderEnabled(String provider) {
//                    Toast.makeText(getApplicationContext(), provider.toString(), Toast.LENGTH_LONG).show();
//                }
//
//                @Override
//                public void onProviderDisabled(String provider) {
//                    if (ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
//                            ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
//
//                    }
//
//                    Toast.makeText(getApplicationContext(), provider.toString(), Toast.LENGTH_LONG).show();
//                }
//            };
//            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 5000, 0, locationListener);
//
//
//            setDataOnViews(simInfo);
//        }


    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == REQUEST_CODE) {

            if ((grantResults.length > 0) && (grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

                }
                allCells = telephonyManager.getAllCellInfo();
                checkOSimCard();

                if (oOperator == -1) {
                    Toast.makeText(getApplicationContext(), R.string.noSimCard, Toast.LENGTH_LONG).show();
                } else {
                    final SimInfo simInfo = getSimInfo();


                    locationListener = new LocationListener() {
                        @Override
                        public void onLocationChanged(Location location) {
                            simInfo.setLatitude(location.getLatitude());
                            simInfo.setLongitude(location.getLongitude());
                            setDataOnViews(simInfo);
                        }

                        @Override
                        public void onStatusChanged(String provider, int status, Bundle extras) {

                        }

                        @Override
                        public void onProviderEnabled(String provider) {
                            Toast.makeText(getApplicationContext(), provider.toString(), Toast.LENGTH_LONG).show();
                        }

                        @Override
                        public void onProviderDisabled(String provider) {

                            Toast.makeText(getApplicationContext(), provider.toString(), Toast.LENGTH_LONG).show();
                        }
                    };
                    locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 5000, 0, locationListener);



                    setDataOnViews(simInfo);
                }
            }


        }


    }


    private Location getLastKnownLocation() {
        List<String> providers = locationManager.getProviders(true);
        Location bestLocation = null;
        for (String provider : providers) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {


            }
            Location l = locationManager.getLastKnownLocation(provider);
            if (l == null) {
                continue;
            }
            if (bestLocation == null || l.getAccuracy() < bestLocation.getAccuracy()) {
                // Found best last known location: %s", l);
                bestLocation = l;
            }
        }
        return bestLocation;
    }

    private void setDataOnViews( SimInfo simInfo) {

        myPhoneStateListener = new MyPhoneStateListener();
        telephonyManager.listen(myPhoneStateListener, PhoneStateListener.LISTEN_SIGNAL_STRENGTHS);
        tvImeiValue.setText(simInfo.getImei());
        tvOperatorIdValue.setText(String.format("%d0%d", simInfo.getMcc(), simInfo.getMnc()));
        tvNetworkTypeValue.setText(simInfo.getNetworkType());
        tvCidValue.setText(String.valueOf(simInfo.getCid()));
        tvLacValue.setText(String.valueOf(simInfo.getLac()));
        tvLatitudeValue.setText(String.valueOf(simInfo.getLatitude()));
        tvLongitudeValue.setText(String.valueOf(simInfo.getLongitude()));

    }

    private SimInfo getSimInfo() {
        SimInfo simInfo = new SimInfo();

        if (allCells.get(oOperator) instanceof CellInfoGsm) {
            if (allCells.get(oOperator).isRegistered()) {
                final CellIdentityGsm gsm = ((CellInfoGsm) allCells.get(oOperator)).getCellIdentity();
                final CellSignalStrengthGsm signal = ((CellInfoGsm) allCells.get(oOperator)).getCellSignalStrength();

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
                        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_PHONE_STATE}, REQUEST_CODE);
                    }else{
                        simInfo.setImei(telephonyManager.getImei(oOperatorSimSlot));
                    }
                }else simInfo.setImei(telephonyManager.getDeviceId(oOperatorSimSlot));

                simInfo.setNetworkType("2G");
                simInfo.setMcc(gsm.getMcc());
                simInfo.setMnc(gsm.getMnc());
                simInfo.setCid(gsm.getCid());
                simInfo.setLac(gsm.getLac());
                simInfo.setSignalStrength(signal.getDbm());

            }
        }

        if (allCells.get(oOperator) instanceof CellInfoWcdma) {
            if (allCells.get(oOperator).isRegistered()) {
                final CellIdentityWcdma wcdma = ((CellInfoWcdma) allCells.get(oOperator)).getCellIdentity();
                final CellSignalStrengthWcdma signal = ((CellInfoWcdma) allCells.get(oOperator)).getCellSignalStrength();

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
                        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_PHONE_STATE}, REQUEST_CODE);
                    }else{
                        simInfo.setImei(telephonyManager.getImei(oOperatorSimSlot));
                    }
                }else simInfo.setImei(telephonyManager.getDeviceId(oOperatorSimSlot));

                simInfo.setNetworkType("3G");
                simInfo.setMcc(wcdma.getMcc());
                simInfo.setMnc(wcdma.getMnc());
                simInfo.setCid(wcdma.getCid());
                simInfo.setLac(wcdma.getLac());
                simInfo.setSignalStrength(signal.getDbm());

            }
        }

        if (allCells.get(oOperator) instanceof CellInfoLte) {
            if(allCells.get(oOperator).isRegistered()){
                final CellIdentityLte lte = ((CellInfoLte) allCells.get(oOperator)).getCellIdentity();
                final CellSignalStrengthLte signal = ((CellInfoLte) allCells.get(oOperator)).getCellSignalStrength();

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
                        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_PHONE_STATE}, REQUEST_CODE);
                    }else{
                        simInfo.setImei(telephonyManager.getImei(oOperatorSimSlot));
                    }
                }else simInfo.setImei(telephonyManager.getDeviceId(oOperatorSimSlot));

                simInfo.setNetworkType("4G");
                simInfo.setMcc(lte.getMcc());
                simInfo.setMnc(lte.getMnc());
                simInfo.setCid(lte.getCi());
                simInfo.setLac(lte.getTac());
                simInfo.setSignalStrength(signal.getDbm());

            }
        }
        return simInfo;
    }

    private void checkOSimCard() {
        for (int i = 0; i < allCells.size(); i++){
            if (allCells.get(i) instanceof CellInfoGsm) {
                if (allCells.get(i).isRegistered()){
                    final CellIdentityGsm gsm = ((CellInfoGsm) allCells.get(i)).getCellIdentity();
                    if (gsm.getMnc() == 9){
                        oOperator = i;
                    }else{
                        anotherOperator = i;
                    }
                }
            }
            if (allCells.get(i) instanceof CellInfoWcdma) {
                if (allCells.get(i).isRegistered()) {
                    final CellIdentityWcdma wcdma = ((CellInfoWcdma) allCells.get(i)).getCellIdentity();
                    if (wcdma.getMnc() == 9){
                        oOperator = i;
                    }else{
                        anotherOperator = i;
                    }
                }
            }
            if (allCells.get(i) instanceof CellInfoLte) {
                if(allCells.get(i).isRegistered()){
                    final CellIdentityLte lte = ((CellInfoLte) allCells.get(i)).getCellIdentity();
                    if (lte.getMnc() == 9){
                        oOperator = i;
                    }else{
                        anotherOperator = i;
                    }
                }
            }
        }
        if (oOperator != -1){
            if (oOperator < anotherOperator){
                oOperatorSimSlot = 0;
            }else{oOperatorSimSlot = 1;}
        }
    }




    private String getNetworkType(int value){
        String type;
        switch (value){
            case 0 : type = "UNKNOWN"; break;
            case 1 : type = "GPRS"; break;
            case 2 : type = "EDGE"; break;
            case 3 : type = "UMTS"; break;
            case 4 : type = "CDMA"; break;
            case 5 : type = "EVDO_0"; break;
            case 6 : type = "EVDO_A"; break;
            case 7 : type = "1xRTT"; break;
            case 8 : type = "HSDPA"; break;
            case 9 : type = "HSUPA"; break;
            case 10 : type = "HSPA"; break;
            case 11 : type = "IDEN"; break;
            case 12 : type = "EVDO_B"; break;
            case 13 : type = "LTE"; break;
            case 14 : type = "EHRPD"; break;
            case 15 : type = "HSPAP"; break;
            case 16 : type = "GSM"; break;
            case 17 : type = "TD_SCDMA"; break;
            case 18 : type = "IWLAN"; break;
            case 19 : type = "LTE_CA"; break;
            default:return "UNKNOWN";
        }

        return type;
    }



    private void initViews() {
        tvImeiValue = findViewById(R.id.tvImeiValue);
        tvOperatorIdValue = findViewById(R.id.tvOperatorIdValue);
        tvNetworkTypeValue = findViewById(R.id.tvNetworkTypeValue);
        tvCidValue = findViewById(R.id.tvCidValue);
        tvLacValue = findViewById(R.id.tvLacValue);
        tvSignalValue = findViewById(R.id.tvSignalValue);
        tvLatitudeValue = findViewById(R.id.tvLatitudeValue);
        tvLongitudeValue = findViewById(R.id.tvLongitudeValue);
        tvDownLoadSpeedValue = findViewById(R.id.tvDownaloadSpeedValue);
        tvUploadSpeedValue = findViewById(R.id.tvUploadSpeedValue);
        tvDownLoadSpeedValue.setText("0:00");
        tvUploadSpeedValue.setText("0:00");
        startSpeedTestBtn = findViewById(R.id.btnStartSpeedTest);
        startSpeedTestBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                handler = new Handler();
                SpeedTestThread speedTestThread = new SpeedTestThread();
                speedTestThread.start();

            }
        });

    }





    class MyPhoneStateListener extends PhoneStateListener {

        @Override
        public void onSignalStrengthsChanged(SignalStrength signalStrength) {
            super.onSignalStrengthsChanged(signalStrength);
            mSignalStrength = signalStrength.getGsmSignalStrength();
            mSignalStrength = (2 * mSignalStrength) - 113; // -> dBm
            tvSignalValue.setText(String.format("%d dBm",mSignalStrength));
        }
    }

    class SpeedTestThread extends Thread{
        private double progressDownloadValue = 0;
        private double completedDownloadValue = 0;
        private double progressUploadValue = 0;
        private double completedUploadValue = 0;



        public void setProgressDownloadValue(double progressDownloadValue) {
            this.progressDownloadValue = progressDownloadValue;
        }

        public void setCompletedDownloadValue(double completedDownloadValue) {
            this.completedDownloadValue = completedDownloadValue;
        }

        public void setProgressUploadValue(double progressUploadValue) {
            this.progressUploadValue = progressUploadValue;
        }

        public void setCompletedUploadValue(double completedUploadValue) {
            this.completedUploadValue = completedUploadValue;
        }

        public double getProgressDownloadValue() {

            return progressDownloadValue;
        }

        public double getCompletedDownloadValue() {
            return completedDownloadValue;
        }

        public double getProgressUploadValue() {
            return progressUploadValue;
        }

        public double getCompletedUploadValue() {
            return completedUploadValue;
        }

        SpeedTestThread() {

        }


        @Override
        public void run() {

            startDownload();
            try {
                sleep(12000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            startUpload();
        }

        private synchronized void startDownload() {

            SpeedTestSocket speedTestSocket = new SpeedTestSocket();

            // add a listener to wait for speedtest completion and progress
            speedTestSocket.addSpeedTestListener(new ISpeedTestListener() {

                @Override
                public void onCompletion(final SpeedTestReport report) {
                    // called when download/upload is finished
                    Log.v("speedtest", "[COMPLETED DOWNLOAD] rate in octet/s : " + report.getTransferRateOctet());
                    Log.v("speedtest", "[COMPLETED DOWNLOAD] rate in bit/s   : " + report.getTransferRateBit());
                    setCompletedDownloadValue(report.getTransferRateBit().doubleValue());
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            DecimalFormatSymbols separator = new DecimalFormatSymbols(Locale.getDefault());
                            separator.setDecimalSeparator('.');
                            String value = new DecimalFormat("#0.00", separator)
                                    .format(getCompletedDownloadValue()/8/1024/1024*8);
                            tvDownLoadSpeedValue.setText(value);
                        }
                    });


                }

                @Override
                public void onError(SpeedTestError speedTestError, String errorMessage) {
                    // called when a download/upload error occur
                }

                @Override
                public void onProgress(float percent, final SpeedTestReport report) {
                    // called to notify download/upload progress
                    Log.v("speedtest", "[PROGRESS DOWNLOAD] progress : " + percent + "%");
                    Log.v("speedtest", "[PROGRESS DOWNLOAD] rate in octet/s : " + report.getTransferRateOctet());
                    Log.v("speedtest", "[PROGRESS DOWNLOAD] rate in bit/s   : " + report.getTransferRateBit());

                    setProgressDownloadValue(report.getTransferRateBit().doubleValue());
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            startSpeedTestBtn.setEnabled(false);
                            DecimalFormatSymbols separator = new DecimalFormatSymbols(Locale.getDefault());
                            separator.setDecimalSeparator('.');
                            String value = new DecimalFormat("#0.00", separator)
                                    .format(getProgressDownloadValue()/8/1024/1024*8);
                            tvDownLoadSpeedValue.setText(value);
                        }
                    });
                }
            });

            speedTestSocket.startFixedDownload("http://ipv4.ikoula.testdebit.info/100M.iso", 10000);

        }

        private synchronized void  startUpload(){
            SpeedTestSocket speedTestSocket = new SpeedTestSocket();

            // add a listener to wait for speedtest completion and progress
            speedTestSocket.addSpeedTestListener(new ISpeedTestListener() {

                @Override
                public void onCompletion(SpeedTestReport report) {
                    // called when download/upload is finished
                    Log.v("speedtest", "[COMPLETED UPLOAD] rate in octet/s : " + report.getTransferRateOctet());
                    Log.v("speedtest", "[COMPLETED UPLOAD] rate in bit/s   : " + report.getTransferRateBit());

                    setCompletedUploadValue(report.getTransferRateBit().doubleValue());
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            startSpeedTestBtn.setEnabled(true);
                            DecimalFormatSymbols separator = new DecimalFormatSymbols(Locale.getDefault());
                            separator.setDecimalSeparator('.');
                            String value = new DecimalFormat("#0.00", separator)
                                    .format(getCompletedUploadValue()/8/1024/1024*8);
                            tvUploadSpeedValue.setText(value);
                        }
                    });

                }

                @Override
                public void onError(SpeedTestError speedTestError, String errorMessage) {
                    // called when a download/upload error occur
                }

                @Override
                public void onProgress(float percent, SpeedTestReport report) {
                    // called to notify download/upload progress
                    Log.v("speedtest", "[PROGRESS UPLOAD] progress : " + percent + "%");
                    Log.v("speedtest", "[PROGRESS UPLOAD] rate in octet/s : " + report.getTransferRateOctet());
                    Log.v("speedtest", "[PROGRESS UPLOAD] rate in bit/s   : " + report.getTransferRateBit());

                    setProgressUploadValue(report.getTransferRateBit().doubleValue());
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            DecimalFormatSymbols separator = new DecimalFormatSymbols(Locale.getDefault());
                            separator.setDecimalSeparator('.');
                            String value = new DecimalFormat("#0.00", separator)
                                    .format(getProgressUploadValue()/8/1024/1024*8);
                            tvUploadSpeedValue.setText(value);
                        }
                    });
                }
            });

            speedTestSocket.startFixedUpload("http://ipv4.ikoula.testdebit.info/", 10000000, 10000);

        }
    }


}
