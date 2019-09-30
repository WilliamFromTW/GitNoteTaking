package inmethod.gitnotetaking;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import inmethod.gitnotetaking.db.RemoteGit;
import inmethod.gitnotetaking.db.RemoteGitDAO;
import inmethod.gitnotetaking.utility.MyGitUtility;

public class ModifyRemoteGitActivity extends AppCompatActivity {

    private String sRemoteURL = null;
    private EditText editUserAccount = null;
    private EditText editUserPassword = null;
    private EditText editNickName = null;
    private EditText editAuthorName = null;
    private EditText editAuthorEmail = null;
    private EditText editRemoteBranch = null;
    private Activity activity;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_modify_remote);
        activity = this;
        Intent myIntent = getIntent();
        sRemoteURL =   myIntent.getStringExtra("GIT_REMOTE_URL");
        editUserAccount = (EditText) findViewById(R.id.editUserAccount);
        editUserPassword = (EditText) findViewById(R.id.editUserPassword);
        editNickName = (EditText) findViewById(R.id.editLocalGitName);
        editAuthorName = (EditText)findViewById(R.id.editAuthorName);
        editAuthorEmail = (EditText)findViewById(R.id.editAuthorEmail);
        editRemoteBranch = (EditText)findViewById(R.id.editRemoteBranch);
        ImageButton aSearchButton = (ImageButton)findViewById(R.id.searchButton);

        Button buttonOK = (Button) findViewById(R.id.buttonOK);

        buttonOK.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (editUserAccount.getText().toString().equals("") || editUserPassword.getText().toString().equals("") || editNickName.getText().toString().equals("")) {
                    AlertDialog.Builder MyAlertDialog = new AlertDialog.Builder(activity);
                    MyAlertDialog.setTitle(getResources().getString(R.string.main_notes_title_modify));
                    MyAlertDialog.setMessage(getResources().getString(R.string.tv_all_parametes_must_be_set));
                    DialogInterface.OnClickListener OkClick = new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                        }
                    };
                    MyAlertDialog.setNeutralButton("OK", OkClick);
                    MyAlertDialog.show();
                    return;
                }else{
                    RemoteGitDAO aRemoteGitDAO = new RemoteGitDAO(activity);
                    RemoteGit aValue = aRemoteGitDAO.getByURL(sRemoteURL);


                    if (aValue != null) {
                        try {
                            aValue.setUid(editUserAccount.getText().toString());
                            aValue.setPwd(editUserPassword.getText().toString());
                            aValue.setNickname(editNickName.getText().toString());
                            aValue.setAuthor_name(editAuthorName.getText().toString());
                            aValue.setAuthor_email(editAuthorEmail.getText().toString());
                            aValue.setBranch(editRemoteBranch.getText().toString());
                            aValue.setRemoteName(editRemoteBranch.getText().toString());
                            aRemoteGitDAO.update(aValue);
                            aRemoteGitDAO.close();
boolean bCheck = MyGitUtility.checkout(MyApplication.getAppContext(),sRemoteURL);
Log.d("adsf","bCheckout="+bCheck);
                        } catch (Exception ex) {

                        }
                    }else{
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(ModifyRemoteGitActivity.this, activity.getResources().getText(R.string.main_notes_not_found), Toast.LENGTH_SHORT).show();
                            }
                        });
                    }

                    onBackPressed();
                    finish();
                }
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        RemoteGitDAO aRemoteGitDAO = new RemoteGitDAO(activity);
        RemoteGit aValue = aRemoteGitDAO.getByURL(sRemoteURL);


        if (aValue != null) {
            try {
                editUserAccount.setText(aValue.getUid());
                editUserPassword.setText(aValue.getPwd());
                editNickName.setText(aValue.getNickname());
                editAuthorName.setText(aValue.getAuthor_name());
                editAuthorEmail.setText(aValue.getAuthor_email());
                editRemoteBranch.setText(aValue.getRemoteName());
            } catch (Exception ex) {

            }
        }else{
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(ModifyRemoteGitActivity.this, activity.getResources().getText(R.string.main_notes_not_found), Toast.LENGTH_SHORT).show();
                }
            });
        }
        aRemoteGitDAO.close();
    }
}
