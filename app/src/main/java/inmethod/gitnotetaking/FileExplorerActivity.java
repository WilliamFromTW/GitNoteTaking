package inmethod.gitnotetaking;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.StrictMode;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

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


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_file_explorer_main);
        Intent myIntent = getIntent(); // gets the previously created intent
        sGitRootDir = myIntent.getStringExtra("GIT_ROOT_DIR");
        sGitName = myIntent.getStringExtra("GIT_NAME");
        Toolbar toolbar = findViewById(R.id.toolbar2);
        toolbar.setTitle("GIT NAME:"+sGitName);
        setSupportActionBar(toolbar);

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

    public void getDirFromRoot(String p_rootPath)
    {
        m_item = new ArrayList<String>();
        Boolean m_isRoot=true;
        m_path = new ArrayList<String>();
        m_files=new ArrayList<String>();
        m_filesPath=new ArrayList<String>();
        File m_file = new File(p_rootPath);
        File[] m_filesArray = m_file.listFiles();
        if(!p_rootPath.equals(sGitRootDir))
        {
            m_item.add("../");
            m_path.add(m_file.getParent());
            m_isRoot=false;
        }
        String m_curDir=p_rootPath;
        //sorting file list in alphabetical order
        Arrays.sort(m_filesArray);
        for(int i=0; i < m_filesArray.length; i++)
        {
            File file = m_filesArray[i];
            if(file.isDirectory())
            {
                m_item.add(file.getName());
                m_path.add(file.getPath());
            }
            else
            {
                m_files.add(file.getName());
                m_filesPath.add(file.getPath());
            }
        }
        for(String m_AddFile:m_files)
        {
            m_item.add(m_AddFile);
        }
        for(String m_AddPath:m_filesPath)
        {
            m_path.add(m_AddPath);
        }
        adapter=new FileExplorerListAdapter(this,m_item,m_path,m_isRoot);
        view.setAdapter(adapter);
        view.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {
                File m_isFile=new File(m_path.get(position));
                if(m_isFile.isDirectory())
                {
                    getDirFromRoot(m_isFile.toString());
                }
                else
                {
                   // Toast.makeText(FileExplorerActivity.this, "File Name = "+m_isFile.getAbsoluteFile()+",uri="+Uri.fromFile(m_isFile), Toast.LENGTH_LONG).show();
                    Intent intent = new Intent(FileExplorerActivity.this, ViewFileActivity.class);
                    intent.putExtra("FILE_PATH",m_isFile.getAbsoluteFile().toString());

                    startActivity(intent);
                }
            }
        });
    }

    public static String getMimeType(String url) {
        String type = null;
        String extension = MimeTypeMap.getFileExtensionFromUrl(url);

        if (extension != null) {
            MimeTypeMap mime = MimeTypeMap.getSingleton();
            type = mime.getMimeTypeFromExtension(extension);
        }
        return type;
    }

}
