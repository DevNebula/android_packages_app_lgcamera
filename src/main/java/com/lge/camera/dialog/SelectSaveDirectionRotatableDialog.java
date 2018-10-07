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
import com.lge.camera.settings.Setting;
import java.util.ArrayList;

public class SelectSaveDirectionRotatableDialog extends RotateSelectListDialog {
    private SaveDirectionSettingAdapter mAdapter = null;

    class SaveDirectionItem {
        String entry;
        String entryValue;

        SaveDirectionItem() {
        }
    }

    class SaveDirectionSettingAdapter extends ArrayAdapter<SaveDirectionItem> {
        public SaveDirectionSettingAdapter(Context context, int res, ArrayList<SaveDirectionItem> objects) {
            super(context, res, objects);
        }

        public View getView(int position, View convertView, ViewGroup parent) {
            ItemViewHolder holder;
            View view = convertView;
            if (view == null) {
                view = SelectSaveDirectionRotatableDialog.this.mGet.inflateView(C0088R.layout.rotate_dialog_listview_single_select_item);
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
            holder.mTextView.setText(((SaveDirectionItem) getItem(position)).entry);
            if (SelectSaveDirectionRotatableDialog.this.mGet.getSettingValue(Setting.KEY_SAVE_DIRECTION).equals(((SaveDirectionItem) getItem(position)).entryValue)) {
                holder.mRadioButton.setChecked(true);
                SelectSaveDirectionRotatableDialog.this.mSelectedPos = position;
            } else {
                holder.mRadioButton.setChecked(false);
            }
            return view;
        }
    }

    public SelectSaveDirectionRotatableDialog(CamDialogInterface function) {
        super(function);
    }

    protected String getDialogTitle() {
        return this.mGet.getAppContext().getString(C0088R.string.sp_save_as_flipped_NORMAL);
    }

    protected void setListItem() {
        ArrayList<SaveDirectionItem> itemList = new ArrayList();
        ListPreference pref = (ListPreference) this.mGet.getListPreference(Setting.KEY_SAVE_DIRECTION);
        String[] entries = (String[]) pref.getEntries();
        String[] entryValues = (String[]) pref.getEntryValues();
        for (int i = 0; i < entryValues.length; i++) {
            SaveDirectionItem item = new SaveDirectionItem();
            item.entry = entries[i];
            item.entryValue = entryValues[i];
            itemList.add(item);
        }
        this.mAdapter = new SaveDirectionSettingAdapter(this.mGet.getAppContext(), C0088R.layout.rotate_dialog_listview_single_select_item, itemList);
    }

    protected void onListItemClick(int position) {
        SaveDirectionItem item = (SaveDirectionItem) this.mAdapter.getItem(position);
        this.mGet.setSetting(Setting.KEY_SAVE_DIRECTION, item.entryValue, true);
        this.mGet.childSettingMenuClicked(Setting.KEY_SAVE_DIRECTION, item.entryValue, -1);
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
