package com.dqsmart.example.zigbee;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.dqsmart.zigbee.core.device.ZigbeeDevice;
import com.dqsmart.zigbee.core.device.ZigbeeDeviceInfo;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class DeviceListAdapter extends RecyclerView.Adapter<DeviceListAdapter.DeviceViewHolder> {
    public interface OnDeviceListClickListener {
        void onDeviceStatusClick(ZigbeeDevice zigbeeDevice, int newStatus);
    }

    private int checkPosition;
    private String newDevName;

    public int getCheckPosition() {
        return checkPosition;
    }

    public void setCheckPosition(int checkPosition) {
        this.checkPosition = checkPosition;
    }

    public String getNewDevName() {
        return newDevName;
    }

    public void setNewDevName(String newDevName) {
        this.newDevName = newDevName;
    }

    Context mContext;

    public Context getmContext() {
        return mContext;
    }

    private List<ZigbeeDevice> mZigbeeDevices;
    private ConcurrentHashMap<String, Integer> mDevStatusMap;
    private ConcurrentHashMap<String, String> mDevNameMap;

    public void setmDevStatusMap(ConcurrentHashMap<String, Integer> mDevStatusMap) {
        this.mDevStatusMap = mDevStatusMap;
    }

    public void setmDevNameMap(ConcurrentHashMap<String, String> mDevNameMap) {
        this.mDevNameMap = mDevNameMap;
    }

    public ConcurrentHashMap<String, String> getmDevNameMap() {
        return mDevNameMap;
    }

    private OnDeviceListClickListener mListener;

    public List<ZigbeeDevice> getmZigbeeDevices() {
        return mZigbeeDevices;
    }

    public OnDeviceListClickListener getmListener() {
        return mListener;
    }

    public ConcurrentHashMap<String, Integer> getmDevStatusMap() {
        return mDevStatusMap;
    }

    public DeviceListAdapter(Context context) {
        mContext = context;
        mZigbeeDevices = new ArrayList<>();
        mDevStatusMap = new ConcurrentHashMap<>();
    }

    public void setOnDeviceListClickListener(OnDeviceListClickListener onDeviceListClickListener) {
        mListener = onDeviceListClickListener;
    }

    public void setZigbeeDevices(List<ZigbeeDevice> zigbeeDevices) {
        mZigbeeDevices.clear();
        if (zigbeeDevices != null) {
            mZigbeeDevices = new ArrayList<>(zigbeeDevices);
        }
        notifyDataSetChanged();
    }

    public void updateDevStatus(int shortAddress, int endpoint, int status) {
        mDevStatusMap.put(getDeviceKey(shortAddress, endpoint), status);
        notifyDataSetChanged();
    }

    private String getDeviceKey(int shortAddress, int endpoint) {
        return String.format("%04X", shortAddress) + "." + endpoint;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @NonNull
    @Override
    public DeviceViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.new_device_list_item, parent, false);
        return new DeviceViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull DeviceViewHolder holder, int position) {
        final ZigbeeDevice zigbeeDevice = mZigbeeDevices.get(position);

        if(this.mDevNameMap.containsKey(getDeviceKey(zigbeeDevice.getSrcAddress(), zigbeeDevice.getEndpoint()))){
            holder.devNameText.setText(this.mDevNameMap.get(getDeviceKey(zigbeeDevice.getSrcAddress(), zigbeeDevice.getEndpoint())));
        }
        else {
            holder.devNameText.setText("chua dat ten");
        }
//        if(this.newDevName != null){
////            Toast.makeText(mContext, "toi day", Toast.LENGTH_SHORT).show();
//            if(this.checkPosition == position){
////                Toast.makeText(mContext, "toi day nua", Toast.LENGTH_SHORT).show();
//                thisDeviceName = this.newDevName;
//                holder.devNameText.setText(thisDeviceName);
//            }
//        }
        holder.devModelIdText.setText(zigbeeDevice.getModelId());
//        holder.devMacAddressText.setText(String.format("%016X", zigbeeDevice.getMacAddress()));
//        holder.devSrcAddressText.setText(String.format("%04X", zigbeeDevice.getSrcAddress()));
//        holder.devEndpointText.setText(String.valueOf(zigbeeDevice.getEndpoint()));
//        holder.devStatusText.setVisibility(View.VISIBLE);
        if (zigbeeDevice.getDeviceType() == ZigbeeDeviceInfo.DEVICE_TYPE_LIGHT || zigbeeDevice.getDeviceType() == ZigbeeDeviceInfo.DEVICE_TYPE_SWITCH) {
            final int value = mDevStatusMap.get(getDeviceKey(zigbeeDevice.getSrcAddress(), zigbeeDevice.getEndpoint())) == null
                    ? 0 : mDevStatusMap.get(getDeviceKey(zigbeeDevice.getSrcAddress(), zigbeeDevice.getEndpoint()));
            Boolean isOn = value == 1;
            final String devStatus = (isOn != null && isOn) ?
                    mContext.getString(R.string.status_on) : mContext.getString(R.string.status_off);
            holder.devStatusText.setText(devStatus);
            String finalThisDeviceName = this.mDevNameMap.get(getDeviceKey(zigbeeDevice.getSrcAddress(), zigbeeDevice.getEndpoint()));
            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
//                    // Toggle status
//                    final int newStatus = (isOn != null && isOn) ? 0 : 1;
//                    if (mListener != null) {
//                        mListener.onDeviceStatusClick(zigbeeDevice, newStatus);
//                    }
//                    selectedPosition = holder.getAdapterPosition();
//                    notifyDataSetChanged();
                    Intent intent = new Intent(view.getContext(), ControllerActivity.class);
                    intent.putExtra("isOn", isOn);
                    intent.putExtra("position", holder.getAdapterPosition());
                    intent.putExtra("deviceName", finalThisDeviceName);
                    intent.putExtra("deviceKey",getDeviceKey(zigbeeDevice.getSrcAddress(), zigbeeDevice.getEndpoint()));
                    view.getContext().startActivity(intent);
                }
            });
        } else if(zigbeeDevice.getDeviceType() == ZigbeeDeviceInfo.DEVICE_TYPE_LOCK){
            final int value = mDevStatusMap.get(getDeviceKey(zigbeeDevice.getSrcAddress(), zigbeeDevice.getEndpoint())) == null
                    ? 0 : mDevStatusMap.get(getDeviceKey(zigbeeDevice.getSrcAddress(), zigbeeDevice.getEndpoint()));

            final String devStatus = (value == 1) ? "Lock" : "Unlock";
            holder.devStatusText.setText(devStatus);
            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
//                    // Toggle status
//                    final int newStatus = (value == 1) ? 1 : 0;
//                    if (mListener != null) {
//                        mListener.onDeviceStatusClick(zigbeeDevice, newStatus);
//                    }
                }
            });
        } else if(zigbeeDevice.getDeviceType() == ZigbeeDeviceInfo.DEVICE_TYPE_COVER){
            holder.devStatusText.setText("control");
            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
//                    // Toggle status
//                    if (mListener != null) {
//                        mListener.onDeviceStatusClick(zigbeeDevice, 0);
//                    }
                }
            });
        }
        else {
            final int value = mDevStatusMap.get(getDeviceKey(zigbeeDevice.getSrcAddress(), zigbeeDevice.getEndpoint())) == null
                    ? 0 : mDevStatusMap.get(getDeviceKey(zigbeeDevice.getSrcAddress(), zigbeeDevice.getEndpoint()));
            holder.devStatusText.setText(Integer.toString(value));
            holder.itemView.setOnClickListener(null);
        }

    }

    @Override
    public int getItemCount() {
        return mZigbeeDevices.size();
    }

    public static class DeviceViewHolder extends RecyclerView.ViewHolder {
        TextView devNameText;
        TextView devModelIdText;
        TextView devStatusText;

        public DeviceViewHolder(@NonNull View itemView){
            super(itemView);
            devNameText = itemView.findViewById(R.id.text_dev_name);
            devModelIdText = itemView.findViewById(R.id.text_dev_model_id);
            devStatusText = itemView.findViewById(R.id.text_dev_status);
        }

//        TextView devModelIdText;
//        TextView devMacAddressText;
//        TextView devSrcAddressText;
//        TextView devEndpointText;
//        TextView devStatusText;
//
//        public DeviceViewHolder(@NonNull View itemView) {
//            super(itemView);
//            devModelIdText = itemView.findViewById(R.id.text_dev_model_id);
//            devMacAddressText = itemView.findViewById(R.id.text_dev_mac_address);
//            devSrcAddressText = itemView.findViewById(R.id.text_dev_src_address);
//            devEndpointText = itemView.findViewById(R.id.text_dev_endpoint);
//            devStatusText = itemView.findViewById(R.id.text_dev_status);
//        }
    }
}
