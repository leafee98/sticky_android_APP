package com.example.sticky.DetailActivity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.sticky.R;
import com.example.sticky.client.Client;
import bistu.share.Detail;

import java.sql.Timestamp;
import java.util.Date;

public class DetailActivity extends AppCompatActivity {

    private Handler handler;
    private Thread stickyUpdater;
    private boolean clickable = true;

    private Detail detail;

    private EditText stickyContent;
    private TextView stickyModify;
    private ProgressBar progressLoading;
    private ProgressBar progressSaving;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);

        this.handler = new HandlerDetailActivity(this);
        this.assignView();

        long id = this.getVariable();
        this.requestDetail(id);

        Toolbar toolbar = findViewById(R.id.toolbar_detail);
        super.setSupportActionBar(toolbar);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_detail, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if(clickable)
            this.removeSticky(detail.getId());
        return true;
    }

    @Override
    public void onConfigurationChanged(@NonNull Configuration newConfig) { }

    /**
     * set content with data come from @param d.
     * Will init updater once be called.
     * @param d the Detail object.
     */
    void assignDetail(Detail d) {
        this.detail = d;
        this.stickyContent.setText(d.getFullText());
        this.stickyModify.setText(d.getModifyTime().toString());
        this.initStickyUpdater();
    }

    void showProgressLoading(boolean show) {
        if (show) {
            this.progressLoading.setVisibility(View.VISIBLE);
            this.clickable = false;
        } else {
            this.progressLoading.setVisibility(View.GONE);
            this.clickable = true;
        }
    }

    void showProgressSaving(boolean show) {
        if (show) {
            this.progressSaving.setVisibility(View.VISIBLE);
        } else {
            this.progressSaving.setVisibility(View.GONE);
        }
    }

    void updateModifyTime(Date d) {
        this.stickyModify.setText(d.toString());
    }

    /**
     * Get content from textView and update sticky by client every 3 seconds.
     * Will update the last time once interrupted.
     */
    private void initStickyUpdater() {
        this.stickyUpdater = new Thread(() -> {
            try {
                while (true) {
                    Thread.sleep(3000);

                    String newContent = this.stickyContent.getText().toString();
                    if (!newContent.equals(this.detail.getFullText())) {
                        Message msg1 = new Message();
                        Message msg2 = new Message();
                        Message msg3 = new Message();
                        msg1.what = HandlerDetailActivity.SHOW_PROGRESS_SAVING;
                        msg2.what = HandlerDetailActivity.HIDE_PROGRESS_SAVING;

                        this.handler.sendMessage(msg1);

                        Date d = new Date();
                        this.detail.setFullText(newContent);
                        this.detail.setModifyTime(new Timestamp(d.getTime()));
                        Log.i(DetailActivity.class.getName(), String.format("Detail: %s", this.detail.toString()));

                        if (Client.getInstance().updateSticky(this.detail))
                            Log.i(DetailActivity.class.getName(), "updated Detail.");
                        else
                            runOnUiThread(() ->
                                    Toast.makeText(this, "fail to update sticky. (auto update every 3 sec)",
                                            Toast.LENGTH_SHORT).show());


                        msg3.what = HandlerDetailActivity.UPDATE_MODIFY;
                        msg3.obj = d;
                        this.handler.sendMessage(msg3);
                        this.handler.sendMessage(msg2);
                    }
                }
            } catch (InterruptedException e) {
                Log.w(DetailActivity.class.getName(), "updater interrupted.");

                String newContent = this.stickyContent.getText().toString();
                if (!newContent.equals(this.detail.getFullText())) {
                    Log.i(DetailActivity.class.getName(), "updating Detail.");
                    Date d = new Date();
                    this.detail.setFullText(newContent);
                    this.detail.setModifyTime(new Timestamp(d.getTime()));

                    if (Client.getInstance().updateSticky(this.detail))
                        Log.i(DetailActivity.class.getName(), "updated Detail.");
                    else
                        runOnUiThread(() ->
                                Toast.makeText(this, "fail to update sticky on activity finish.",
                                        Toast.LENGTH_SHORT).show());

                    Log.i(DetailActivity.class.getName(), "updated Detail.");
                }
            }
        });
        this.stickyUpdater.start();
    }

    /**
     * Request Detail by client (network request).
     * Will call <b>this.assignDetail()</b> by handler;
     * @param id the if of Detail requested.
     */
    private void requestDetail(long id) {
        this.showProgressLoading(true);
        Log.i(DetailActivity.class.getName(), String.format("requiring Detail, id=%d", id));
        new Thread(() -> {
            Detail d = Client.getInstance().getDetail(id);
            if (d != null) {
                Message msg1 = new Message();
                msg1.what = HandlerDetailActivity.ASSIGN_DETAIL;
                msg1.obj = d;
                handler.sendMessage(msg1);
            } else {
                Toast.makeText(this, "fail to get detail of sticky.", Toast.LENGTH_SHORT).show();
            }

            Message msg2 = new Message();
            msg2.what = HandlerDetailActivity.HIDE_PROGRESS_BAR;
            handler.sendMessage(msg2);
        }).start();
    }

    /**
     * assign this.client from intent
     * @return the id gotten from intent.
     */
    private long getVariable() {
        Intent intent = getIntent();
        return intent.getLongExtra("id", -1);
    }

    private void assignView() {
        this.stickyContent = this.findViewById(R.id.editText_sticky_detail_content);
        this.stickyModify = this.findViewById(R.id.textView_sticky_detail_modify);
        this.progressLoading = this.findViewById(R.id.progressBar_loading_detail);
        this.progressSaving = this.findViewById(R.id.progressBar_saving_detail);
    }

    /**
     * showProgressLoading -> remove sticky on server (network request)
     * -> finish activity.
     * @param id the id of sticky which will be removed.
     */
    private void removeSticky(long id) {
        this.showProgressLoading(true);
        new Thread(() -> {
            if (!Client.getInstance().removeStick(id))
                Toast.makeText(this, "fail to remove sticky due to network error.",
                        Toast.LENGTH_SHORT).show();

            Message msg1 = new Message();
            Message msg2 = new Message();
            msg1.what = HandlerDetailActivity.HIDE_PROGRESS_BAR;
            msg2.what = HandlerDetailActivity.FINISH_ACTIVITY;
            handler.sendMessage(msg1);
            handler.sendMessage(msg2);
        }).start();
    }

    @Override
    protected void onDestroy() {
        Log.w(DetailActivity.class.getName(), "Activity destroyed.");
        this.stickyUpdater.interrupt();
        super.onDestroy();
    }
}
