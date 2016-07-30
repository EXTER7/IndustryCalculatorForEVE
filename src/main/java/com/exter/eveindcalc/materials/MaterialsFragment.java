package com.exter.eveindcalc.materials;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.util.SparseIntArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.exter.eveindcalc.EICApplication;
import com.exter.eveindcalc.EICFragmentActivity;
import com.exter.eveindcalc.IEveCalculatorFragment;
import com.exter.eveindcalc.R;
import com.exter.eveindcalc.TaskHelper;
import com.exter.eveindcalc.data.EveDatabase;
import com.exter.eveindcalc.data.blueprint.BlueprintHistoryDA;
import com.exter.eveindcalc.util.XUtil;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import exter.eveindustry.data.item.Item;
import exter.eveindustry.data.planet.Planet;
import exter.eveindustry.data.planet.PlanetBuilding;
import exter.eveindustry.data.reaction.Reaction;
import exter.eveindustry.item.ItemStack;
import exter.eveindustry.market.Market;
import exter.eveindustry.task.GroupTask;
import exter.eveindustry.task.ManufacturingTask;
import exter.eveindustry.task.PlanetTask;
import exter.eveindustry.task.ReactionTask;
import exter.eveindustry.task.Task;
import exter.eveindustry.task.TaskFactory;


public class MaterialsFragment extends Fragment implements IEveCalculatorFragment
{

  private static class MaterialHolderComparator implements Comparator<ViewHolderMaterial>
  {
    @Override
    public int compare(ViewHolderMaterial lhs, ViewHolderMaterial rhs)
    {
      // Sort by type, material amount, item ID.
      if(lhs.type.weight == rhs.type.weight)
      {
        if(lhs.material.amount == rhs.material.amount)
        {
          return lhs.material.item.id - rhs.material.item.id;
        }
        long diff = rhs.material.amount - lhs.material.amount; 
        if(diff < 0)
        {
          return -1;
        }
        if(diff > 0)
        {
          return 1;
        }
        return 0;
      }
      return lhs.type.weight - rhs.type.weight;
    }
  }

  // Used to sort material by type (Mineral, PI, Reaction, ...)
  private enum ItemType
  {
    Market(4),
    Group_Blueprint(3),
    Group_Reaction(2),
    Group_Planet(1),
    Planet_Extractor(2),
    Planet_Factory(1),
    Reaction_Reactor(2),
    Reaction_Miner(1);
    
    public final int weight;
    
    ItemType(int w)
    {
      weight = w;
    }
  }

  private class ViewHolderMaterial
  {
    private class MarketPriceClickListener implements OnClickListener
    {
      @Override
      public void onClick(View v)
      {
        Bundle args = new Bundle();
        MarketFetchDialogFragment dialog = new MarketFetchDialogFragment();
        args.putInt("type", MarketFetchDialogFragment.TYPE_ITEM);
        args.putInt("item", material.item.id);
        TaskHelper.priceToBundle(task.getMaterialMarket(material.item),args);
        dialog.setArguments(args);
        dialog.setOnAcceptListener(calc.new EveCalculatorMarketFetchAcceptListener());
        dialog.show(calc.getSupportFragmentManager(), "MarketFetchDialogFragment");
      }
    }
    
    private class ComponentClickListener implements OnClickListener
    {
      private Planet getPlanetFromResources(SparseIntArray raw, boolean advanced)
      {
        planet:for(int id: factory.planets.getIDs())
        {
          Planet p = factory.planets.get(id);
          if(advanced && !p.advanced)
          {
            continue;
          }
          int i;
          for(i = 0; i < raw.size(); i++)
          {
            if(!p.resources.contains(factory.items.get(raw.keyAt(i))))
            {
              continue planet;
            }
          }
          return p;
        }
        return null;
      }
      
      private void addPlanetSubProcess(PlanetTask task, PlanetBuilding prod, SparseIntArray raw)
      {
        for(ItemStack m : prod.materials)
        {
          int i;
          PlanetBuilding sub = factory.planetbuildings.get(m.item.id);
          long amount = XUtil.divCeil(m.amount,sub.product.amount);
          if(sub.level == 0)
          {
            int raw_amount = raw.get(sub.product.item.id, 0);
            raw_amount += sub.product.amount * amount;
            raw.put(sub.product.item.id, raw_amount);
          } else
          {
            for(i = 0; i < amount; i++)
            {
              task.addBuilding(sub.product.item.id);
              switch(prod.level)
              {
                case 4:
                  if(sub.level < 2)
                  {
                    addPlanetSubProcess(task, sub, raw);
                  }
                  break;
                case 3:
                  break;
                default:
                  addPlanetSubProcess(task, sub, raw);
                  break;
              }
            }
          }
        }
      }
      
     
      @Override
      public void onClick(View v)
      {
        switch(type)
        {
          case Market:
          break;
          case Group_Blueprint:
          {
            GroupTask group = (GroupTask)calc.getCurrentTask();
            ManufacturingTask t = factory.newManufacturing(material.item.id);
            SharedPreferences sp = getActivity().getSharedPreferences("EIC", Context.MODE_PRIVATE);
            t.setHardwiring(ManufacturingTask.Hardwiring.fromInt(sp.getInt("manufacturing.hardwiring", ManufacturingTask.Hardwiring.None.value)));
            t.setSolarSystem(sp.getInt("manufacturing.system", 30000142));

            t.setRuns((int)XUtil.divCeil(material.amount,t.getBlueprint().product.amount * group.getScale()));
            BlueprintHistoryDA.Entry histent = database.da_blueprinthistory.getEntry(t.getBlueprint().product.item.id);
            if(histent != null)
            {
              t.setME(histent.getME());
              t.setTE(histent.getTE());
            }

            group.addTask(t.getBlueprint().product.item.name, t);
          }
          break;
          case Group_Planet:
          {
            GroupTask group = (GroupTask)calc.getCurrentTask();
            int i;
            PlanetTask t = factory.newPlanet(11);
            PlanetBuilding p = factory.planetbuildings.get(material.item.id);
            t.addBuilding(material.item.id);
            t.setRunTime((int)XUtil.divCeil( material.amount,p.product.amount * 24 * group.getScale()));
            SparseIntArray raw = new SparseIntArray();
            addPlanetSubProcess(t, p, raw);
            Planet pl = getPlanetFromResources(raw, p.level == 4);
            if(pl != null)
            {
              t.setPlanet(pl.id);
              for(i = 0; i < raw.size(); i++)
              {
                int j;
                PlanetBuilding rp = factory.planetbuildings.get(raw.keyAt(i));
                long amount = XUtil.divCeil(raw.valueAt(i),rp.product.amount);
                for(j = 0; j < amount; j++)
                {
                  t.addBuilding(rp.product.item.id);
                }
              }
            }
            group.addTask(material.item.name, t);
          }
          break;
          case Group_Reaction:
          {
            GroupTask group = (GroupTask)calc.getCurrentTask();
            ReactionTask t = factory.newReaction(factory.towers.getIDs().iterator().next());
            Reaction r = factory.reactions.get(material.item.id);
            t.addReaction(material.item.id);
            t.setRunTime((int)XUtil.divCeil(material.amount,r.getMainOutput().amount * 24 * group.getScale()));
            group.addTask(material.item.name, t);
          }
          break;
          case Planet_Extractor:
          case Planet_Factory:
          {
            PlanetTask t = (PlanetTask)calc.getCurrentTask();
            PlanetBuilding p = factory.planetbuildings.get(material.item.id);
            int times = (int)XUtil.divCeil(material.amount, p.product.amount * t.getRunTime() * 24);
            int i;
            for(i = 0; i < times; i++)
            {
              t.addBuilding(material.item.id);
            }
          }
          break;
          case Reaction_Miner:
          case Reaction_Reactor:
          {
            ReactionTask t = (ReactionTask)calc.getCurrentTask();
            Reaction r = factory.reactions.get(material.item.id);
            int times = (int)XUtil.divCeil(material.amount, r.getMainOutput().amount * t.getRunTime() * 24);
            int i;
            for(i = 0; i < times; i++)
            {
              t.addReaction(material.item.id);
            }
          }
          break;
        }
        calc.notifyTaskChanged();
        onMaterialSetChanged();
      }
    }

    private ImageView im_icon;
    private TextView tx_material;
    private TextView tx_material_volume;
    private TextView tx_material_totalprice;
    private TextView tx_price;
    private TextView tx_source;
    private ImageButton bt_marketcost;
    private ImageButton bt_component;


    public final ItemStack material;
    public final ItemType type;
    final View holder_view;
    public final Task task;

    void update()
    {
      Item item = material.item;

      tx_material_volume.setText(String.format("%s m3", DECIMAL_FORMATTER.format(material.amount * item.volume)));
      Market price = task.getMaterialMarket(material.item);
      switch(price.order)
      {
        case SELL:
          tx_source.setText(String.format("Sell orders: %s", factory.solarsystems.get(price.system).name));
          break;
        case BUY:
          tx_source.setText(String.format("Buy orders: %s", factory.solarsystems.get(price.system).name));
          break;
        case MANUAL:
          tx_source.setText("Manual");
          break;
      }
      tx_material.setText(String.format("%s × %s", item.name, INTEGER_FORMATTER.format(material.amount)));
      updatePriceValue();
    }

    void updatePriceValue()
    {
      BigDecimal price_value = task.getMaterialMarketPrice(material.item);
      if(price_value == null)
      {
        tx_material_totalprice.setText("--");
        tx_price.setText("--");
      } else
      {
        tx_material_totalprice.setText(String.format("%s ISK", DECIMAL_FORMATTER.format(price_value.multiply(new BigDecimal(material.amount)))));
        tx_price.setText(String.format("%s ISK/unit", DECIMAL_FORMATTER.format(price_value)));
      }
    }

    ViewHolderMaterial(View view, ItemStack mat, boolean required)
    {
      holder_view = view;
      material = mat;
      task = calc.getCurrentTask();
      im_icon = (ImageView) view.findViewById(R.id.im_material_icon);
      tx_material = (TextView) view.findViewById(R.id.tx_material_bill);
      tx_material_volume = (TextView) view.findViewById(R.id.tx_material_volume);
      tx_material_totalprice = (TextView) view.findViewById(R.id.tx_material_totalprice);
      tx_price = (TextView) view.findViewById(R.id.ed_material_price);
      tx_source = (TextView) view.findViewById(R.id.ed_material_source);
      bt_marketcost = (ImageButton) view.findViewById(R.id.bt_material_marketcost);
      bt_component = (ImageButton) view.findViewById(R.id.bt_material_cost);

      application.setImageViewItemIcon(im_icon, material.item);

      bt_component.setOnClickListener(null);
      bt_component.setVisibility(View.GONE);
      if(required && !calc.isRootTask())
      {
        Task task = calc.getCurrentTask();
        Drawable dr = ContextCompat.getDrawable(MaterialsFragment.this.getActivity(), R.drawable.cost_blank);
        if(task instanceof GroupTask)
        {
          if(factory.blueprints.get(mat.item.id) != null)
          {
            type = ItemType.Group_Blueprint;
            dr = ContextCompat.getDrawable(MaterialsFragment.this.getActivity(), R.drawable.cost_manufacuring);
          } else if(factory.reactions.get(mat.item.id) != null)
          {
            type = ItemType.Group_Reaction;
            dr = ContextCompat.getDrawable(MaterialsFragment.this.getActivity(), R.drawable.cost_reaction);
          } else if(factory.planetbuildings.get(mat.item.id) != null)
          {
            type = ItemType.Group_Planet;
            dr = ContextCompat.getDrawable(MaterialsFragment.this.getActivity(), R.drawable.cost_pi);
          } else
          {
            type = ItemType.Market;
          }
        } else if(task instanceof PlanetTask)
        {
          PlanetTask t = (PlanetTask)task;
          PlanetBuilding p = factory.planetbuildings.get(mat.item.id);
          if(p == null)
          {
            type = ItemType.Market;
          } else
          {
            if(p.level > 0)
            {
              type = ItemType.Planet_Factory;
              dr = ContextCompat.getDrawable(MaterialsFragment.this.getActivity(), R.drawable.planet_process);
            } else if(t.getPlanet().resources.contains(mat.item))
            {
              type = ItemType.Planet_Extractor;
              dr = ContextCompat.getDrawable(MaterialsFragment.this.getActivity(), R.drawable.planet_extractor);
            } else
            {
              type = ItemType.Market;
            }
          }
        } else if(task instanceof ReactionTask)
        {
          Reaction r = factory.reactions.get(mat.item.id);
          if(r == null)
          {
            type = ItemType.Market;
          } else
          {
            if(r.inputs.size() > 0)
            {
              type = ItemType.Reaction_Reactor;
              dr = ContextCompat.getDrawable(MaterialsFragment.this.getActivity(), R.drawable.reactor);
            } else
            {
              type = ItemType.Reaction_Miner;
              dr = ContextCompat.getDrawable(MaterialsFragment.this.getActivity(), R.drawable.moonminer);
            }
          }
        } else
        {
          type = ItemType.Market;
        }
        if(type != ItemType.Market)
        {
          bt_component.setVisibility(View.VISIBLE);
          bt_component.setOnClickListener(new ComponentClickListener());
        }

        bt_component.setImageDrawable(dr);
      } else
      {
        type = ItemType.Market;
      }
      bt_marketcost.setOnClickListener(new MarketPriceClickListener());
    }
    
    ItemStack getMaterial()
    {
      return material;
    }
  }


  private class ViewHolderGroup
  {

    TextView tx_name;
    TextView tx_totalprice;
    TextView tx_volume;
    TextView tx_extra;
    LinearLayout ly_extra;
    public Task task;
    public List<ItemStack> materials;
    public String name;
    private ImageButton bt_marketcost;
    boolean produced;

    private class MarketPriceClickListener implements OnClickListener
    {
      @Override
      public void onClick(View v)
      {
        Bundle args = new Bundle();
        MarketFetchDialogFragment dialog = new MarketFetchDialogFragment();
        args.putInt("type", produced?MarketFetchDialogFragment.TYPE_PRODUCED:MarketFetchDialogFragment.TYPE_REQUIRED);
        TaskHelper.priceToBundle(produced? database.getDefaultProducedMarket(): database.getDefaultRequiredMarket(),args);
        dialog.setArguments(args);
        dialog.setOnAcceptListener(calc.new EveCalculatorMarketFetchAcceptListener());
        dialog.show(calc.getSupportFragmentManager(), "MarketFetchDialogFragment");
      }
    }

    void update()
    {
      BigDecimal total_price = BigDecimal.ZERO;
      double total_volume = 0;
      for(ItemStack m : materials)
      {
        Item i = m.item;
        BigDecimal p = task.getMaterialMarketPrice(m.item);
        if(p == null)
        {
          p = BigDecimal.ZERO;
        }
        total_price = total_price.add(p.multiply(new BigDecimal(m.amount)));
        total_volume += m.amount * i.volume;
      }
      DecimalFormat formatter_decimal = new DecimalFormat("###,###.##");
      tx_volume.setText(String.format("%s m3", formatter_decimal.format(total_volume)));
      tx_totalprice.setText(String.format("%s ISK", formatter_decimal.format(total_price)));

      tx_name.setText(name);
      if(!produced)
      {
        tx_extra.setText(String.format("Additional expense: %s ISK", formatter_decimal.format(calc.getCurrentTask().getExtraExpense())));
      }
    }

    ViewHolderGroup(View view, List<ItemStack> mat, String n,boolean prod)
    {
      materials = mat;
      name = n;
      task = calc.getCurrentTask();
      produced = prod;

      tx_name = (TextView) view.findViewById(R.id.tx_materialgroup_name);
      tx_volume = (TextView) view.findViewById(R.id.tx_materialgroup_volume);
      tx_totalprice = (TextView) view.findViewById(R.id.tx_materialgroup_totalprice);
      bt_marketcost = (ImageButton) view.findViewById(R.id.bt_materialgroup_marketcost);
      tx_extra = (TextView) view.findViewById(R.id.tx_materialgroup_extra);
      ly_extra = (LinearLayout) view.findViewById(R.id.ly_materialgroup_extra);

      if(mat.size() < 2)
      {
        tx_volume.setVisibility(View.GONE);
        tx_totalprice.setVisibility(View.GONE);
      }
      bt_marketcost.setOnClickListener(new MarketPriceClickListener());
      
      if(!produced)
      {
        ly_extra.setVisibility(View.VISIBLE);
      }
    }
  }

  private EICFragmentActivity calc;

  private LinearLayout ly_materials;

  private List<ViewHolderMaterial> req_holders;
  private List<ViewHolderMaterial> prod_holders;
  private ViewHolderGroup req_holder;
  private ViewHolderGroup prod_holder;
  private LayoutInflater ly_inflater;

  private TaskFactory factory;
  private EveDatabase database;

  private EICApplication application;

  static private final DecimalFormat DECIMAL_FORMATTER = new DecimalFormat("###,###.##");
  static private final DecimalFormat INTEGER_FORMATTER = new DecimalFormat("###,###");

  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
  {
    View rootView = inflater.inflate(R.layout.matlist, container, false);

    calc = (EICFragmentActivity) getActivity();
    application = (EICApplication) calc.getApplication();
    factory = application.factory;
    database = application.database;
    ly_inflater = (LayoutInflater) calc.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    ly_materials = (LinearLayout) rootView.findViewById(R.id.ly_matlist_materials);

    onMaterialSetChanged();

    return rootView;
  }

  
  private ViewHolderMaterial findHolder(int item)
  {
    for(ViewHolderMaterial m:prod_holders)
    {
      if(m.getMaterial().item.id == item)
      {
        return m;
      }
    }
    for(ViewHolderMaterial m:req_holders)
    {
      if(m.getMaterial().item.id == item)
      {
        return m;
      }
    }
    return null;
  }

  @Override
  public void onTaskChanged()
  {

  }

  @Override
  public void onPriceValueChanged()
  {
    if(req_holders != null)
    {
      for(ViewHolderMaterial m:req_holders)
      {
        m.updatePriceValue();
      }
    }
    if(prod_holders != null)
    {
      for(ViewHolderMaterial m:prod_holders)
      {
        m.updatePriceValue();
      }
    }
    if(req_holder != null)
    {
      req_holder.update();
    }
    if(prod_holder != null)
    {
      prod_holder.update();
    }
  }

  @Override
  public void onMaterialChanged(int item)
  {
    ViewHolderMaterial m = findHolder(item);
    if(m != null)
    {
      m.update();
    }
  }

  @Override
  public void onMaterialSetChanged()
  {
    if(calc == null)
    {
      return;
    }

    Task task = calc.getCurrentTask();
    
    List<ItemStack> req_materials = task.getRequiredMaterials();
    List<ItemStack> prod_materials = task.getProducedMaterials();


    req_holders = new ArrayList<>();
    prod_holders = new ArrayList<>();

    ly_materials.removeAllViews();

    // Add produced material header.
    View v = ly_inflater.inflate(R.layout.material_group, ly_materials, false);
    prod_holder = new ViewHolderGroup(v,prod_materials,"Produced",true);
    prod_holder.update();
    ly_materials.addView(v);

    for(ItemStack m:prod_materials)
    {
      v = ly_inflater.inflate(R.layout.material_src, ly_materials, false);
      ViewHolderMaterial mat_holder = new ViewHolderMaterial(v,m, false);
      mat_holder.update();
      prod_holders.add(mat_holder);
    }

    // Add all produced materials, sorted.
    Collections.sort(prod_holders,new MaterialHolderComparator());
    for(ViewHolderMaterial holder:prod_holders)
    {
      ly_materials.addView(holder.holder_view);
    }

    // Add produced requred header.
    v = ly_inflater.inflate(R.layout.material_group, ly_materials, false);
    req_holder = new ViewHolderGroup(v,req_materials,"Required",false);
    req_holder.update();
    ly_materials.addView(v);
    for(ItemStack m:req_materials)
    {
      v = ly_inflater.inflate(R.layout.material_src, ly_materials, false);
      ViewHolderMaterial mat_holder = new ViewHolderMaterial(v,m, true);
      mat_holder.update();
      req_holders.add(mat_holder);
    }

    // Add all required materials, sorted.
    Collections.sort(req_holders,new MaterialHolderComparator());
    for(ViewHolderMaterial holder:req_holders)
    {
      ly_materials.addView(holder.holder_view);
    }
  }


  @Override
  public void onExtraExpenseChanged()
  {
    if(req_holder != null)
    {
      req_holder.update();
    }
  }

  @Override
  public void onTaskParameterChanged(int param)
  {

  }

}
