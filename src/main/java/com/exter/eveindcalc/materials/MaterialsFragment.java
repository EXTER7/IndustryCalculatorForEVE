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

import com.exter.eveindcalc.EICFragmentActivity;
import com.exter.eveindcalc.IEveCalculatorFragment;
import com.exter.eveindcalc.R;
import com.exter.eveindcalc.TaskHelper;
import com.exter.eveindcalc.data.EveDatabase;
import com.exter.eveindcalc.data.blueprint.BlueprintDA;
import com.exter.eveindcalc.data.blueprint.BlueprintHistoryDA;
import com.exter.eveindcalc.data.inventory.Item;
import com.exter.eveindcalc.data.planet.Planet;
import com.exter.eveindcalc.data.planet.PlanetDA;
import com.exter.eveindcalc.data.planet.PlanetProduct;
import com.exter.eveindcalc.data.planet.PlanetProductDA;
import com.exter.eveindcalc.data.reaction.Reaction;
import com.exter.eveindcalc.data.reaction.ReactionDA;
import com.exter.eveindcalc.data.starbase.StarbaseTowerDA;
import com.exter.eveindcalc.data.starmap.Starmap;
import com.exter.eveindcalc.util.XUtil;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import exter.eveindustry.item.ItemStack;
import exter.eveindustry.task.GroupTask;
import exter.eveindustry.task.ManufacturingTask;
import exter.eveindustry.task.PlanetTask;
import exter.eveindustry.task.ReactionTask;
import exter.eveindustry.task.Task;


public class MaterialsFragment extends Fragment implements IEveCalculatorFragment
{

  static public class MaterialHolderComparator implements Comparator<ViewHolderMaterial>
  {
    @Override
    public int compare(ViewHolderMaterial lhs, ViewHolderMaterial rhs)
    {
      if(lhs.type.weight == rhs.type.weight)
      {
        if(lhs.material.amount == rhs.material.amount)
        {
          return lhs.material.item.getID() - rhs.material.item.getID();
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
        args.putInt("item", material.item.getID());
        TaskHelper.PriceToBundle(task.getMaterialMarket(material.item),args);
        dialog.setArguments(args);
        dialog.setOnAcceptListener(calc.new EveCalculatorMarketFetchAcceptListener());
        dialog.show(calc.getSupportFragmentManager(), "MarketFetchDialogFragment");
      }
    }
    
    private class ComponentClickListener implements OnClickListener
    {
      private Planet getPlanetFromResources(SparseIntArray raw, boolean advanced)
      {
        planet:for(int pid:PlanetDA.getPlanetIDs())
        {
          Planet p = PlanetDA.getPlanet(pid);
          if(advanced && !p.Advanced)
          {
            continue;
          }
          int i;
          for(i = 0; i < raw.size(); i++)
          {
            if(!p.Resources.contains(raw.keyAt(i)))
            {
              continue planet;
            }
          }
          return p;
        }
        return null;
      }
      
      private void addPlanetSubProcess(PlanetTask task, PlanetProduct prod, SparseIntArray raw)
      {
        for(ItemStack m : prod.Materials)
        {
          int i;
          PlanetProduct sub = PlanetProductDA.GetProduct(m.item.getID());
          long amount = m.amount / sub.ProductItem.amount;
          if(m.amount % sub.ProductItem.amount > 0)
          {
            amount++;
          }
          if(sub.Level == 0)
          {
            int raw_amount = raw.get(sub.ProductItem.item.getID(), 0);
            raw_amount += sub.ProductItem.amount * amount;
            raw.put(sub.ProductItem.item.getID(), raw_amount);
          } else
          {
            for(i = 0; i < amount; i++)
            {
              task.addBuilding(sub);
              switch(prod.Level)
              {
                case 4:
                  if(sub.Level < 2)
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
            GroupTask group = (GroupTask)calc.getTask();
            ManufacturingTask t = new ManufacturingTask(BlueprintDA.getBlueprint(material.item.getID()));
            SharedPreferences sp = getActivity().getSharedPreferences("EIC", Context.MODE_PRIVATE);
            t.setHardwiring(ManufacturingTask.Hardwiring.fromInt(sp.getInt("manufacturing.hardwiring", ManufacturingTask.Hardwiring.None.value)));
            t.setSolarSystem(sp.getInt("manufacturing.system", 30000142));

            t.setRuns((int)XUtil.DivCeil(material.amount,t.getBlueprint().getProduct().amount * group.getScale()));
            BlueprintHistoryDA.Entry histent = BlueprintHistoryDA.GetEntry(t.getBlueprint().getID());
            if(histent != null)
            {
              t.setME(histent.GetME());
              t.setTE(histent.GetTE());
            }

            group.addTask(((Item)t.getBlueprint().getProduct().item).Name, t);
          }
          break;
          case Group_Planet:
          {
            GroupTask group = (GroupTask)calc.getTask();
            int i;
            PlanetTask t = new PlanetTask(PlanetDA.getPlanet(11));
            PlanetProduct p = PlanetProductDA.GetProduct(material.item.getID());
            t.addBuilding(p);
            t.setRunTime((int)XUtil.DivCeil( material.amount,p.ProductItem.amount * 24 * group.getScale()));
            SparseIntArray raw = new SparseIntArray();
            addPlanetSubProcess(t, p, raw);
            Planet pl = getPlanetFromResources(raw, p.Level == 4);
            if(pl != null)
            {
              t.setPlanet(pl);
              for(i = 0; i < raw.size(); i++)
              {
                int j;
                PlanetProduct rp = PlanetProductDA.GetProduct(raw.keyAt(i));
                long amount = raw.valueAt(i) / rp.getProduct().amount;
                if(raw.valueAt(i) % rp.getProduct().amount > 0)
                {
                  amount++;
                }
                for(j = 0; j < amount; j++)
                {
                  t.addBuilding(rp);
                }
              }
            }
            group.addTask(((Item)material.item).Name, t);
          }
          break;
          case Group_Reaction:
          {
            GroupTask group = (GroupTask)calc.getTask();
            ReactionTask t = new ReactionTask(StarbaseTowerDA.GetTower(StarbaseTowerDA.GetTowerIDs().get(0)));
            Reaction r = ReactionDA.GetReaction(material.item.getID());
            t.addReaction(r);
            t.setRunTime((int)XUtil.DivCeil(material.amount,r.GetMainOutputAmount() * 24 * group.getScale()));
            group.addTask(((Item)material.item).Name, t);
          }
          break;
          case Planet_Extractor:
          case Planet_Factory:
          {
            PlanetTask t = (PlanetTask)calc.getTask();
            PlanetProduct p = PlanetProductDA.GetProduct(material.item.getID());
            int times = (int)XUtil.DivCeil(material.amount, p.ProductItem.amount * t.getRunTime() * 24);
            int i;
            for(i = 0; i < times; i++)
            {
              t.addBuilding(p);
            }
          }
          break;
          case Reaction_Miner:
          case Reaction_Reactor:
          {
            ReactionTask t = (ReactionTask)calc.getTask();
            Reaction r = ReactionDA.GetReaction(material.item.getID());
            int times = (int)XUtil.DivCeil(material.amount, r.GetMainOutputAmount() * t.getRunTime() * 24);
            int i;
            for(i = 0; i < times; i++)
            {
              t.addReaction(r);
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
    public final View holder_view;
    public final Task task;

    public void update()
    {
      Item item = (Item)material.item;

      DecimalFormat formatter = new DecimalFormat("###,###");
      DecimalFormat formatter_decimal = new DecimalFormat("###,###.##");
      tx_material_volume.setText(formatter_decimal.format(material.amount * item.Volume) + " m3");
      Task.Market price = task.getMaterialMarket(material.item);
      switch(price.order)
      {
        case SELL:
          tx_source.setText("Sell orders: " + Starmap.getSolarSystem(price.system).Name);
          break;
        case BUY:
          tx_source.setText("Buy orders: " + Starmap.getSolarSystem(price.system).Name);
          break;
        case MANUAL:
          tx_source.setText("Manual");
          break;
      }
      tx_material.setText(item.Name + " \u00D7 " + formatter.format(material.amount));
      updatePriceValue();
    }

    public void updatePriceValue()
    {
      BigDecimal price_value = task.getMaterialMarketPrice(material.item);
      if(price_value == null)
      {
        tx_material_totalprice.setText("--");
        tx_price.setText("--");
      } else
      {
        DecimalFormat formatter_decimal = new DecimalFormat("###,###.##");
        tx_material_totalprice.setText(formatter_decimal.format(price_value.multiply(new BigDecimal(material.amount))) + " ISK");
        tx_price.setText(price_value.toPlainString() + " ISK/unit");
      }
    }

    public ViewHolderMaterial(View view, ItemStack mat, boolean required)
    {
      holder_view = view;
      material = mat;
      task = calc.getTask();
      im_icon = (ImageView) view.findViewById(R.id.im_material_icon);
      tx_material = (TextView) view.findViewById(R.id.tx_material_bill);
      tx_material_volume = (TextView) view.findViewById(R.id.tx_material_volume);
      tx_material_totalprice = (TextView) view.findViewById(R.id.tx_material_totalprice);
      tx_price = (TextView) view.findViewById(R.id.ed_material_price);
      tx_source = (TextView) view.findViewById(R.id.ed_material_source);
      bt_marketcost = (ImageButton) view.findViewById(R.id.bt_material_marketcost);
      bt_component = (ImageButton) view.findViewById(R.id.bt_material_cost);

      TaskHelper.setImageViewItemIcon(im_icon, (Item) material.item);

      bt_component.setOnClickListener(null);
      bt_component.setVisibility(View.GONE);
      if(required && !calc.IsMainTask())
      {
        Task task = calc.getTask();
        Drawable dr = ContextCompat.getDrawable(MaterialsFragment.this.getActivity(), R.drawable.cost_blank);
        if(task instanceof GroupTask)
        {
          if(BlueprintDA.isItemBlueprint(mat.item.getID()))
          {
            type = ItemType.Group_Blueprint;
            dr = ContextCompat.getDrawable(MaterialsFragment.this.getActivity(), R.drawable.cost_manufacuring);
          } else if(ReactionDA.IsItemReaction(mat.item.getID()))
          {
            type = ItemType.Group_Reaction;
            dr = ContextCompat.getDrawable(MaterialsFragment.this.getActivity(), R.drawable.cost_reaction);
          } else if(PlanetProductDA.IsItemFromPlanet(mat.item.getID()))
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
          PlanetProduct p = PlanetProductDA.GetProduct(mat.item.getID());
          if(p == null)
          {
            type = ItemType.Market;
          } else
          {
            if(p.Level > 0)
            {
              type = ItemType.Planet_Factory;
              dr = ContextCompat.getDrawable(MaterialsFragment.this.getActivity(), R.drawable.planet_process);
            } else if(t.getPlanet().getResources().contains(mat.item))
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
          Reaction r = ReactionDA.GetReaction(mat.item.getID());
          if(r == null)
          {
            type = ItemType.Market;
          } else
          {
            if(r.Inputs.size() > 0)
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
    
    public ItemStack GetMaterial()
    {
      return material;
    }
  }


  private class ViewHolderGroup
  {

    public TextView tx_name;
    public TextView tx_totalprice;
    public TextView tx_volume;
    public TextView tx_extra;
    public LinearLayout ly_extra;
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
        TaskHelper.PriceToBundle(produced?EveDatabase.GetDefaultProducedPrice():EveDatabase.GetDefaultRequiredPrice(),args);
        dialog.setArguments(args);
        dialog.setOnAcceptListener(calc.new EveCalculatorMarketFetchAcceptListener());
        dialog.show(calc.getSupportFragmentManager(), "MarketFetchDialogFragment");
      }
    }

    public void update()
    {
      BigDecimal total_price = BigDecimal.ZERO;
      double total_volume = 0;
      for(ItemStack m : materials)
      {
        Item i = (Item)m.item;
        BigDecimal p = task.getMaterialMarketPrice(m.item);
        if(p == null)
        {
          p = BigDecimal.ZERO;
        }
        total_price = total_price.add(p.multiply(new BigDecimal(m.amount)));
        total_volume += m.amount * i.Volume;
      }
      DecimalFormat formatter_decimal = new DecimalFormat("###,###.##");
      tx_volume.setText(formatter_decimal.format(total_volume) + " m3");
      tx_totalprice.setText(formatter_decimal.format(total_price) + " ISK");

      tx_name.setText(name);
      if(!produced)
      {
        tx_extra.setText("Additional expense: "+ formatter_decimal.format(calc.getTask().getExtraExpense()) +" ISK");
      }
    }

    public ViewHolderGroup(View view, List<ItemStack> mat, String n,boolean prod)
    {
      materials = mat;
      name = n;
      task = calc.getTask();
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
  ViewHolderGroup req_holder;
  ViewHolderGroup prod_holder;
  private LayoutInflater ly_inflater;

  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
  {

    View rootView = inflater.inflate(R.layout.matlist, container, false);
    
    calc = (EICFragmentActivity) getActivity();
    ly_inflater = (LayoutInflater) calc.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    ly_materials = (LinearLayout) rootView.findViewById(R.id.ly_matlist_materials);

    onMaterialSetChanged();

    return rootView;
  }

  
  private ViewHolderMaterial findHolder(int item)
  {
    for(ViewHolderMaterial m:prod_holders)
    {
      if(m.GetMaterial().item.getID() == item)
      {
        return m;
      }
    }
    for(ViewHolderMaterial m:req_holders)
    {
      if(m.GetMaterial().item.getID() == item)
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

    Task task = calc.getTask();
    
    List<ItemStack> req_materials = task.getRequiredMaterials();
    List<ItemStack> prod_materials = task.getProducedMaterials();


    req_holders = new ArrayList<>();
    prod_holders = new ArrayList<>();

    ly_materials.removeAllViews();
        
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

    Collections.sort(prod_holders,new MaterialHolderComparator());
    for(ViewHolderMaterial holder:prod_holders)
    {
      ly_materials.addView(holder.holder_view);
    }

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
