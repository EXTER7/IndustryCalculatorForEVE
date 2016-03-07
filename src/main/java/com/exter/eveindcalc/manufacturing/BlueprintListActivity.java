package com.exter.eveindcalc.manufacturing;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.SparseArray;
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

import com.exter.eveindcalc.EICApplication;
import com.exter.eveindcalc.R;
import com.exter.eveindcalc.TaskHelper;
import com.exter.eveindcalc.data.EveDatabase;

import java.util.ArrayList;
import java.util.List;

import exter.eveindustry.dataprovider.blueprint.Blueprint;
import exter.eveindustry.dataprovider.item.Item;
import exter.eveindustry.dataprovider.item.ItemCategory;
import exter.eveindustry.dataprovider.item.ItemGroup;
import exter.eveindustry.dataprovider.item.ItemMetaGroup;

public class BlueprintListActivity extends FragmentActivity
{
  private TextView tx_category;
  private ImageButton bt_category_clear;

  public ItemCategory filter_category;
  public ItemGroup filter_group;
  public String filter_name;
  public ItemMetaGroup filter_metagroup;

  public List<Integer> bplist_filtered;

  public EveDatabase provider;
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
      Blueprint bp = provider.getBlueprint(bplist_filtered.get(position));
      assert bp != null;
      ItemGroup cat = provider.getItemGroup(bp.Product.item.getGroupID());
      ItemCategory group = provider.getItemCategory(cat.Category);
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
      holder.tx_category.setText(String.format("%s / %s", group.Name, cat.Name));
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

  public class BlueprintGroupsAdapter extends BaseExpandableListAdapter implements ExpandableListView.OnGroupClickListener,ExpandableListView.OnChildClickListener
  {
    private class Holder
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
      if(groupPosition == 0)
      {
        return null;
      }
      return getGroups(categories.get(groupPosition - 1)).get(childPosition);
    }

    @Override
    public long getChildId(int groupPosition, int childPosition)
    {
      return childPosition;
    }

    @Override
    public View getChildView(int groupPosition, final int childPosition, boolean isLastChild, View convertView, ViewGroup parent)
    {
      Holder holder;
      if(convertView == null)
      {
        convertView = inflater.inflate(R.layout.itemfilter_group, parent, false);
        holder = new Holder();
        holder.tx_name = (TextView) convertView.findViewById(R.id.tx_item_name);
        holder.im_icon = (ImageView) convertView.findViewById(R.id.im_item_icon);
        convertView.setTag(holder);
      } else
      {
        holder = (Holder) convertView.getTag();
      }
      ItemGroup group = (ItemGroup) getChild(groupPosition, childPosition);
      TaskHelper.setImageViewItemIcon(holder.im_icon, group.Icon, 0.75f);
      holder.tx_name.setText(group.Name);
      return convertView;
    }

    @Override
    public int getChildrenCount(int groupPosition)
    {
      if(groupPosition == 0)
      {
        return 0;
      }
      return getGroups(categories.get(groupPosition - 1)).size();
    }

    @Override
    public Object getGroup(int groupPosition)
    {
      return groupPosition == 0?null: categories.get(groupPosition - 1);
    }

    @Override
    public int getGroupCount()
    {
      return categories == null?1: categories.size() + 1;
    }

    @Override
    public long getGroupId(int groupPosition)
    {
      return groupPosition;
    }

    @Override
    public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent)
    {
      Holder holder;
      if(convertView == null)
      {
        convertView = inflater.inflate(R.layout.itemfilter_groupcategory, parent, false);
        holder = new Holder();
        holder.tx_name = (TextView) convertView.findViewById(R.id.tx_item_name);
        holder.im_icon = (ImageView) convertView.findViewById(R.id.im_item_icon);
        convertView.setTag(holder);
      } else
      {
        holder = (Holder) convertView.getTag();
      }
      if(groupPosition == 0)
      {
        TaskHelper.setImageViewItemIcon(holder.im_icon, 9001);
        holder.tx_name.setText("All");
      } else
      {
        ItemCategory category = categories.get(groupPosition - 1);
        TaskHelper.setImageViewItemIcon(holder.im_icon, category.Icon);
        holder.tx_name.setText(category.Name);
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

    @Override
    public boolean onGroupClick(ExpandableListView parent, View v, int groupPosition, long id)
    {
      v.setSelected(true);
      if(groupPosition == 0)
      {
        filter_group = null;
        filter_category = null;
      } else
      {
        filter_group = null;
        filter_category = categories.get(groupPosition - 1);
      }
      updateFilter();
      return false;
    }

    @Override
    public boolean onChildClick(ExpandableListView parent, View v, int groupPosition, int childPosition, long id)
    {
      v.setSelected(true);
      if(groupPosition == 0)
      {
        filter_group = null;
        filter_category = null;
      } else
      {
        ItemCategory category = categories.get(groupPosition - 1);
        filter_group = getGroups(category).get(childPosition);
        filter_category = category;
      }
      updateFilter();
      return false;
    }
  }

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

  //Contains all filter parameters
  private class Filter
  {
    public String name;
    public ItemCategory category;
    public ItemGroup group;
    public ItemMetaGroup metagroup;

    public Filter(String nm, ItemCategory cat, ItemGroup grp, ItemMetaGroup mg)
    {
      name = nm !=null?nm.toLowerCase():null;
      category = cat;
      group = grp;
      metagroup = mg;
    }

    public Cursor query()
    {
      String query = "SELECT blueprints.id FROM blueprints,groups WHERE groups.id = blueprints.gid";
      if(name != null && name.length() > 2)
      {
        String[] tokens = name.split(" ");
        for(String t:tokens)
        {
          if(t != null && t.length() > 1)
          {
            t = DatabaseUtils.sqlEscapeString(t);
            query = query + " AND blueprints.name LIKE '%" + t.substring(1, t.length() - 1) + "%'";
          }
        }
      }
      if(group != null)
      {
        query = query + " AND blueprints.gid = " + String.valueOf(group.ID);
      }
      if(category != null)
      {
        query = query + " AND groups.cid = " + String.valueOf(category.ID);
      }
      if(metagroup != null)
      {
        query = query + " AND blueprints.mgid = " + String.valueOf(metagroup.ID);
      }
      query = query + " ORDER BY blueprints.name;";
      return EveDatabase.getDatabase().rawQuery(query, null);
    }
  }

  private class FilterResult
  {
    // Filter used for this result.
    public final Filter filter;
    // List of blueprint that match the filer.
    public final List<Integer> blueprints;

    public FilterResult(Filter f, List<Integer> bps)
    {
      filter = f;
      blueprints = bps;
    }
  }

  private class FilterTask extends AsyncTask<Filter, Integer, FilterResult>
  {
    private List<Integer> applyFilter(Filter filter)
    {
      if(isCancelled())
      {
        return null;
      }

      List<Integer> products = new ArrayList<>();
      Cursor c = filter.query();
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

      return new FilterResult(filter, applyFilter(filter));
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
        ItemCategory c = result.filter.category;
        ItemGroup g = result.filter.group;
        
        String text;
        if(c != null)
        {
          text = c.Name;
          if(g != null)
          {
            text += " / " + g.Name;
          }
          bt_category_clear.setEnabled(true);
        } else
        {
          text = "All";
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
      filter_name = s.toString();
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
      if(pos == 0)
      {
        filter_group = null;
      } else
      {
        filter_group = getGroups(filter_category).get(pos - 1);
      }
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
        filter_category = null;
      } else
      {
        filter_category = categories.get(pos - 1);
      }
      updateGroupSpinner();
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
        filter_metagroup = null;
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
      filter_group = null;
      filter_category = null;
      updateFilter();
    }
  }

  static private List<ItemCategory> categories = null;
  static private List<ItemMetaGroup> metagroups = null;

  private Spinner sp_groups;

  private SparseArray<List<ItemGroup>> category_groups = new SparseArray<>();

  public List<ItemGroup> getGroups(ItemCategory category)
  {
    List<ItemGroup> group = category_groups.get(category.ID);
    if(group == null)
    {
      group = loadCategoryGroups(category);
      category_groups.put(category.ID,group);
    }
    return group;
  }

  private List<ItemGroup> loadCategoryGroups(ItemCategory category)
  {
    List<ItemGroup> groups = new ArrayList<>();
    Cursor c = EveDatabase.getDatabase().query("groups",new String[] {"id"},"cid = ?",new String[] {String.valueOf(category.ID)},null,null,"name");
    while(c.moveToNext())
    {
      groups.add(provider.getItemGroup(c.getInt(0)));
    }
    c.close();
    return groups;
  }

  @Override
  public void onCreate(Bundle savedInstanceState)
  {
    super.onCreate(savedInstanceState);

    provider = EICApplication.getDataProvider();

    if(categories == null)
    {
      categories = new ArrayList<>();
      metagroups = new ArrayList<>();
      Cursor c = EveDatabase.getDatabase().query("categories",new String[] {"id"},null,null,null,null,"name");
      while(c.moveToNext())
      {
        categories.add(provider.getItemCategory(c.getInt(0)));
      }
      c.close();
      c = EveDatabase.getDatabase().query("metagroups",new String[] {"id"},null,null,null,null,null);
      while(c.moveToNext())
      {
        metagroups.add(provider.getItemMetaGroup(c.getInt(0)));
      }
      c.close();
    }

    filter_name = null;
    filter_group = null;
    filter_category = null;
    filter_metagroup = null;

    setContentView(R.layout.itemfilter);


    TextView tx_search = (TextView) findViewById(R.id.tx_itemfilter_search);
    Spinner sp_metagroups = (Spinner) findViewById(R.id.sp_itemfilter_metagroup);
    tx_search.addTextChangedListener(new SearchChangeListener());


    ExpandableListView ls_groups = (ExpandableListView) findViewById(R.id.ls_itemfilter_groups);
    if(ls_groups == null)
    {
      sp_groups = (Spinner) findViewById(R.id.sp_itemfilter_group);
      Spinner sp_categories = (Spinner) findViewById(R.id.sp_itemfilter_category);


      ArrayList<CharSequence> category_names = new ArrayList<>();
      category_names.add("All");
      for(ItemCategory category:categories)
      {
        category_names.add(category.Name);
      }
      ArrayAdapter<CharSequence> category_adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, category_names);
      category_adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
      sp_categories.setAdapter(category_adapter);

      updateGroupSpinner();

      sp_groups.setOnItemSelectedListener(new GroupSelectedListener());
      sp_categories.setOnItemSelectedListener(new CategorySelectedListener());
    } else
    {
      groups_adapter = new BlueprintGroupsAdapter(this);
      ls_groups.setAdapter(groups_adapter);
      ls_groups.setOnChildClickListener(groups_adapter);
      ls_groups.setOnGroupClickListener(groups_adapter);
      ls_groups.setSelection(0);
      
      tx_category = (TextView) findViewById(R.id.tx_itemfilter_category);
      bt_category_clear = (ImageButton) findViewById(R.id.bt_itemfilter_category_clear);
      bt_category_clear.setEnabled(false);
      bt_category_clear.setOnClickListener(new CategoryClearClickListener());
    }

    ArrayList<CharSequence> metagroup_list = new ArrayList<>();
    metagroup_list.add("All");
    for(ItemMetaGroup m : metagroups)
    {
      metagroup_list.add(m.Name);
    }
    ArrayAdapter<CharSequence> metagroup_adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, metagroup_list);
    metagroup_adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
    sp_metagroups.setOnItemSelectedListener(new MetaGroupSelectedListener());
    sp_metagroups.setAdapter(metagroup_adapter);

    ListView ls_blueprints = (ListView) findViewById(R.id.ls_itemfilter_items);

    bpl_adapter = new BlueprintListAdapter(this);
    ls_blueprints.setAdapter(bpl_adapter);
    ls_blueprints.setOnItemClickListener(new BlueprintListClickListener());


    setTitle("Manufacturing");
  }

  private void updateGroupSpinner()
  {
    ArrayList<CharSequence> group_list = new ArrayList<>();
    group_list.add("All");
    filter_group = null;
    if(filter_category != null)
    {
      for (ItemGroup group : getGroups(filter_category))
      {
        group_list.add(group.Name);
      }
    }
    ArrayAdapter<CharSequence> group_adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, group_list);
    group_adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
    sp_groups.setAdapter(group_adapter);
    sp_groups.setSelection(0);

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
