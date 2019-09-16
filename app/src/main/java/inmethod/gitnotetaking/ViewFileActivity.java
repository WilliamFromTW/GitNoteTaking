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
import androidx.preference.PreferenceManager;

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
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

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
        if (id == android.R.id.home) {
            onBackPressed();
            return true;
        } else if (id == R.id.view_file_action_edit) {
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


            final EditText txtUrl = new EditText(this);

            //  txtUrl.setHint("your hint");

            new AlertDialog.Builder(this)
                    .setTitle(getResources().getString(R.string.commit))
                    .setMessage(getResources().getString(R.string.commit_messages))
                    .setView(txtUrl)
                    .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int whichButton) {
                            AlertDialog.Builder builder = new AlertDialog.Builder(activity);
                            builder.setCancelable(false);
                            builder.setView(R.layout.loading_dialog);
                            final AlertDialog dialogs = builder.create();
                            dialogs.show();
                            boolean bCommitStatus = MyGitUtility.commit(activity, sGitRemoteUrl, txtUrl.getText().toString());
                            dialogs.dismiss();
                            if (bCommitStatus) {
                                new Thread(new Runnable() {
                                    @Override
                                    public void run() {

                                        MyGitUtility.push(activity, sGitRemoteUrl);

                                    }
                                }).start();
                            }
                        }
                    }).show();

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
