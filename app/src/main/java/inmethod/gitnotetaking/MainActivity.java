package inmethod.gitnotetaking;

import android.Manifest;
import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.icu.text.SimpleDateFormat;
import android.os.Bundle;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.PopupMenu;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.preference.PreferenceManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.StrictMode;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;

import org.eclipse.jgit.revwalk.RevCommit;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import inmethod.gitnotetaking.db.RemoteGit;
import inmethod.gitnotetaking.db.RemoteGitDAO;
import inmethod.gitnotetaking.utility.MyGitUtility;
import inmethod.gitnotetaking.view.GitList;
import inmethod.gitnotetaking.view.RecyclerAdapterForDevice;


public class MainActivity extends AppCompatActivity {

    public static final String TAG = "GitNoteTaking";
    private Activity activity = this;
    RecyclerView rv = null;
    RecyclerAdapterForDevice adapter = null;

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

    /**
     * No need to use any more after  android 4.1
     *
     * @return
     */
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
                if (grantResults.length > 0)
                    if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                        Log.v(TAG, "Permission: " + permissions[0] + "was " + grantResults[0]);
                        //resume tasks needing this permission
                    } else {
                    }
                break;

            case 3:
                Log.d(TAG, "External storage1");
                if (grantResults.length > 0)
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
        //isReadStoragePermissionGranted();
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
                RemoteGit aRemoteGit = MyGitUtility.getRemoteGit(activity, sRemoteUrl);
                if (aRemoteGit.getStatus() == MyGitUtility.GIT_STATUS_PULLING) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(activity, getString(R.string.toast_pulling) , Toast.LENGTH_SHORT).show();
                        }
                    });
                } else if (aRemoteGit.getStatus() == MyGitUtility.GIT_STATUS_CLONING) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(activity, getString(R.string.toast_cloning), Toast.LENGTH_SHORT).show();
                        }
                    });
                } else {
                    Intent.putExtra("GIT_ROOT_DIR", MyGitUtility.getLocalGitDirectory(activity, sRemoteUrl));
                    Intent.putExtra("GIT_NAME", sGitName);
                    Intent.putExtra("GIT_REMOTE_URL", sRemoteUrl);
                    startActivity(Intent);

                }
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
                        if (popup.getMenu().getItem(i).getItemId() == R.id.show_all_remote_branches) {
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
                                adapter.addData(new GitList(a.getNickname(), a.getUrl(), (int) a.getStatus(), a.getBranch()));
                            }
                            return true;
                        } else if (id == R.id.Push) {
                            if (!MyApplication.isNetworkConnected()) {
                                Log.d(TAG, "no netework ");

                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        Toast.makeText(activity, "No Network", Toast.LENGTH_SHORT).show();
                                    }
                                });
                            } else {
                                AlertDialog.Builder builder = new AlertDialog.Builder(activity);
                                builder.setCancelable(false);
                                builder.setView(R.layout.loading_dialog);
                                final AlertDialog dialog = builder.create();
                                dialog.show();
                                new Thread(new Runnable() {
                                    @Override
                                    public void run() {
                                        if (MyGitUtility.push(MyApplication.getAppContext(), ((TextView) aTextView[1]).getText().toString())) {
                                            ((TextView) aTextView[0]).setTextColor(Color.BLACK);
                                        }
                                        dialog.dismiss();
                                    }
                                }).start();
                            }

                        } else if (id == R.id.Pull) {
                            if (!MyApplication.isNetworkConnected()) {
                                Log.d(TAG, "no netework ");

                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        Toast.makeText(activity, "No Network", Toast.LENGTH_SHORT).show();
                                    }
                                });
                            } else {
                                AlertDialog.Builder builder = new AlertDialog.Builder(activity);
                                builder.setCancelable(false);
                                builder.setView(R.layout.loading_dialog);
                                final AlertDialog dialog = builder.create();
                                dialog.show();
                                new Thread(new Runnable() {
                                    @Override
                                    public void run() {
                                        if (MyGitUtility.pull(MyApplication.getAppContext(), ((TextView) aTextView[1]).getText().toString())) {

                                        }
                                        dialog.dismiss();
                                    }
                                }).start();
                            }

                        } else if (id == R.id.Modify) {
                            Intent intent = null;
                            if (sRemoteUrl.indexOf("local") == -1)
                                intent = new Intent(MainActivity.this, ModifyRemoteGitActivity.class);
                            else
                                intent = new Intent(MainActivity.this, ModifyLocalGitActivity.class);
                            String sRemoteUrl = ((TextView) aTextView[1]).getText().toString();
                            intent.putExtra("GIT_REMOTE_URL", sRemoteUrl);
                            startActivity(intent);
                        } else if (id == R.id.show_commit_short_log) {
                            String sNoteName = ((TextView) aTextView[0]).getText().toString();
                            String sRemoteUrl = ((TextView) aTextView[1]).getText().toString();
                            AlertDialog.Builder dialogbuilder = new AlertDialog.Builder(activity, android.R.style.Theme_Material_Light_Dialog_NoActionBar_MinWidth);
                            TextView txtUrl = new TextView(activity);
                            String sListMessages = "";
                            txtUrl.setMaxLines(10);
                            txtUrl.setMovementMethod(new ScrollingMovementMethod());
                            //    txtUrl.setCompoundDrawablesWithIntrinsicBounds(R.drawable.list24, 0, 0, 0);
                            int i = 0;


                            FrameLayout container = new FrameLayout(activity);
                            FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                            params.leftMargin = 20;
                            params.rightMargin = 20;
                            txtUrl.setLayoutParams(params);
                            container.addView(txtUrl);


                            for (RevCommit aRev : MyGitUtility.getLocalCommiLogtList(activity, sRemoteUrl)) {
                                i++;

                                sListMessages = sListMessages + "\n" + getDate(aRev.getCommitTime()) + "\n--\n" + aRev.getFullMessage() + "\n";
                                if (i == 50) break;

                            }
                            txtUrl.setText(sListMessages);
                            dialogbuilder.setView(container).setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int whichButton) {
                                    new Thread(new Runnable() {
                                        @Override
                                        public void run() {

                                        }
                                    }).start();
                                }
                            });
                            dialogbuilder.create().show();
                        } else if (id == R.id.show_all_remote_branches) {
                            String sNoteName = ((TextView) aTextView[0]).getText().toString();
                            String sRemoteUrl = ((TextView) aTextView[1]).getText().toString();
                            AlertDialog.Builder dialogbuilder = new AlertDialog.Builder(activity, android.R.style.Theme_Material_Light_Dialog_NoActionBar_MinWidth);
                            TextView txtUrl = new TextView(activity);
                            String sListMessages = "";
                            txtUrl.setMaxLines(10);
                            txtUrl.setMovementMethod(new ScrollingMovementMethod());
                            //    txtUrl.setCompoundDrawablesWithIntrinsicBounds(R.drawable.list24, 0, 0, 0);
                            int i = 0;


                            FrameLayout container = new FrameLayout(activity);
                            FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                            params.leftMargin = 20;
                            params.rightMargin = 20;
                            txtUrl.setLayoutParams(params);
                            container.addView(txtUrl);

                            List<String> aBranchesList = MyGitUtility.fetchGitBranches(activity, sRemoteUrl);
                            for (String sBranch : aBranchesList) {
                                i++;

                                sListMessages = sListMessages + "\n" + sBranch;
                                if (i == 50) break;

                            }
                            txtUrl.setText(sListMessages);
                            dialogbuilder.setView(container).setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int whichButton) {
                                    new Thread(new Runnable() {
                                        @Override
                                        public void run() {

                                        }
                                    }).start();
                                }
                            });
                            dialogbuilder.create().show();
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
        MyApplication.resetFiles();
        ArrayList<RemoteGit> aList = MyGitUtility.getRemoteGitList(MyApplication.getAppContext());
        boolean bCloning = false;
        for (final RemoteGit a : aList) {
            adapter.addData(new GitList(a.getNickname(), a.getUrl(), (int) a.getStatus(), a.getBranch()));
            if (a.getUrl().indexOf("local") == -1 && a.getStatus() != MyGitUtility.GIT_STATUS_CLONING) {
                Log.d(TAG, "try to pull from remote git to local repository");
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        String sBranchName = MyGitUtility.getLocalBranchName(MyApplication.getAppContext(), a.getUrl());
                        Log.d(TAG, "remote url = " + a.getUrl() +",local branch name = " + sBranchName + ", setting's branch name = " + a.getRemoteName());
                        if (sBranchName != null && sBranchName.equalsIgnoreCase(a.getRemoteName()))
                            if (MyApplication.isNetworkConnected())
                                MyGitUtility.pull(MyApplication.getAppContext(), a.getUrl());
                    }
                }).start();
            }
            if (a.getStatus() == MyGitUtility.GIT_STATUS_CLONING)
                bCloning = true;

        }
        if (bCloning) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    boolean bCloning = true;
                    // 3 min
                    for (int i = 0; i < 1000; i++) {
                        try {
                            Thread.sleep(3000);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        if (!bCloning) {
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    adapter.clear();
                                    ArrayList<RemoteGit> aList = MyGitUtility.getRemoteGitList(activity);
                                    for (final RemoteGit a : aList) {
                                        adapter.addData(new GitList(a.getNickname(), a.getUrl(), (int) a.getStatus(), a.getBranch()));
                                    }
                                }
                            });
                            break;
                        }
                        ArrayList<RemoteGit> aList = MyGitUtility.getRemoteGitList(MyApplication.getAppContext());
                        bCloning = false;
                        for (final RemoteGit a : aList) {
                            if (a.getStatus() == MyGitUtility.GIT_STATUS_CLONING) bCloning = true;
                            else if (a.getStatus() == MyGitUtility.GIT_STATUS_FAIL) {
                                RemoteGitDAO aRemoteGitDAO = new RemoteGitDAO(activity);
                                aRemoteGitDAO.delete(a.getUrl());
                                aRemoteGitDAO.close();
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        Toast.makeText(MainActivity.this, a.getNickname() + " " + MyApplication.getAppContext().getResources().getString(R.string.tv_clone_fail), Toast.LENGTH_SHORT).show();
                                    }
                                });
                                break;
                            }
                        }
                    }
                    if (bCloning) {
                        ArrayList<RemoteGit> aList = MyGitUtility.getRemoteGitList(MyApplication.getAppContext());
                        for (final RemoteGit a : aList) {
                            if (a.getStatus() == MyGitUtility.GIT_STATUS_CLONING)
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

    private String getDate(long time) {
        Date date = new Date(time * 1000L); // *1000 is to convert seconds to milliseconds
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd  HH:mm:ss"); // the format of your date
        // sdf.setTimeZone(TimeZone.getTimeZone("GMT+8"));

        return sdf.format(date);
    }
}
