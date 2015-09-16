package com.exter.eveindcalc.refine;

import android.app.Activity;
import android.content.Intent;

import com.exter.eveindcalc.data.Index;
import com.exter.eveindcalc.data.refine.RefineDA;
import com.exter.eveindcalc.itemlist.ItemListActivity;

public class RefineListActivity extends ItemListActivity
{
  @Override
  protected void onPickItem(int item)
  {
    Intent i = new Intent();
    i.putExtra("refine", item);
    setResult(Activity.RESULT_OK,i);
    finish();
  }


  @Override
  protected String getListTitle()
  {
    return "Refining";
  }


  @Override
  protected Index loadIndex()
  {
    return RefineDA.getIndex();
  }
}
