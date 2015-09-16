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

import com.exter.eveindcalc.R;
import com.exter.eveindcalc.TaskHelper;
import com.exter.eveindcalc.data.inventory.InventoryDA;
import com.exter.eveindcalc.data.inventory.Item;
import com.exter.eveindcalc.data.planet.Planet;
import com.exter.eveindcalc.data.planet.PlanetDA;

import java.util.List;

import exter.eveindustry.data.inventory.IItem;

public class PlanetListActivity extends FragmentActivity
{
  
  @Override
  public void onCreate(Bundle savedInstanceState)
  {
    super.onCreate(savedInstanceState);
    planet_ids = PlanetDA.getPlanetIDs();
    setContentView(R.layout.planetlist);


    ListView ls_planets = (ListView) findViewById(R.id.ls_planets);
    
    //activity.bplist = BlueprintData.getAllBlueprints();

    PlanetListAdapter planet_adapter = new PlanetListAdapter(this);
    ls_planets.setAdapter(planet_adapter);
    ls_planets.setOnItemClickListener(new PlanetListClickListener());
    setTitle("Planets");
  }
  
  private class PlanetListAdapter extends BaseAdapter
  {
    private LayoutInflater inflater;

    public PlanetListAdapter(Context context)
    {
      inflater = LayoutInflater.from(context);      
    }
      
    @Override
    public int getCount()
    {
      if(planet_ids == null)
      {
        return 0;
      }
      {
        return planet_ids.size();
      }
    }
       
    @Override
    public Object getItem(int position)
    {
      if(planet_ids == null)
      {
         return null;
      } else
      {
        return planet_ids.get(position);        
      }
    }
       
    
    @Override
    public long getItemId(int position)
    {
      return position;
    }
    
    private class ItemHolder
    {
      public TextView tx_name;
      public LinearLayout ly_resources;
      public ImageView im_icon;
    }
         
    @Override
    public View getView(int position, View convertView, ViewGroup parent)
    {
      Planet planet = PlanetDA.getPlanet(planet_ids.get(position));
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
      TaskHelper.setImageViewItemIcon(holder.im_icon, InventoryDA.getItem(planet.ID));

      holder.tx_name.setText(planet.TypeName);
      holder.ly_resources.removeAllViews();
      for(IItem res:planet.Resources)
      {
        View v = inflater.inflate(R.layout.planet_info_resource, holder.ly_resources, false);
        ImageView res_icon = (ImageView)v.findViewById(R.id.im_planet_info_resource);
        TaskHelper.setImageViewItemIcon(res_icon, (Item) res);
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
      i.putExtra("planet", (Integer)parent.getItemAtPosition(position));
      setResult(Activity.RESULT_OK,i);
      finish();
    }
  }

  private List<Integer> planet_ids;

}
