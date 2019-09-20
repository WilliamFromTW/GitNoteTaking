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
    public static final int MODE_EDIT = 1;
    public static final int MODE_READ = 0;
    private int iMode = MODE_READ;
    private MenuItem itemEdit;
    private MenuItem itemSave;
    EditText editText;
    LinearLayout layoutAttachment;
    public static int READ_REQUEST_CODE = 2;
    private boolean isModify = false;
    //  TextView tvCountFiles;


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
            editText.setText("");
            layoutAttachment = findViewById(R.id.layoutAttachment);
            layoutAttachment.removeAllViews();

            int iTextSize = Integer.parseInt(PreferenceManager.getDefaultSharedPreferences(activity).getString("GitEditTextSize", "16"));
            editText.setTextSize(iTextSize);
            editText.setTextColor(Color.BLACK);
            if (file.exists()) {
                FileReader fr = new FileReader(file);
                BufferedReader br = new BufferedReader(fr);
                while (br.ready()) {
                    editText.append(br.readLine() + "\n");
                }
                fr.close();
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
                                aTV.setCompoundDrawablesWithIntrinsicBounds(R.drawable.image20, 0, 0, 0);
                            else if (getMimeType(filuri, activity).toLowerCase().indexOf("plain") != -1)
                                aTV.setCompoundDrawablesWithIntrinsicBounds(R.drawable.txt20, 0, 0, 0);
                            else if (getMimeType(filuri, activity).toLowerCase().indexOf("excel") != -1)
                                aTV.setCompoundDrawablesWithIntrinsicBounds(R.drawable.xls20, 0, 0, 0);
                            else if (getMimeType(filuri, activity).toLowerCase().indexOf("word") != -1)
                                aTV.setCompoundDrawablesWithIntrinsicBounds(R.drawable.doc20, 0, 0, 0);
                            else if (getMimeType(filuri, activity).toLowerCase().indexOf("pdf") != -1)
                                aTV.setCompoundDrawablesWithIntrinsicBounds(R.drawable.pdf20, 0, 0, 0);
                            else
                                aTV.setCompoundDrawablesWithIntrinsicBounds(R.drawable.unknown20, 0, 0, 0);


                            aTV.setTextSize(16);
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
                                    new AlertDialog.Builder(activity)
                                            .setTitle(getResources().getString(R.string.view_title_remove_attach))
                                            .setMessage(file.getName())
                                            .setCancelable(true)
                                            .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                                public void onClick(DialogInterface dialog, int whichButton) {
                                                    try {
                                                        file.getCanonicalFile().delete();

                                                        new Thread(new Runnable() {
                                                            @Override
                                                            public void run() {
                                                                if (MyGitUtility.commit(activity, sGitRemoteUrl, "delte attachment file"))
                                                                    MyGitUtility.push(activity, sGitRemoteUrl);
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
    public void onBackPressed() {
        if (isModify) {
            editText.setEnabled(false);
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
                            isModify = false;
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
        } else
            super.onBackPressed();

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
            editText.requestFocus();
            isModify = true;
            return true;
        } else if (id == R.id.view_file_action_save) {
            iMode = MODE_READ;
            isModify = false;
            editText.setEnabled(false);
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
                            if (sGitRemoteUrl.indexOf("local") == -1) {
                                if (bCommitStatus) {
                                    new Thread(new Runnable() {
                                        @Override
                                        public void run() {

                                            MyGitUtility.push(activity, sGitRemoteUrl);

                                        }
                                    }).start();
                                }
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
        } else if (id == R.id.view_file_action_select_file) {

            Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
            intent.addCategory(Intent.CATEGORY_OPENABLE);
            //   intent.setType("image/*");
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
        // The ACTION_OPEN_DOCUMENT intent was sent with the request code
        // READ_REQUEST_CODE. If the request code seen here doesn't match, it's the
        // response to some other intent, and the code below shouldn't run at all.

        if (requestCode == READ_REQUEST_CODE) {
            // The document selected by the user won't be returned in the intent.
            // Instead, a URI to that document will be contained in the return intent
            // provided to this method as a parameter.
            // Pull that URI using resultData.getData().
            Uri uri = null;
            if (resultData != null) {
                uri = resultData.getData();
                File aSelectedFile = new File(FileUtility.getPath(activity, uri));
                try {
                    File aDestFileDirectory = new File(file.getCanonicalPath().toString() + "_attach".trim());
                    //  Log.d(TAG,"aDestFileDirectory file = "+aDestFileDirectory.getCanonicalPath());
                    if (!aDestFileDirectory.isDirectory())
                        aDestFileDirectory.mkdir();
                    File aDestFile = new File(aDestFileDirectory.getCanonicalPath() + File.separator + aSelectedFile.getName().trim());
                    //     Log.d(TAG,"dest file = "+aDestFile.getCanonicalPath());
                    Files.copy(aSelectedFile.toPath(), aDestFile.toPath());
                    MyGitUtility.commit(activity, sGitRemoteUrl, "add attach file = " + aSelectedFile.getName().trim());
                    if (sGitRemoteUrl.indexOf("local") == -1)
                        new Thread(new Runnable() {
                            @Override
                            public void run() {
                                MyGitUtility.push(activity, sGitRemoteUrl);
                            }
                        }).start();
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