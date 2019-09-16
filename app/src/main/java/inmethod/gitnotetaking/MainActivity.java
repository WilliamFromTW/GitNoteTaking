package inmethod.gitnotetaking;

import android.Manifest;
import android.app.Activity;
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
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Environment;
import android.os.StrictMode;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

import org.eclipse.jgit.lib.Ref;

import java.io.File;
import java.io.FileDescriptor;
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
        if (Build.VERSION.SDK_INT >= 23) {
            if (checkSelfPermission(Manifest.permission.INTERNET)
                    == PackageManager.PERMISSION_GRANTED) {
                Log.v(TAG, "Permission is granted1");
                return true;
            } else {

                Log.v(TAG, "Permission is revoked1");
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.INTERNET}, 1);
                return false;
            }
        } else { //permission is automatically granted on sdk<23 upon installation
            Log.v(TAG, "Permission is granted1");
            return true;
        }
    }

    public boolean isReadStoragePermissionGranted() {
        if (Build.VERSION.SDK_INT >= 23) {
            if (checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE)
                    == PackageManager.PERMISSION_GRANTED) {
                Log.v(TAG, "Permission is granted1");
                return true;
            } else {

                Log.v(TAG, "Permission is revoked1");
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 3);
                return false;
            }
        } else { //permission is automatically granted on sdk<23 upon installation
            Log.v(TAG, "Permission is granted1");
            return true;
        }
    }

    public boolean isWriteStoragePermissionGranted() {
        if (Build.VERSION.SDK_INT >= 23) {
            if (checkSelfPermission(android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    == PackageManager.PERMISSION_GRANTED) {
                Log.v(TAG, "Permission is granted2");
                return true;
            } else {

                Log.v(TAG, "Permission is revoked2");
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 2);
                return false;
            }
        } else { //permission is automatically granted on sdk<23 upon installation
            Log.v(TAG, "Permission is granted2");
            return true;
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
                Intent.putExtra("GIT_ROOT_DIR", MyGitUtility.getLocalGitDirectory(activity,sRemoteUrl));
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
                //    Toast.makeText(activity, "Long click position=" + position+",git name="+sRemoteUrl, Toast.LENGTH_LONG).show();
                //Creating the instance of PopupMenu
                PopupMenu popup = new PopupMenu(MainActivity.this, view);
                //Inflating the Popup using xml file
                popup.getMenuInflater()
                        .inflate(R.menu.lognclick_popup_menu, popup.getMenu());
                //registering popup with OnMenuItemClickListener
                popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    public boolean onMenuItemClick(MenuItem item) {
                        int id = item.getItemId();
                        if (id == R.id.Remove) {
                            MyGitUtility.deleteByRemoteUrl(activity,((TextView) aTextView[1]).getText().toString());
                            Log.d(TAG, "try to delete local git repository");
                            MyGitUtility.deleteLocalGitRepository(activity,sRemoteUrl);
                            adapter.clear();
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
                                    if (MyGitUtility.push(activity,((TextView) aTextView[1]).getText().toString())) {
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
    }

    @Override
    public void onResume() {
        super.onResume();
        ArrayList<RemoteGit> aList = MyGitUtility.getRemoteGitList(activity);
        adapter.clear();
        //if (aList.size() > 0)
            //Toast.makeText(activity, "pull from remote to local will run in backupgroud", Toast.LENGTH_LONG).show();
        for (final RemoteGit a : aList) {
            adapter.addData(new GitList(a.getNickname(), a.getUrl(), (int) a.getPush_status()));
            Log.d(TAG, "try to update remote git to local repository");
            new Thread(new Runnable() {
                @Override
                public void run() {
                    MyGitUtility.pull(activity,a.getUrl());
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
            Intent Intent = new Intent(MainActivity.this, PreferencesSettings.class);
            startActivity(Intent);
            return true;
        } else

        if (id == R.id.action_create) {
            Intent Intent = new Intent(MainActivity.this, CloneGitActivity.class);
            startActivity(Intent);

        }

        return super.onOptionsItemSelected(item);
    }


}
