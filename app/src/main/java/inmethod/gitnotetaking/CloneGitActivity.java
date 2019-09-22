package inmethod.gitnotetaking;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.preference.PreferenceManager;

import android.app.Activity;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import org.eclipse.jgit.util.FileUtils;

import java.io.File;

import inmethod.gitnotetaking.db.RemoteGit;
import inmethod.gitnotetaking.db.RemoteGitDAO;
import inmethod.gitnotetaking.utility.MyGitUtility;
import inmethod.gitnotetaking.view.GitList;

public class CloneGitActivity extends AppCompatActivity {

    private String sRemoteName = "";
    private EditText editRemoteURL = null;
    private EditText editUserName = null;
    private EditText editUserPassword = null;
    private EditText editNickName = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_clone_git);
        final Activity activity = this;

        sRemoteName = PreferenceManager.getDefaultSharedPreferences(activity).getString("GitRemoteName", "origin");
        editRemoteURL = (EditText) findViewById(R.id.editRemoteURL);
        editUserName = (EditText) findViewById(R.id.editUserName);
        editUserPassword = (EditText) findViewById(R.id.editUserPassword);
        editNickName = (EditText) findViewById(R.id.editLocalGitName);

        Button buttonOK = (Button) findViewById(R.id.buttonOK);

        buttonOK.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (sRemoteName.equals("") || editRemoteURL.getText().toString().equals("") || editUserName.getText().toString().equals("") || editUserPassword.getText().toString().equals("") || editNickName.getText().toString().equals("")) {
                    AlertDialog.Builder MyAlertDialog = new AlertDialog.Builder(activity);
                    MyAlertDialog.setTitle(getResources().getString(R.string.tv_title_remote_git_clone));
                    MyAlertDialog.setMessage(getResources().getString(R.string.tv_all_parametes_must_be_set));
                    DialogInterface.OnClickListener OkClick = new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                        }
                    };
                    MyAlertDialog.setNeutralButton("OK", OkClick);
                    MyAlertDialog.show();
                    return;
                }

                RemoteGitDAO aRemoteGitDAO = new RemoteGitDAO(activity);
                RemoteGit aValue = aRemoteGitDAO.getByURL(editRemoteURL.getText().toString());


                if (aValue == null) {
                    try {
                        if (MyGitUtility.checkLocalGitRepository(activity, editRemoteURL.getText().toString())) {
                            String sLocalDirectory = MyGitUtility.getLocalGitDirectory(activity,editRemoteURL.getText().toString());
                            FileUtils.delete(new File(sLocalDirectory),FileUtils.RECURSIVE);
                        }


                            AlertDialog.Builder builder = new AlertDialog.Builder(activity);
                            builder.setCancelable(false);
                            builder.setView(R.layout.loading_dialog);
                            final AlertDialog dialog = builder.create();
                            dialog.show();
                            aValue = new RemoteGit();
                            aValue.setId(0);
                            aValue.setRemoteName(sRemoteName);
                            aValue.setUrl(editRemoteURL.getText().toString());
                            aValue.setUid(editUserName.getText().toString());
                            aValue.setPwd(editUserPassword.getText().toString());
                            aValue.setNickname(editNickName.getText().toString());
                            aValue.setPush_status(GitList.CLONING);
                            aValue.setAuthor_name(PreferenceManager.getDefaultSharedPreferences(activity).getString("GitAuthorName", "root"));
                            aValue.setAuthor_email(PreferenceManager.getDefaultSharedPreferences(activity).getString("GitAuthorEmail", "root@your.email.com"));
                            aRemoteGitDAO.insert(aValue);
                            aRemoteGitDAO.close();
                            dialog.dismiss();
                            onBackPressed();
                            try {
                                new Thread(new Runnable() {
                                    @Override
                                    public void run() {
                                        RemoteGit aValue = new RemoteGit();
                                        aValue.setId(0);
                                        aValue.setRemoteName(sRemoteName);
                                        aValue.setUrl(editRemoteURL.getText().toString());
                                        aValue.setUid(editUserName.getText().toString());
                                        aValue.setPwd(editUserPassword.getText().toString());
                                        aValue.setNickname(editNickName.getText().toString());
                                        aValue.setAuthor_name(PreferenceManager.getDefaultSharedPreferences(activity).getString("GitAuthorName", "root"));
                                        aValue.setAuthor_email(PreferenceManager.getDefaultSharedPreferences(activity).getString("GitAuthorEmail", "root@your.email.com"));
                                        RemoteGitDAO aRemoteGitDAO = new RemoteGitDAO(MyApplication.getAppContext());
                                        if (MyGitUtility.cloneGit(MyApplication.getAppContext(), editRemoteURL.getText().toString(), editUserName.getText().toString(), editUserPassword.getText().toString())) {
                                            aValue.setPush_status(GitList.PUSH_SUCCESS);
                                            if( aRemoteGitDAO.updateByRemoteUrl(aValue) )
                                                Log.d("CloneGitActivity",aValue.getNickname() +"cloning success");
                                            else
                                                Log.d("CloneGitActivity",aValue.getNickname() +"cloning fail");

                                            aRemoteGitDAO.close();
                                        } else {
                                            aRemoteGitDAO.delete(aValue.getUrl());
                                            aRemoteGitDAO.close();
                                        }

                                    }
                                }).start();

                            } catch (Exception ee) {
                                ee.printStackTrace();
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
