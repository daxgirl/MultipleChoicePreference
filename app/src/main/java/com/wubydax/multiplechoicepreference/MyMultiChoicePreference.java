package com.wubydax.multiplechoicepreference;

import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.preference.DialogPreference;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Created by Anna Berkovitch on 03/03/2016. Duh-Duh...
 * For Wuby :-)
 */
public class MyMultiChoicePreference extends DialogPreference {
    private static final String LOG_TAG = "Preference";
    private Context c;
    private PackageManager pm;
    private List<AppInfo> mList;
    private AsyncTask<Void, Void, Void> mAppLoader;
    private RecyclerView recyclerView;
    private List<String> mPersistedPackagesList;
    private ProgressBar mProgressBar;

    public MyMultiChoicePreference(Context context, AttributeSet attrs) {
        super(context, attrs);
        c = context;
        pm = c.getPackageManager();
        setDialogLayoutResource(R.layout.dialog_layout);
    }

    private List<AppInfo> getAppList() {
        List<AppInfo> list = new ArrayList<>();
        Intent intent = new Intent(Intent.ACTION_MAIN, null);
        intent.addCategory(Intent.CATEGORY_LAUNCHER);
        List<ResolveInfo> resolveInfos = pm.queryIntentActivities(intent, 0);
        String persistedString = getPersistedString("");
        Log.d(LOG_TAG, "getAppList persisted string is " + persistedString);
        if (persistedString != null) {
            String[] packagesArray = persistedString.split(";");
            Collections.addAll(mPersistedPackagesList, packagesArray);
        }
        for (int i = 0; i < resolveInfos.size(); i++) {
            AppInfo appInfo = new AppInfo();
            appInfo.appIcon = resolveInfos.get(i).activityInfo.loadIcon(pm);
            appInfo.appLabel = resolveInfos.get(i).loadLabel(pm).toString();
            appInfo.appPackage = resolveInfos.get(i).activityInfo.packageName;
            appInfo.isSelected = mPersistedPackagesList.contains(appInfo.appPackage);
            ActivityInfo ai = resolveInfos.get(i).activityInfo;

            ComponentName name = new ComponentName(ai.applicationInfo.packageName,
                    ai.name);
            Intent launchIntent = new Intent(Intent.ACTION_MAIN);
            launchIntent.addCategory(Intent.CATEGORY_LAUNCHER);
            launchIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            launchIntent.setComponent(name);
            appInfo.intent = launchIntent;
            list.add(appInfo);
        }
        return list;
    }

    @Override
    public void onClick(final DialogInterface dialog, int which) {
        if (which == AlertDialog.BUTTON_POSITIVE) {
            StringBuilder sb = new StringBuilder();
            Log.d(LOG_TAG, "onClick triggered by positive button");
            for (String value : mPersistedPackagesList) {
                Log.d(LOG_TAG, "onClick value is " + value);
                if (!value.equals("")) {
                    sb.append(value).append(";");
                } else {
                    sb.append("");
                }

            }
            persistString(sb.toString());
            Log.d(LOG_TAG, "onClick persisted string is " + getPersistedString("nothing found"));
        }


    }

    @Override
    protected void onBindDialogView(View view) {
        super.onBindDialogView(view);
        recyclerView = (RecyclerView) view.findViewById(R.id.recyclerView);
        mPersistedPackagesList = new ArrayList<>();
        mProgressBar = (ProgressBar) view.findViewById(R.id.progressBar);
        loadApps();
    }

    public class AppInfo {
        String appLabel;
        String appPackage;
        Drawable appIcon;
        Intent intent;
        boolean isSelected;
    }

    private void loadApps() {
        mAppLoader = new AsyncTask<Void, Void, Void>() {

            @Override
            protected void onPreExecute() {
                mProgressBar.setVisibility(View.VISIBLE);
            }

            @Override
            protected Void doInBackground(Void... params) {
                mList = getAppList();
                Collections.sort(mList, new Comparator<AppInfo>() {

                    @Override
                    public int compare(AppInfo lhs, AppInfo rhs) {
                        return String.CASE_INSENSITIVE_ORDER.compare(lhs.appLabel, rhs.appLabel);
                    }
                });
                return null;
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                mProgressBar.setVisibility(View.GONE);
                LinearLayoutManager llm = new LinearLayoutManager(c);
                recyclerView.setLayoutManager(llm);
                AppAdapter appAdapter = new AppAdapter();
                recyclerView.setAdapter(appAdapter);
            }
        }.execute();
    }

    public class AppAdapter extends RecyclerView.Adapter<AppAdapter.ViewHolder> {


        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item, parent, false);
            final ViewHolder vh = new ViewHolder(v);
            v.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    vh.checkBox.setChecked(!vh.checkBox.isChecked());
                }
            });
            return vh;
        }

        @Override
        public void onBindViewHolder(ViewHolder holder, final int position) {
            holder.appName.setText(mList.get(position).appLabel);
            holder.appIcon.setImageDrawable(mList.get(position).appIcon);
            holder.checkBox.setFocusable(false);
            holder.checkBox.setOnCheckedChangeListener(null);
            holder.checkBox.setChecked(mList.get(position).isSelected);
            holder.checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    if (isChecked) {
                        mPersistedPackagesList.add(mList.get(position).appPackage);
                    } else {
                        mPersistedPackagesList.remove(mList.get(position).appPackage);
                    }
                    mList.get(position).isSelected = isChecked;

                }
            });
        }


        @Override
        public int getItemCount() {
            if (mList != null) {
                return mList.size();

            } else {
                return 0;
            }
        }

        class ViewHolder extends RecyclerView.ViewHolder {
            CheckBox checkBox;
            ImageView appIcon;
            TextView appName;

            public ViewHolder(View itemView) {
                super(itemView);
                checkBox = (CheckBox) itemView.findViewById(R.id.checkBox);
                appIcon = (ImageView) itemView.findViewById(R.id.appIcon);
                appName = (TextView) itemView.findViewById(R.id.appName);
            }
        }
    }

    @Override
    public void onDismiss(DialogInterface dialog) {
        super.onDismiss(dialog);
        if (mAppLoader != null && mAppLoader.getStatus() == AsyncTask.Status.RUNNING) {
            mAppLoader.cancel(true);
            mAppLoader = null;
        }
    }
}
