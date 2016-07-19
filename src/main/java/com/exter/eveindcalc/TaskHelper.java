package com.exter.eveindcalc;


import android.os.Bundle;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import exter.eveindustry.data.planet.PlanetBuilding;
import exter.eveindustry.data.reaction.Reaction;
import exter.eveindustry.item.ItemStack;
import exter.eveindustry.market.Market;
import exter.eveindustry.task.GroupTask;
import exter.eveindustry.task.ManufacturingTask;
import exter.eveindustry.task.PlanetTask;
import exter.eveindustry.task.ReactionTask;
import exter.eveindustry.task.RefiningTask;
import exter.eveindustry.task.Task;
import exter.eveindustry.task.TaskFactory;

public class TaskHelper
{

  private final TaskFactory factory;

  // Used to provide the task icon and description based on the type of task
  private interface ITaskIconProvider
  {
    int getIcon(Task task);
    boolean hasBackground(Task task);
    String getDescription(Task task);
  }

  private class ManufacturingTaskDescriptionProvider implements ITaskIconProvider
  {
    @Override
    public int getIcon(Task task)
    {
       return ((ManufacturingTask)task).getBlueprint().product.item.icon_id;
    }

    @Override
    public boolean hasBackground(Task task)
    {
      return true;
    }

    @Override
    public String getDescription(Task task)
    {
      ManufacturingTask t = (ManufacturingTask)task;
      int r = t.getRuns();
      int c = t.getCopies();
      return "ME: " + String.valueOf(t.getME())
           + "\nTE: " + String.valueOf(t.getTE())
           + "\n" + String.valueOf(r) + (r == 1?" run":" runs")
           + "\n" + String.valueOf(c) + (c == 1?" copy":" copies");
    }
  }

  private class RefiningTaskIconProvider implements ITaskIconProvider
  {
    @Override
    public int getIcon(Task task)
    {
      return ((RefiningTask)task).getRefinable().item.item.icon_id;
    }

    @Override
    public boolean hasBackground(Task task)
    {
      return true;
    }

    @Override
    public String getDescription(Task task)
    {
      RefiningTask t = (RefiningTask)task;
      return String.valueOf(t.getOreAmount()) + " units\n" + String.valueOf((int)(t.getEfficiency() * 100)) + "% Effiency";
    }
  }

  private class ReactionTaskIconProvider implements ITaskIconProvider
  {
    @Override
    public int getIcon(Task task)
    {
      List<ItemStack> m = task.getProducedMaterials();
      if(m.size() == 1)
      {
        return m.iterator().next().item.icon_id;
      }
      return ((ReactionTask) task).getStarbaseTower().item.icon_id;
    }

    @Override
    public boolean hasBackground(Task task)
    {
      return task.getProducedMaterials().size() == 1;
    }

    @Override
    public String getDescription(Task task)
    {
      ReactionTask t = (ReactionTask)task;
      int reactors = 0;
      int miners = 0;
      for(Reaction r:t.getReactions())
      {
        if(r.inputs.size() > 0)
        {
          reactors++;
        } else
        {
          miners++;
        }
      }

      return String.valueOf(reactors) + (reactors == 1?" reactor":" reactors")
          + "\n" + String.valueOf(miners) + (miners == 1?" miner":" miners");
    }
  }

  private class PlanetTaskIconProvider implements ITaskIconProvider
  {
    @Override
    public int getIcon(Task task)
    {
      List<ItemStack> m = task.getProducedMaterials();
      if(m.size() == 1)
      {
        return (m.iterator().next().item).icon_id;
      }
      return factory.items.get(((PlanetTask) task).getPlanet().id).icon_id;
    }

    @Override
    public boolean hasBackground(Task task)
    {
      return task.getProducedMaterials().size() == 1;
    }

    @Override
    public String getDescription(Task task)
    {
      PlanetTask t = (PlanetTask)task;
      int factories = 0;
      int extractors = 0;
      for(PlanetBuilding p:t.getBuildings())
      {
        if(p.materials.size() > 0)
        {
          factories++;
        } else
        {
          extractors++;
        }
      }
      
      return String.valueOf(factories) + (factories == 1?" factory":" factories")
            + "\n" + String.valueOf(extractors) + (extractors == 1?" extractor":" extractors");
    }
  }

  static private class GroupTaskIconProvider implements ITaskIconProvider
  {
    @Override
    public int getIcon(Task task)
    {
      List<ItemStack> m = task.getProducedMaterials();
      if(m.size() == 1)
      {
        return (m.iterator().next().item).icon_id;
      }
      return -1;
    }

    @Override
    public boolean hasBackground(Task task)
    {
      return task.getProducedMaterials().size() == 1;
    }

    @Override
    public String getDescription(Task task)
    {
      GroupTask t = (GroupTask)task;
      int s = t.getTaskList().size();
      return String.valueOf(s) + (s == 1?" Task":" Tasks")
            +"\nScale: " + String.valueOf(t.getScale());
    }
  }

  static private BigDecimal getBigDecimal(Bundle bundle,String key,BigDecimal def)
  {
    String str = bundle.getString(key);
    if(str == null)
    {
      return def;
    } else
    {
      return new BigDecimal(str);
    }
  }

  static public Market priceFromBundle(Bundle bundle)
  {
    int system = bundle.getInt("system",30000142);
    Market.Order source = Market.Order.fromInt(bundle.getInt("source",Market.Order.SELL.value));
    BigDecimal manual = getBigDecimal(bundle,"manual",BigDecimal.ZERO);
    BigDecimal broker = getBigDecimal(bundle,"broker",new BigDecimal("3"));
    BigDecimal tax = getBigDecimal(bundle,"tax",new BigDecimal("2"));
    return new Market(system, source, manual,broker,tax);
  }

  static public void priceToBundle(Market p, Bundle bundle)
  {
    bundle.putInt("system", p.system);
    bundle.putInt("source", p.order.value);
    bundle.putString("manual", p.manual.toPlainString());
    bundle.putString("broker",p.broker.toPlainString());
    bundle.putString("tax",p.transaction.toPlainString());
  }



  static private Map<Class<? extends Task>,ITaskIconProvider> icon_providers;
  

  static public int getTaskIcon(Task task)
  {
    if(task == null)
    {
      return -1;
    }
    return icon_providers.get(task.getClass()).getIcon(task);
  }

  public boolean taskHasBackground(Task task)
  {
    return task != null && icon_providers.get(task.getClass()).hasBackground(task);
  }
  
  public String getTaskDescription(Task task)
  {
    if(task == null)
    {
      return "";
    }
    return icon_providers.get(task.getClass()).getDescription(task);
  }

  public TaskHelper(TaskFactory factory)
  {
    this.factory = factory;
    icon_providers = new HashMap<>();
    icon_providers.put(ManufacturingTask.class, new ManufacturingTaskDescriptionProvider());
    icon_providers.put(RefiningTask.class, new RefiningTaskIconProvider());
    icon_providers.put(ReactionTask.class, new ReactionTaskIconProvider());
    icon_providers.put(GroupTask.class, new GroupTaskIconProvider());
    icon_providers.put(PlanetTask.class, new PlanetTaskIconProvider());
  }
}
