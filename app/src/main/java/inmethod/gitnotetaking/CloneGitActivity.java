package inmethod.gitnotetaking;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import inmethod.gitnotetaking.db.RemoteGit;
import inmethod.gitnotetaking.db.RemoteGitDAO;
import inmethod.gitnotetaking.utility.MyGitUtility;

public class CloneGitActivity extends AppCompatActivity {

    private EditText editRemoteURL = null;
    private EditText editUserName = null;
    private EditText editUserPassword = null;
    private EditText editNickName = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_clone_git);
        final Activity activity = this;

        editRemoteURL = (EditText) findViewById(R.id.editRemoteURL);
        editUserName = (EditText) findViewById(R.id.editUserName);
        editUserPassword = (EditText) findViewById(R.id.editUserPassword);
        editNickName = (EditText) findViewById(R.id.editNickName);

        Button buttonOK = (Button) findViewById(R.id.buttonOK);

        buttonOK.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (editRemoteURL.getText().toString().equals("") || editUserName.getText().toString().equals("") || editUserPassword.getText().toString().equals("") || editNickName.getText().toString().equals("")) {
                    AlertDialog.Builder MyAlertDialog = new AlertDialog.Builder(activity);
                    MyAlertDialog.setTitle("Remote Git Clong");
                    MyAlertDialog.setMessage("All Parametes must be set");
                    DialogInterface.OnClickListener OkClick = new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                        }
                    };
                    MyAlertDialog.setNeutralButton("OK", OkClick);
                    MyAlertDialog.show();
                    return;
                }

                final RemoteGitDAO aRemoteGitDAO = new RemoteGitDAO(activity);
                RemoteGit aValue = aRemoteGitDAO.getByURL(editRemoteURL.getText().toString());


                if (aValue == null) {
                    try {
                        if (MyGitUtility.checkLocalGitRepository(editRemoteURL.getText().toString())) {
                            AlertDialog.Builder MyAlertDialog = new AlertDialog.Builder(activity);
                            MyAlertDialog.setTitle("Remote Git Clong");
                            MyAlertDialog.setMessage("Local Git Repository already exists");
                            DialogInterface.OnClickListener OkClick = new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                }
                            };
                            MyAlertDialog.setNeutralButton("OK", OkClick);
                            MyAlertDialog.show();
                            return;
                        } else {

                            AlertDialog.Builder builder = new AlertDialog.Builder(activity);
                            builder.setCancelable(false); // if you want user to wait for some process to finish,
                            builder.setView(R.layout.loading_dialog);
                            final AlertDialog dialog = builder.create();
                            dialog.show();
                            try{
                                new Thread(new Runnable(){
                                    @Override
                                    public void run() {
                                        if (MyGitUtility.cloneGit(editRemoteURL.getText().toString(), editUserName.getText().toString(), editUserPassword.getText().toString())) {
                                            RemoteGit aValue = new RemoteGit();
                                            aValue.setId(0);
                                            aValue.setUrl(editRemoteURL.getText().toString());
                                            aValue.setUid(editUserName.getText().toString());
                                            aValue.setPwd(editUserPassword.getText().toString());
                                            aValue.setNickname(editNickName.getText().toString());
                                            aRemoteGitDAO.insert(aValue);
                                            dialog.dismiss();
                                            Looper.prepare();
                                            final AlertDialog.Builder MyAlertDialog = new AlertDialog.Builder(activity);
                                            MyAlertDialog.setTitle("Remote Git ");
                                            MyAlertDialog.setMessage("Clone Success!");
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
                                            dialog.dismiss();
                                            Looper.prepare();
                                            final AlertDialog.Builder MyAlertDialog = new AlertDialog.Builder(activity);
                                            MyAlertDialog.setTitle("Remote Git ");
                                            MyAlertDialog.setMessage("Clone Failed!");
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
                    MyAlertDialog.setTitle("Remote Git ");
                    MyAlertDialog.setMessage("Git already clone");
                    DialogInterface.OnClickListener OkClick = new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                        }
                    };
                    MyAlertDialog.setNeutralButton("OK", OkClick);
                    MyAlertDialog.show();
                }
                Log.d("asdf", "count=" + aRemoteGitDAO.getCount());
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();

    }
}
