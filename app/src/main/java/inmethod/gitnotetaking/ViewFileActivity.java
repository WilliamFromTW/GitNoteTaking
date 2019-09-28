package inmethod.gitnotetaking;

import android.Manifest;
import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Looper;
import android.provider.MediaStore;
import android.text.InputType;
import android.text.SpannableString;
import android.text.method.BaseKeyListener;
import android.text.method.KeyListener;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.preference.PreferenceManager;

import org.w3c.dom.Text;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import inmethod.gitnotetaking.db.RemoteGit;
import inmethod.gitnotetaking.db.RemoteGitDAO;
import inmethod.gitnotetaking.utility.FileUtility;
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
    private MenuItem itemEdit;
    private MenuItem itemSave;
    EditText editText;
    LinearLayout layoutAttachment;
    public static int READ_REQUEST_CODE = 2;
    private boolean isModify = false;
    KeyListener listener = null;
    //  TextView tvCountFiles;


    @Override
    public void onSaveInstanceState(Bundle outState) {
    /*Save your data to be restored here
    Example : outState.putLong("time_state", time); , time is a long variable*/
        outState.putBoolean("isModify", isModify);
        outState.putString("editText", editText.getText().toString());
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (savedInstanceState != null) {
       /*When rotation occurs
        Example : time = savedInstanceState.getLong("time_state", 0); */
            isModify = savedInstanceState.getBoolean("isModify");
        } else {
            isModify = false;
            //When onCreate is called for the first time
        }

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
        editText = findViewById(R.id.editFile);
        editText.setText("");
        listener = editText.getKeyListener();

        try {
            layoutAttachment = findViewById(R.id.layoutAttachment);
            layoutAttachment.removeAllViews();

            int iTextSize = Integer.parseInt(PreferenceManager.getDefaultSharedPreferences(activity).getString("GitEditTextSize", "16"));
            editText.setTextSize(iTextSize);
            editText.setTextColor(Color.BLACK);

            if (file.exists()) {
                if (isModify) {
                    editText.setText(savedInstanceState.getString("editText"));
                } else {
                    editText.setText("");
                    FileReader fr = new FileReader(file);
                    BufferedReader br = new BufferedReader(fr);
                    while (br.ready()) {
                        editText.append(br.readLine() + "\n");
                    }
                    fr.close();

                }
                Log.d(TAG, "file = " + file.getCanonicalPath());
                File attachDirectory = new File(file.getAbsolutePath() + "_attach");
                if (attachDirectory.isDirectory()) {
                    for (final File file : attachDirectory.listFiles()) {
                        if (file.isFile()) {
                            final TextView aTV = new TextView(activity);
                            aTV.setText(file.getName());
                            aTV.setTextColor(Color.BLUE);
                            aTV.setBackgroundColor(Color.LTGRAY);

                            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                            lp.setMargins(0, 0, 10, 0);
                            aTV.setLayoutParams(lp);
                            final Uri filuri = Uri.fromFile(file);
                            if (getMimeType(filuri, activity).toLowerCase().indexOf("image") != -1)
                                aTV.setCompoundDrawablesWithIntrinsicBounds(R.drawable.image24, 0, 0, 0);
                            else if (getMimeType(filuri, activity).toLowerCase().indexOf("plain") != -1)
                                aTV.setCompoundDrawablesWithIntrinsicBounds(R.drawable.txt24, 0, 0, 0);
                            else if (getMimeType(filuri, activity).toLowerCase().indexOf("excel") != -1)
                                aTV.setCompoundDrawablesWithIntrinsicBounds(R.drawable.xls24, 0, 0, 0);
                            else if (getMimeType(filuri, activity).toLowerCase().indexOf("word") != -1)
                                aTV.setCompoundDrawablesWithIntrinsicBounds(R.drawable.doc24, 0, 0, 0);
                            else if (getMimeType(filuri, activity).toLowerCase().indexOf("pdf") != -1)
                                aTV.setCompoundDrawablesWithIntrinsicBounds(R.drawable.pdf24, 0, 0, 0);
                            else if (getMimeType(filuri, activity).toLowerCase().indexOf("powerpoint") != -1)
                                aTV.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ppt24, 0, 0, 0);
                            else if (getMimeType(filuri, activity).toLowerCase().indexOf("presentation") != -1)
                                aTV.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ppt24, 0, 0, 0);
                            else
                                aTV.setCompoundDrawablesWithIntrinsicBounds(R.drawable.unknown24, 0, 0, 0);

                            aTV.setTextSize(22);
                            aTV.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {
                                    Intent intent = new Intent();
                                    intent.setAction(android.content.Intent.ACTION_VIEW);
                                    intent.setDataAndType(filuri, getMimeType(filuri, activity));
                                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                    try {
                                        startActivity(intent);
                                    } catch (ActivityNotFoundException e) {
                                    }
                                }
                            });
                            aTV.setOnLongClickListener(new View.OnLongClickListener() {
                                @Override
                                public boolean onLongClick(View view) {


                                    final String sFileName;
                                    try {
                                        sFileName = file.getCanonicalPath().toString().substring(MyGitUtility.getLocalGitDirectory(activity, sGitRemoteUrl).length());
                                    } catch (IOException e) {
                                        e.printStackTrace();
                                        return false;
                                    }
                                    new AlertDialog.Builder(activity)
                                            .setTitle(getResources().getString(R.string.view_title_remove_attach))
                                            .setMessage(sFileName)
                                            .setCancelable(true)
                                            .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                                public void onClick(DialogInterface dialog, int whichButton) {
                                                    try {
                                                        file.getCanonicalFile().delete();

                                                        new Thread(new Runnable() {
                                                            @Override
                                                            public void run() {
                                                                if (MyGitUtility.commit(MyApplication.getAppContext(), sGitRemoteUrl, MyApplication.getAppContext().getString(R.string.view_file_delete_attachment_file_commit) + "\n" + sFileName))
                                                                    MyGitUtility.push(MyApplication.getAppContext(), sGitRemoteUrl);
                                                                else {

                                                                }
                                                            }
                                                        }).start();
                                                        layoutAttachment.removeView(aTV);
                                                    } catch (IOException e) {
                                                        e.printStackTrace();
                                                    }

                                                }
                                            })
                                            .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                                                public void onClick(DialogInterface dialog, int which) {
                                                    // finish();
                                                }
                                            })
                                            .show();

                                    return true;
                                }
                            });
                            layoutAttachment.addView(aTV);
                        }
                    }
                    //           tvCountFiles.setText(countFilesInDirectory(attachDirectory) + getResources().getString(R.string.view_attach_files));
                    //       tvCountFiles.setTextColor(Color.BLUE);
                } else {
                    //        tvCountFiles.setText("");
                }
            } else {
                Toast.makeText(activity, "File read error!", Toast.LENGTH_SHORT).show();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }


    }


    private void disable() {
        editText.setKeyListener(null);
        editText.requestFocus();
        itemEdit.setVisible(true);
        itemSave.setVisible(false);


    }


    private void enable() {
        editText.setKeyListener(listener);
        editText.setFocusableInTouchMode(true);
        editText.setFocusable(true);
        itemEdit.setVisible(false);
        itemSave.setVisible(true);
        SpannableString s = new SpannableString(itemSave.getTitle());
        s.setSpan(new ForegroundColorSpan(Color.RED), 0, s.length(), 0);
        itemSave.setTitle(s);

    }

    @Override
    public void onStart() {
        super.onStart();

    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_view_file, menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        itemEdit = menu.findItem(R.id.view_file_action_edit);
        itemSave = menu.findItem(R.id.view_file_action_save);
        if (isModify)
            enable();
        else
            disable();
        return super.onPrepareOptionsMenu(menu);

    }

    @Override
    public void onBackPressed() {
        if (isModify) {
            FileWriter fw = null;
            final EditText txtUrl = new EditText(this);
            txtUrl.setMaxLines(3);
            txtUrl.setLines(3);
            txtUrl.setText("");
            try {
                fw = new FileWriter(new File(sFilePath));
                BufferedWriter bw = new BufferedWriter(fw);
                fw.write(editText.getText().toString());
                fw.close();
            } catch (IOException e) {
                e.printStackTrace();
            }

            if (PreferenceManager.getDefaultSharedPreferences(activity).getBoolean("GitCheckBoxCommitMessage", false)) {
                new AlertDialog.Builder(this)
                        .setTitle(getResources().getString(R.string.commit))
                        .setMessage(getResources().getString(R.string.commit_messages))
                        .setView(txtUrl)
                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {

                            public void onClick(DialogInterface dialog, int whichButton) {
                                isModify = false;
                                disable();
                                txtUrl.setText(txtUrl.getText() + "\n" + file.getName() + " modified");
                                boolean bCommitStatus = MyGitUtility.commit(MyApplication.getAppContext(), sGitRemoteUrl, txtUrl.getText().toString());

                                new Thread(new Runnable() {
                                    @Override
                                    public void run() {
                                        if (bCommitStatus) {
                                            if (sGitRemoteUrl.indexOf("local") == -1) {
                                                MyGitUtility.push(MyApplication.getAppContext(), sGitRemoteUrl);
                                            }
                                        }
                                    }
                                }).start();
                            }
                        }).show();
            } else {
                Log.d(TAG, "onback");
                txtUrl.setText(txtUrl.getText() + "\n" + file.getName() + " modified");
                boolean bCommitStatus = MyGitUtility.commit(MyApplication.getAppContext(), sGitRemoteUrl, txtUrl.getText().toString());
                isModify = false;
                disable();

                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            Thread.sleep(1000);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        if (bCommitStatus) {
                            if (sGitRemoteUrl.indexOf("local") == -1) {
                                if (bCommitStatus) {
                                    MyGitUtility.push(MyApplication.getAppContext(), sGitRemoteUrl);
                                }
                            }

                        }
                    }
                }).start();
            }
        }
            super.onBackPressed();

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) {
            onBackPressed();
            return true;
        } else if (id == R.id.view_file_action_edit) {
            enable();
            Log.d(TAG, "asdfdddddd");
            isModify = true;
            return true;
        } else if (id == R.id.view_file_action_save) {
            FileWriter fw = null;
            final EditText txtUrl = new EditText(this);
            //  txtUrl.setHint("your hint");
            txtUrl.setMaxLines(3);
            txtUrl.setLines(3);
            try {
                fw = new FileWriter(new File(sFilePath));
                BufferedWriter bw = new BufferedWriter(fw);
                fw.write(editText.getText().toString());
                fw.close();
            } catch (IOException e) {
                e.printStackTrace();
            }

            if (PreferenceManager.getDefaultSharedPreferences(activity).getBoolean("GitCheckBoxCommitMessage", false)) {

                new AlertDialog.Builder(this)
                        .setTitle(getResources().getString(R.string.commit))
                        .setMessage(getResources().getString(R.string.commit_messages))
                        .setView(txtUrl)
                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {

                                txtUrl.setText(txtUrl.getText() + "\n" + file.getName() + " modified");
                                boolean bCommitStatus = MyGitUtility.commit(MyApplication.getAppContext(), sGitRemoteUrl, txtUrl.getText().toString());
                                if (bCommitStatus) {
                                    isModify = false;
                                    disable();
                                    Log.d(TAG, "asdf");
                                }

                                new Thread(new Runnable() {
                                    @Override
                                    public void run() {
                                        if (bCommitStatus) {
                                            if (sGitRemoteUrl.indexOf("local") == -1) {
                                                MyGitUtility.push(MyApplication.getAppContext(), sGitRemoteUrl);
                                            }
                                        }
                                    }
                                }).start();
                            }
                        }).show();
            } else {
                txtUrl.setText(txtUrl.getText() + "\n" + file.getName() + " modified");
                boolean bCommitStatus = MyGitUtility.commit(MyApplication.getAppContext(), sGitRemoteUrl, txtUrl.getText().toString());
                if (bCommitStatus) {
                    isModify = false;
                    disable();
                }
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            Thread.sleep(1000);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        if (bCommitStatus) {
                            if (sGitRemoteUrl.indexOf("local") == -1) {
                                MyGitUtility.push(activity, sGitRemoteUrl);
                            }
                        }
                    }
                }).start();
            }
            return true;
        } else if (id == R.id.view_file_action_select_file) {

            Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
            intent.addCategory(Intent.CATEGORY_OPENABLE);
            intent.setType("*/*");
            startActivityForResult(intent, READ_REQUEST_CODE);
        }
        return super.onOptionsItemSelected(item);
    }

    public static int countFilesInDirectory(File directory) {

        int count = 0;
        for (File file : directory.listFiles()) {
            if (file.isFile()) {
                count++;
            }
            if (file.isDirectory()) {
                count += countFilesInDirectory(file);
            }
        }
        return count;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode,
                                 Intent resultData) {
        super.onActivityResult(requestCode, resultCode, resultData);

        if (requestCode == READ_REQUEST_CODE) {
            Uri uri = null;
            if (resultData != null) {
                uri = resultData.getData();
                final File aSelectedFile = new File(FileUtility.getPath(activity, uri));
                try {
                    final File aDestFileDirectory = new File(file.getCanonicalPath().toString() + "_attach".trim());
                    //  Log.d(TAG,"aDestFileDirectory file = "+aDestFileDirectory.getCanonicalPath());
                    if (!aDestFileDirectory.isDirectory())
                        aDestFileDirectory.mkdir();

                    final EditText txtUrl = new EditText(this);
                    txtUrl.setText(aSelectedFile.getName());
                    txtUrl.setMaxLines(3);
                    txtUrl.setLines(3);
                    new AlertDialog.Builder(this)
                            .setTitle(getResources().getString(R.string.dialog_title_modify))
                            .setMessage(getResources().getString(R.string.dialog_file_name))
                            .setView(txtUrl)
                            .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int whichButton) {
                                    isModify = false;
                                    disable();

                                    new Thread(new Runnable() {
                                        @Override
                                        public void run() {
                                            final File aDestFile;
                                            try {
                                                aDestFile = new File(aDestFileDirectory.getCanonicalPath() + File.separator + txtUrl.getText().toString().trim());
                                                //     Log.d(TAG,"dest file = "+aDestFile.getCanonicalPath());
                                                final String sDestFileNameString;
                                                sDestFileNameString = aDestFile.getCanonicalPath().toString().substring(MyGitUtility.getLocalGitDirectory(activity, sGitRemoteUrl).length());
                                                Files.copy(aSelectedFile.toPath(), aDestFile.toPath());
                                                new Thread(new Runnable() {
                                                    @Override
                                                    public void run() {
                                                        try {
                                                            Thread.sleep(1000);
                                                        } catch (InterruptedException e) {
                                                            e.printStackTrace();
                                                        }
                                                        boolean bCommit = false;
                                                        bCommit = MyGitUtility.commit(MyApplication.getAppContext(), sGitRemoteUrl, MyApplication.getAppContext().getString(R.string.view_file_add_attachment_file_commit) + "\n" + sDestFileNameString);
                                                        if (sGitRemoteUrl.indexOf("local") == -1 && bCommit)
                                                            MyGitUtility.push(MyApplication.getAppContext(), sGitRemoteUrl);

                                                    }
                                                }).start();

                                            } catch (IOException e) {
                                                e.printStackTrace();
                                            }

                                        }
                                    }).start();
                                    finish();
                                    startActivity(getIntent());

                                }
                            }).show();


                } catch (IOException e) {
                    e.printStackTrace();
                }


                try {
                    Log.d(TAG, "selected file name = " + aSelectedFile.getCanonicalPath());

                } catch (IOException e) {
                    e.printStackTrace();
                }


            }
        }
    }

    public String getMimeType(Uri uri, Context context) {
        String mimeType = null;
        if (uri.getScheme().equals(ContentResolver.SCHEME_CONTENT)) {
            ContentResolver cr = context.getContentResolver();
            mimeType = cr.getType(uri);
        } else {
            String fileExtension = MimeTypeMap.getFileExtensionFromUrl(uri
                    .toString());
            mimeType = MimeTypeMap.getSingleton().getMimeTypeFromExtension(
                    fileExtension.toLowerCase());
        }
        return mimeType;
    }
}
