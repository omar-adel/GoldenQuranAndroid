package com.blackstone.goldenquran.ui;

import android.Manifest;
import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.AppBarLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.Toast;

import com.blackstone.goldenquran.Fragments.MainListFragment;
import com.blackstone.goldenquran.Fragments.QuranImageFragment;
import com.blackstone.goldenquran.R;
import com.blackstone.goldenquran.api.DownloadService;
import com.blackstone.goldenquran.utilities.SharedPreferencesManager;
import com.blackstone.goldenquran.utilities.Utils;

import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;

public class MainActivity extends BaseActivity implements DrawerCloser {
    public static final String MESSAGE_PROGRESS = "message_progress";
    private static final int PERMISSION_REQUEST_CODE = 1;
    private boolean isManuallyHideShownActionBar;

    @BindView(R.id.left_drawer)
    FrameLayout mLeftDrawer;
    @BindView(R.id.drawer_layout)
    DrawerLayout mDrawerLayout;
    @BindView(R.id.toolbar)
    Toolbar mToolbar;
//    @BindView(R.id.progress)
//    ProgressBar mProgressBar;
//    @BindView(R.id.progress_text)
//    TextView mProgressText;
    String[] mainlist;
    @BindView(R.id.SurAppBarLayout)
    AppBarLayout mAppBarLayout;
//    @BindView(R.id.btn_download)
//    AppCompatButton btnDownload;
    private ActionBarDrawerToggle mDrawerToggle;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        if (SharedPreferencesManager.getBoolean(this, "isArabic", true)) {
            String languageToLoad = "ar";
            Locale locale = new Locale(languageToLoad);
            Locale.setDefault(locale);
            Configuration config = new Configuration();
            config.locale = locale;
            this.getResources().updateConfiguration(config, this.getResources().getDisplayMetrics());
        } else {

            String languageToLoad = "en";
            Locale locale = new Locale(languageToLoad);
            Locale.setDefault(locale);
            Configuration config = new Configuration();
            config.locale = locale;
            this.getResources().updateConfiguration(config, this.getResources().getDisplayMetrics());
        }

        mainlist = getResources().getStringArray(R.array.MainList);

        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        getSupportFragmentManager().beginTransaction().replace(R.id.container, new QuranImageFragment()).commit();

        setupSupportActionBar(mToolbar, true, true);

        //  hideToolbar();

        final Handler handler = new Handler();

        final Runnable r = new Runnable() {
            public void run() {
                hideActionBar();
                handler.postDelayed(this, 5000);
            }
        };

        handler.postDelayed(r, 5000);

        moveToolbarDown();

        setupDrawer();

        registerReceiver();
    }

    int mToolbarHeight, mAnimDuration = 600/* milliseconds */;

    ValueAnimator mVaActionBar;

    void hideActionBar() {
        // initialize `mToolbarHeight`
        if (mToolbarHeight == 0) {
            mToolbarHeight = mToolbar.getHeight();
        }

        if (mVaActionBar != null && mVaActionBar.isRunning()) {
            // we are already animating a transition - block here
            return;
        }

        // animate `Toolbar's` height to zero.
        mVaActionBar = ValueAnimator.ofInt(mToolbarHeight, 0);
        mVaActionBar.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                // update LayoutParams
                ((AppBarLayout.LayoutParams) mToolbar.getLayoutParams()).height
                        = (Integer) animation.getAnimatedValue();
                mToolbar.requestLayout();
            }
        });

        mVaActionBar.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);

                if (getSupportActionBar() != null) { // sanity check
                    getSupportActionBar().hide();
                }
            }
        });

        mVaActionBar.setDuration(mAnimDuration);
        mVaActionBar.start();
    }

    void showActionBar() {
        if (mVaActionBar != null && mVaActionBar.isRunning()) {
            // we are already animating a transition - block here
            return;
        }

        // restore `Toolbar's` height
        mVaActionBar = ValueAnimator.ofInt(0, mToolbarHeight);
        mVaActionBar.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                // update LayoutParams
                ((AppBarLayout.LayoutParams) mToolbar.getLayoutParams()).height
                        = (Integer) animation.getAnimatedValue();
                mToolbar.requestLayout();
            }
        });

        mVaActionBar.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationStart(Animator animation) {
                super.onAnimationStart(animation);

                if (getSupportActionBar() != null) { // sanity check
                    getSupportActionBar().show();
                }
            }
        });

        mVaActionBar.setDuration(mAnimDuration);
        mVaActionBar.start();
    }


    private void registerReceiver() {

        LocalBroadcastManager bManager = LocalBroadcastManager.getInstance(this);
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(MESSAGE_PROGRESS);
     //   bManager.registerReceiver(broadcastReceiver, intentFilter);
    }


//    private BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
//        @Override
//        public void onReceive(Context context, Intent intent) {
//
//            if (intent.getAction().equals(MESSAGE_PROGRESS)) {
//
//                Download download = intent.getParcelableExtra(getString(R.string.szdDownload));
//                mProgressBar.setProgress(download.getProgress());
//                if (download.getProgress() == 100) {
//
//                    mProgressText.setText(R.string.zsdDonloaded);
//
//                } else {
//
//                    mProgressText.setText(String.format("Download (%d/%d) MB", download.getCurrentFileSize(), download.getTotalFileSize()));
//
//                }
//            }
//        }
//    };


    private void startDownload() {

        Intent intent = new Intent(this, DownloadService.class);
        startService(intent);

    }

    private void setupDrawer() {
        //setup the size of the drawer to 2/3 of the screen size
        int width = Utils.getScreenWidth(this) - (Utils.getScreenWidth(this) / 3);
        DrawerLayout.LayoutParams params = (DrawerLayout.LayoutParams) mLeftDrawer.getLayoutParams();
        params.width = width;
        mLeftDrawer.setLayoutParams(params);

        mDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout,
                mToolbar, R.string.drawer_open, R.string.drawer_close) {
            public void onDrawerClosed(View view) {
                super.onDrawerClosed(view);
                invalidateOptionsMenu();
            }

            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
                invalidateOptionsMenu();
            }
        };

        // Set the drawer toggle as the DrawerListener
        mDrawerToggle.setDrawerIndicatorEnabled(true);
        mDrawerLayout.addDrawerListener(mDrawerToggle);
        mDrawerToggle.syncState();
        //add the left drawer content
        getSupportFragmentManager().beginTransaction().replace(R.id.left_drawer, new MainListFragment()).commit();

    }


    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        super.dispatchTouchEvent(ev);
        if (!getSupportActionBar().isShowing()) {
            showActionBar();
        }

        return true;
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        // Sync the toggle state after onRestoreInstanceState has occurred.
        mDrawerToggle.syncState();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        mDrawerToggle.onConfigurationChanged(newConfig);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Pass the event to ActionBarDrawerToggle, if it returns
        // true, then it has handled the app icon touch event
        if (mDrawerToggle.onOptionsItemSelected(item)) {
            return true;
        }
        // Handle your other action bar items...

        return super.onOptionsItemSelected(item);
    }


    private boolean checkPermission() {
        int result = ContextCompat.checkSelfPermission(this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE);
        if (result == PackageManager.PERMISSION_GRANTED) {

            return true;

        } else {

            return false;
        }
    }

    private void requestPermission() {

        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, PERMISSION_REQUEST_CODE);

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case PERMISSION_REQUEST_CODE:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    startDownload();
                } else {
                    Toast.makeText(MainActivity.this, "Permission Denied, Please allow to proceed !", Toast.LENGTH_SHORT).show();
                }
                break;
        }
    }

//    @OnClick(R.id.btn_download)
//    public void downloadFile() {
//
//        if (checkPermission()) {
//            startDownload();
//        } else {
//            requestPermission();
//        }
//    }

    @Override
    public void close(boolean isDrawerLocked) {
        if (isDrawerLocked) {
            mDrawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);
        } else {
            mDrawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED);
        }
    }

    @Override
    public void title(int pos) {
        mToolbar.setTitle(mainlist[pos + 1]);
        mAppBarLayout.setExpanded(true, true);
    }

    @Override
    public void moveToolbarDown() {
        mAppBarLayout.setExpanded(true, true);
    }

}
