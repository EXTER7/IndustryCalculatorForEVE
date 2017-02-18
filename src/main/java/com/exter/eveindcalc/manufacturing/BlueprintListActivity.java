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
import com.exter.eveindcalc.data.EveDatabase;

import java.util.ArrayList;
import java.util.List;

import exter.eveindustry.data.blueprint.Blueprint;
import exter.eveindustry.data.item.Item;
import exter.eveindustry.data.item.ItemCategory;
import exter.eveindustry.data.item.ItemGroup;
import exter.eveindustry.data.item.ItemMetaGroup;
import exter.eveindustry.task.TaskFactory;

public class BlueprintListActivity extends FragmentActivity
{
  private TextView tx_category;
  private ImageButton bt_category_clear;

  private ItemCategory filter_category;
  private ItemGroup filter_group;
  private String filter_name;
  private ItemMetaGroup filter_metagroup;

  private List<Integer> bplist_filtered;

  public EveDatabase database;
  public TaskFactory factory;
  private FilterTask task;

  private class BlueprintListAdapter extends BaseAdapter
  {
    private LayoutInflater inflater;

    BlueprintListAdapter(Context context)
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
      TextView tx_product;
      TextView tx_category;
      ImageView im_icon;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent)
    {
      Blueprint bp = factory.blueprints.get(bplist_filtered.get(position));
      assert bp != null;
      Item product = bp.product.item;
      ItemGroup cat = factory.item_groups.get(product.group_id);
      ItemCategory group = factory.item_categories.get(cat.category_id);
      ItemHolder holder;
      if(convertView == null)
      {
        if(application.useTableUI())
        {
          convertView = inflater.inflate(R.layout.blueprint_xlarge, parent, false);
        } else
        {
          convertView = inflater.inflate(R.layout.blueprint, parent, false);
        }
        holder = new ItemHolder();
        holder.tx_product = (TextView) convertView.findViewById(R.id.tx_blueprint_product);
        holder.tx_category = (TextView) convertView.findViewById(R.id.tx_blueprint_category);
        holder.im_icon = (ImageView) convertView.findViewById(R.id.im_blueprint_icon);
        convertView.setTag(holder);
      } else
      {
        holder = (ItemHolder) convertView.getTag();
      }
      application.setImageViewItemIcon(holder.im_icon, product);

      holder.tx_product.setText(product.name);
      holder.tx_category.setText(String.format("%s / %s", group.name, cat.name));
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

  private class BlueprintGroupsAdapter extends BaseExpandableListAdapter implements ExpandableListView.OnGroupClickListener,ExpandableListView.OnChildClickListener
  {
    private class Holder
    {
      TextView tx_name;
      ImageView im_icon;
    }

    private LayoutInflater inflater;

    BlueprintGroupsAdapter(Context context)
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
      application.setImageViewItemIcon(holder.im_icon, group.icon_id, 0.75f);
      holder.tx_name.setText(group.name);
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
        application.setImageViewItemIcon(holder.im_icon, 9001);
        holder.tx_name.setText("All");
      } else
      {
        ItemCategory category = categories.get(groupPosition - 1);
        application.setImageViewItemIcon(holder.im_icon, category.icon_id);
        holder.tx_name.setText(category.name);
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

  private void updateFilter()
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
    ItemMetaGroup metagroup;

    Filter(String nm, ItemCategory cat, ItemGroup grp, ItemMetaGroup mg)
    {
      name = nm !=null?nm.toLowerCase():null;
      category = cat;
      group = grp;
      metagroup = mg;
    }

    Cursor query()
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
        query = query + " AND blueprints.gid = " + String.valueOf(group.id);
      }
      if(category != null)
      {
        query = query + " AND groups.cid = " + String.valueOf(category.id);
      }
      if(metagroup != null)
      {
        query = query + " AND blueprints.mgid = " + String.valueOf(metagroup.id);
      }
      query = query + " ORDER BY blueprints.name;";
      return database.getDatabase().rawQuery(query, null);
    }
  }

  private class FilterResult
  {
    // Filter used for this result.
    final Filter filter;
    // List of blueprint that match the filer.
    public final List<Integer> blueprints;

    FilterResult(Filter f, List<Integer> bps)
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
          text = c.name;
          if(g != null)
          {
            text += " / " + g.name;
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

  private EICApplication application;

  private Spinner sp_groups;

  private SparseArray<List<ItemGroup>> category_groups = new SparseArray<>();

  private List<ItemGroup> getGroups(ItemCategory category)
  {
    List<ItemGroup> group = category_groups.get(category.id);
    if(group == null)
    {
      group = loadCategoryGroups(category);
      category_groups.put(category.id,group);
    }
    return group;
  }

  private List<ItemGroup> loadCategoryGroups(ItemCategory category)
  {
    List<ItemGroup> groups = new ArrayList<>();
    Cursor c = database.getDatabase().query("groups",new String[] {"id"},"cid = ?",new String[] {String.valueOf(category.id)},null,null,"name");
    while(c.moveToNext())
    {
      groups.add(factory.item_groups.get(c.getInt(0)));
    }
    c.close();
    return groups;
  }

  @Override
  public void onCreate(Bundle savedInstanceState)
  {
    super.onCreate(savedInstanceState);

    application = (EICApplication)getApplication();
    factory = application.factory;
    database = application.database;

    if(categories == null)
    {
      categories = new ArrayList<>();
      metagroups = new ArrayList<>();
      Cursor c = database.getDatabase().query("categories",new String[] {"id"},null,null,null,null,"name");
      while(c.moveToNext())
      {
        categories.add(factory.item_categories.get(c.getInt(0)));
      }
      c.close();
      c = database.getDatabase().query("metagroups",new String[] {"id"},null,null,null,null,null);
      while(c.moveToNext())
      {
        metagroups.add(factory.item_metagroups.get(c.getInt(0)));
      }
      c.close();
    }

    filter_name = null;
    filter_group = null;
    filter_category = null;
    filter_metagroup = null;

    if(application.useTableUI())
    {
      setContentView(R.layout.itemfilter_xlarge);
    } else
    {
      setContentView(R.layout.itemfilter);
    }


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
        category_names.add(category.name);
      }
      ArrayAdapter<CharSequence> category_adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, category_names);
      category_adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
      sp_categories.setAdapter(category_adapter);

      updateGroupSpinner();

      sp_groups.setOnItemSelectedListener(new GroupSelectedListener());
      sp_categories.setOnItemSelectedListener(new CategorySelectedListener());
    } else
    {
      BlueprintGroupsAdapter groups_adapter = new BlueprintGroupsAdapter(this);
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
      metagroup_list.add(m.name);
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
        group_list.add(group.name);
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

  private void cancelTask()
  {
    if(task != null)
    {
      task.cancel(true);
    }
  }
}
