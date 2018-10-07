package com.lge.camera.dialog;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.TextView;
import com.lge.camera.C0088R;
import com.lge.camera.settings.ListPreference;
import com.lge.camera.util.SettingKeyWrapper;
import java.util.ArrayList;

public class SelectSizeRotatableDialog extends RotateSelectListDialog {
    private SimpleResolutionAdapter mAdapter = null;

    class SimpleResolutionAdapter extends ArrayAdapter<SizeItem> {
        public SimpleResolutionAdapter(Context context, int res, ArrayList<SizeItem> objects) {
            super(context, res, objects);
        }

        public View getView(int position, View convertView, ViewGroup parent) {
            ItemViewHolder holder;
            View view = convertView;
            if (view == null) {
                view = SelectSizeRotatableDialog.this.mGet.inflateView(C0088R.layout.rotate_dialog_listview_single_select_item);
                if (view == null) {
                    return null;
                }
                holder = new ItemViewHolder();
                holder.mTextView = (TextView) view.findViewById(C0088R.id.item_key);
                holder.mRadioButton = (RadioButton) view.findViewById(C0088R.id.item_radio_button);
                holder.mRadioButton.setClickable(false);
                view.setTag(holder);
            } else {
                holder = (ItemViewHolder) view.getTag();
            }
            holder.mTextView.setText(((SizeItem) getItem(position)).mKey);
            if (SelectSizeRotatableDialog.this.mGet.getSettingValue(SettingKeyWrapper.getPictureSizeKey(SelectSizeRotatableDialog.this.mGet.getShotMode(), SelectSizeRotatableDialog.this.mGet.getCameraId())).equals(((SizeItem) getItem(position)).mImageSize)) {
                holder.mRadioButton.setChecked(true);
                SelectSizeRotatableDialog.this.mSelectedPos = position;
            } else {
                holder.mRadioButton.setChecked(false);
            }
            return view;
        }
    }

    class SizeItem {
        String mImageSize;
        String mKey;
        String mVideoSize;

        SizeItem() {
        }
    }

    public SelectSizeRotatableDialog(CamDialogInterface function) {
        super(function);
    }

    protected String getDialogTitle() {
        return this.mGet.getAppContext().getString(C0088R.string.sp_resolution);
    }

    protected void setListItem() {
        ListPreference imageSizePref = (ListPreference) this.mGet.getListPreference(SettingKeyWrapper.getPictureSizeKey(this.mGet.getShotMode(), this.mGet.getCameraId()));
        ListPreference videoSizePref = (ListPreference) this.mGet.getListPreference(SettingKeyWrapper.getVideoSizeKey(this.mGet.getShotMode(), this.mGet.getCameraId()));
        if (imageSizePref.getEntryValues().length == 1) {
            onDismiss();
            return;
        }
        String[] keyList = new String[]{this.mGet.getAppContext().getString(C0088R.string.sp_resolution_high), this.mGet.getAppContext().getString(C0088R.string.sp_resolution_standard)};
        ArrayList<SizeItem> itemList = new ArrayList();
        for (int i = 0; i < keyList.length; i++) {
            SizeItem item = new SizeItem();
            item.mKey = keyList[i];
            item.mImageSize = imageSizePref.getEntryValues()[i].toString();
            item.mVideoSize = videoSizePref.getEntryValues()[i].toString();
            itemList.add(item);
        }
        this.mAdapter = new SimpleResolutionAdapter(this.mGet.getAppContext(), C0088R.layout.rotate_dialog_listview_single_select_item, itemList);
    }

    protected void onListItemClick(int position) {
        SizeItem item = (SizeItem) this.mAdapter.getItem(position);
        this.mGet.setSetting(SettingKeyWrapper.getPictureSizeKey(this.mGet.getShotMode(), this.mGet.getCameraId()), item.mImageSize, true);
        this.mGet.childSettingMenuClicked(SettingKeyWrapper.getPictureSizeKey(this.mGet.getShotMode(), this.mGet.getCameraId()), item.mImageSize, -1);
        this.mGet.setSetting(SettingKeyWrapper.getVideoSizeKey(this.mGet.getShotMode(), this.mGet.getCameraId()), item.mVideoSize, true);
        if (this.mGet.isVideoAttachMode()) {
            this.mGet.childSettingMenuClicked(SettingKeyWrapper.getVideoSizeKey(this.mGet.getShotMode(), this.mGet.getCameraId()), item.mVideoSize, -1);
        }
        this.mAdapter.notifyDataSetChanged();
    }

    protected void setListViewAdapter(ListView listView) {
        listView.setAdapter(this.mAdapter);
    }

    public boolean onDismiss() {
        if (this.mAdapter != null) {
            this.mAdapter.clear();
            this.mAdapter = null;
        }
        return super.onDismiss();
    }
}
