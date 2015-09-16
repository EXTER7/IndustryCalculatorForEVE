package com.exter.eveindcalc.reaction;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;

import com.exter.controls.IntegerEditText;
import com.exter.eveindcalc.EICFragmentActivity;
import com.exter.eveindcalc.IEveCalculatorFragment;
import com.exter.eveindcalc.R;
import com.exter.eveindcalc.data.inventory.InventoryDA;
import com.exter.eveindcalc.data.reaction.Reaction;
import com.exter.eveindcalc.data.reaction.ReactionDA;
import com.exter.eveindcalc.data.starbase.StarbaseTower;
import com.exter.eveindcalc.data.starbase.StarbaseTowerDA;

import java.util.ArrayList;
import java.util.List;

import exter.eveindustry.data.reaction.IReaction;
import exter.eveindustry.task.ReactionTask;

public class ReactionFragment extends Fragment implements IEveCalculatorFragment
{

  private class RunTimeChangeWatcher implements IntegerEditText.ValueListener
  {
    @Override
    public void onValueChanged(int new_value)
    {
      if(reaction_task == null)
      {
        return;
      }
      reaction_task.setRunTime(new_value);
    }
  }

  private class SovChangeListener implements CheckBox.OnCheckedChangeListener
  {

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked)
    {
      if(reaction_task == null)
      {
        return;
      }
      reaction_task.setSovereignty(isChecked);
    }
  }

  private class TowerSelectedListener implements Spinner.OnItemSelectedListener
  {
    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int pos, long id)
    {
      if(reaction_task == null)
      {
        return;
      }

      StarbaseTower tower = StarbaseTowerDA.GetTower(tower_ids.get(pos));
      reaction_task.setStarbaseTower(tower);
      SharedPreferences sp = activity.getSharedPreferences("EIC", Context.MODE_PRIVATE);
      SharedPreferences.Editor ed = sp.edit();
      ed.putInt("reaction.tower", tower.TowerItem.ID);
      ed.apply();
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent)
    {

    }
  }

  private class AddReactorClickListener implements OnClickListener
  {
    @Override
    public void onClick(View v)
    {
      if(reaction_task == null)
      {
        return;
      }
      Intent in = new Intent(getActivity(), ReactionListActivity.class);
      startActivityForResult(in, 0);
    }
  }

  private class AddMoonMinerClickListener implements OnClickListener
  {
    @Override
    public void onClick(View v)
    {
      if(reaction_task == null)
      {
        return;
      }
      Intent in = new Intent(getActivity(), MoonMinerListActivity.class);
      startActivityForResult(in, 0);
    }
  }

  private Spinner sp_tower;
  private IntegerEditText ed_runtime;

  public List<ViewHolderReactor> holders;
  private LinearLayout ly_process;
  private LayoutInflater ly_inflater;

  private EICFragmentActivity activity;

  private List<Integer> tower_ids;
  public ReactionTask reaction_task;

  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
  {
    activity = (EICFragmentActivity) getActivity();
    reaction_task = (ReactionTask) activity.getTask();
    ly_inflater = (LayoutInflater) activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

    tower_ids = StarbaseTowerDA.GetTowerIDs();

    View root_view = inflater.inflate(R.layout.reaction, container, false);
    List<String> tower_names = new ArrayList<>();
    for(Integer id : tower_ids)
    {
      tower_names.add(StarbaseTowerDA.GetTower(id).Name);
    }
    sp_tower = (Spinner) root_view.findViewById(R.id.sp_reaction_tower);
    ed_runtime = new IntegerEditText((EditText) root_view.findViewById(R.id.ed_reaction_runtime), 1, 99999, 0, new RunTimeChangeWatcher());
    CheckBox ch_sov = (CheckBox) root_view.findViewById(R.id.ch_reaction_sov);
    ly_process = (LinearLayout) root_view.findViewById(R.id.ly_reaction_process);
    Button bt_addreactor = (Button) root_view.findViewById(R.id.bt_reaction_add_reactor);
    Button bt_addmoonminer = (Button) root_view.findViewById(R.id.bt_reaction_add_moonminer);
    bt_addreactor.setOnClickListener(new AddReactorClickListener());
    bt_addmoonminer.setOnClickListener(new AddMoonMinerClickListener());

    ch_sov.setOnCheckedChangeListener(new SovChangeListener());

    ArrayAdapter<String> spinnerArrayAdapter = new ArrayAdapter<>(activity, android.R.layout.simple_spinner_item, tower_names);
    spinnerArrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
    sp_tower.setAdapter(spinnerArrayAdapter);
    sp_tower.setOnItemSelectedListener(new TowerSelectedListener());

    onTaskChanged();
    return root_view;
  }

  @Override
  public void onActivityResult(int requestCode, int resultCode, Intent data)
  {
    super.onActivityResult(requestCode, resultCode, data);
    if(resultCode == Activity.RESULT_OK)
    {
      activity.getTask().registerListener(activity.GetListener());
      reaction_task.addReaction(ReactionDA.GetReaction(data.getIntExtra("reaction", -1)));
      onTaskChanged();
    }
  }

  @Override
  public void onTaskChanged()
  {
    if(activity == null || reaction_task == null)
    {
      return;
    }

    ed_runtime.setValue(reaction_task.getRunTime());
    sp_tower.setSelection(tower_ids.indexOf(reaction_task.getStarbaseTower().getID()));

    holders = new ArrayList<>();

    ly_process.removeAllViews();

    for(IReaction proc:reaction_task.getReactions())
    {
      View v = ly_inflater.inflate(R.layout.process, ly_process, false);
      ViewHolderReactor proc_holder = new ViewHolderReactor(v, (Reaction)proc);
      ly_process.addView(v);
      holders.add(proc_holder);
    }
  }

  @Override
  public void onPriceValueChanged()
  {

  }

  @Override
  public void onMaterialSetChanged()
  {

  }

  @Override
  public void onMaterialChanged(int item)
  {

  }

  private class ViewHolderReactor
  {
    private class RemoveListener implements OnClickListener
    {
      @Override
      public void onClick(View v)
      {
        reaction_task.removeReaction(building);
        onTaskChanged();
      }
    }

    // private ImageView im_icon;
    private TextView tx_name;
    private ImageButton bt_remove;
    private ImageView im_icon;

    private Reaction building;
    
    public ViewHolderReactor(View view, Reaction proc)
    {
      building = proc;
      tx_name = (TextView) view.findViewById(R.id.tx_process_name);
      bt_remove = (ImageButton) view.findViewById(R.id.bt_process_remove);
      im_icon = (ImageView) view.findViewById(R.id.im_process_icon);

      if(proc.Inputs.size() > 0)
      {
        im_icon.setImageResource(R.drawable.reactor);
      } else
      {
        im_icon.setImageResource(R.drawable.moonminer);
      }
      tx_name.setText(InventoryDA.getItem(proc.ID).Name);
      bt_remove.setOnClickListener(new RemoveListener());
    }
  }

  @Override
  public void onExtraExpenseChanged()
  {

  }

  @Override
  public void onTaskParameterChanged(int param)
  {

  }
}
