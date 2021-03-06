package com.bitlove.fetlife.view.activity;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.bitlove.fetlife.R;
import com.bitlove.fetlife.event.NotificationReceivedEvent;
import com.bitlove.fetlife.model.pojos.NotificationHistoryItem;
import com.bitlove.fetlife.view.adapter.NotificationHistoryRecyclerAdapter;
import com.raizlabs.android.dbflow.sql.language.Delete;

import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

public class NotificationHistoryActivity extends ResourceListActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    private NotificationHistoryRecyclerAdapter notificationHistoryAdapter;

    public static void startActivity(Context context, boolean newTask) {
        context.startActivity(createIntent(context, newTask));
    }

    public static Intent createIntent(Context context, boolean newTask) {
        Intent intent = new Intent(context, NotificationHistoryActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        if (newTask) {
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        }
        return intent;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        floatingActionButton.setVisibility(View.GONE);

        notificationHistoryAdapter = new NotificationHistoryRecyclerAdapter();
        notificationHistoryAdapter.setOnNotificationHistoryItemClickListener(new NotificationHistoryRecyclerAdapter.OnNotificationHistoryItemClickListener() {
            @Override
            public void onItemClick(NotificationHistoryItem notificationHistoryItem) {
                String launchUrl = notificationHistoryItem.getLaunchUrl();
                if (launchUrl != null && launchUrl.trim().length() != 0) {
                    Intent intent = new Intent(Intent.ACTION_VIEW);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    intent.setData(Uri.parse(launchUrl));
                    startActivity(intent);
                }
            }
        });
        recyclerView.setAdapter(notificationHistoryAdapter);

        showToast(getResources().getString(R.string.notificationhistory_activity_hint));
    }

    @Override
    protected void onStart() {
        super.onStart();
        getFetLifeApplication().getEventBus().register(this);
    }

    @Override
    protected void onStop() {
        super.onStop();
        getFetLifeApplication().getEventBus().unregister(this);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onNotificationReceived(NotificationReceivedEvent notificationReceivedEvent) {
        notificationHistoryAdapter.refresh();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.activity_notificationhistory, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case R.id.action_clear_notification_history:
                //TODO: think of moving it to a db thread
                new Delete().from(NotificationHistoryItem.class).query();
                notificationHistoryAdapter.refresh();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}
