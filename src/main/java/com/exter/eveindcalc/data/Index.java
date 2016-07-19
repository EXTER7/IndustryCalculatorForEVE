package com.exter.eveindcalc.data;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import exter.eveindustry.data.filesystem.IFileSystemHandler;
import exter.eveindustry.data.item.Item;
import exter.tsl.InvalidTSLException;
import exter.tsl.TSLObject;
import exter.tsl.TSLReader;

public class Index implements IFileSystemHandler.IReadHandler<Object>
{
  public class Entry
  {
    public final int item_id;
    public final int group;
    
    Entry(int item,int g)
    {
      this.item_id = item;
      this.group = g;
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
  private List<Integer> item_ids;
  private List<Group> groups_list;
  
  public Index(IFileSystemHandler fs,String path)
  {
    groups_list = new ArrayList<>();
    entries = new ArrayList<>();
    item_ids = new ArrayList<>();
    
    fs.readFile(path, this);
  }
  
  public Index(String group_name,Set<Integer> itemids)
  {
    groups_list = new ArrayList<>();
    entries = new ArrayList<>();
    item_ids = new ArrayList<>();

    Group g = new Group(0,group_name);
    groups_list.add(g);

    for(int id:itemids)
    {
      entries.add(new Entry(id, 0));
      item_ids.add(id);
    }
  }


  public Index(String group_name,List<Item> itemlist)
  {
    groups_list = new ArrayList<Group>();
    entries = new ArrayList<Entry>();
    item_ids = new ArrayList<Integer>();

    Group g = new Group(0,group_name);
    groups_list.add(g);

    for(Item it:itemlist)
    {
      entries.add(new Entry(it.id, 0));
      item_ids.add(it.id);
    }
  }
  
  public List<Entry> getEntries()
  {
    return entries;
  }
  
  public List<Integer> getItemIDs()
  {
    return item_ids;
  }

  public List<Group> getGroups()
  {
    return groups_list;
  }

  @Override
  public Object readFile(InputStream stream) throws IOException
  {
    try
    {
      TSLObject node = new TSLObject();
      TSLReader reader = new TSLReader(stream);
      reader.moveNext();
      if(!reader.getName().equals("index"))
      {
        throw new RuntimeException("TSL file is not an index");
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
            if(group < 0)
            {
              throw new RuntimeException("Invalid group value.");
            }
            entries.add(new Entry(id, group));
            item_ids.add(id);
          } else if(node_name.equals("group"))
          {
            node.loadFromReader(reader);
            int id = node.getStringAsInt("id", -1);
            String name = node.getString("name", null);
            if(id < -1 || name == null)
            {
              throw new RuntimeException();
            }
            Group g = new Group(id, name);
            groups_list.add(g);
          } else
          {
            reader.skipObject();
          }
        }
      }
    } catch(InvalidTSLException e)
    {
      throw new RuntimeException(e);
    }
    return null;
  }
}
