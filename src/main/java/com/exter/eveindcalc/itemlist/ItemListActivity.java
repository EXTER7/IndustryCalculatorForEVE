package com.exter.eveindcalc.itemlist;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
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

import exter.eveindustry.dataprovider.index.Index;


public abstract class ItemListActivity extends FragmentActivity
{

  private class GroupsAdapter extends BaseAdapter
  {
    private LayoutInflater inflater;

    GroupsAdapter(Context context)
    {
      inflater = LayoutInflater.from(context);      
    }

    @Override
    public int getCount()
    {
      return index.getGroups().size();
    }

    @Override
    public Object getItem(int position)
    {
      return index.getGroups().get(position);
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
      Index.Group group = index.getGroups().get(position);
      ViewHolder holder;
      if (convertView == null)
      {
        holder = new ViewHolder();
        convertView = inflater.inflate(R.layout.itemgroup, parent, false);
        holder.tx_name = (TextView)convertView.findViewById(R.id.tx_item_name);
        holder.im_icon = (ImageView)convertView.findViewById(R.id.im_item_icon);
        convertView.setTag(holder);
      } else
      {
        holder = (ViewHolder)convertView.getTag();
      }

      EICApplication application = (EICApplication)getApplication();
      for(Index.Entry e:index.getEntries())
      {
        if(e.Group == group.ID)
        {
          application.setImageViewItemIcon(holder.im_icon, application.provider.getItem(e.ItemID));
          break;
        }
      }
      holder.tx_name.setText(group.Name);
      return convertView;
    }
  }


  private class GroupsClickListener implements ListView.OnItemClickListener
  {
    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id)
    {
      view.setSelected(true);
      item_list.setGroup(index.getGroups().get(position).ID);
    }
  }
  private Index index;


  protected abstract void onPickItem(int item);
  
  protected abstract String getListTitle();

  protected abstract Index loadIndex();

  private PagerAdapter pager_adapter;
  private ItemListFragment item_list;
  private GroupsAdapter groups_adapter;
  private class PagerAdapter extends FragmentStatePagerAdapter
  {
    private ItemListFragment[] fragments;
    PagerAdapter(FragmentManager fm)
    {
      super(fm);
      fragments = new ItemListFragment[getCount()];
    }
    

    @Override
    public Fragment getItem(int pos)
    {
      ItemListFragment fragment = new ItemListFragment();
      Bundle args = new Bundle();
      args.putInt("group", index.getGroups().get(pos).ID);
      fragment.setArguments(args);
      fragments[pos] = fragment;
      return fragment;
    }


    @Override
    public void destroyItem(ViewGroup container, int position, Object object)
    {
      super.destroyItem(container, position, object);
      fragments[position] = null;
    }

    @Override
    public int getCount()
    {
      return index.getGroups().size();
    }

    @Override
    public CharSequence getPageTitle(int position)
    {
      return index.getGroups().get(position).Name;
    }
    
    
    void puaseFragments()
    {
      int i;
      for(i = 0; i < getCount(); i++)
      {
        ItemListFragment f = fragments[i];
        if(f != null)
        {
          f.onFragmentPause();
        }
      }
    }

    void resumeFragments()
    {
      int i;
      for(i = 0; i < getCount(); i++)
      {
        ItemListFragment f = fragments[i];
        if(f != null)
        {
          f.onFragmentResume();
        }
      }
    }
  }
  
  
  @Override
  public final void onCreate(Bundle savedInstanceState)
  {
    super.onCreate(savedInstanceState);

    setContentView(R.layout.itemlist);
    
    
    index = loadIndex();

    ViewPager pager = (ViewPager) findViewById(R.id.itemlist_pager);
    if(pager == null)
    {
      ListView ls_groups = (ListView) findViewById(R.id.ls_itemlist_categories);
      item_list = (ItemListFragment)getSupportFragmentManager().findFragmentById(R.id.fragment_itemlist);
      groups_adapter = new GroupsAdapter(this);
      ls_groups.setAdapter(groups_adapter);
      ls_groups.setOnItemClickListener(new GroupsClickListener());
      item_list.setGroup(0);
      ls_groups.setSelection(0);
    } else
    {
      pager_adapter = new PagerAdapter(getSupportFragmentManager());
      pager.setAdapter(pager_adapter);
    }
    setTitle(getListTitle());
  }
  
  @Override
  protected final void onPause()
  {
    if(pager_adapter != null)
    {
      pager_adapter.puaseFragments();
    }
    super.onPause();
  }

  @Override
  protected final void onResume()
  {
    super.onResume();
    if(pager_adapter != null)
    {
      pager_adapter.resumeFragments();
    }
  }
  
  public final Index getIndex()
  {
    return index;
  }
}
