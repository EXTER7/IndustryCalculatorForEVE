package com.exter.eveindcalc.planet;

import android.app.Activity;
import android.content.Intent;

import com.exter.eveindcalc.EICApplication;
import com.exter.eveindcalc.itemlist.ItemListActivity;

import exter.eveindustry.dataprovider.index.Index;
import exter.eveindustry.dataprovider.planet.Planet;

public class PlanetResourceListActivity extends ItemListActivity
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
    return "Planet Resources";
  }

  @Override
  protected Index loadIndex()
  {
    Planet p = ((EICApplication)getApplication()).provider.getPlanet(getIntent().getIntExtra("planet", -1));
    return new Index(p.TypeName,p.Resources);
  }
}
