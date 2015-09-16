package com.exter.eveindcalc.manufacturing;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;

import com.exter.controls.DoubleEditText;
import com.exter.controls.IntegerEditText;
import com.exter.eveindcalc.EICFragmentActivity;
import com.exter.eveindcalc.IEveCalculatorFragment;
import com.exter.eveindcalc.R;
import com.exter.eveindcalc.SolarSystemDialogFragment;
import com.exter.eveindcalc.data.blueprint.BlueprintHistoryDA;
import com.exter.eveindcalc.data.blueprint.Installation;
import com.exter.eveindcalc.data.blueprint.InstallationDA;
import com.exter.eveindcalc.data.blueprint.InstallationGroup;
import com.exter.eveindcalc.data.blueprint.InventionInstallation;
import com.exter.eveindcalc.data.decryptor.Decryptor;
import com.exter.eveindcalc.data.decryptor.DecryptorDA;
import com.exter.eveindcalc.data.inventory.InventoryDA;
import com.exter.eveindcalc.data.inventory.Item;
import com.exter.eveindcalc.data.starmap.RecentSystemsDA;
import com.exter.eveindcalc.data.starmap.Starmap;
import com.exter.eveindcalc.util.XUtil;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import exter.eveindustry.data.blueprint.IBlueprint;
import exter.eveindustry.task.ManufacturingTask;

public class ManufacturingFragment extends Fragment implements IEveCalculatorFragment
{
  private class InventionEnableListener implements CheckBox.OnCheckedChangeListener
  {
    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked)
    {
      if(man_task == null)
      {
        return;
      }
      man_task.setInventionEnabled(isChecked);
      onTaskChanged();
      updateTime();
      updateChance();
    }
  }

  private class ViewHolderSkill
  {
    private class SkillItemSelectedListener implements Spinner.OnItemSelectedListener
    {
      @Override
      public void onItemSelected(AdapterView<?> parent, View view, int pos, long id)
      {
        if(man_task == null)
        {
          return;
        }
        setLevel(pos);
      }

      @Override
      public void onNothingSelected(AdapterView<?> parent)
      {

      }
    }

    private TextView tx_name;
    private Spinner sp_level;

    public void Update()
    {
      tx_name.setText(getName() + " skill:");
      sp_level.setSelection(getLevel());
    }

    public ViewHolderSkill(View view, int id)
    {
      skill = id;
      tx_name = (TextView) view.findViewById(R.id.tx_manufacturing_skillname);
      sp_level = (Spinner) view.findViewById(R.id.sp_manufacturing_skilllevel);

      sp_level.setOnItemSelectedListener(new SkillItemSelectedListener());
    }

    private int skill;

    public String getName()
    {
      if(man_task == null)
      {
        return "";
      }
      return InventoryDA.getItem(skill).Name;
    }

    public int getLevel()
    {
      if(man_task == null)
      {
        return 0;
      }
      return man_task.getSkillLevel(skill);
    }

    public void setLevel(int level)
    {
      if(man_task == null)
      {
        return;
      }
      man_task.setSkillLevel(skill, level);
      ed_copies.setValue(man_task.getCopies());
      SharedPreferences sp = activity.getSharedPreferences("EIC", Context.MODE_PRIVATE);
      SharedPreferences.Editor ed = sp.edit();
      ed.putInt("skill_" + String.valueOf(skill), man_task.getSkillLevel(skill));
      ed.apply();
      updateChance();
      updateTime();
    }
  }

  private class AttemptsChangeWatcher implements IntegerEditText.ValueListener
  {
    @Override
    public void onValueChanged(int new_value)
    {
      if(man_task == null || man_task.getInvention() == null)
      {
        return;
      }
      man_task.getInvention().setAttempts(new_value);
      ed_copies.setValue(man_task.getCopies());
    }
  }

  private class DecryptorSelectedListener implements Spinner.OnItemSelectedListener
  {
    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int pos, long id)
    {
      if(man_task == null || man_task.getInvention() == null)
      {
        return;
      }
      man_task.getInvention().setDecryptor(pos == 0 ? null : DecryptorDA.GetDecryptors().get(pos - 1));

      ed_runs.setValue(man_task.getRuns());
      ed_copies.setValue(man_task.getCopies());
      ed_meresearch.setValue(man_task.getME());
      ed_teresearch.setValue(man_task.getTE());

      updateChance();
      updateTime();
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent)
    {

    }
  }

  private class RelicSelectedListener implements Spinner.OnItemSelectedListener
  {
    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int pos, long id)
    {
      if(man_task == null || man_task.getInvention() == null)
      {
        return;
      }

      man_task.getInvention().setRelic(relic_ids.get(pos));

      ed_runs.setValue(man_task.getRuns());
      ed_copies.setValue(man_task.getCopies());

      updateChance();
      updateTime();
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent)
    {

    }
  }

  private class MeResearchChangeWatcher implements IntegerEditText.ValueListener
  {
    @Override
    public void onValueChanged(int new_value)
    {
      if(man_task == null)
      {
        return;
      }
      man_task.setME(new_value);
      histent.SetME(new_value);
    }
  }

  private class TeResearchChangeWatcher implements IntegerEditText.ValueListener
  {
    @Override
    public void onValueChanged(int new_value)
    {
      if(man_task == null)
      {
        return;
      }
      man_task.setTE(new_value);
      histent.SetTE(new_value);
      updateTime();
    }
  }

  private class RunsChangeWatcher implements IntegerEditText.ValueListener
  {
    @Override
    public void onValueChanged(int new_value)
    {
      if(man_task == null)
      {
        return;
      }
      man_task.setRuns(new_value);
      updateTime();
    }
  }

  private class InventionRunsChangeWatcher implements IntegerEditText.ValueListener
  {
    @Override
    public void onValueChanged(int new_value)
    {
      if(man_task == null || man_task.getInvention() == null)
      {
        return;
      }
      man_task.getInvention().setInventionRuns(new_value);
      ed_copies.setValue(man_task.getCopies());
      updateTime();
    }
  }

  private class CopiesChangeWatcher implements IntegerEditText.ValueListener
  {
    @Override
    public void onValueChanged(int new_value)
    {
      if(man_task == null)
      {
        return;
      }
      man_task.setCopies(new_value);
      if(man_task.getInvention() != null)
      {
        updateTime();
      }
    }
  }

  private class HardwiringItemSelectedListener implements Spinner.OnItemSelectedListener
  {
    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int pos, long id)
    {
      if(man_task == null)
      {
        return;
      }
      man_task.setHardwiring(ManufacturingTask.Hardwiring.fromInt(pos));
      SharedPreferences sp = getActivity().getSharedPreferences("EIC", Context.MODE_PRIVATE);
      SharedPreferences.Editor ed = sp.edit();
      ed.putInt("manufacturing.hardwiring", pos);
      ed.apply();
      updateTime();
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent)
    {

    }
  }

  private class InstallationSelectedListener implements Spinner.OnItemSelectedListener
  {
    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int pos, long id)
    {
      if(man_task == null)
      {
        return;
      }
      man_task.setInstallation(installation_groups.get(pos));
      updateTime();
      activity.notiftyExtraExpenseChanged();
      activity.onProfitChanged();
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent)
    {

    }
  }

  private class InventionInstallationSelectedListener implements Spinner.OnItemSelectedListener
  {
    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int pos, long id)
    {
      if(man_task == null)
      {
        return;
      }
      man_task.getInvention().setInstallation(InstallationDA.getInventionInstallation(invention_installations.get(pos)));
      updateTime();
      activity.notiftyExtraExpenseChanged();
      activity.onProfitChanged();
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent)
    {

    }
  }

  private class SystemSelectedListener implements Spinner.OnItemSelectedListener
  {
    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int pos, long id)
    {
      if(man_task == null)
      {
        return;
      }
      int i = system_ids.get(pos);
      if(i == -1)
      {
        SolarSystemDialogFragment dialog = new ManufacturingSolarSystemDialogFragment();
        dialog.show(activity.getSupportFragmentManager(), "SolarSystemDialogFragment");
      } else
      {
        SharedPreferences sp = getActivity().getSharedPreferences("EIC", Context.MODE_PRIVATE);
        SharedPreferences.Editor ed = sp.edit();
        ed.putInt("manufacturing.system", i);
        ed.apply();
        man_task.setSolarSystem(i);
        activity.notiftyExtraExpenseChanged();
        activity.onProfitChanged();
      }
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent)
    {

    }
  }

  private class TaxChangeWatcher implements DoubleEditText.ValueListener
  {
    @Override
    public void valueChanged(int tag, double new_value)
    {
      if(man_task == null)
      {
        return;
      }
      man_task.setInstallationTax((float) new_value);
      activity.notiftyExtraExpenseChanged();
      activity.onProfitChanged();
    }
  }

  private Spinner sp_hardwiring;
  private Spinner sp_system;
  private IntegerEditText ed_meresearch;
  private IntegerEditText ed_teresearch;
  private IntegerEditText ed_runs;
  private IntegerEditText ed_copies;
  private DoubleEditText ed_tax;
  private Spinner sp_installation;
  private TextView tx_time;
  private TextView tx_invention_success;
  private TextView tx_invention_time;
  private Spinner sp_invention_relic;
  private LinearLayout ly_invention_relic;
  private LinearLayout ly_invention_enable;
  private LinearLayout ly_invention;
  private LinearLayout ly_skills;
  private LinearLayout ly_invention_installation;
  private CheckBox ch_invention_enable;

  private IntegerEditText ed_invention_attempts;
  private IntegerEditText ed_invention_runs;
  private Spinner sp_invention_decryptor;
  private Spinner sp_invention_installation;

  private List<Integer> system_ids;

  private List<InstallationGroup> installation_groups;
  private List<Integer> invention_installations;

  private EICFragmentActivity activity;

  private LayoutInflater ly_inflater;

  private List<Integer> relic_ids;
  public ManufacturingTask man_task;
  public BlueprintHistoryDA.Entry histent;

  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
  {
    activity = (EICFragmentActivity) getActivity();

    man_task = (ManufacturingTask) activity.getTask();
    histent = BlueprintHistoryDA.GetEntry(man_task.getBlueprint().getID());
    if(histent == null)
    {
      histent = new BlueprintHistoryDA.Entry(man_task.getBlueprint().getID(), man_task.getME(), man_task.getTE());
      histent.Update();
    }

    View root_view = inflater.inflate(R.layout.manufacturing_main, container, false);
    ly_inflater = (LayoutInflater) activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

    sp_hardwiring = (Spinner) root_view.findViewById(R.id.sp_bpcalc_hardwiring);
    sp_system = (Spinner) root_view.findViewById(R.id.sp_bpcalc_solarsystem);
    ed_meresearch = new IntegerEditText((EditText) root_view.findViewById(R.id.ed_bpcalc_melevel), 0, 10, 0, new MeResearchChangeWatcher());
    ed_teresearch = new IntegerEditText((EditText) root_view.findViewById(R.id.ed_bpcalc_pelevel), 0, 20, 0, new TeResearchChangeWatcher());
    sp_installation = (Spinner) root_view.findViewById(R.id.sp_bpcalc_installation);
    ed_tax = new DoubleEditText((EditText) root_view.findViewById(R.id.ed_bpcalc_tax), -1, 0.0, 100.0, 15.0, new TaxChangeWatcher());

    ed_runs = new IntegerEditText((EditText) root_view.findViewById(R.id.ed_bpcalc_runs), 1, 9999999, 0, new RunsChangeWatcher());
    ed_copies = new IntegerEditText((EditText) root_view.findViewById(R.id.ed_bpcalc_copies), 0, 9999999, 0, new CopiesChangeWatcher());
    tx_time = (TextView) root_view.findViewById(R.id.tx_bpcalc_time);
    tx_invention_success = (TextView) root_view.findViewById(R.id.tx_bpcalc_invention_success);
    tx_invention_time = (TextView) root_view.findViewById(R.id.tx_bpcalc_invention_time);

    ly_invention = (LinearLayout) root_view.findViewById(R.id.ly_bpcalc_invention);
    ly_invention_enable = (LinearLayout) root_view.findViewById(R.id.ly_bpcalc_invention_enable);
    ly_invention_relic = (LinearLayout) root_view.findViewById(R.id.ly_bpcalc_relic);
    ly_skills = (LinearLayout) root_view.findViewById(R.id.ly_bpcalc_skills);
    ly_invention_installation = (LinearLayout) root_view.findViewById(R.id.ly_bpcalc_invention_installation);
    ch_invention_enable = (CheckBox) root_view.findViewById(R.id.ch_bpcalc_invention_enable);
    sp_invention_relic = (Spinner) root_view.findViewById(R.id.sp_bpcalc_relic);

    ed_invention_attempts = new IntegerEditText((EditText) root_view.findViewById(R.id.ed_bpcalc_attempts), 1, 9999999, 0, new AttemptsChangeWatcher());
    ed_invention_runs = new IntegerEditText((EditText) root_view.findViewById(R.id.ed_bpcalc_invruns), 1, 9999999, 0, new InventionRunsChangeWatcher());
    sp_invention_decryptor = (Spinner) root_view.findViewById(R.id.sp_bpcalc_decryptor);
    sp_invention_installation = (Spinner) root_view.findViewById(R.id.sp_bpcalc_invention_installation);

    sp_hardwiring.setOnItemSelectedListener(new HardwiringItemSelectedListener());
    sp_system.setOnItemSelectedListener(new SystemSelectedListener());

    sp_installation.setOnItemSelectedListener(new InstallationSelectedListener());

    sp_invention_installation.setOnItemSelectedListener(new InventionInstallationSelectedListener());
    sp_invention_decryptor.setOnItemSelectedListener(new DecryptorSelectedListener());
    sp_invention_relic.setOnItemSelectedListener(new RelicSelectedListener());

    ch_invention_enable.setOnCheckedChangeListener(new InventionEnableListener());

    onTaskChanged();

    return root_view;
  }

  @Override
  public void onPause()
  {
    histent.Update();
    super.onPause();
  }

  @Override
  public void onDestroy()
  {
    histent.Update();
    super.onDestroy();
  }

  private void updateChance()
  {
    if(man_task == null || man_task.getInvention() == null)
    {
      return;
    }
    DecimalFormat f = new DecimalFormat("##.#");
    tx_invention_success.setText(f.format(man_task.getInvention().getChance() * 100) + " %");
  }

  private void updateTime()
  {
    int eff_time = man_task.getProductionTime();
    tx_time.setText(XUtil.TimeToStr(eff_time));
    if(man_task.getInvention() != null)
    {
      tx_invention_time.setText(XUtil.TimeToStr(man_task.getInvention().getInventionTime()));
    }
    activity.notiftyExtraExpenseChanged();
  }

  @Override
  public void onTaskChanged()
  {
    if(activity == null || man_task == null)
    {
      return;
    }

    List<InstallationGroup> installations = InstallationDA.getBlueprintInstallations(man_task.getBlueprint());
    List<String> installation_names = new ArrayList<>();
    installation_groups = new ArrayList<>();
    for(InstallationGroup ig : installations)
    {
      Installation ins = InstallationDA.getInstallation(ig.Installation);
      if(ins.ID == InstallationDA.getDefaultInstallation())
      {
        installation_groups.add(0, ig);
        installation_names.add(0, ins.Name);
      } else
      {
        installation_groups.add(ig);
        installation_names.add(ins.Name);
      }
    }
    ArrayAdapter<String> spinnerArrayAdapter = new ArrayAdapter<>(activity, android.R.layout.simple_spinner_item, installation_names);
    spinnerArrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
    sp_installation.setAdapter(spinnerArrayAdapter);
    sp_installation.setSelection(installation_groups.indexOf(man_task.getInstallation()));

    ed_tax.SetValue(man_task.getInstallationTax());

    sp_hardwiring.setSelection(man_task.getHardwiring().value);

    int system = man_task.getSolarSystem();
    RecentSystemsDA.putSystem(system);

    system_ids = new ArrayList<>();
    List<String> system_names = new ArrayList<>();
    for(int id : RecentSystemsDA.getSystems())
    {
      system_ids.add(id);
      system_names.add(Starmap.getSolarSystem(id).Name);
    }
    system_ids.add(-1);
    system_names.add("[ Other ... ]");

    sp_system.setOnItemSelectedListener(null);
    ArrayAdapter<String> sys_spinner_adapter = new ArrayAdapter<>(activity, android.R.layout.simple_spinner_item, system_names);
    sys_spinner_adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
    sp_system.setAdapter(sys_spinner_adapter);
    sp_system.setSelection(system_ids.indexOf(system));
    sp_system.setOnItemSelectedListener(new SystemSelectedListener());

    onTaskParameterChanged(ManufacturingTask.PARAMETER_SKILLS);
    IBlueprint.IInvention bpinv = man_task.getBlueprint().getInvention();
    ManufacturingTask.Invention tinv = man_task.getInvention();
    if(bpinv != null)
    {
      if(tinv != null)
      {

        ly_invention_enable.setVisibility(View.VISIBLE);
        ly_invention_installation.setVisibility(View.VISIBLE);

        ed_invention_attempts.setValue(tinv.getAttempts());
        ed_invention_runs.setValue(tinv.getInventionRuns());

        List<Decryptor> decryptors = DecryptorDA.GetDecryptors();
        List<String> decr_names = new ArrayList<>();
        decr_names.add(" [None]");
        for(Decryptor d : decryptors)
        {
          decr_names.add(((Item)d.getItem()).Name);
        }
        sp_invention_decryptor.setOnItemSelectedListener(null);
        ArrayAdapter<String> decr_spinner_a = new ArrayAdapter<>(activity, android.R.layout.simple_spinner_item, decr_names);
        decr_spinner_a.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        sp_invention_decryptor.setAdapter(decr_spinner_a);
        Decryptor d = (Decryptor) tinv.getDecryptor();
        if(d == null)
        {
          sp_invention_decryptor.setSelection(0);
        } else
        {
          sp_invention_decryptor.setSelection(decryptors.indexOf(d) + 1);
        }
        sp_invention_decryptor.setOnItemSelectedListener(new DecryptorSelectedListener());

        if(bpinv.usesRelics())
        {
          invention_installations = InstallationDA.GetRelicInventionInstallationIDs();
        } else
        {
          invention_installations = InstallationDA.getInventionInstallationIDs();
        }
        List<String> invention_installation_names = new ArrayList<>();
        for(Integer i : invention_installations)
        {
          InventionInstallation ins = InstallationDA.getInventionInstallation(i);
          invention_installation_names.add(ins.Name);
        }
        ArrayAdapter<String> invention_spinnerArrayAdapter = new ArrayAdapter<>(activity, android.R.layout.simple_spinner_item, invention_installation_names);
        invention_spinnerArrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        sp_invention_installation.setAdapter(invention_spinnerArrayAdapter);
        sp_invention_installation.setSelection(invention_installations.indexOf(tinv.GetInstallation().getID()));

        Set<Integer> relics = bpinv.getRelicIDs();
        if(relics != null)
        {
          relic_ids = new ArrayList<>(relics);
          ly_invention_relic.setVisibility(View.VISIBLE);
          List<String> relic_names = new ArrayList<>();
          for(int r : relic_ids)
          {
            relic_names.add(InventoryDA.getItem(r).Name);
          }
          ArrayAdapter<String> relic_spinnerArrayAdapter = new ArrayAdapter<>(activity, android.R.layout.simple_spinner_item, relic_names);
          relic_spinnerArrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
          sp_invention_relic.setAdapter(relic_spinnerArrayAdapter);
          sp_invention_relic.setSelection(relic_ids.indexOf(tinv.GetRelic().getID()));
        } else
        {
          ly_invention_relic.setVisibility(View.GONE);
          relic_ids = null;
        }

        ed_meresearch.SetEnabled(false);
        ed_teresearch.SetEnabled(false);
        ed_runs.SetEnabled(false);

        ch_invention_enable.setOnCheckedChangeListener(null);
        ch_invention_enable.setChecked(true);
        ch_invention_enable.setOnCheckedChangeListener(new InventionEnableListener());
      } else
      {
        ch_invention_enable.setOnCheckedChangeListener(null);
        ch_invention_enable.setChecked(false);
        ch_invention_enable.setOnCheckedChangeListener(new InventionEnableListener());
        ly_invention_enable.setVisibility(View.GONE);
        ly_invention_installation.setVisibility(View.GONE);
        ed_meresearch.SetEnabled(true);
        ed_teresearch.SetEnabled(true);
        ed_runs.SetEnabled(true);
      }
    } else
    {
      ly_invention.setVisibility(View.GONE);
      ly_invention_installation.setVisibility(View.GONE);
      ed_meresearch.SetEnabled(true);
      ed_teresearch.SetEnabled(true);
      ed_runs.SetEnabled(true);
    }
    ed_meresearch.setValue(man_task.getME());
    ed_teresearch.setValue(man_task.getTE());
    ed_runs.setValue(man_task.getRuns());
    ed_copies.setValue(man_task.getCopies());
    updateTime();
    updateChance();
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

  @Override
  public void onExtraExpenseChanged()
  {

  }

  @Override
  public void onTaskParameterChanged(int param)
  {
    switch(param)
    {
      case ManufacturingTask.PARAMETER_INVENTION_ATTEMPTS:
        ed_invention_attempts.setValue(man_task.getInvention().getAttempts());
        break;
      case ManufacturingTask.PARAMETER_INVENTION_RUNS:
        ed_invention_runs.setValue(man_task.getInvention().getInventionRuns());
        break;
      case ManufacturingTask.PARAMETER_SKILLS:
        ly_skills.removeAllViews();
        SortedSet<Integer> skills = new TreeSet<>(man_task.getSkills());
        for(int s : skills)
        {
          View v = ly_inflater.inflate(R.layout.manufacturing_skill, ly_skills, false);
          ViewHolderSkill skill_holder = new ViewHolderSkill(v, s);
          skill_holder.Update();
          ly_skills.addView(v);
        }
        break;
    }
  }
}
