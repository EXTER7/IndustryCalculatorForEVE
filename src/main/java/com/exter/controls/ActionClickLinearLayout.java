package com.exter.controls;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.support.annotation.NonNull;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.LinearLayout;

public class ActionClickLinearLayout extends LinearLayout
{

  public ActionClickLinearLayout(Context context)
  {
    super(context);
  }

  public ActionClickLinearLayout(Context context, AttributeSet attrs)
  {
    super(context, attrs);
  }  
  
  @SuppressLint("ClickableViewAccessibility")
  @TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
  @Override
  public boolean onTouchEvent(@NonNull MotionEvent event)
  {
    if(Build.VERSION.SDK_INT >= 14 && event.getButtonState() == MotionEvent.BUTTON_SECONDARY)
    {
      performLongClick();
      return true;
    }
    return super.onTouchEvent(event);
  }
}
