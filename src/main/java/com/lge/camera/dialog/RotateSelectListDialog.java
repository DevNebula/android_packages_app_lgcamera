package com.lge.camera.dialog;

import android.app.Instrumentation;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnKeyListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.LinearLayout.LayoutParams;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.ScrollView;
import android.widget.TextView;
import com.lge.camera.C0088R;
import com.lge.camera.constants.CameraConstants;
import com.lge.camera.constants.ModelProperties;
import com.lge.camera.util.CamLog;
import com.lge.camera.util.Utils;
import com.lge.camera.util.ViewUtil;

public class RotateSelectListDialog extends RotateDialog {
    private boolean mIsSecondInput = false;
    protected int mSelectedPos = 0;

    /* renamed from: com.lge.camera.dialog.RotateSelectListDialog$1 */
    class C07631 implements OnClickListener {
        C07631() {
        }

        public void onClick(View v) {
            CamLog.m3d(CameraConstants.TAG, "cancel button click....");
            RotateSelectListDialog.this.onDismiss();
        }
    }

    /* renamed from: com.lge.camera.dialog.RotateSelectListDialog$3 */
    class C07653 implements OnItemClickListener {
        C07653() {
        }

        public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
            RotateSelectListDialog.this.onListItemClick(position);
            RotateSelectListDialog.this.onDismiss();
        }
    }

    class ItemViewHolder {
        RadioButton mRadioButton;
        TextView mTextView;

        ItemViewHolder() {
        }
    }

    public RotateSelectListDialog(CamDialogInterface function) {
        super(function);
    }

    public void create() {
        View v = this.mGet.inflateView(C0088R.layout.rotate_dialog);
        setView(v, false, false);
        setOneButtonLayout(v, false);
        Button btnCancel = (Button) v.findViewById(C0088R.id.cancel_button);
        if (btnCancel != null) {
            btnCancel.setOnClickListener(new C07631());
        }
        super.create(v, true, false);
        setListItem();
        addListViewToDialog(v);
    }

    protected void setView(View dialogView, boolean useCheckBox, boolean isHelpDialog) {
        super.setView(dialogView, useCheckBox, isHelpDialog);
        ((TextView) dialogView.findViewById(C0088R.id.title_text)).setText(getDialogTitle());
    }

    protected void notifyFocusingFinished(final View view) {
        if (ModelProperties.isKeyPadSupported(this.mGet.getAppContext())) {
            this.mGet.getHandler().postDelayed(new Thread(new Runnable() {
                public void run() {
                    RotateSelectListDialog.this.mGet.removePostRunnable(this);
                    ListView listView = (ListView) view.findViewById(C0088R.id.rotate_dialog_listview);
                    if (listView != null) {
                        listView.setSelection(RotateSelectListDialog.this.mSelectedPos);
                        listView.sendAccessibilityEvent(32768);
                        listView.requestFocus();
                    }
                }
            }), (long) (ViewUtil.isAccessibilityServiceEnabled(this.mGet.getAppContext()) ? 5000 : 0));
        }
    }

    protected void addListViewToDialog(View v) {
        if (ModelProperties.isKeyPadSupported(this.mGet.getAppContext())) {
            final ListView listView = new ListView(this.mGet.getAppContext());
            listView.setBackgroundResource(C0088R.drawable.dialog_middle_holo_light);
            listView.setDivider(this.mGet.getAppContext().getResources().getDrawable(17301522));
            LayoutParams param = new LayoutParams(-2, (Utils.getPx(this.mGet.getAppContext(), C0088R.dimen.rotate_dialog_listview_item.height) * 2) + listView.getDividerHeight());
            listView.setChoiceMode(1);
            listView.setLayoutParams(param);
            listView.setFocusable(true);
            listView.setId(C0088R.id.rotate_dialog_listview);
            listView.setSelector(C0088R.drawable.selector_rotate_dialog_list);
            setListViewAdapter(listView);
            listView.setOnItemClickListener(new C07653());
            listView.setOnKeyListener(new OnKeyListener() {
                public boolean onKey(View v, int keyCode, KeyEvent event) {
                    return RotateSelectListDialog.this.onKeyInListView(listView, keyCode, event);
                }
            });
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
    }

    private boolean onKeyInListView(ListView listView, int keyCode, KeyEvent event) {
        if (this.mGet.getOrientationDegree() == 0) {
            return false;
        }
        int position = listView.getSelectedItemPosition();
        if (event.getRepeatCount() > 0) {
            CamLog.m3d(CameraConstants.TAG, "Repeat count is bigger than 0, return.");
            return true;
        }
        switch (keyCode) {
            case 19:
            case 20:
            case 21:
            case 22:
                if (!this.mIsSecondInput) {
                    return doDpadKey(keyCode, event, position);
                }
                this.mIsSecondInput = false;
                CamLog.m3d(CameraConstants.TAG, "isSecondInput  set to false");
                return false;
            default:
                return false;
        }
    }

    private boolean doDpadKey(int keycode, KeyEvent event, int selectedPosition) {
        if (event.getAction() == 1) {
            this.mIsSecondInput = false;
            CamLog.m3d(CameraConstants.TAG, "isSecondInput  set to false");
        } else {
            int orientation = this.mGet.getOrientationDegree();
            int correctedKeycode = keycode;
            if (orientation == 90 || orientation == 270) {
                switch (keycode) {
                    case 19:
                    case 20:
                        correctedKeycode += 2;
                        break;
                    case 21:
                    case 22:
                        correctedKeycode -= 2;
                        break;
                }
            }
            if (orientation == 180 || orientation == 270) {
                switch (keycode) {
                    case 19:
                    case 21:
                        correctedKeycode++;
                        break;
                    case 20:
                    case 22:
                        correctedKeycode--;
                        break;
                }
            }
            final int finalKeycode = correctedKeycode;
            new Thread(new Runnable() {
                public void run() {
                    RotateSelectListDialog.this.mIsSecondInput = true;
                    CamLog.m3d(CameraConstants.TAG, "isSecondInput  set to true");
                    new Instrumentation().sendKeyDownUpSync(finalKeycode);
                }
            }).start();
        }
        return true;
    }

    protected void setNextFocusID(int degree) {
        if (ModelProperties.isKeyPadSupported(this.mGet.getAppContext())) {
            View v = this.mGet.findViewById(C0088R.id.rotate_dialog_inner_layout);
            if (v != null) {
                Button btnCancel = (Button) v.findViewById(C0088R.id.cancel_button);
                ListView listView = (ListView) v.findViewById(C0088R.id.rotate_dialog_listview);
                if (btnCancel != null && listView != null) {
                    btnCancel.setNextFocusUpId(btnCancel.getId());
                    btnCancel.setNextFocusDownId(btnCancel.getId());
                    btnCancel.setNextFocusLeftId(btnCancel.getId());
                    btnCancel.setNextFocusRightId(btnCancel.getId());
                    listView.setNextFocusUpId(listView.getId());
                    listView.setNextFocusDownId(btnCancel.getId());
                    listView.setNextFocusLeftId(listView.getId());
                    listView.setNextFocusRightId(listView.getId());
                    switch (this.mGet.getOrientationDegree()) {
                        case 90:
                            btnCancel.setNextFocusLeftId(listView.getId());
                            return;
                        case 180:
                            btnCancel.setNextFocusDownId(listView.getId());
                            return;
                        case 270:
                            btnCancel.setNextFocusRightId(listView.getId());
                            return;
                        default:
                            btnCancel.setNextFocusUpId(listView.getId());
                            return;
                    }
                }
            }
        }
    }

    protected String getDialogTitle() {
        return null;
    }

    protected void setListItem() {
    }

    protected void setListViewAdapter(ListView listView) {
    }

    protected void onListItemClick(int position) {
    }
}
