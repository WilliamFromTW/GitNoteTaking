package inmethod.gitnotetaking;

import android.Manifest;
import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Looper;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import inmethod.gitnotetaking.db.RemoteGit;
import inmethod.gitnotetaking.db.RemoteGitDAO;
import inmethod.gitnotetaking.utility.MyGitUtility;
import inmethod.gitnotetaking.view.FileExplorerListAdapter;


public class ViewFileActivity extends AppCompatActivity {

    public static final String TAG = "GitNoteTaking";
    private Activity activity = this;
    ListView view = null;
    FileExplorerListAdapter adapter = null;
    private List<String> m_item;
    private List<String> m_path;
    private List<String> m_files;
    private List<String> m_filesPath;
    private String sFilePath;
    private String sGitRemoteUrl;

    private File file = null;
    public static final int MODE_EDIT = 1;
    public static final int MODE_READ = 0;
    private int iMode = MODE_READ;
    private MenuItem itemEdit;
    private MenuItem itemSave;
    EditText editText;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_file_main);
        Intent myIntent = getIntent(); // gets the previously created intent
        sFilePath = myIntent.getStringExtra("FILE_PATH");
        sGitRemoteUrl = myIntent.getStringExtra("GIT_REMOTE_URL");
        Toolbar toolbar = findViewById(R.id.toolbar3);
        file = new File(sFilePath);
        if (file.exists())
            toolbar.setTitle(file.getName());
        else
            toolbar.setTitle("");
        setSupportActionBar(toolbar);

    }


    @Override
    public void onStart() {
        super.onStart();
        try {
            editText = findViewById(R.id.editFile);
            editText.setEnabled(false);
            if (file.exists()) {
                FileReader fr = new FileReader(file);

                BufferedReader br = new BufferedReader(fr);
                while (br.ready()) {
                    editText.append(br.readLine() + "\n");
                }
                fr.close();
            } else {
                Toast.makeText(activity, "File read error!", Toast.LENGTH_SHORT).show();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    @Override
    public void onResume() {
        super.onResume();


    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // menu.getItem(1).setEnabled(false);
        getMenuInflater().inflate(R.menu.menu_view_file, menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        invalidateOptionsMenu();
        itemEdit = menu.findItem(R.id.view_file_action_edit);
        itemSave = menu.findItem(R.id.view_file_action_save);
        if (iMode == MODE_READ) {
            itemEdit.setVisible(true);
            itemSave.setVisible(false);
            return true;
        } else if (iMode == MODE_EDIT) {
            itemEdit.setVisible(false);
            itemSave.setVisible(true);
            return true;

        } else return super.onPrepareOptionsMenu(menu);

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.view_file_action_edit) {
            iMode = MODE_EDIT;
            editText.setEnabled(true);
            itemSave.setVisible(false);
            itemEdit.setVisible(false);
            return true;
        } else if (id == R.id.view_file_action_save) {
            iMode = MODE_READ;

            editText.setEnabled(false);
            itemSave.setVisible(false);
            itemEdit.setVisible(false);

            FileWriter fw = null;
            final RemoteGitDAO aRemoteGitDAO = new RemoteGitDAO(activity);
            final RemoteGit aRemoteGit = aRemoteGitDAO.getByURL(sGitRemoteUrl);


            if (aRemoteGit != null) {
                final EditText txtUrl = new EditText(this);

                //  txtUrl.setHint("http://www.librarising.com/astrology/celebs/images2/QR/queenelizabethii.jpg");

                new AlertDialog.Builder(this)
                        .setTitle("Commit")
                        .setMessage("Please input your commit message!")
                        .setView(txtUrl)
                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {
                                AlertDialog.Builder builder = new AlertDialog.Builder(activity);
                                builder.setCancelable(false);
                                builder.setView(R.layout.loading_dialog);
                                final AlertDialog dialogs = builder.create();
                                dialogs.show();
                                new Thread(new Runnable() {
                                    @Override
                                    public void run() {
                                        boolean bCommitStatus = MyGitUtility.commit(sGitRemoteUrl, aRemoteGit.getUid(), aRemoteGit.getPwd(), txtUrl.getText().toString());
                                        boolean bPullStatus = false;
                                        String sPullMessage = "";
                                        if (bCommitStatus) {
                                            bPullStatus = MyGitUtility.push(aRemoteGit.getRemoteName(), sGitRemoteUrl, aRemoteGit.getUid(), aRemoteGit.getPwd());
                                            if (bPullStatus) {
                                                sPullMessage = "\nPush success!";
                                                aRemoteGit.setPush_status(0);
                                                aRemoteGitDAO.update(aRemoteGit);
                                            }
                                            else {
                                                sPullMessage = "\nPush failed try later";
                                                aRemoteGit.setPush_status(-1);
                                                aRemoteGitDAO.update(aRemoteGit);
                                            }
                                            dialogs.dismiss();
                                            Looper.prepare();
                                            final AlertDialog.Builder MyAlertDialog = new AlertDialog.Builder(activity);
                                            MyAlertDialog.setTitle("Git Repository");
                                            MyAlertDialog.setMessage("Commit Success!" + sPullMessage);
                                            DialogInterface.OnClickListener OkClick = new DialogInterface.OnClickListener() {
                                                public void onClick(DialogInterface dialog, int which) {
                                                    aRemoteGitDAO.close();
                                                }
                                            };
                                            MyAlertDialog.setNeutralButton("OK", OkClick);
                                            MyAlertDialog.show();
                                            Looper.loop();
                                        } else {
                                            Looper.prepare();
                                            dialogs.dismiss();
                                            final AlertDialog.Builder MyAlertDialog = new AlertDialog.Builder(activity);
                                            MyAlertDialog.setTitle("Local Git Repository");
                                            MyAlertDialog.setMessage("Commit Fail!");
                                            DialogInterface.OnClickListener OkClick = new DialogInterface.OnClickListener() {
                                                public void onClick(DialogInterface dialog, int which) {
                                                    aRemoteGitDAO.close();
                                                }
                                            };
                                            MyAlertDialog.setNeutralButton("OK", OkClick);
                                            MyAlertDialog.show();
                                            Looper.loop();
                                        }

                                    }
                                }).start();

                            }
                        }).show();
            } else {
                Toast.makeText(activity, "No Git Info in DB", Toast.LENGTH_SHORT).show();
            }
            try {
                fw = new FileWriter(new File(sFilePath));
                BufferedWriter bw = new BufferedWriter(fw);
                fw.write(editText.getText().toString());
                fw.close();
            } catch (IOException e) {
                e.printStackTrace();
            }


            return true;
        }
        return super.onOptionsItemSelected(item);
    }

}
