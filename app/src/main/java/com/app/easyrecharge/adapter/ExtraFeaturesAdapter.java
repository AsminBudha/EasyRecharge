package com.app.easyrecharge.adapter;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.support.v4.app.ActivityCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.app.easyrecharge.OcrCaptureActivity;
import com.app.easyrecharge.R;
import com.app.easyrecharge.model.ExtraFeatures;

import java.util.List;

/**
 * Created by Ashmin on 3/30/2017.
 */
public class ExtraFeaturesAdapter extends RecyclerView.Adapter<ExtraFeaturesAdapter.ExtraFeaturesViewHolder> {
    private List<ExtraFeatures> dataList;
    private LayoutInflater layoutInflater;
    private Context context;

    public ExtraFeaturesAdapter(List<ExtraFeatures> dataList, Context context) {
        this.dataList = dataList;
        this.layoutInflater = LayoutInflater.from(context);
        this.context = context;
    }

    @Override
    public ExtraFeaturesViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = layoutInflater.inflate(R.layout.feature_list_item, parent, false);
        ExtraFeaturesViewHolder extraFeaturesViewHolder = new ExtraFeaturesViewHolder(view);
        return extraFeaturesViewHolder;
    }

    @Override
    public void onBindViewHolder(ExtraFeaturesViewHolder holder, int position) {
        ExtraFeatures extraFeatures = dataList.get(position);
        holder.setData(extraFeatures, position);
    }

    @Override
    public int getItemCount() {
        return dataList.size();
    }

    public void request(String pin) {

        Intent callIntent = new Intent();
        callIntent.setAction(Intent.ACTION_CALL);
        callIntent.setData(Uri.parse("tel:" + Uri.encode(pin)));

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

    class ExtraFeaturesViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        private TextView textView;
        private ImageView imageView;
        private int position;
        private ExtraFeatures extraFeatures;
        private LinearLayout linearLayout;

        public ExtraFeaturesViewHolder(View itemView) {
            super(itemView);
            textView = (TextView) itemView.findViewById(R.id.textView);
            imageView = (ImageView) itemView.findViewById(R.id.imageView);
            linearLayout = (LinearLayout) itemView.findViewById(R.id.extra_features_list_item);
            linearLayout.setOnClickListener(ExtraFeaturesViewHolder.this);
        }

        public void setData(ExtraFeatures extraFeatures, int position) {
            imageView.setImageResource(extraFeatures.getImageId());
            textView.setText(extraFeatures.getTitle());
            this.position = position;
            this.extraFeatures = extraFeatures;

        }

        @Override
        public void onClick(View view) {
            int id = view.getId();
            if (id == R.id.extra_features_list_item) {
                OcrCaptureActivity ocr = (OcrCaptureActivity) context;
                switch (position) {
                    case 0:
                        ocr.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                ((OcrCaptureActivity) context).chooseNetworkProvider(context.getResources().getString(R.string.check_balance));
                            }
                        });
                        break;
                    case 1:
                        ocr.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                ((OcrCaptureActivity) context).chooseNetworkProvider(context.getResources().getString(R.string.call_center));
                            }
                        });
                        break;
                    default:
                        break;

                }
            }
        }
    }
}
