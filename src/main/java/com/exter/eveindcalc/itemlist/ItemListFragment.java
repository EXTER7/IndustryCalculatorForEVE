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
import com.exter.eveindcalc.TaskHelper;

import java.util.ArrayList;
import java.util.List;

import exter.eveindustry.dataprovider.index.Index;
import exter.eveindustry.dataprovider.item.Item;

public class ItemListFragment extends Fragment
{

  private class ItemListAdapter extends BaseAdapter
  {
    private LayoutInflater inflater;

    public ItemListAdapter(Context context)
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
      Item prod = EICApplication.getDataProvider().getItem(items.get(position));
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

      TaskHelper.setImageViewItemIcon(holder.im_icon, prod);
      holder.tx_name.setText(prod.Name);
      return convertView;
    }
  }

  private ItemListActivity activity;

  private ListView ls_items;
  private ItemListAdapter itemlist_adapter;
  private List<Integer> items;
  
  public void onFragmentPause()
  {
    ls_items.setAdapter(null);
  }
  
  public void onFragmentResume()
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
  
  public void SetGroup(int group)
  {
    Index index = activity.getIndex();
    if(index == null)
    {
      return;
    }
    items = new ArrayList<>();
    for(Index.Entry e:index.getEntries())
    {
      if(e.Group == group)
      {
        items.add(e.ItemID);
      }
    }
    itemlist_adapter.notifyDataSetChanged();
  }

  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
  {
    activity = (ItemListActivity)getActivity();
    View rootView = inflater.inflate(R.layout.itemlist_main, container, false);

    ls_items = (ListView)rootView.findViewById(R.id.ls_itemlist_items);
    
    itemlist_adapter = new ItemListAdapter(activity);
    ls_items.setAdapter(itemlist_adapter);
    Bundle args = getArguments();
    if(args != null)
    {
      SetGroup(args.getInt("group",0));
    } else
    {
      SetGroup(0);
    }
    ls_items.setOnItemClickListener(new ItemListClickListener());
    return rootView;
  }
}
