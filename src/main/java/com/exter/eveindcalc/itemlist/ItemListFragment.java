package com.exter.eveindcalc.itemlist;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.exter.eveindcalc.EICApplication;
import com.exter.eveindcalc.R;
import com.exter.eveindcalc.data.Index;

import java.util.ArrayList;
import java.util.List;

import exter.eveindustry.data.item.Item;


public class ItemListFragment extends Fragment
{

  private class ItemListAdapter extends BaseAdapter
  {
    private LayoutInflater inflater;

    ItemListAdapter(Context context)
    {
      inflater = LayoutInflater.from(context);      
    }

    @Override
    public int getCount()
    {
      return items == null?0:items.size();
    }

    @Override
    public Object getItem(int position)
    {
      return items.get(position);        
    }

    @Override
    public long getItemId(int position)
    {
      return position;
    }
    
    private class ViewHolder
    {
      TextView tx_name;
      ImageView im_icon;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent)
    {
      Item prod = application.factory.items.get(items.get(position));
      ViewHolder holder;
      if (convertView == null)
      {
        holder = new ViewHolder();
        convertView = inflater.inflate(R.layout.item, parent, false);
        holder.tx_name = (TextView)convertView.findViewById(R.id.tx_item_name);
        holder.im_icon = (ImageView)convertView.findViewById(R.id.im_item_icon);
        convertView.setTag(holder);
      } else
      {
        holder = (ViewHolder)convertView.getTag();
      }

      application.setImageViewItemIcon(holder.im_icon, prod);
      holder.tx_name.setText(prod.name);
      return convertView;
    }
  }

  private ItemListActivity activity;
  private EICApplication application;

  private ListView ls_items;
  private ItemListAdapter itemlist_adapter;
  private List<Integer> items;
  
  void onFragmentPause()
  {
    ls_items.setAdapter(null);
  }
  
  void onFragmentResume()
  {
    ls_items.setAdapter(itemlist_adapter);
  }

  private class ItemListClickListener implements ListView.OnItemClickListener
  {
    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id)
    {
      activity.onPickItem(items.get(position));
    }
  }
  
  void setGroup(int group)
  {
    Index index = activity.getIndex();
    if(index == null)
    {
      return;
    }
    items = new ArrayList<>();
    for(Index.Entry e:index.getEntries())
    {
      if(e.group == group)
      {
        items.add(e.item_id);
      }
    }
    itemlist_adapter.notifyDataSetChanged();
  }

  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
  {
    activity = (ItemListActivity)getActivity();
    application = (EICApplication) activity.getApplication();
    View rootView = inflater.inflate(R.layout.itemlist_main, container, false);

    ls_items = (ListView)rootView.findViewById(R.id.ls_itemlist_items);
    
    itemlist_adapter = new ItemListAdapter(activity);
    ls_items.setAdapter(itemlist_adapter);
    Bundle args = getArguments();
    if(args != null)
    {
      setGroup(args.getInt("group",0));
    } else
    {
      setGroup(0);
    }
    ls_items.setOnItemClickListener(new ItemListClickListener());
    return rootView;
  }
}
