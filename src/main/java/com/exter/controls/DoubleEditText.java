package com.exter.controls;

import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;

public class DoubleEditText
{
  public interface ValueListener
  {
    void valueChanged(int tag, double value);
  }

  public interface FocusListener
  {
    void FocusReceived(int tag);
  }
  
  private double getValue(String text)
  {
    double value;
    try
    {
      value = Double.valueOf(text);
    } catch(NumberFormatException e)
    {
      value = invalid;
    }
    if(value < min)
    {
      value = min;
    }
    if(value > max)
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
      listener.valueChanged(tag, getValue(str));
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
      }/* else
      {
        double i = get(ed.getText().toString());
        ed.setText(String.valueOf(i));
      }*/
      ed.addTextChangedListener(w);
    }
  }

  
  double min;
  double max;
  double invalid;
  int tag;
  ValueListener listener;
  FocusListener focus_listener;
  EditText editor;
  
  public DoubleEditText(EditText ed,int edtag,double vmin,double vmax,double vinvalid,ValueListener vlistener)
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

  public void setValue(double value)
  {
    TextWatcher w = (TextWatcher)editor.getTag();
    editor.removeTextChangedListener(w);
    if(value < min)
    {
      value = min;
    }
    if(value > max)
    {
      value = max;
    }
    editor.setText(String.valueOf(value));
    editor.addTextChangedListener(w);   
  }

}
