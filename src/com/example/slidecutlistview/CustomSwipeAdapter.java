
package com.example.slidecutlistview;

import java.util.List;

import android.content.Context;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.widget.BaseAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.example.slidecutlistview.CustomSwipeListView.RemoveDirection;
import com.example.slidecutlistview.CustomSwipeListView.RemoveListener;

/**
 * ʵ�ֳ���������Adapter
 */
public class CustomSwipeAdapter extends BaseAdapter implements CancelListener, RemoveListener {

    private static final int INVALID_POSITION = -1;

    protected Context mContext;

    private TestModel deleteModel;

    // �������ݵ�ʵ�����б�
    private List<TestModel> testModels;

    // ��¼ɾ����item��λ��
    private int deletedPosition;

    // �Ƿ���ɾ����item
    private boolean cancelRemoveItem = false;

    // �����ķ���
    private RemoveDirection deleteDirection;

    // ��¼�Ƿ���һ�ε�����û��ʧ
    private boolean isCountingTime;

    // ������������߳�
    private Runnable dismissRunnable;
    private Handler handler;

    private CustomSwipeCancelDialog cancelDialog;

    public CustomSwipeAdapter(Context context, List<TestModel> Objects) {
        mContext = context;
        testModels = Objects;

        handler = new Handler();
        dismissRunnable = new DismissRunnable();

        cancelDialog = new CustomSwipeCancelDialog(context);
        cancelDialog.setcancelActionListener(this);
    }

    @Override
    public TestModel getItem(int position) {
        return testModels.get(position);
    }

    @Override
    public int getCount() {
        return testModels.size();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view;
        ViewHolder holder;
        if (convertView == null) {
            view = LayoutInflater.from(mContext).inflate(R.layout.test_listview_item_view, parent,
                    false);
            holder = new ViewHolder();
            holder.tvDate = (TextView) view.findViewById(R.id.test_date);
            holder.tvTitle = (TextView) view.findViewById(R.id.test_title);
            view.setTag(holder);
        } else {
            view = convertView;
            holder = (ViewHolder) view.getTag();
        }
        holder.tvTitle.setText(getItem(position).getTestTitle());
        holder.tvDate.setText(getItem(position).getTestDate());

        if (cancelRemoveItem) {
            cancelActionAnimation(view.findViewById(R.id.ll_cotentview), position);
        }

        return view;
    }

    class ViewHolder {
        TextView tvTitle;
        TextView tvDate;
    }

    /**
     * ִ�г�������
     */
    private void cancelActionAnimation(View contentView, int undoPosition) {
        if (undoPosition == deletedPosition) {
            switch (deleteDirection) {
                case LEFT:
                    contentView.startAnimation(AnimationUtils.loadAnimation(mContext,
                            R.anim.canceldialog_push_left_in));
                    break;

                case RIGHT:
                    contentView.startAnimation(AnimationUtils.loadAnimation(mContext,
                            R.anim.canceldialog_push_right_in));
                    break;

                default:
                    break;
            }

            clearDeletedObject();
        } else {
            contentView.clearAnimation();
        }
    }

    /**
     * ����dialog��ʧʱ����
     */
    @Override
    public void normalAction() {
        if (!cancelRemoveItem) {
            clearDeletedObject();
        }
    }

    public void clearDeletedObject() {
        deleteModel = null;
        cancelRemoveItem = false;
        deletedPosition = INVALID_POSITION;
    }

    /**
     * ɾ�����������Ĳ���
     */
    @Override
    public void executeCancelAction() {
        if (deletedPosition <= testModels.size() && deletedPosition != INVALID_POSITION) {
            testModels.add(deletedPosition, deleteModel);
            cancelRemoveItem = true;
            notifyDataSetChanged();
        }
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    /**
     * ����ɾ��֮��Ļص�����
     */
    @Override
    public void removeItem(RemoveDirection direction, int position) {
        // ��һ��ɾ��item���ӳٵ�ʱ���ڣ���ɾ����һ����Ҫ����ֹ��һ��runnable
        if (isCountingTime) {
            handler.removeCallbacks(dismissRunnable);
        }

        TestModel model = removeItemByPosition(position, direction);
        cancelDialog.setMessage("Delete" + model.getTestTitle()).showCancelDialog();

        dismissDialog();

        switch (direction) {
            case RIGHT:
                Toast.makeText(mContext, "����ɾ��  " + position, Toast.LENGTH_SHORT).show();
                break;
            case LEFT:
                Toast.makeText(mContext, "����ɾ��  " + position, Toast.LENGTH_SHORT).show();
                break;

            default:
                break;
        }

    }

    /**
     * ɾ�����������汻ɾ��������Ϣ
     */
    public TestModel removeItemByPosition(int position, RemoveDirection direction) {
        if (position < getCount() && position != INVALID_POSITION) {
            deleteModel = testModels.remove(position);
            deletedPosition = position;
            deleteDirection = direction;
            notifyDataSetChanged();
            return deleteModel;
        } else {
            throw new IndexOutOfBoundsException("The position is invalid!");
        }
    }

    /**
     * ���������Ի����һ��ʱ���ڣ�5�룩��û�κβ����Ļ����Ի����Զ���ʧ
     */
    private void dismissDialog() {
        isCountingTime = true;

        handler.postDelayed(dismissRunnable, 5000);
    }

    class DismissRunnable implements Runnable {

        @Override
        public void run() {
            if (cancelDialog.isShowing()) {
                cancelDialog.closeCancelDialog();
                clearDeletedObject();
                isCountingTime = false;
            }
        }

    }
}
