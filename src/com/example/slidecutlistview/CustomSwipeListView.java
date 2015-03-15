
package com.example.slidecutlistview;

import android.content.Context;
import android.graphics.Color;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Scroller;
import android.widget.TextView;

/**
 * 2015-2-13 �Զ���ListView
 */
public class CustomSwipeListView extends ListView {
    /**
     * ��ǰ������ListView��position
     */
    private int slidePosition;

    /**
     * ��ָ����X������
     */
    private int downY;
    /**
     * ��ָ����Y������
     */
    private int downX;
    /**
     * ��Ļ���
     */
    private int screenWidth;
    /**
     * ListView��item
     */
    private View itemView;

    /**
     * item�������������
     */
    private View contentView;

    /**
     * ������
     */
    private Scroller scroller;

    /**
     * �����ٶȼ���ֵ
     */
    private final int SNAP_VELOCITY = CustomSwipeUtils.convertDptoPx(getContext(), 1000);
    /**
     * �ٶ�׷�ٶ���
     */
    private VelocityTracker velocityTracker;
    /**
     * �Ƿ���Ӧ������Ĭ��Ϊ����Ӧ
     */
    private boolean isSlide = false;
    /**
     * ��Ϊ���û���������С����
     */
    private int mTouchSlop;
    /**
     * �Ƴ�item��Ļص��ӿ�
     */
    private RemoveListener mRemoveListener;
    /**
     * ����ָʾitem������Ļ�ķ���,�����������,��һ��ö��ֵ�����
     */
    private RemoveDirection removeDirection;

    private boolean isRemoveScroll = false;

    /**
     * ָ�������ĸ�����ٶ�
     */
    private int mPointerId;

    /**
     * �������ִ��һ��fling���ƶ���������ٶ�ֵ
     */
    private int mMaxVelocity;

    int velocityX = 0;

    // ����ɾ�������ö��ֵ
    public enum RemoveDirection {
        RIGHT, LEFT;
    }

    public CustomSwipeListView(Context context) {
        this(context, null);
    }

    public CustomSwipeListView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public CustomSwipeListView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        screenWidth = ((WindowManager) context.getSystemService(Context.WINDOW_SERVICE))
                .getDefaultDisplay().getWidth();
        scroller = new Scroller(context);

        // ����û���moveǰ�����ľ���,�ƶ���������������ſ�ʼ�㻬��
        mTouchSlop = ViewConfiguration.get(getContext()).getScaledTouchSlop();

        mMaxVelocity = ViewConfiguration.get(getContext()).getScaledMaximumFlingVelocity();

    }

    /**
     * ���û���ɾ���Ļص��ӿ�
     * 
     * @param removeListener
     */
    public void setRemoveListener(RemoveListener removeListener) {
        this.mRemoveListener = removeListener;
    }

    /**
     * �ַ��¼�����Ҫ�������жϵ�������Ǹ�item, �Լ�ͨ��postDelayed��������Ӧ���һ����¼�
     */
    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {
        addVelocityTracker(event);

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:

                mPointerId = event.getPointerId(0);

                // ����scroller������û�н���������ֱ�ӷ���
                if (!scroller.isFinished()) {
                    return super.dispatchTouchEvent(event);
                }
                downX = (int) event.getX();
                downY = (int) event.getY();

                slidePosition = pointToPosition(downX, downY);

                // ��Ч��position, �����κδ���
                if (slidePosition == AdapterView.INVALID_POSITION) {
                    return super.dispatchTouchEvent(event);
                }

                // ��ȡ���ǵ����item view
                itemView = getChildAt(slidePosition - getFirstVisiblePosition());
                contentView = itemView.findViewById(R.id.ll_cotentview);

                break;

            case MotionEvent.ACTION_MOVE:

                if (Math.abs(getScrollVelocity()) > SNAP_VELOCITY
                        || (Math.abs(event.getX() - downX) > mTouchSlop && Math.abs(event.getY()
                                - downY) < mTouchSlop)) {
                    isSlide = true;
                }
                break;

            case MotionEvent.ACTION_UP:
                recycleVelocityTracker();
                break;
        }

        return super.dispatchTouchEvent(event);
    }

    /**
     * ���һ�����getScrollX()���ص������Ե�ľ��룬������View���ԵΪԭ�㵽��ʼ�����ľ��룬�������ұ߻���Ϊ��ֵ
     */
    private void scrollRight() {
        isRemoveScroll = true;
        removeDirection = RemoveDirection.RIGHT;

        final int delta = (screenWidth + itemView.getScrollX());
        // ����startScroll����������һЩ�����Ĳ�����������computeScroll()�����е���scrollTo������item
        scroller.startScroll(itemView.getScrollX(), 0, -delta, 0, Math.abs(delta));
        postInvalidate(); // ˢ��itemView
    }

    /**
     * ���󻬶���������������֪�����󻬶�Ϊ��ֵ
     */
    private void scrollLeft() {
        isRemoveScroll = true;
        removeDirection = RemoveDirection.LEFT;

        final int delta = (screenWidth - itemView.getScrollX());
        // ����startScroll����������һЩ�����Ĳ�����������computeScroll()�����е���scrollTo������item
        scroller.startScroll(itemView.getScrollX(), 0, delta, 0, Math.abs(delta));
        postInvalidate(); // ˢ��itemView
    }

    /**
     * ������ָ����itemView�ľ������ж��ǹ�������ʼλ�û�������������ҹ���
     */
    private void scrollByDistanceX() {
        // �����������ľ��������Ļ�Ķ���֮һ��������ɾ��
        if (itemView.getScrollX() >= screenWidth / 2) {
            scrollLeft();
        } else if (itemView.getScrollX() <= -screenWidth / 2) {
            scrollRight();
        } else {
            scrollToOrigin();
        }

    }

    // ��������ٶȲ����Ҿ��벻��1/3����ԭ�ػ�����ԭ��
    private void scrollToOrigin() {
        isRemoveScroll = false;
        int scrollX = itemView.getScrollX();

        // �����򻬶���ȥ
        scroller.startScroll(scrollX, 0, -scrollX, 0, 400);
    }

    /**
     * ���������϶�ListView item���߼�
     */
    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        if (isSlide && slidePosition != AdapterView.INVALID_POSITION) {
            addVelocityTracker(ev);
            final int action = ev.getAction();
            int x = (int) ev.getX();

            switch (action) {
                case MotionEvent.ACTION_MOVE:
                    int deltaX = downX - x;
                    downX = x;
                    // ��ָ�϶�itemView����, deltaX����0���������С��0���ҹ�
                    itemView.scrollBy(deltaX, 0);

                    setCotentViewAlpha(getAlphaRatio());

                    velocityX = getScrollVelocity();

                    return true;
                case MotionEvent.ACTION_UP:

                    Log.i("scrollvelocity x ========== ", velocityX + "  " + SNAP_VELOCITY);

                    if (velocityX > SNAP_VELOCITY) {
                        scrollRight();
                    } else if (velocityX < -SNAP_VELOCITY) {
                        scrollLeft();
                    } else {
                        scrollByDistanceX();
                    }

                    recycleVelocityTracker();

                    // ��ָ�뿪��ʱ��Ͳ���Ӧ���ҹ���
                    isSlide = false;
                    break;
            }

        }

        // ����ֱ�ӽ���ListView������onTouchEvent�¼�
        return super.onTouchEvent(ev);
    }

    /**
     * ��ȡ�ƶ������͸���ȵı��ʣ��ܾ���Ϊ1/2 ��Ļ��͸���ȴ�0~255
     */
    private int getAlphaRatio() {
        int scrollX = Math.abs(itemView.getScrollX());
        int xRatio = (int) Math.round(((2 * scrollX) / (float) screenWidth) * 255);
        // ͸�������ֵΪ255
        xRatio = 255 - (xRatio > 255 ? 255 : xRatio);
        return xRatio;
    }

    /**
     * �������������͸����
     */
    private void setCotentViewAlpha(int xRatio) {

        contentView.getBackground().setAlpha(xRatio);

        TextView tvTitle = (TextView) contentView.findViewById(R.id.test_title);
        TextView tvDate = (TextView) contentView.findViewById(R.id.test_date);
        setTextAlpha(xRatio, tvTitle);
        setTextAlpha(xRatio, tvDate);
    }

    /**
     * �������ֵ�͸��ɫ
     */
    private void setTextAlpha(int ratio, TextView textView) {
        int color = textView.getCurrentTextColor();
        textView.setTextColor(Color.argb(ratio, Color.red(color), Color.green(color),
                Color.blue(color)));
    }

    @Override
    public void computeScroll() {
        // ����startScroll��ʱ��scroller.computeScrollOffset()����true��
        if (scroller.computeScrollOffset()) {
            // ��ListView item���ݵ�ǰ�Ĺ���ƫ�������й���
            itemView.scrollTo(scroller.getCurrX(), scroller.getCurrY());
            setCotentViewAlpha(getAlphaRatio());

            postInvalidate();

            // ��������������ʱ����ûص��ӿ�
            if (scroller.isFinished() && isRemoveScroll) {
                if (mRemoveListener == null) {
                    throw new NullPointerException(
                            "RemoveListener is null, we should called setRemoveListener()");
                }
                mRemoveListener.removeItem(removeDirection, slidePosition);

                // ɾ��item��Ҫ��͸���Ⱥ�����ָ�����ʼֵ
                itemView.scrollTo(0, 0);
                setCotentViewAlpha(255);
            }
        }
    }

    /**
     * ����û����ٶȸ�����
     * 
     * @param event
     */
    private void addVelocityTracker(MotionEvent event) {
        if (velocityTracker == null) {
            velocityTracker = VelocityTracker.obtain();
        }

        velocityTracker.addMovement(event);
    }

    /**
     * �Ƴ��û��ٶȸ�����
     */
    private void recycleVelocityTracker() {
        if (velocityTracker != null) {
            velocityTracker.clear();
            velocityTracker.recycle();
            velocityTracker = null;
        }
    }

    /**
     * ��ȡX����Ļ����ٶ�,����0���һ�������֮����
     * 
     * @return
     */
    private int getScrollVelocity() {
        velocityTracker.computeCurrentVelocity(1000, mMaxVelocity);
        int velocity = (int) velocityTracker.getXVelocity(mPointerId);

        return velocity;
    }

    /**
     * ��ListView item������Ļ���ص�����ӿ� ������Ҫ�ڻص�����removeItem()���Ƴ���Item,Ȼ��ˢ��ListView
     */
    public interface RemoveListener {
        public void removeItem(RemoveDirection direction, int position);
    }

}
