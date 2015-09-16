package com.exter.eveindcalc.manufacturing;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.BaseExpandableListAdapter;
import android.widget.ExpandableListView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;

import com.exter.eveindcalc.R;
import com.exter.eveindcalc.TaskHelper;
import com.exter.eveindcalc.data.blueprint.Blueprint;
import com.exter.eveindcalc.data.blueprint.BlueprintDA;
import com.exter.eveindcalc.data.inventory.InventoryDA;
import com.exter.eveindcalc.data.inventory.Item;
import com.exter.eveindcalc.data.inventory.ItemCategory;
import com.exter.eveindcalc.data.inventory.ItemGroup;

import java.util.ArrayList;
import java.util.List;

public class BlueprintListActivity extends FragmentActivity
{
  private TextView tx_search;
  private Spinner sp_groups;
  private Spinner sp_categories;
  private Spinner sp_metagroups;
  private ExpandableListView ls_groups;
  private TextView tx_category;
  private ImageButton bt_category_clear;

  private List<Integer> groups;
  private List<Integer> categories;
  private List<Integer> metagroups;

  public int filter_category;
  public int filter_group;
  public String filter_name;
  public int filter_metagroup;
  public List<Integer> bplist;

  public List<Integer> bplist_filtered;

  public BlueprintDA blueprints;
  private FilterTask task;

  BlueprintGroupsAdapter groups_adapter;
  
  private class BlueprintListAdapter extends BaseAdapter
  {
    private LayoutInflater inflater;

    public BlueprintListAdapter(Context context)
    {
      inflater = LayoutInflater.from(context);
    }

    @Override
    public int getCount()
    {
      if(bplist_filtered == null)
      {
        return 0;
      }
      {
        return bplist_filtered.size();
      }
    }

    @Override
    public Object getItem(int position)
    {
      if(bplist_filtered == null)
      {
        return null;
      } else
      {
        return bplist_filtered.get(position);
      }
    }

    @Override
    public long getItemId(int position)
    {
      return position;
    }

    private class ItemHolder
    {
      public TextView tx_product;
      public TextView tx_category;
      public ImageView im_icon;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent)
    {
      Blueprint bp = BlueprintDA.getBlueprint(bplist_filtered.get(position));
      ItemGroup cat = InventoryDA.getGroup(bp.Product.item.getGroupID());
      ItemCategory group = InventoryDA.getCategory(cat.Category);
      ItemHolder holder;
      if(convertView == null)
      {
        convertView = inflater.inflate(R.layout.blueprint, parent, false);
        holder = new ItemHolder();
        holder.tx_product = (TextView) convertView.findViewById(R.id.tx_blueprint_product);
        holder.tx_category = (TextView) convertView.findViewById(R.id.tx_blueprint_category);
        holder.im_icon = (ImageView) convertView.findViewById(R.id.im_blueprint_icon);
        convertView.setTag(holder);
      } else
      {
        holder = (ItemHolder) convertView.getTag();
      }
      Item product = (Item)bp.Product.item;
      TaskHelper.setImageViewItemIcon(holder.im_icon, product);

      holder.tx_product.setText(product.Name);
      holder.tx_category.setText(group.Name + " / " + cat.Name);
      return convertView;
    }
  }

  private class BlueprintListClickListener implements ListView.OnItemClickListener
  {
    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id)
    {
      Intent i = new Intent();
      i.putExtra("product", (Integer) parent.getItemAtPosition(position));
      setResult(Activity.RESULT_OK, i);
      finish();
    }
  }

  public class BlueprintGroupsAdapter extends BaseExpandableListAdapter
  {
    private class ItemHolder
    {
      public TextView tx_name;
      public ImageView im_icon;
    }

    private LayoutInflater inflater;

    public BlueprintGroupsAdapter(Context context)
    {
      inflater = LayoutInflater.from(context);
    }

    @Override
    public Object getChild(int groupPosition, int childPosition)
    {
      return InventoryDA.blueprintCategories(groups.get(groupPosition - 1)).get(childPosition);
    }

    @Override
    public long getChildId(int groupPosition, int childPosition)
    {
      return childPosition;
    }

    @Override
    public View getChildView(int groupPosition, final int childPosition, boolean isLastChild, View convertView, ViewGroup parent)
    {
      ItemHolder holder;
      if(convertView == null)
      {
        convertView = inflater.inflate(R.layout.itemfilter_group, parent, false);
        holder = new ItemHolder();
        holder.tx_name = (TextView) convertView.findViewById(R.id.tx_item_name);
        holder.im_icon = (ImageView) convertView.findViewById(R.id.im_item_icon);
        convertView.setTag(holder);
      } else
      {
        holder = (ItemHolder) convertView.getTag();
      }
      ItemGroup cat = InventoryDA.getGroup((Integer) getChild(groupPosition, childPosition));
      TaskHelper.setImageViewItemIcon(holder.im_icon, cat.Icon, 0.75f);
      holder.tx_name.setText(cat.Name);
      return convertView;
    }

    @Override
    public int getChildrenCount(int groupPosition)
    {
      return groupPosition == 0?0:InventoryDA.blueprintCategories(groups.get(groupPosition - 1)).size();
    }

    @Override
    public Object getGroup(int groupPosition)
    {
      return groupPosition == 0?-1:groups.get(groupPosition - 1);
    }

    @Override
    public int getGroupCount()
    {
      return groups == null?1:groups.size() + 1;
    }

    @Override
    public long getGroupId(int groupPosition)
    {
      return groupPosition;
    }

    @Override
    public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent)
    {
      ItemHolder holder;
      if(convertView == null)
      {
        convertView = inflater.inflate(R.layout.itemfilter_groupcategory, parent, false);
        holder = new ItemHolder();
        holder.tx_name = (TextView) convertView.findViewById(R.id.tx_item_name);
        holder.im_icon = (ImageView) convertView.findViewById(R.id.im_item_icon);
        convertView.setTag(holder);
      } else
      {
        holder = (ItemHolder) convertView.getTag();
      }
      if(groupPosition == 0)
      {
        TaskHelper.setImageViewItemIcon(holder.im_icon, 9001);
        holder.tx_name.setText("All");
      } else
      {
        ItemCategory group = InventoryDA.getCategory(groups.get(groupPosition - 1));
        TaskHelper.setImageViewItemIcon(holder.im_icon, group.Icon);
        holder.tx_name.setText(group.Name);
      }

      return convertView;
    }

    @Override
    public boolean hasStableIds()
    {
      return false;
    }

    @Override
    public boolean isChildSelectable(int groupPosition, int childPosition)
    {
      return true;
    }
  }


  private class CategoryGroupClickListener implements ExpandableListView.OnGroupClickListener
  {

    @Override
    public boolean onGroupClick(ExpandableListView parent, View v, int groupPosition, long id)
    {
      v.setSelected(true);
      if(groupPosition == 0)
      {
        filter_group = -1;
        filter_category = -1;
      } else
      {
        filter_group = groups.get(groupPosition - 1);
        filter_category = -1;
      }
      updateFilter();
      return false;
    }
  }

  private class CategoryClickListener implements ExpandableListView.OnChildClickListener
  {

    @Override
    public boolean onChildClick(ExpandableListView parent, View v, int groupPosition, int childPosition, long id)
    {
      v.setSelected(true);
      if(groupPosition == 0)
      {
        filter_group = -1;
        filter_category = -1;
      } else
      {
        filter_group = groups.get(groupPosition - 1);
        filter_category = InventoryDA.blueprintCategories(filter_group).get(childPosition);
      }
      updateFilter();
      return false;
    }
  }
  
  private ListView ls_blueprints;
  private BlueprintListAdapter bpl_adapter;

  public void updateFilter()
  {
    Filter f = new Filter(filter_name, filter_category, filter_group, filter_metagroup);
    if(task != null)
    {
      task.cancel(true);
    }
    task = new FilterTask();
    task.execute(f);
  }

  private class Filter
  {
    public String name;
    public int category;
    public int group;
    public int metagroup;

    public Filter(String nm, int cat, int grp, int mg)
    {
      name = nm;
      category = cat;
      group = grp;
      metagroup = mg;
    }
  }

  private class FilterResult
  {
    public final Filter filter;
    public final List<Integer> blueprints;

    public FilterResult(Filter f, List<Integer> bps)
    {
      filter = f;
      blueprints = bps;
    }
  }

  private class FilterTask extends AsyncTask<Filter, Integer, FilterResult>
  {
    private List<Integer> applyFilter(Filter filter, BlueprintDA src)
    {
      if(isCancelled())
      {
        return null;
      }

      ArrayList<Integer> products = new ArrayList<Integer>();
      Cursor c = src.queryBlueprints(filter.name, filter.category, filter.group, filter.metagroup);
      if(c != null)
      {
        while(c.moveToNext())
        {
          if(this.isCancelled())
          {
            break;
          }
          products.add(c.getInt(0));
        }
        c.close();
      }
      return products;
    }

    @Override
    protected FilterResult doInBackground(Filter... filters)
    {
      if(filters.length != 1)
      {
        return null;
      }

      Filter filter = filters[0];

      return new FilterResult(filter, applyFilter(filter, blueprints));
    }

    @Override
    protected void onProgressUpdate(Integer... progress)
    {

    }

    @Override
    protected void onPostExecute(FilterResult result)
    {
      task = null;
      bplist_filtered = result.blueprints;
      bpl_adapter.notifyDataSetChanged();
      if(tx_category != null)
      {
        int c = result.filter.category;
        int g = result.filter.group;
        
        String text;
        if(g >= 0)
        {
          text = InventoryDA.getCategory(g).Name;
          if(c >= 0)
          {
            text += " / " + InventoryDA.getGroup(c).Name;
          }
          bt_category_clear.setEnabled(true);
        } else
        {
          text = "All getAllBlueprints";
          bt_category_clear.setEnabled(false);
        }        
        tx_category.setText(text);
      }
    }
  }

  private class SearchChangeListener implements TextWatcher
  {

    @SuppressLint("DefaultLocale")
    @Override
    public void afterTextChanged(Editable s)
    {
      filter_name = s.toString().toLowerCase();
      updateFilter();
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

  private class GroupSelectedListener implements Spinner.OnItemSelectedListener
  {
    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int pos, long id)
    {
      ArrayList<CharSequence> category_list = new ArrayList<CharSequence>();
      category_list.add("All");
      if(pos == 0)
      {
        filter_group = -1;
        categories = new ArrayList<Integer>();
      } else
      {
        filter_group = groups.get(pos - 1);
        categories = new ArrayList<Integer>(InventoryDA.blueprintCategories(filter_group));
        for(int cat : categories)
        {
          category_list.add(InventoryDA.getGroup(cat).Name);
        }
      }
      filter_category = -1;
      ArrayAdapter<CharSequence> category_adapter = new ArrayAdapter<CharSequence>(BlueprintListActivity.this, android.R.layout.simple_spinner_item, category_list);
      category_adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
      sp_categories.setAdapter(category_adapter);
      sp_categories.setSelection(0, true);
      updateFilter();
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent)
    {

    }
  }

  private class CategorySelectedListener implements Spinner.OnItemSelectedListener
  {
    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int pos, long id)
    {
      if(pos == 0)
      {
        filter_category = -1;
      } else
      {
        filter_category = categories.get(pos - 1);
      }
      updateFilter();
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent)
    {
    }
  }

  private class MetaGroupSelectedListener implements Spinner.OnItemSelectedListener
  {
    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int pos, long id)
    {
      if(pos == 0)
      {
        filter_metagroup = -1;
      } else
      {
        filter_metagroup = metagroups.get(pos - 1);
      }
      updateFilter();
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent)
    {
    }
  }

  private class CategoryClearClickListener implements ImageButton.OnClickListener
  {

    @Override
    public void onClick(View v)
    {
      filter_group = -1;
      filter_category = -1;
      updateFilter();
    }
  }
  @Override
  public void onCreate(Bundle savedInstanceState)
  {
    super.onCreate(savedInstanceState);

    blueprints = new BlueprintDA();
    groups = InventoryDA.blueprintGroups();
    categories = new ArrayList<Integer>();
    metagroups = InventoryDA.metaGroups();
    filter_name = null;
    filter_group = -1;
    filter_category = -1;
    filter_metagroup = -1;

    setContentView(R.layout.itemfilter);

    bplist = blueprints.getAllBlueprints();

    tx_search = (TextView) findViewById(R.id.tx_itemfilter_search);
    sp_metagroups = (Spinner) findViewById(R.id.sp_itemfilter_metagroup);
    tx_search.addTextChangedListener(new SearchChangeListener());


    ls_groups = (ExpandableListView) findViewById(R.id.ls_itemfilter_groups);
    if(ls_groups == null)
    {
      sp_groups = (Spinner) findViewById(R.id.sp_itemfilter_group);
      sp_categories = (Spinner) findViewById(R.id.sp_itemfilter_category);

      ArrayList<CharSequence> group_list = new ArrayList<CharSequence>();
      group_list.add("All");
      for(int g : groups)
      {
        group_list.add(InventoryDA.getCategory(g).Name);
      }

      ArrayList<CharSequence> category_list = new ArrayList<CharSequence>();
      category_list.add("All");


      ArrayAdapter<CharSequence> group_adapter = new ArrayAdapter<CharSequence>(this, android.R.layout.simple_spinner_item, group_list);
      group_adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
      ArrayAdapter<CharSequence> category_adapter = new ArrayAdapter<CharSequence>(this, android.R.layout.simple_spinner_item, category_list);
      category_adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);


      sp_groups.setAdapter(group_adapter);
      sp_categories.setAdapter(category_adapter);
      sp_groups.setOnItemSelectedListener(new GroupSelectedListener());
      sp_categories.setOnItemSelectedListener(new CategorySelectedListener());
    } else
    {
      groups_adapter = new BlueprintGroupsAdapter(this);
      ls_groups.setAdapter(groups_adapter);
      ls_groups.setOnChildClickListener(new CategoryClickListener());
      ls_groups.setOnGroupClickListener(new CategoryGroupClickListener());
      ls_groups.setSelection(0);
      
      tx_category = (TextView) findViewById(R.id.tx_itemfilter_category);
      bt_category_clear = (ImageButton) findViewById(R.id.bt_itemfilter_category_clear);
      bt_category_clear.setEnabled(false);
      bt_category_clear.setOnClickListener(new CategoryClearClickListener());
    }

    ArrayList<CharSequence> metagroup_list = new ArrayList<CharSequence>();
    metagroup_list.add("All");
    for(int m : metagroups)
    {
      metagroup_list.add(InventoryDA.getMetaGroup(m).Name);
    }
    ArrayAdapter<CharSequence> metagroup_adapter = new ArrayAdapter<CharSequence>(this, android.R.layout.simple_spinner_item, metagroup_list);
    metagroup_adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
    sp_metagroups.setOnItemSelectedListener(new MetaGroupSelectedListener());
    sp_metagroups.setAdapter(metagroup_adapter);

    ls_blueprints = (ListView) findViewById(R.id.ls_itemfilter_items);

    bpl_adapter = new BlueprintListAdapter(this);
    ls_blueprints.setAdapter(bpl_adapter);
    ls_blueprints.setOnItemClickListener(new BlueprintListClickListener());


    setTitle("Manufacturing");
  }

  @Override
  protected void onPause()
  {
    cancelTask();
    super.onPause();
  }

  @Override
  protected void onResume()
  {
    super.onResume();
    updateFilter();
  }

  public void cancelTask()
  {
    if(task != null)
    {
      task.cancel(true);
    }
  }
}
