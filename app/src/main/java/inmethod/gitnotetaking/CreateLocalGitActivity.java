package inmethod.gitnotetaking;

import android.app.Activity;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Looper;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.preference.PreferenceManager;

import java.io.File;

import inmethod.gitnotetaking.db.RemoteGit;
import inmethod.gitnotetaking.db.RemoteGitDAO;
import inmethod.gitnotetaking.utility.MyGitUtility;
import inmethod.gitnotetaking.view.GitList;

public class CreateLocalGitActivity extends AppCompatActivity {

    private String sRemoteName = "";
    private String sRemoteURL = null;
    private EditText editLocalGitName = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_local_git);
        final Activity activity = this;

        sRemoteName = PreferenceManager.getDefaultSharedPreferences(activity).getString("GitRemoteName", "origin");
        sRemoteURL = "localhost://local";
        editLocalGitName = (EditText) findViewById(R.id.editLocalGitName);

        Button buttonOK = (Button) findViewById(R.id.buttonOK);

        buttonOK.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (editLocalGitName.getText().toString().equals("")) {
                    AlertDialog.Builder MyAlertDialog = new AlertDialog.Builder(activity);
                    MyAlertDialog.setTitle(getResources().getString(R.string.tv_title_create_local_git));
                    MyAlertDialog.setMessage(getResources().getString (R.string.tv_all_parametes_must_be_set));
                    DialogInterface.OnClickListener OkClick = new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                        }
                    };
                    MyAlertDialog.setNeutralButton("OK", OkClick);
                    MyAlertDialog.show();
                    return;
                }

                final RemoteGitDAO aRemoteGitDAO = new RemoteGitDAO(activity);
                sRemoteURL =  sRemoteURL+ File.separator+ editLocalGitName.getText().toString()+".git";
                RemoteGit aValue = aRemoteGitDAO.getByURL( sRemoteURL);

                if (aValue == null) {
                    try {
                        if (MyGitUtility.checkLocalGitRepository(activity,sRemoteURL)) {
                            aValue = new RemoteGit();
                            aValue.setId(0);
                            aValue.setRemoteName(sRemoteName);
                            aValue.setUrl(sRemoteURL);
                            aValue.setUid("UID");
                            aValue.setPwd("PWD");
                            aValue.setNickname(editLocalGitName.getText().toString());
                            aValue.setStatus(GitList.PUSH_SUCCESS);
                            aValue.setAuthor_name(PreferenceManager.getDefaultSharedPreferences(activity).getString("GitAuthorName", "root"));
                            aValue.setAuthor_email(PreferenceManager.getDefaultSharedPreferences(activity).getString("GitAuthorEmail", "root@your.email.com"));
                            aRemoteGitDAO.insert(aValue);
                            aRemoteGitDAO.close();
                            onBackPressed();
                            return;
                        } else {
                            try{
                                new Thread(new Runnable(){
                                    @Override
                                    public void run() {
                                        if (MyGitUtility.createLocalGitRepository(activity,editLocalGitName.getText().toString() )) {
                                            RemoteGit aValue = new RemoteGit();
                                            aValue.setId(0);
                                            aValue.setRemoteName(sRemoteName);
                                            aValue.setUrl(sRemoteURL);
                                            aValue.setUid("UID");
                                            aValue.setPwd("PWD");
                                            aValue.setNickname(editLocalGitName.getText().toString());
                                            aValue.setStatus(GitList.PUSH_SUCCESS);
                                            aValue.setAuthor_name(PreferenceManager.getDefaultSharedPreferences(activity).getString("GitAuthorName", "root"));
                                            aValue.setAuthor_email(PreferenceManager.getDefaultSharedPreferences(activity).getString("GitAuthorEmail", "root@your.email.com"));
                                            aRemoteGitDAO.insert(aValue);
                                            aRemoteGitDAO.close();
                                            Looper.prepare();
                                            final AlertDialog.Builder MyAlertDialog = new AlertDialog.Builder(activity);
                                            MyAlertDialog.setTitle(getResources().getString(R.string.tv_create_local_git_repository));
                                            MyAlertDialog.setMessage(getResources().getString(R.string.tv_create_local_git_success));
                                            DialogInterface.OnClickListener OkClick = new DialogInterface.OnClickListener() {
                                                public void onClick(DialogInterface dialog, int which) {
                                                    runOnUiThread(new Runnable() {
                                                        @Override
                                                        public void run() {
                                                            onBackPressed();
                                                        }
                                                    });
                                                }
                                            };
                                            MyAlertDialog.setNeutralButton("OK", OkClick);
                                            MyAlertDialog.show();
                                            Looper.loop();
                                        } else {

                                            Looper.prepare();
                                            final AlertDialog.Builder MyAlertDialog = new AlertDialog.Builder(activity);
                                            MyAlertDialog.setTitle(getResources().getString(R.string.tv_create_local_git_repository));
                                            MyAlertDialog.setMessage(getResources().getString(R.string.tv_clone_fail));
                                            DialogInterface.OnClickListener OkClick = new DialogInterface.OnClickListener() {
                                                public void onClick(DialogInterface dialog, int which) {
                                                }
                                            };
                                            MyAlertDialog.setNeutralButton("OK", OkClick);
                                            MyAlertDialog.show();
                                            Looper.loop();
                                        }
                                    }
                                }).start();

                            }catch(Exception ee){
                                ee.printStackTrace();
                            }

                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                } else {
                    AlertDialog.Builder MyAlertDialog = new AlertDialog.Builder(activity);
                    MyAlertDialog.setTitle(getResources().getString(R.string.tv_title_remote_git_clone));
                    MyAlertDialog.setMessage(getResources().getString(R.string.tv_git_already_cloned));
                    DialogInterface.OnClickListener OkClick = new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                        }
                    };
                    MyAlertDialog.setNeutralButton("OK", OkClick);
                    MyAlertDialog.show();
                }
              //  Log.d("asdf", "count=" + aRemoteGitDAO.getCount());
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();

    }
}
