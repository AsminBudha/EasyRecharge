/*
 * Copyright (C) The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.app.easyrecharge;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.support.v4.app.ActivityCompat;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.util.SparseArray;
import android.widget.Toast;

import com.app.easyrecharge.ui.camera.GraphicOverlay;
import com.google.android.gms.vision.Detector;
import com.google.android.gms.vision.text.TextBlock;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

/**
 * A very simple Processor which gets detected TextBlocks and adds them to the overlay
 * as OcrGraphics.
 *
 */
public class OcrDetectorProcessor implements Detector.Processor<TextBlock> {

    private GraphicOverlay<OcrGraphic> mGraphicOverlay;
    private Context context;
    public static final int RequestCallPhonePermissionID = 1002;
    TelephonyManager telephonyManager;

    public OcrDetectorProcessor(GraphicOverlay<OcrGraphic> ocrGraphicOverlay, Context context) {
        mGraphicOverlay = ocrGraphicOverlay;
        this.context = context;
        telephonyManager = (TelephonyManager) this.context.getSystemService(Context.TELEPHONY_SERVICE);

    }

    @Override
    public void release() {
        mGraphicOverlay.clear();
    }

    @Override
    public void receiveDetections(Detector.Detections<TextBlock> detections) {
        mGraphicOverlay.clear();
        SparseArray<TextBlock> items = detections.getDetectedItems();

        if (items.size() != 0) {
            StringBuilder stringBuilder = new StringBuilder();
            String pin;
            List<TextBlock> possiblePin=new ArrayList<TextBlock>();
            for (int i = 0; i < items.size(); ++i) {
                TextBlock item = items.valueAt(i);

                if (item != null && item.getValue() != null) {
                    pin = item.getValue();
                    boolean isPin = pin.charAt(0)!='1';
                    pin = pin.replace(" ", "");
                    //Log.d("Max value of double", "run: "+Double.MAX_VALUE);
                    if (pin.length() == 16 && isPin) {

                        try {
                            Double.valueOf(pin);
                            possiblePin.add(item);

                            OcrGraphic graphic = new OcrGraphic(mGraphicOverlay, item);
                            mGraphicOverlay.add(graphic);
                            processSim(pin);

                        } catch (Exception ex) {
                            Log.d("Integer Parse Exception", "run: " + ex);
                        }
                    }
                }

            }
//            if (possiblePin.size()!=0){
//
//                TextBlock initText=possiblePin.get(0);
//                pin=initText.getValue();
//                for(int i=1;i<possiblePin.size();i++){
//                    if(possiblePin.get(i).getBoundingBox().top>initText.getBoundingBox().top){
//                        initText=possiblePin.get(i);
//                        pin=initText.getValue();
//                    }
//                }
//                OcrGraphic graphic = new OcrGraphic(mGraphicOverlay, initText);
//                mGraphicOverlay.add(graphic);
//                processSim(pin);
//
//            }

        }

    }

    public void processSim(final String pin) {
        try {
            boolean hasTwoSim = hasTwoActiveSims(context);
            if (hasTwoSim) {

                OcrCaptureActivity ocr=(OcrCaptureActivity)context;
                ocr.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        ((OcrCaptureActivity) context).chooseNetworkProvider(pin);
                    }
                });
            } else {
                String simCode = telephonyManager.getSimOperator();
                if (simCode.equals(context.getResources().getString(R.string.ncell))) {
                    requestRecharge(R.string.ncell, pin);
                } else if (simCode.equals(context.getResources().getString(R.string.ntc))) {
                    requestRecharge(R.string.ntc, pin);
                }
            }
            Toast.makeText(context, pin, Toast.LENGTH_SHORT).show();
            Log.d("Scanned Text:", pin);
        } catch (Exception ex) {
            Log.d("Exception", "processSim: "+ex);
            Log.d("Scanned Text", "run: " + pin);
        }
        this.release();
    }

    public void requestRecharge(int networkProvider, String pin) {

        Intent callIntent = new Intent();
        callIntent.setAction(Intent.ACTION_CALL);
        
        switch (networkProvider) {
            case R.string.ncell:
                callIntent.setData(Uri.parse("tel:" + Uri.encode("*902*"+pin+"#")));

                break;
            case R.string.ntc:
                callIntent.setData(Uri.parse("tel:" + Uri.encode("*412*"+pin+"#")));

                break;
            default:
                Toast.makeText(context, "Could not found network provider", Toast.LENGTH_SHORT).show();
                break;
        }

        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {

            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }

        context.startActivity(callIntent);

    }

    private static String[] simStatusMethodNames = {"getSimStateGemini", "getSimState"};


    public static boolean hasTwoActiveSims(Context context) {

        boolean first = false, second = false;

        for (String methodName : simStatusMethodNames) {
            // try with sim 0 first
            try {
                first = getSIMStateBySlot(context, methodName, 0);
                // no exception thrown, means method exists
                second = getSIMStateBySlot(context, methodName, 1);
                return first && second;
            } catch (GeminiMethodNotFoundException e) {
                // method does not exist, nothing to do but test the next
            }
        }
        return false;
    }

    private static boolean getSIMStateBySlot(Context context, String predictedMethodName, int slotID) throws GeminiMethodNotFoundException {

        boolean isReady = false;

        TelephonyManager telephony = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);

        try {

            Class<?> telephonyClass = Class.forName(telephony.getClass().getName());

            Class<?>[] parameter = new Class[1];
            parameter[0] = int.class;
            Method getSimStateGemini = telephonyClass.getMethod(predictedMethodName, parameter);

            Object[] obParameter = new Object[1];
            obParameter[0] = slotID;
            Object ob_phone = getSimStateGemini.invoke(telephony, obParameter);

            if (ob_phone != null) {
                int simState = Integer.parseInt(ob_phone.toString());
                if (simState == TelephonyManager.SIM_STATE_READY) {
                    isReady = true;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw new GeminiMethodNotFoundException(predictedMethodName);
        }

        return isReady;
    }

    private static class GeminiMethodNotFoundException extends Exception {

        private static final long serialVersionUID = -996812356902545308L;

        public GeminiMethodNotFoundException(String info) {
            super(info);
        }
    }

}
