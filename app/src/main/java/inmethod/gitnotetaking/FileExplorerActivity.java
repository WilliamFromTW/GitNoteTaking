package inmethod.gitnotetaking;

import android.Manifest;
import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.StrictMode;
import android.text.InputType;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.PopupMenu;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import inmethod.gitnotetaking.db.RemoteGit;
import inmethod.gitnotetaking.db.RemoteGitDAO;
import inmethod.gitnotetaking.utility.MyGitUtility;
import inmethod.gitnotetaking.view.FileExplorerListAdapter;
import inmethod.gitnotetaking.view.GitList;
import inmethod.gitnotetaking.view.RecyclerAdapterForDevice;


public class FileExplorerActivity extends AppCompatActivity {

    public static final String TAG = "GitNoteTaking";
    private Activity activity = this;
    ListView view = null;
    FileExplorerListAdapter adapter = null;
    private List<String> m_item;
    private List<String> m_path;
    private List<String> m_files;
    private List<String> m_filesPath;
    private String sGitRootDir;
    private String sGitName;
    private String sGitRemoteUrl;
    private String m_curDir;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_file_explorer_main);
        Intent myIntent = getIntent(); // gets the previously created intent
        sGitRemoteUrl = myIntent.getStringExtra("GIT_REMOTE_URL");
        sGitRootDir = myIntent.getStringExtra("GIT_ROOT_DIR");
        sGitName = myIntent.getStringExtra("GIT_NAME");
        Toolbar toolbar = findViewById(R.id.toolbar2);
        toolbar.setTitle(sGitName);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

    }


    @Override
    public void onStart() {
        super.onStart();
        view = (ListView) findViewById(R.id.rl_lvListRoot);
        getDirFromRoot(sGitRootDir);
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    void deleteFile() {
        if (adapter.m_selectedItem.size() == 0) {
            Toast.makeText(FileExplorerActivity.this, getResources().getString(R.string.select_file_or_directory), Toast.LENGTH_SHORT).show();
        } else {
            for (int m_delItem : adapter.m_selectedItem) {
                File m_delFile = new File(m_path.get(m_delItem));
                Log.d("file", m_path.get(m_delItem));
                boolean m_isDelete = m_delFile.delete();
            }
            getDirFromRoot(m_curDir);
            if (MyGitUtility.commit(activity, sGitRemoteUrl, "delete files"))
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        MyGitUtility.push(activity, sGitRemoteUrl);
                    }
                }).start();
        }
    }


    public void getDirFromRoot(String p_rootPath) {
        m_item = new ArrayList<String>();
        Boolean m_isRoot = true;
        m_path = new ArrayList<String>();
        m_files = new ArrayList<String>();
        m_filesPath = new ArrayList<String>();
        File m_file = new File(p_rootPath);
        File[] m_filesArray = m_file.listFiles();
        //Log.d(TAG,"rootPath="+p_rootPath+",GitRootDir="+sGitRootDir);
        if (!p_rootPath.equals(sGitRootDir)) {
            m_item.add("../");
            m_path.add(m_file.getParent());
            m_isRoot = false;
        }
        m_curDir = p_rootPath;
        if (m_filesArray == null) return;
        //sorting file list in alphabetical order
        Arrays.sort(m_filesArray);
        for (int i = 0; i < m_filesArray.length; i++) {
            File file = m_filesArray[i];


                if (file.isDirectory()) {
                    try {
                        if( file.getCanonicalPath().indexOf("_attach")!=-1)
                            continue;
                        else if(file.getName().substring(0,1).equals(".") )
                            continue;
                        else{
                            m_item.add(file.getName());
                            m_path.add(file.getPath());
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                        continue;
                    }
                } else {
                    m_files.add(file.getName());
                    m_filesPath.add(file.getPath());
                }
        }
        for (String m_AddFile : m_files) {
            m_item.add(m_AddFile);
        }
        for (String m_AddPath : m_filesPath) {
            m_path.add(m_AddPath);
        }
        adapter = new FileExplorerListAdapter(this, m_item, m_path, m_isRoot);
        view.setAdapter(adapter);
        view.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {
                File m_isFile = new File(m_path.get(position));

                if (m_isFile.isDirectory()) {
                    getDirFromRoot(m_isFile.toString());
                } else {
                    String sFileName = m_isFile.getName().toLowerCase();
                    //Log.d(TAG,"file name = "+ sFileName);
                    if (sFileName.indexOf(".txt") != -1 ||
                            sFileName.indexOf(".xml") != -1 ||
                            sFileName.indexOf(".kt") != -1 ||
                            sFileName.indexOf(".java") != -1 ||
                            sFileName.indexOf(".html") != -1 ||
                            sFileName.indexOf(".py") != -1 ||
                            sFileName.indexOf(".sql") != -1 ||
                            sFileName.indexOf(".md") != -1
                    ) {
                        // Toast.makeText(FileExplorerActivity.this, "File Name = "+m_isFile.getAbsoluteFile()+",uri="+Uri.fromFile(m_isFile), Toast.LENGTH_LONG).show();
                        Intent intent = new Intent(FileExplorerActivity.this, ViewFileActivity.class);
                        intent.putExtra("FILE_PATH", m_isFile.getAbsoluteFile().toString());
                        intent.putExtra("GIT_REMOTE_URL", sGitRemoteUrl);
                        startActivity(intent);
                    } else if (sFileName.indexOf(".png") != -1 ||
                            sFileName.indexOf(".gif") != -1 ||
                            sFileName.indexOf(".jpg") != -1 ||
                            sFileName.indexOf(".jpeg") != -1 ||
                            sFileName.indexOf(".bmp") != -1
                    ) {
                        Uri path = Uri.fromFile(m_isFile);
                        if (m_isFile.exists()) {
                            Intent intent = new Intent();
                            intent.setAction(android.content.Intent.ACTION_VIEW);
                            //Log.d(TAG, "file type = " + getMimeType(Uri.fromFile(m_isFile), activity));
                            intent.setDataAndType(path, getMimeType(path, activity));
                            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                            try {
                                startActivity(intent);
                            } catch (ActivityNotFoundException e) {
                            }
                        }
                    } else {

                    }
                }
            }
        });
    }

    void createNewFolder(final int p_opt) {
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        if (p_opt == 1)
            builder.setTitle(getResources().getString(R.string.create_folder));
        else builder.setTitle(getResources().getString(R.string.create_file));
        // Set up the input
        final EditText m_edtinput = new EditText(this);
        // Specify the type of input expected;
        m_edtinput.setInputType(InputType.TYPE_CLASS_TEXT);

        // Set up the buttons
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String m_text = m_edtinput.getText().toString();
                if (p_opt == 1) {
                    File m_newPath = new File(m_curDir, m_text);
                    Log.d("cur dir", m_curDir);
                    if (!m_newPath.exists()) {
                        m_newPath.mkdirs();
                    }
                } else {
                    try {
                        FileOutputStream m_Output = new FileOutputStream((m_curDir + File.separator + m_text), false);
                        m_Output.close();

                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                getDirFromRoot(m_curDir);

            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        builder.setView(m_edtinput);
        builder.show();
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.file_explorer_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) {
            onBackPressed();
            return true;
        } else if (id == R.id.action_delete) {
            deleteFile();
        } else if (id == R.id.action_create_folder) {
            createNewFolder(1);
        } else if (id == R.id.action_create_file) {
            createNewFolder(0);
        }
        return super.onOptionsItemSelected(item);
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
