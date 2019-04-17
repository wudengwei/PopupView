package com.wudengwei.popupview;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.support.annotation.IdRes;
import android.support.annotation.LayoutRes;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.PopupWindow;

import java.lang.ref.WeakReference;

/**
 * Created by wudengwei
 * on 2019/4/11
 */
public class PopupView {
    private Activity mActivity;
    private PopupWindow mPopupWindow;
    private View mContentView;

    //view点击事件
    private OnClickListener mOnClickListener;
    //popupwindow消失事件
    private OnDismissListener mOnDismissListener;

    public void setmOnDismissListener(OnDismissListener mOnDismissListener) {
        this.mOnDismissListener = mOnDismissListener;
    }

    public void setOnClickListener(int[] viewIdArr, OnClickListener mOnClickListener) {
        this.mOnClickListener = mOnClickListener;
        if (mContentView != null) {
            for (int i=0;i<viewIdArr.length;i++) {
                View child = mContentView.findViewById(viewIdArr[i]);
                if (child == null) {
                    throw new NullPointerException("mContentView 不存在id为"+mActivity.getResources().getResourceName(viewIdArr[i])+"的view");
                }
                child.setOnClickListener(viewClickListener);
            }
        }
    }

    private View.OnClickListener viewClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (mOnClickListener != null) {
                mOnClickListener.onClick(v);
            }
        }
    };

    private PopupView() {}

    public static class Builder {
        public WeakReference<Activity> mActivity;
        private int contentViewlayoutId;//内容layoutid
        private int width = ViewGroup.LayoutParams.MATCH_PARENT;//宽
        private int height = ViewGroup.LayoutParams.MATCH_PARENT;//高
        private float bgAlpha = 1f;//背景透明度
        private boolean focusable = true;//是否获取焦点,focusable=false，touchable=true时，内容layout外点击事件不拦截
        private boolean touchable = true;//是否可点击,false时点击事件不拦截,true时内容layout外点击事件不拦截
        private boolean outsideTouchable = true;//内容layout外点击是否消失，6.0以上无效
        private int animationStyle = -1;//style设置的进出动画

        public Builder(Activity activity) {
            mActivity = new WeakReference<>(activity);
        }

        public Builder setwidth(int width) {
            this.width = width;
            return this;
        }

        public Builder setheight(int height) {
            this.height = height;
            return this;
        }

        public Builder setBgAlpha(float bgAlpha) {
            this.bgAlpha = bgAlpha;
            return this;
        }

        public Builder setContentView(@LayoutRes int layoutId) {
            this.contentViewlayoutId = layoutId;
            return this;
        }

        public Builder setFocusable(boolean focusable) {
            this.focusable = focusable;
            return this;
        }

        public Builder setTouchable(boolean touchable) {
            this.touchable = touchable;
            return this;
        }

        public Builder setOutsideTouchable(boolean outsideTouchable) {
            this.outsideTouchable = outsideTouchable;
            return this;
        }

        public Builder setAnimationStyle(int animationStyle) {
            this.animationStyle = animationStyle;
            return this;
        }

        public PopupView build() {
            final PopupView popupView = new PopupView();
            popupView.mActivity = mActivity.get();
            // 一个自定义的布局，作为显示的内容
            popupView.mContentView = LayoutInflater.from(mActivity.get()).inflate(contentViewlayoutId, null);
            popupView.mPopupWindow = new PopupWindow(popupView.mContentView, width, height, focusable);
            popupView.mPopupWindow.setTouchable(touchable);
            // 如果不设置PopupWindow的背景，无论是点击外部区域还是Back键都无法dismiss弹框
            popupView.mPopupWindow.setBackgroundDrawable(new BitmapDrawable(mActivity.get().getResources(), (Bitmap) null));
            if (animationStyle != -1) {
                popupView.mPopupWindow.setAnimationStyle(animationStyle);
            }
            popupView.mPopupWindow.setOutsideTouchable(outsideTouchable);
            popupView.mPopupWindow.setTouchInterceptor(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    if (!outsideTouchable) {
                        View mView = popupView.mPopupWindow.getContentView();
                        if (null != mView)
                            mView.dispatchTouchEvent(event);
                    }
                    return focusable && !outsideTouchable;
                }
            });
            popupView.mPopupWindow.setOnDismissListener(new PopupWindow.OnDismissListener() {
                @Override
                public void onDismiss() {
                    // popupWindow隐藏时恢复屏幕正常透明度
                    setBackgroundAlpha(1.0f);
                    if (popupView.mOnDismissListener != null) {
                        popupView.mOnDismissListener.onDismiss();
                    }
                }
            });
            setBackgroundAlpha(bgAlpha);
            return popupView;
        }

        /*屏幕透明度*/
        private void setBackgroundAlpha(float bgAlpha) {
            WindowManager.LayoutParams lp = mActivity.get().getWindow().getAttributes();
            lp.alpha = bgAlpha;
            mActivity.get().getWindow().addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
            mActivity.get().getWindow().setAttributes(lp);
        }
    }

    public View getView(@IdRes int viewId) {
        return mContentView.findViewById(viewId);
    }

    public void showAsDropDown(View anchor, int xoff, int yoff) {
        if (mPopupWindow != null) {
            if (!mPopupWindow.isShowing()) {
                mPopupWindow.showAsDropDown(anchor, xoff, yoff);
            }
        }
    }

    public void showAtLocation(View parent, int gravity, int x, int y) {
        if (mPopupWindow != null) {
            if (!mPopupWindow.isShowing()) {
                mPopupWindow.showAtLocation(parent, gravity, x, y);
            }
        }
    }

    public interface OnClickListener {
        void onClick(View view);
    }

    public interface OnDismissListener {
        void onDismiss();
    }
}