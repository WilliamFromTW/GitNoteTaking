package inmethod.gitnotetaking;

import android.Manifest;
import android.app.Activity;
import android.app.Application;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.PopupMenu;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.preference.PreferenceManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Environment;
import android.os.StrictMode;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

import org.eclipse.jgit.lib.Ref;

import java.io.File;
import java.io.FileDescriptor;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import inmethod.gitnotetaking.db.RemoteGit;
import inmethod.gitnotetaking.db.RemoteGitDAO;
import inmethod.gitnotetaking.utility.MyGitUtility;
import inmethod.gitnotetaking.view.GitList;
import inmethod.gitnotetaking.view.RecyclerAdapterForDevice;
import inmethod.jakarta.vcs.GitUtil;


public class MainActivity extends AppCompatActivity {

    public static final String TAG = "GitNoteTaking";
    private Activity activity = this;
    RecyclerView rv = null;
    RecyclerAdapterForDevice adapter = null;
    private RemoteGitDAO aRemoteGitDAO = null;

    public boolean isInternetPermissionGranted() {
        if (checkSelfPermission(Manifest.permission.INTERNET)
                == PackageManager.PERMISSION_GRANTED) {
            Log.v(TAG, "Permission is granted1");
            return true;
        } else {

            Log.v(TAG, "Permission is revoked1");
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.INTERNET}, 1);
            return false;
        }

    }

    public boolean isReadStoragePermissionGranted() {

        if (checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE)
                == PackageManager.PERMISSION_GRANTED) {
            Log.v(TAG, "Permission is granted1");
            return true;
        } else {

            Log.v(TAG, "Permission is revoked1");
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 3);
            return false;
        }

    }

    public boolean isWriteStoragePermissionGranted() {

        if (checkSelfPermission(android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
                == PackageManager.PERMISSION_GRANTED) {
            Log.v(TAG, "Permission is granted2");
            return true;
        } else {

            Log.v(TAG, "Permission is revoked2");
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 2);
            return false;
        }

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case 2:
                Log.d(TAG, "External storage2");
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Log.v(TAG, "Permission: " + permissions[0] + "was " + grantResults[0]);
                    //resume tasks needing this permission
                } else {
                }
                break;

            case 3:
                Log.d(TAG, "External storage1");
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Log.v(TAG, "Permission: " + permissions[0] + "was " + grantResults[0]);
                    //resume tasks needing this permission
                } else {
                }
                break;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        isInternetPermissionGranted();
        isReadStoragePermissionGranted();
        isWriteStoragePermissionGranted();
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);
        try {
            Method m = StrictMode.class.getMethod("disableDeathOnFileUriExposure");
            m.invoke(null);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    @Override
    public void onStart() {
        super.onStart();
        rv = (RecyclerView) findViewById(R.id.rv);
        adapter = new RecyclerAdapterForDevice(this);
        LinearLayoutManager llm = new LinearLayoutManager(this);
        rv.setLayoutManager(llm);
        rv.setAdapter(adapter);
        adapter.setOnItemClickListener(new RecyclerAdapterForDevice.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                // Toast.makeText(activity, "click position=" + position, Toast.LENGTH_LONG).show();
                Intent Intent = new Intent(MainActivity.this, FileExplorerActivity.class);
                Object[] aTextView = GitList.getDeviceInfoFromLayoutId(view);
                String sGitName = ((TextView) aTextView[0]).getText().toString();
                String sRemoteUrl = ((TextView) aTextView[1]).getText().toString();
                Intent.putExtra("GIT_ROOT_DIR", MyGitUtility.getLocalGitDirectory(activity, sRemoteUrl));
                Intent.putExtra("GIT_NAME", sGitName);
                Intent.putExtra("GIT_REMOTE_URL", sRemoteUrl);
                startActivity(Intent);
            }
        });

        adapter.setOnItemLongClickListener(new RecyclerAdapterForDevice.OnItemLongClickListener() {
            @Override
            public void onItemLongClick(View view, int position) {
                final Object[] aTextView = GitList.getDeviceInfoFromLayoutId(view);
                final String sRemoteUrl = ((TextView) aTextView[1]).getText().toString();

                PopupMenu popup = new PopupMenu(MainActivity.this, view);

                popup.getMenuInflater()
                        .inflate(R.menu.lognclick_popup_menu, popup.getMenu());
                if (sRemoteUrl.indexOf("local") != -1) {
                    for (int i = 0; i < popup.getMenu().size(); i++) {
                        if (popup.getMenu().getItem(i).getItemId() == R.id.Push) {
                            popup.getMenu().getItem(i).setVisible(false);
                        }
                    }
                }
                //registering popup with OnMenuItemClickListener
                popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    public boolean onMenuItemClick(MenuItem item) {
                        int id = item.getItemId();
                        if (id == R.id.Remove) {
                            MyGitUtility.deleteByRemoteUrl(activity, ((TextView) aTextView[1]).getText().toString());
                            Log.d(TAG, "try to delete local git repository");
                            MyGitUtility.deleteLocalGitRepository(activity, sRemoteUrl);
                            adapter.clear();
                            ArrayList<RemoteGit> aList = MyGitUtility.getRemoteGitList(activity);
                            for (final RemoteGit a : aList) {
                                adapter.addData(new GitList(a.getNickname(), a.getUrl(), (int) a.getPush_status()));
                            }
                            return true;
                        } else if (id == R.id.Push) {
                            AlertDialog.Builder builder = new AlertDialog.Builder(activity);
                            builder.setCancelable(false); // if you want user to wait for some process to finish,
                            builder.setView(R.layout.loading_dialog);
                            final AlertDialog dialog = builder.create();
                            dialog.show();
                            new Thread(new Runnable() {
                                @Override
                                public void run() {
                                    if (MyGitUtility.push(MyApplication.getAppContext(), ((TextView) aTextView[1]).getText().toString())) {
                                        ((TextView) aTextView[0]).setTextColor(Color.BLACK);
                                        dialog.dismiss();
                                    } else {
                                        dialog.dismiss();
                                    }

                                }
                            }).start();


                        }
                        return true;
                    }
                });

                popup.show();

            }
        });
        String sGitAuthorName = PreferenceManager.getDefaultSharedPreferences(activity).getString("GitAuthorName", null);
        String sGitAuthorEmail = PreferenceManager.getDefaultSharedPreferences(activity).getString("GitAuthorEmail", null);

        if (sGitAuthorName == null || sGitAuthorName.equals("") || sGitAuthorEmail == null || sGitAuthorEmail.equals("")) {
            final AlertDialog.Builder MyAlertDialog = new AlertDialog.Builder(activity);
            MyAlertDialog.setTitle(getResources().getString(R.string.tv_title_first_time));
            MyAlertDialog.setMessage(getResources().getString(R.string.tv_first_time));
            DialogInterface.OnClickListener OkClick = new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    Intent intent = new Intent(MainActivity.this, PreferencesSettings.class);
                    startActivity(intent);
                    dialog.dismiss();
                }
            };
            MyAlertDialog.setNeutralButton("OK", OkClick);
            MyAlertDialog.show();

        }

    }

    @Override
    public void onResume() {
        super.onResume();
        adapter.clear();
        ArrayList<RemoteGit> aList = MyGitUtility.getRemoteGitList(MyApplication.getAppContext());
        boolean bCloning = false;
        for (final RemoteGit a : aList) {
            adapter.addData(new GitList(a.getNickname(), a.getUrl(), (int) a.getPush_status()));
            if (a.getUrl().indexOf("local") == -1 && a.getPush_status() == GitList.PUSH_SUCCESS) {
                Log.d(TAG, "try to pull from remote git to local repository");
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        MyGitUtility.pull(MyApplication.getAppContext(), a.getUrl());
                    }
                }).start();
            }
            if (a.getPush_status() == GitList.CLONING)
                bCloning = true;

        }
        if (bCloning) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    boolean bCloning = true;
                    // 3 min
                    for (int i = 0; i < 60; i++) {
                        try {
                            Thread.sleep(5000);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        if (!bCloning) {
                            finish();
                            startActivity(getIntent());
                            break;
                        }
                        ArrayList<RemoteGit> aList = MyGitUtility.getRemoteGitList(MyApplication.getAppContext());
                        bCloning = false;
                        for (final RemoteGit a : aList) {
                            if (a.getPush_status() == GitList.CLONING) bCloning = true;
                        }
                    }
                    if (bCloning) {
                        ArrayList<RemoteGit> aList = MyGitUtility.getRemoteGitList(MyApplication.getAppContext());
                        for (final RemoteGit a : aList) {
                            if (a.getPush_status() == GitList.CLONING)
                                MyGitUtility.deleteByRemoteUrl(MyApplication.getAppContext(), a.getUrl());
                        }
                    }
                }
            }).start();
        }

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_settings) {
            Intent intent = new Intent(MainActivity.this, PreferencesSettings.class);
            startActivity(intent);
            return true;
        } else if (id == R.id.action_clone_git_remote) {
            Intent intent = new Intent(MainActivity.this, CloneGitActivity.class);
            startActivity(intent);
        } else if (id == R.id.action_create_local_git) {
            Intent intent = new Intent(MainActivity.this, CreateLocalGitActivity.class);
            startActivity(intent);
        }
        return super.onOptionsItemSelected(item);
    }

}
