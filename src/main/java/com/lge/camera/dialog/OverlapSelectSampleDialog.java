package com.lge.camera.dialog;

import android.content.Context;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout.LayoutParams;
import android.widget.ListView;
import android.widget.ScrollView;
import android.widget.TextView;
import com.lge.camera.C0088R;
import com.lge.camera.constants.CameraConstants;
import com.lge.camera.util.CamLog;
import com.lge.camera.util.Utils;
import java.util.ArrayList;

public class OverlapSelectSampleDialog extends RotateDialog {
    private SampleChoiceAdapter mAdapter = null;

    /* renamed from: com.lge.camera.dialog.OverlapSelectSampleDialog$1 */
    class C07511 implements OnClickListener {
        C07511() {
        }

        public void onClick(View v) {
            CamLog.m3d(CameraConstants.TAG, "cancel button click....");
            OverlapSelectSampleDialog.this.onDismiss();
        }
    }

    /* renamed from: com.lge.camera.dialog.OverlapSelectSampleDialog$2 */
    class C07522 implements OnItemClickListener {
        C07522() {
        }

        public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
            OverlapSelectSampleDialog.this.onListItemClick(position);
            OverlapSelectSampleDialog.this.onDismiss();
        }
    }

    class ItemViewHolder {
        TextView mTextView;

        ItemViewHolder() {
        }
    }

    class ListItem {
        int entry;
        String entryValue;

        ListItem() {
        }
    }

    class SampleChoiceAdapter extends ArrayAdapter<ListItem> {
        public SampleChoiceAdapter(Context context, int res, ArrayList<ListItem> objects) {
            super(context, res, objects);
        }

        public View getView(int position, View convertView, ViewGroup parent) {
            ItemViewHolder holder;
            CamLog.m3d(CameraConstants.TAG, " position " + position);
            View view = convertView;
            if (view == null) {
                view = OverlapSelectSampleDialog.this.mGet.inflateView(C0088R.layout.rotate_dialog_overlap_sample_item);
                if (view == null) {
                    return null;
                }
                holder = new ItemViewHolder();
                holder.mTextView = (TextView) view.findViewById(C0088R.id.item_key);
                view.setTag(holder);
            } else {
                holder = (ItemViewHolder) view.getTag();
            }
            holder.mTextView.setText(((ListItem) getItem(position)).entryValue);
            return view;
        }
    }

    public OverlapSelectSampleDialog(CamDialogInterface function) {
        super(function);
    }

    public void create() {
        View v = this.mGet.inflateView(C0088R.layout.rotate_dialog_overlap);
        setView(v, false, false);
        setOneButtonLayout(v, false);
        Button btnCancel = (Button) v.findViewById(C0088R.id.cancel_button);
        if (btnCancel != null) {
            btnCancel.setOnClickListener(new C07511());
        }
        super.create(v, true, false);
        setListItem();
        addListViewToDialog(v);
    }

    protected void setView(View dialogView, boolean useCheckBox, boolean isHelpDialog) {
        super.setView(dialogView, useCheckBox, isHelpDialog);
        ((TextView) dialogView.findViewById(C0088R.id.title_text)).setText(getDialogTitle());
    }

    protected void addListViewToDialog(View v) {
        ListView listView = new ListView(this.mGet.getActivity());
        LayoutParams param = new LayoutParams(-1, (Utils.getPx(this.mGet.getAppContext(), C0088R.dimen.rotate_dialog_listview_item.height) * 2) + listView.getDividerHeight());
        listView.setChoiceMode(1);
        listView.setLayoutParams(param);
        listView.setFocusable(true);
        listView.setId(C0088R.id.rotate_dialog_listview);
        setListViewAdapter(listView);
        listView.setOnItemClickListener(new C07522());
        ScrollView scrView = (ScrollView) v.findViewById(C0088R.id.message_scroll);
        if (scrView != null) {
            ViewGroup parent = (ViewGroup) scrView.getParent();
            if (parent == null) {
                onDismiss();
                return;
            }
            int scrViewIndex = parent.indexOfChild(scrView);
            if (scrViewIndex != -1) {
                parent.removeViewAt(scrViewIndex);
                parent.addView(listView, scrViewIndex);
                return;
            }
            parent.addView(listView);
        }
    }

    protected String getDialogTitle() {
        return this.mGet.getAppContext().getString(C0088R.string.add_sample);
    }

    protected void setListItem() {
        ArrayList<ListItem> itemList = new ArrayList();
        ListItem camera = new ListItem();
        camera.entry = C0088R.string.option_take_photo;
        camera.entryValue = this.mGet.getAppContext().getString(camera.entry);
        itemList.add(camera);
        ListItem gallery = new ListItem();
        gallery.entry = C0088R.string.select_photo;
        gallery.entryValue = this.mGet.getAppContext().getString(gallery.entry);
        itemList.add(gallery);
        this.mAdapter = new SampleChoiceAdapter(this.mGet.getAppContext(), C0088R.layout.rotate_dialog_overlap_sample_item, itemList);
    }

    protected void onListItemClick(int position) {
        this.mGet.doSelectInOverlapSampleDialog(((ListItem) this.mAdapter.getItem(position)).entry);
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
