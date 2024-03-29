package com.example.sticky.Overview;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.example.sticky.DetailActivity.DetailActivity;
import com.example.sticky.R;
import com.example.sticky.client.Client;
import bistu.share.Overview;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.List;

public class OverviewActivity extends AppCompatActivity {

    private Button addSticky;
    private RecyclerView recycler;
    private ProgressBar progress;
    private StickyOverviewAdapter adapter;

    private boolean startup = true;
    private boolean clickable = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_overview);

        Toolbar toolbar = findViewById(R.id.toolbar_overview);
        setSupportActionBar(toolbar);

        this.assignView();
        this.assignAction();

        // connect last connected server at startup.
        SharedPreferences sp = this.getSharedPreferences("serverAddress", MODE_PRIVATE);
        String address = sp.getString("address", null);
        if (address != null) {
            this.initClient(address);
        }else {
            this.showConfigureServerDialog();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = this.getMenuInflater();
        inflater.inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (clickable) {
            switch (item.getItemId()) {
                case R.id.refresh_list:
                    this.refreshOverviewList();
                    break;
                case R.id.connect_configuration:
                    this.showConfigureServerDialog();
                    break;
            }
        }
        return true;
    }

    /**
     * refresh list while come back from detail.
     */
    @Override
    protected void onResume() {
        super.onResume();
        if (!this.startup)
            this.refreshOverviewList();
        else
            this.startup = false;
    }

    @Override
    protected void onDestroy() {
        new Thread(() -> Client.getInstance().shutdown()).start();
        super.onDestroy();
    }

    @Override
    public void onConfigurationChanged(@NonNull Configuration newConfig) { }

    /**
     * start DetailActivity to view sticky's detail.
     * @param id the id of sticky to view.
     */
    void viewDetail(long id) {
        if (clickable) {
            Intent instant = new Intent(this, DetailActivity.class);
            instant.putExtra("id", id);
            startActivity(instant);
        }
    }

    private void assignAction() {
        this.addSticky.setOnClickListener((View v) ->
            new Thread(() -> {
                if (clickable) {
                    this.runOnUiThread(() -> this.showProgress(true));
                    long id = Client.getInstance().addSticky();
                    if (id > 0)
                        this.runOnUiThread(() -> {
                            this.showProgress(false);
                            this.viewDetail(id);
                        });
                    else
                        Toast.makeText(this, "unknown error occurred while adding sticky",
                                Toast.LENGTH_SHORT).show();
                }
            }).start()
        );
    }

    private void assignView() {
        this.addSticky = this.findViewById(R.id.button_add_sticky);
        this.progress = this.findViewById(R.id.progressBar_loading_overview);
        this.recycler = this.findViewById(R.id.recycler_sticky_overview);

        this.recycler.setLayoutManager(new LinearLayoutManager(this));
    }

    private void initClient(String address) {
        this.showProgress(true);
        new Thread(() -> {
            try {
                Log.i("initClient()", String.format("Address: %s", address));
                int index = address.indexOf(':');
                if (index < 0)
                    throw new NumberFormatException();
                String hostAddress = address.substring(0, index);
                int port = Integer.parseInt(address.substring(index + 1));

                if(Client.init(InetAddress.getByName(hostAddress), port)) {
                    Log.i("initClient()", "connected to server.");

                    // save address connected.
                    SharedPreferences.Editor editor = this.getSharedPreferences("serverAddress", MODE_PRIVATE).edit();
                    editor.putString("address", address);
                    editor.apply();

                    this.runOnUiThread(() -> {
                        Toast.makeText(this, "connected to server.", Toast.LENGTH_SHORT).show();
                        this.refreshOverviewList();
                    });
                } else {
                    this.runOnUiThread(() -> {
                        Toast.makeText(this, "fail to connect to server due to unknown error.", Toast.LENGTH_SHORT).show();
                        this.showConfigureServerDialog();
                    });
                }
            } catch (NumberFormatException e) {
                this.runOnUiThread(() -> {
                    Toast.makeText(this, "server address format error!", Toast.LENGTH_SHORT).show();
                    this.showConfigureServerDialog();
                });
            } catch (UnknownHostException e) {
                this.runOnUiThread(() -> {
                    Toast.makeText(this, "unable to resolve address!", Toast.LENGTH_SHORT).show();
                    this.showConfigureServerDialog();
                });
            }

            this.runOnUiThread(() -> this.showProgress(false));
        }).start();
    }

    private void showProgress(boolean show) {
        if (show) {
            this.progress.setVisibility(View.VISIBLE);
            this.clickable = false;
            Log.i("progressBar", "show progress bar.");
        } else {
            this.progress.setVisibility(View.INVISIBLE);
            this.clickable = true;
            Log.i("progressBar", "hide progress bar.");
        }
    }

    private void refreshOverviewList() {
        this.showProgress(true);
        new Thread(() -> {
            List<Overview> overviews = Client.getInstance().getList();
            if (overviews != null) {
                this.adapter = new StickyOverviewAdapter(this, overviews);
                this.runOnUiThread(() -> {
                    this.recycler.setAdapter(adapter);
                    this.showProgress(false);
                });
            } else {
                this.runOnUiThread(() -> {
                    Toast.makeText(this, "disconnected from server due to network error.", Toast.LENGTH_SHORT).show();
                    this.showProgress(false);
                });
            }
        }).start();
    }

    private void showConfigureServerDialog() {
        AlertDialog.Builder builder =new AlertDialog.Builder(this);

        View dialogLayout = View.inflate(this, R.layout.dialog_connect_to, null);
        builder.setView(dialogLayout);
        EditText serverAddress = dialogLayout.findViewById(R.id.editText_server_address);

        // set text on editText to be server last connected.
        SharedPreferences sp = getSharedPreferences("serverAddress", MODE_PRIVATE);
        serverAddress.setText(sp.getString("address", ""));

        builder.setTitle(R.string.dialog_title_connect_to);
        builder.setPositiveButton("OK", (DialogInterface dia, int which) ->
               this.initClient(serverAddress.getText().toString()));
        builder.setCancelable(false);
        builder.show();
    }

}
