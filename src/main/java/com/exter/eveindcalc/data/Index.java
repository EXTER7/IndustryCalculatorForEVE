package com.exter.eveindcalc.data;

import android.util.SparseArray;

import com.exter.eveindcalc.data.exception.EveDataException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import exter.eveindustry.data.inventory.IItem;
import exter.tsl.InvalidTSLException;
import exter.tsl.TSLObject;
import exter.tsl.TSLReader;

public class Index
{
  public class Entry
  {
    public final int ID;
    public final int Group;
    
    public Entry(int i,int g)
    {
      ID = i;
      Group = g;
    }
  }
  
  public class Group
  {
    public final int ID;
    public final String Name;

    public Group(int i,String n)
    {
      ID = i;
      Name = n;
    }

  }
  
  private List<Entry> entries;
  private List<Integer> items;
  private SparseArray<Group> groups;
  private List<Group> groups_list;
  
  public Index(TSLReader reader) throws EveDataException
  {
    groups = new SparseArray<Group>();
    groups_list = new ArrayList<Group>();
    entries = new ArrayList<Entry>();
    items = new ArrayList<Integer>();

    TSLObject node = new TSLObject();

    try
    {
      reader.moveNext();

      if(!reader.getName().equals("index"))
      {
        throw new EveDataException();
      }
      while(true)
      {
        reader.moveNext();
        TSLReader.State type = reader.getState();
        if(type == TSLReader.State.ENDOBJECT)
        {
          break;
        } else if(type == TSLReader.State.OBJECT)
        {
          String node_name = reader.getName();
          if(node_name.equals("item"))
          {
            node.loadFromReader(reader);
            int id = node.getStringAsInt("id", -1);
            int group = node.getStringAsInt("group", -1);
            if(id < 0 || group < 0)
            {
              throw new EveDataException();
            }
            entries.add(new Entry(id, group));
            items.add(id);
          } else if(node_name.equals("group"))
          {
            node.loadFromReader(reader);
            int id = node.getStringAsInt("id",-1);
            String name = node.getString("name",null);
            if(id < -1 || name == null)
            {
              throw new EveDataException();
            }
            Group g = new Group(id, name);
            groups.put(g.ID, g);
            groups_list.add(g);
          } else
          {
            reader.skipObject();
          }
        }
      }
    } catch(InvalidTSLException e)
    {
      throw new EveDataException();
    } catch(IOException e)
    {
      throw new EveDataException();
    }
  }
  
  public Index(String group_name,Set<Integer> itemids)
  {
    groups = new SparseArray<Group>();
    groups_list = new ArrayList<Group>();
    entries = new ArrayList<Entry>();
    items = new ArrayList<Integer>();

    Group g = new Group(0,group_name);
    groups.put(0, g);
    groups_list.add(g);

    for(int id:itemids)
    {
      entries.add(new Entry(id, 0));
      items.add(id);
    }
  }


  public Index(String group_name,List<IItem> itemlist)
  {
    groups = new SparseArray<Group>();
    groups_list = new ArrayList<Group>();
    entries = new ArrayList<Entry>();
    items = new ArrayList<Integer>();

    Group g = new Group(0,group_name);
    groups.put(0, g);
    groups_list.add(g);

    for(IItem it:itemlist)
    {
      entries.add(new Entry(it.getID(), 0));
      items.add(it.getID());
    }
  }
  public List<Entry> GetEntries()
  {
    return entries;
  }
  
  public List<Integer> GetItems()
  {
    return items;
  }

  public List<Group> GetGroups()
  {
    return groups_list;
  }

  public Group GetGroup(int id)
  {
    return groups.get(id);
  }
}
