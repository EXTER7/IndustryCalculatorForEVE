package com.exter.controls;

import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;

import java.math.BigDecimal;
import java.text.DecimalFormat;

public class BigDecimalEditText
{
  public interface ValueListener
  {
    void ValueChanged(int tag,BigDecimal value);
  }
  
  public interface FocusListener
  {
    void FocusReceived(int tag);
  }
  
  private BigDecimal GetValue(String text)
  {
    BigDecimal value;
    try
    {
      value = new BigDecimal(text);
    } catch(NumberFormatException e)
    {
      value = invalid;
    }
    if(value.compareTo(min) < 0)
    {
      value = min;
    }
    if(value.compareTo(max) > 0)
    {
      value = max;
    }
    return value;
  }

  private class ChangeWatcher implements TextWatcher
  { 
    @Override
    public void afterTextChanged(Editable s)
    {
      String str = s.toString();
      listener.ValueChanged(tag,GetValue(str));
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after)
    {

    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count)
    {

    }
  }

  private class FocusChangeListener implements View.OnFocusChangeListener
  {
    @Override
    public void onFocusChange(View v, boolean has_focus)
    {
      EditText ed = (EditText)v;
      TextWatcher w = (TextWatcher)ed.getTag();
      ed.removeTextChangedListener(w);
      if(has_focus)
      {
        ed.setSelection(ed.getText().length());
        if(focus_listener != null)
        {
          focus_listener.FocusReceived(tag);
        }
      }
      ed.addTextChangedListener(w);
    }
  }

  
  BigDecimal min;
  BigDecimal max;
  BigDecimal invalid;
  int tag;
  ValueListener listener;
  FocusListener focus_listener;
  EditText editor;
  
  public BigDecimalEditText(EditText ed,int edtag,BigDecimal vmin,BigDecimal vmax,BigDecimal vinvalid,ValueListener vlistener)
  {
    editor = ed;
    min = vmin;
    max = vmax;
    invalid = vinvalid;
    tag = edtag;
    listener = vlistener;
    focus_listener = null;
    ChangeWatcher cw = new ChangeWatcher();
    editor.setTag(cw);
    editor.addTextChangedListener(cw);
    editor.setOnFocusChangeListener(new FocusChangeListener());
  }

  public void SetValue(BigDecimal value)
  {
    TextWatcher w = (TextWatcher)editor.getTag();
    editor.removeTextChangedListener(w);
    if(value.compareTo(min) < 0)
    {
      value = min;
    }
    if(value.compareTo(max) > 0)
    {
      value = max;
    }
    DecimalFormat formatter = new DecimalFormat("###.##");
    editor.setText(formatter.format(value));
    editor.addTextChangedListener(w);   
  }

  public void setEnabled(boolean enabled)
  {
    editor.setEnabled(enabled);
  }
}
