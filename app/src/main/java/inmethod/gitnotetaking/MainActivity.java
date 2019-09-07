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
import java.util.List;

import inmethod.jakarta.vcs.GitUtil;


public class MainActivity extends AppCompatActivity {
    public static final String TAG = "gitnotetaking";
    public Activity activity = this;

    public boolean isInternetPermissionGranted(){
        if (Build.VERSION.SDK_INT >= 23) {
            if (checkSelfPermission(Manifest.permission.INTERNET)
                    == PackageManager.PERMISSION_GRANTED) {
                Log.v(TAG,"Permission is granted1");
                return true;
            } else {

                Log.v(TAG,"Permission is revoked1");
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.INTERNET}, 1);
                return false;
            }
        }
        else { //permission is automatically granted on sdk<23 upon installation
            Log.v(TAG,"Permission is granted1");
            return true;
        }
    }

    public  boolean isReadStoragePermissionGranted() {
        if (Build.VERSION.SDK_INT >= 23) {
            if (checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE)
                    == PackageManager.PERMISSION_GRANTED) {
                Log.v(TAG,"Permission is granted1");
                return true;
            } else {

                Log.v(TAG,"Permission is revoked1");
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 3);
                return false;
            }
        }
        else { //permission is automatically granted on sdk<23 upon installation
            Log.v(TAG,"Permission is granted1");
            return true;
        }
    }

    public  boolean isWriteStoragePermissionGranted() {
        if (Build.VERSION.SDK_INT >= 23) {
            if (checkSelfPermission(android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    == PackageManager.PERMISSION_GRANTED) {
                Log.v(TAG,"Permission is granted2");
                return true;
            } else {

                Log.v(TAG,"Permission is revoked2");
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 2);
                return false;
            }
        }
        else { //permission is automatically granted on sdk<23 upon installation
            Log.v(TAG,"Permission is granted2");
            return true;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case 2:
                Log.d(TAG, "External storage2");
                if(grantResults[0]== PackageManager.PERMISSION_GRANTED){
                    Log.v(TAG,"Permission: "+permissions[0]+ "was "+grantResults[0]);
                    //resume tasks needing this permission
                }else{
                }
                break;

            case 3:
                Log.d(TAG, "External storage1");
                if(grantResults[0]== PackageManager.PERMISSION_GRANTED){
                    Log.v(TAG,"Permission: "+permissions[0]+ "was "+grantResults[0]);
                    //resume tasks needing this permission
                }else{
                }
                break;
        }
    }
    private void gittest(){
        String sRemoteUrl = "https://gitlab.hlmt.com.tw/920405/testtest.git";
        String sLocalDirectory = Environment.getExternalStorageDirectory() +
                File.separator + "gitnotetaking";
        String sUserName = "920405";
        String sUserPassword = "!lois0023";
        GitUtil aGitUtil;
        try {
            aGitUtil = new GitUtil(sRemoteUrl, sLocalDirectory);

            System.out.println("Remote repository exists ? " + aGitUtil.checkRemoteRepository(sUserName, sUserPassword));
            System.out.println("Local repository exists ? " + aGitUtil.checkLocalRepository());
            if (aGitUtil.checkRemoteRepository(sUserName, sUserPassword) && !aGitUtil.checkLocalRepository()) {
                System.out.println("try to clone remote repository if local repository is not exists \n");
                if (aGitUtil.clone(sUserName, sUserPassword))
                    System.out.println("clone finished!");
                else
                    System.out.println("clone failed!");
            } else if (aGitUtil.checkRemoteRepository(sUserName, sUserPassword) && aGitUtil.checkLocalRepository()) {
                System.out.println("pull branch = " + aGitUtil.getDefaultBranch() + " , status : "
                        + aGitUtil.update(sUserName, sUserPassword));
            }

            System.out.println("Default branch : " + aGitUtil.getDefaultBranch());
            if (aGitUtil.checkLocalRepository()) {
                List<Ref> aAllBranches = aGitUtil.getBranches();
                if (aAllBranches != null) {
                    System.out.println("\nList All Local Branch Name\n--------------------------------");
                    for (Ref aBranch : aAllBranches) {
                        System.out.println("branch : " +  aBranch.getName());
                    }
                    System.out.println("");
                }
                System.out.println("Switch local branch to master: " + aGitUtil.checkout("master"));
                List<Ref> aAllTags = aGitUtil.getLocalTags();
                if (aAllTags != null) {
                    System.out.println("\nList All Local Tags Name\n--------------------------------");
                    for (Ref aTag : aAllTags) {
                        System.out.println("Tag : " + aTag.getName() +"("+aGitUtil.getTagDate(aTag,"yyyy-MM-dd HH:mm:ss")+" created!)" );
                        System.out.println("Commit messages\n==\n" + aGitUtil.getCommitMessageByTagName(aTag) + "\n");
                    }
                    System.out.println("");
                }
            }
        } catch (Exception e) {
            // TODO Auto-generated catch block
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

                final String sGitUrl = PreferenceManager.getDefaultSharedPreferences(activity).getString("GitUrl", null);
                String sUserName = PreferenceManager.getDefaultSharedPreferences(activity).getString("UserName", null);
                String sPassword = PreferenceManager.getDefaultSharedPreferences(activity).getString("Password", null);
                boolean writepermission = isWriteStoragePermissionGranted();
                Log.d(TAG, "writepermission = " + writepermission + ",sGitUrl=" + sGitUrl + ",sUserName=" + sUserName + ",sPassword" + sPassword);
                if (sGitUrl != null && sUserName != null && sPassword != null && writepermission) {
                    try {
                        Log.d(TAG, "boolean success=asdfasdf");
                      //  File folder = new File(Environment.getExternalStorageDirectory() +
                       //         File.separator + "gitnotetaking");
                        boolean success = true;
                        Log.d(TAG, "boolean success=" + success + ",path=" + Environment.getExternalStorageDirectory());
                   //     if (!folder.exists()) {
                       //     success = folder.mkdirs();
              //          }
                    //    if (success) {
                            new Thread(new Runnable(){
                                @Override
                                public void run() {
                                    gittest();
                                    /*
                                    try {
                                        GitUtil aGitUtil = new GitUtil("https://gitlab.hlmt.com.tw/920405/testtest.git", Environment.getExternalStorageDirectory() +
                                                File.separator + "gitnotetaking");
                                        if (aGitUtil.clone("920405", "!lois0023")) {

                                        }
                                    }catch(Exception eeee){

                                    }*/
                                }
                            }).start();


                            /*
                            GitUtil aGitUtil = new GitUtil(sGitUrl, Environment.getExternalStorageDirectory() +
                                    File.separator + "/gitnotetaking");
                            if (aGitUtil.clone("920405", "!lois0023")) {
                                Snackbar.make(view, "Git Clone Success!", Snackbar.LENGTH_LONG)
                                        .setAction("Action", null).show();
                            }
*/
                     //   } else {
                            // Do something else on failure
                    //    }


                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        });





    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            Intent Intent = new Intent(MainActivity.this, PreferencesSettings.class);
            startActivity(Intent);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
