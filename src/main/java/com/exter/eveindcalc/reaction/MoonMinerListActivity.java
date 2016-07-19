package com.exter.eveindcalc.reaction;

import android.app.Activity;
import android.content.Intent;

import com.exter.eveindcalc.data.Index;
import com.exter.eveindcalc.itemlist.ItemListActivity;


public class MoonMinerListActivity extends ItemListActivity
{
  @Override
  protected void onPickItem(int item)
  {
    Intent i = new Intent();
    i.putExtra("reaction", item);
    setResult(Activity.RESULT_OK,i);
    finish();
  }


  @Override
  protected String getListTitle()
  {
    return "Moon Resources";
  }

  @Override
  protected Index loadIndex()
  {
    return new Index(application.fs,"planet/index_moon.tsl");
  }

}
