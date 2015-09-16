package com.exter.controls;

import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;

public class IntegerEditText
{
  public interface ValueListener
  {
    void onValueChanged(int value);
  }
  
  private int GetValue(String text)
  {
    int value;
    try
    {
      value = Integer.valueOf(text);
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
      listener.onValueChanged(GetValue(str));
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
      } else
      {
        int i = GetValue(ed.getText().toString());
        ed.setText(String.valueOf(i));
      }
      ed.addTextChangedListener(w);
    }    
  }

  
  int min;
  int max;
  int invalid;
  ValueListener listener;
  EditText editor;
  
  public IntegerEditText(EditText ed,int vmin,int vmax,int vinvalid,ValueListener vlistener)
  {
    editor = ed;
    min = vmin;
    max = vmax;
    invalid = vinvalid;
    listener = vlistener;
    ChangeWatcher cw = new ChangeWatcher();
    editor.setTag(cw);
    editor.addTextChangedListener(cw);
    editor.setOnFocusChangeListener(new FocusChangeListener());
  }

  public void setValue(int value)
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
  
  public void SetEnabled(boolean enabled)
  {
    editor.setEnabled(enabled);
  }
}
