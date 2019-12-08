package com.example.sticky.DetailActivity;

import android.os.Handler;
import android.os.Message;

import androidx.annotation.NonNull;

import bistu.share.Detail;

import java.util.Date;

class HandlerDetailActivity extends Handler {

    private DetailActivity activity;

    static final int HIDE_PROGRESS_BAR = 1;
    static final int SHOW_PROGRESS_SAVING = 2;
    static final int HIDE_PROGRESS_SAVING = 3;
    static final int ASSIGN_DETAIL = 4;
    static final int UPDATE_MODIFY = 5;
    static final int FINISH_ACTIVITY = 9;

    HandlerDetailActivity(DetailActivity activity) {
        this.activity = activity;
    }

    @Override
    public void handleMessage(@NonNull Message msg) {
        super.handleMessage(msg);
        switch (msg.what) {
            case HIDE_PROGRESS_BAR: this.activity.showProgressLoading(false); break;
            case SHOW_PROGRESS_SAVING: this.activity.showProgressSaving(true); break;
            case HIDE_PROGRESS_SAVING: this.activity.showProgressSaving(false); break;
            case ASSIGN_DETAIL: this.activity.assignDetail((Detail) msg.obj); break;
            case UPDATE_MODIFY: this.activity.updateModifyTime((Date) msg.obj); break;
            case FINISH_ACTIVITY: this.activity.finish(); break;
        }
    }

}
