package com.team242.robozzle;

import android.content.Context;
import android.content.res.Resources;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import com.team242.robozzle.achievements.Achievement;

import java.util.List;

/**
 * Created by influnza on 10.10.2015.
 */
public class AchievementAdapter extends ArrayAdapter<Achievement> {

    GenericPuzzleActivity activity;
    private final int listEntryViewId;

    public AchievementAdapter(GenericPuzzleActivity activity, int resourceId, List<Achievement> items) {
        super(activity, resourceId, items);
        this.activity = activity;
        this.listEntryViewId = resourceId;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        Achievement achievement = getItem(position);
        if (convertView == null || convertView instanceof TextView) {
            LayoutInflater vi = (LayoutInflater)parent.getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            try {
                convertView = vi.inflate(listEntryViewId, null);
            } catch (OutOfMemoryError e){
                TextView lowMemAchievement = convertView == null ? new TextView(activity) : (TextView)convertView;
                Resources res = activity.getResources();
                lowMemAchievement.setText(res.getText(achievement.titleID));
                convertView = lowMemAchievement;
            }
        }

        setAchievement(convertView, achievement);

        return convertView;
    }

    private void setAchievement(View view, Achievement achievement) {
        TextView title = (TextView)view.findViewById(R.id.achievementTitle);
        TextView description = (TextView)view.findViewById(R.id.achievementDescription);
        ImageView icon = (ImageView)view.findViewById(R.id.achievementIcon);

        Resources res = activity.getResources();
        title.setText(res.getText(achievement.titleID));
        description.setText(res.getText(achievement.descriptionID));

        try {
            if (achievement.isStateSolved(this.activity.pref)) {
                icon.setImageResource(achievement.iconID);
            } else {
                icon.setImageResource(android.R.drawable.ic_lock_lock);
            }
        }catch (OutOfMemoryError e){
            icon.setImageResource(R.drawable.lowmem);
        }
    }
}
