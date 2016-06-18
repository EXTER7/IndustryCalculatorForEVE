package com.exter.eveindcalc.planet;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.exter.eveindcalc.EICApplication;
import com.exter.eveindcalc.R;
import com.exter.eveindcalc.data.EveDatabase;


import java.util.ArrayList;
import java.util.List;

import exter.eveindustry.data.inventory.IItem;
import exter.eveindustry.dataprovider.item.Item;
import exter.eveindustry.dataprovider.planet.Planet;

public class PlanetListActivity extends FragmentActivity
{
  private class PlanetListAdapter extends BaseAdapter
  {
    private LayoutInflater inflater;

    PlanetListAdapter(Context context)
    {
      inflater = LayoutInflater.from(context);      
    }
      
    @Override
    public int getCount()
    {
      if(planets == null)
      {
        return 0;
      }
      {
        return planets.size();
      }
    }
       
    @Override
    public Object getItem(int position)
    {
      if(planets == null)
      {
         return null;
      } else
      {
        return planets.get(position);
      }
    }
       
    
    @Override
    public long getItemId(int position)
    {
      return position;
    }
    
    private class ItemHolder
    {
      TextView tx_name;
      LinearLayout ly_resources;
      ImageView im_icon;
    }
         
    @Override
    public View getView(int position, View convertView, ViewGroup parent)
    {
      Planet planet = planets.get(position);
      ItemHolder holder;
      if (convertView == null)
      {
        convertView = inflater.inflate(R.layout.planet_info, parent, false);
        holder = new ItemHolder();
        holder.tx_name = (TextView)convertView.findViewById(R.id.tx_planet_info_name);
        holder.im_icon = (ImageView)convertView.findViewById(R.id.im_planet_info_icon);
        holder.ly_resources = (LinearLayout)convertView.findViewById(R.id.ly_planet_info_resources);
        convertView.setTag(holder);
      } else
      {
        holder = (ItemHolder)convertView.getTag();
      }
      application.setImageViewItemIcon(holder.im_icon, provider.getItem(planet.ID));

      holder.tx_name.setText(planet.TypeName);
      holder.ly_resources.removeAllViews();
      for(IItem res:planet.Resources)
      {
        View v = inflater.inflate(R.layout.planet_info_resource, holder.ly_resources, false);
        ImageView res_icon = (ImageView)v.findViewById(R.id.im_planet_info_resource);
        application.setImageViewItemIcon(res_icon, (Item) res);
        holder.ly_resources.addView(v);
      }
      return convertView;
    }
  }

  
  private class PlanetListClickListener implements ListView.OnItemClickListener
  {
    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id)
    {
      Intent i = new Intent();
      i.putExtra("planet", ((Planet)parent.getItemAtPosition(position)).ID);
      setResult(Activity.RESULT_OK,i);
      finish();
    }
  }

  private List<Planet> planets;
  private EveDatabase provider;
  private EICApplication application;

  @Override
  public void onCreate(Bundle savedInstanceState)
  {
    super.onCreate(savedInstanceState);
    application = (EICApplication)getApplication();
    provider = application.provider;
    planets = new ArrayList<>(provider.allPlanets());
    setContentView(R.layout.planetlist);


    ListView ls_planets = (ListView) findViewById(R.id.ls_planets);


    PlanetListAdapter planet_adapter = new PlanetListAdapter(this);
    ls_planets.setAdapter(planet_adapter);
    ls_planets.setOnItemClickListener(new PlanetListClickListener());
    setTitle("Planets");
  }
}
