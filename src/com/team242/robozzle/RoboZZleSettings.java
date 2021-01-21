/**
 * 
 */
package com.team242.robozzle;

import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.preference.PreferenceFragment;

/**
 * @author lost
 *
 */
public class RoboZZleSettings extends PreferenceActivity {
	public static final String SHARED_PREFERENCES_NAME = "team242_robozzle_settings";
	
	public static final String SHOW_SOLVED = "show_solved";
	public static final String FIRST_TIME_SOLVER = "first_time_solver";
	public static final String HIDE_UNPOPULAR = "hide_unpopular";
	public static final String LOGIN = "login_setting";
	public static final String PASSWORD = "password";
	public static final String SPEED = "speed";
	public static final String AUTO_SOLUTION_SUBMIT = "submit_solutions";
	public static final String ROB_AI_TELEMETRY_ENABLED = "rob_ai_telemetry_enabled";
	public static final String THUMBNAIL_MEMORY_USAGE = "thumbnail_memory_usage";

	public static final boolean ROB_AI_TELEMETRY_DEFAULT = false;
	public static final boolean AUTO_SOLUTION_SUBMIT_DEFAULT = true;

	@Override
	protected void onCreate(Bundle state){
		super.onCreate(state);

		getFragmentManager()
			.beginTransaction()
				.replace(android.R.id.content, new RoboZZlePreferencesFragment())
			.commit();
	}

	public boolean isValidFragment(String fragmentName){
		return RoboZZlePreferencesFragment.class.getName().equals(fragmentName);
	}

	public static class RoboZZlePreferencesFragment extends PreferenceFragment {
		@Override
		public void onCreate(final Bundle savedInstanceState){
			super.onCreate(savedInstanceState);
			getPreferenceManager().setSharedPreferencesName(SHARED_PREFERENCES_NAME);
			addPreferencesFromResource(R.xml.team242_robozzle_settings);
		}
	}
}
