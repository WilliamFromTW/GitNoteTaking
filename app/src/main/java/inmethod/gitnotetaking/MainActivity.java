package inmethod.gitnotetaking;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;

import android.os.Environment;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;

import org.eclipse.jgit.lib.Ref;

import java.io.File;
import java.io.FileDescriptor;
import java.util.List;

import inmethod.jakarta.vcs.GitUtil;


public class MainActivity extends AppCompatActivity {

    public static final String TAG = "GitNoteTaking";
    private Activity activity = this;
    private String sRemoteUrl = null;
    private String sUserName = null;
    private String sUserPassword = null;

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

    private void cloneGit(String sRemoteUrl,String sUserName,String sUserPassword) {

        String sLocalDirectory = Environment.getExternalStorageDirectory() +
                File.separator + "gitnotetaking"+ File.separator+ getLocalGitPath(sRemoteUrl);
        Log.d(TAG,"default local directory = " + sLocalDirectory);
        boolean bIsRemoteRepositoryExist = false;
        GitUtil aGitUtil;
        try {
            aGitUtil = new GitUtil(sRemoteUrl, sLocalDirectory);
            bIsRemoteRepositoryExist = aGitUtil.checkRemoteRepository(sUserName, sUserPassword);
            if( !bIsRemoteRepositoryExist){
                Log.e(TAG,"check remote url failed");
                return ;
            }

            System.out.println("Remote repository exists ? " + bIsRemoteRepositoryExist);
            System.out.println("Local repository exists ? " + aGitUtil.checkLocalRepository());
            if (bIsRemoteRepositoryExist && !aGitUtil.checkLocalRepository()) {
                System.out.println("try to clone remote repository if local repository is not exists \n");
                if (aGitUtil.clone(sUserName, sUserPassword))
                    System.out.println("clone finished!");
                else
                    System.out.println("clone failed!");
            } else if (bIsRemoteRepositoryExist && aGitUtil.checkLocalRepository()) {
                System.out.println("pull branch = " + aGitUtil.getDefaultBranch() + " , status : "
                        + aGitUtil.update(sUserName, sUserPassword));
            }

            System.out.println("Default branch : " + aGitUtil.getDefaultBranch());
            if (aGitUtil.checkLocalRepository()) {
                List<Ref> aAllBranches = aGitUtil.getBranches();
                if (aAllBranches != null) {
                    System.out.println("\nList All Local Branch Name\n--------------------------------");
                    for (Ref aBranch : aAllBranches) {
                        System.out.println("branch : " + aBranch.getName());
                    }
                    System.out.println("");
                }
                System.out.println("Switch local branch to master: " + aGitUtil.checkout("master"));
                List<Ref> aAllTags = aGitUtil.getLocalTags();
                if (aAllTags != null) {
                    System.out.println("\nList All Local Tags Name\n--------------------------------");
                    for (Ref aTag : aAllTags) {
                        System.out.println("Tag : " + aTag.getName() + "(" + aGitUtil.getTagDate(aTag, "yyyy-MM-dd HH:mm:ss") + " created!)");
                        System.out.println("Commit messages\n==\n" + aGitUtil.getCommitMessageByTagName(aTag) + "\n");
                    }
                    System.out.println("");
                }
                aGitUtil.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        isInternetPermissionGranted();
        //   isReadStoragePermissionGranted();
        boolean writepermission = isWriteStoragePermissionGranted();
        FloatingActionButton fab = findViewById(R.id.fab);

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                final String sRemoteUrl = PreferenceManager.getDefaultSharedPreferences(activity).getString("GitUrl", null);
                final String sUserName = PreferenceManager.getDefaultSharedPreferences(activity).getString("UserName", null);
                final String sUserPassword = PreferenceManager.getDefaultSharedPreferences(activity).getString("Password", null);
                boolean writepermission = isWriteStoragePermissionGranted();
                Log.d(TAG, "writepermission = " + writepermission + ",sGitUrl=" + sRemoteUrl + ",sUserName=" + sUserName + ",sPassword" + sUserPassword);
                if (sRemoteUrl != null && sUserName != null && sUserPassword != null && writepermission) {
                    try {

                        new Thread(new Runnable() {
                            @Override
                            public void run() {
                                cloneGit (sRemoteUrl,sUserName,sUserPassword);

                            }
                        }).start();


                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        });


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
        }

        return super.onOptionsItemSelected(item);
    }

    public static String getLocalGitPath(String sRemoteUrl) {
        String sReturn = "";
        int lastPath = sRemoteUrl.lastIndexOf("//");
        if (lastPath!=-1){
            sReturn = sRemoteUrl.substring(lastPath+1);
        }
        sReturn = sReturn.substring(0,sReturn.length()-4);
        return sReturn;
    }
}
