package com.exter.eveindcalc.planet;

import android.app.Activity;
import android.content.Intent;


import com.exter.eveindcalc.data.Index;
import com.exter.eveindcalc.itemlist.ItemListActivity;

import exter.eveindustry.data.planet.Planet;

public class PlanetProductListActivity extends ItemListActivity
{
  @Override
  protected void onPickItem(int item)
  {
    Intent i = new Intent();
    i.putExtra("planetproduct", item);
    setResult(Activity.RESULT_OK,i);
    finish();
  }


  @Override
  protected String getListTitle()
  {
    return "Planet Products";
  }

  @Override
  protected Index loadIndex()
  {
    Planet planet = application.factory.planets.get(getIntent().getIntExtra("planet", -1));
    String path = planet.advanced?"planet/index_advancel.tsl":"planet/index.tsl";
    return new Index(application.fs,path);
  }

}
