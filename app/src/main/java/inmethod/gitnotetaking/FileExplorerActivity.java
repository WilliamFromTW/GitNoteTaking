package inmethod.gitnotetaking;


import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Looper;
import android.text.InputType;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.FileProvider;

import org.eclipse.jgit.util.FileUtils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import inmethod.gitnotetaking.utility.FileUtility;
import inmethod.gitnotetaking.utility.MyGitUtility;
import inmethod.gitnotetaking.view.FileExplorerListAdapter;
import inmethod.gitnotetaking.view.FileExplorerViewHolder;


public class FileExplorerActivity extends AppCompatActivity {

    public static final String TAG =MainActivity.TAG;

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
    public static int READ_REQUEST_CODE = 2;
    private static boolean bFinishCopy=false;

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
        m_curDir = sGitRootDir;
    }


    @Override
    public void onStart() {
        super.onStart();
        view = (ListView) findViewById(R.id.rl_lvListRoot);
        getDirFromRoot(m_curDir);
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    boolean pasteFile() {
        try {
            if (MyApplication.getCutCopyStatus() == MyApplication.CUT) {
                List<File> aCutFileList = MyApplication.getStoreFiles();
                for (File aFile : aCutFileList) {
                    if (aFile.isDirectory()) {
                        Files.move(aFile.toPath(), new File(m_curDir + File.separator + aFile.getName()).toPath());
                    } else {
                        Files.move(aFile.toPath(), new File(m_curDir + File.separator + aFile.getName()).toPath());
                    }
                }
            } else if (MyApplication.getCutCopyStatus() == MyApplication.COPY) {
                List<File> aCutFileList = MyApplication.getStoreFiles();
                for (File aFile : aCutFileList) {
                    if (aFile.isDirectory()) {
                        Log.d(TAG, "directory name = " + aFile.getName());
                        Files.copy(aFile.toPath(), new File(m_curDir + File.separator + aFile.getName()).toPath());
                    } else {
                        Files.copy(aFile.toPath(), new File(m_curDir + File.separator + aFile.getName()).toPath());
                    }
                }
            }
            return true;
        } catch (Exception ee) {
            ee.printStackTrace();
            return false;
        }
    }

    boolean storeFile() {
        if (adapter.m_selectedItem.size() == 0) {
            Toast.makeText(FileExplorerActivity.this, getResources().getString(R.string.select_file_or_directory), Toast.LENGTH_SHORT).show();
            return false;
        } else {

            MyApplication.resetFiles();
            for (int m_delItem : adapter.m_selectedItem) {
                File m_delFile = new File(m_path.get(m_delItem));

                Log.d(TAG, m_path.get(m_delItem));
                if (m_delFile.isDirectory()) {
                    MyApplication.storeFiles(m_delFile);
                } else {
                    MyApplication.storeFiles(m_delFile);
                }
            }
            return true;
        }
    }

    boolean copyFile() {
        boolean bStatus = storeFile();
        if (bStatus) {
            MyApplication.setCutCopyStatus(MyApplication.COPY);
        } else MyApplication.setCutCopyStatus(MyApplication.NONE);

        return bStatus;
    }

    boolean cutFile() {
        boolean bStatus = storeFile();
        if (bStatus) {
            MyApplication.setCutCopyStatus(MyApplication.CUT);
        } else MyApplication.setCutCopyStatus(MyApplication.NONE);
        return bStatus;
    }

    void deleteFile() {
        if (adapter.m_selectedItem.size() == 0) {
            Toast.makeText(FileExplorerActivity.this, getResources().getString(R.string.select_file_or_directory), Toast.LENGTH_SHORT).show();
        } else {
            String sTemp = "";
            for (int m_delItem : adapter.m_selectedItem) {
                File m_delFile = new File(m_path.get(m_delItem));
                sTemp = m_delFile.getName() + "\n" + sTemp;
                Log.d(TAG, m_path.get(m_delItem));
                if (m_delFile.isDirectory()) {
                    try {
                        FileUtils.delete(m_delFile, FileUtils.RECURSIVE);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                } else {
                    try {
                        File fileAttach = new File(m_delFile.getCanonicalPath() + "_attach");
                        if (fileAttach.exists() && fileAttach.isDirectory()) {
                            FileUtils.delete(fileAttach, FileUtils.RECURSIVE);
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    boolean m_isDelete = m_delFile.delete();
                }
            }
            final String sDeleteFilesName = sTemp;
            getDirFromRoot(m_curDir);
            if (sGitRemoteUrl.indexOf("local") == -1)
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        if (MyGitUtility.commit(MyApplication.getAppContext(), sGitRemoteUrl, "delete files  = \n" + sDeleteFilesName)) {
                            MyGitUtility.push(MyApplication.getAppContext(), sGitRemoteUrl);
                        }
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
                    if (file.getCanonicalPath().indexOf("_attach") != -1)
                        continue;
                    else if (file.getName().substring(0, 1).equals("."))
                        continue;
                    else {
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
                    if (MyApplication.isText(sFileName)) {
                        // Toast.makeText(FileExplorerActivity.this, "File Name = "+m_isFile.getAbsoluteFile()+",uri="+Uri.fromFile(m_isFile), Toast.LENGTH_LONG).show();
                        Intent intent = new Intent(FileExplorerActivity.this, ViewFileActivity.class);
                        intent.putExtra("FILE_PATH", m_isFile.getAbsoluteFile().toString());
                        intent.putExtra("GIT_REMOTE_URL", sGitRemoteUrl);
                        startActivity(intent);
                    } else {
                        Uri path = Uri.fromFile(m_isFile);
                        if (m_isFile.exists()) {
                            Intent intent = new Intent();
                            intent.setAction(android.content.Intent.ACTION_VIEW);
                            Log.d(TAG, "file type = " + getMimeType(Uri.fromFile(m_isFile), activity)+", uri="+path.getPath());
                            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                            intent.setFlags (Intent.FLAG_GRANT_READ_URI_PERMISSION);
                            intent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
                            String authority = activity.getPackageName()+".fileprovider";
                            Uri uri = FileProvider.getUriForFile(activity,authority,m_isFile);
                            intent.setDataAndType(uri, getMimeType(path, activity));

                            try {
                                startActivity(intent);
                            } catch (ActivityNotFoundException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                }
            }
        });
    }

    void addFile() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("*/*");
        startActivityForResult(intent, READ_REQUEST_CODE);
    }

    void createNewFolder(final int p_opt) {
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        final EditText m_edtinput = new EditText(this);
        if (p_opt == 1) {
            builder.setTitle(getResources().getString(R.string.create_folder));
            m_edtinput.setText("NewFolder");
        } else {
            builder.setTitle(getResources().getString(R.string.create_file));
            m_edtinput.setText("NewFile.txt");
        }
        // Set up the input
        // Specify the type of input expected;
        m_edtinput.setInputType(InputType.TYPE_CLASS_TEXT);
        // Set up the buttons
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String m_text = m_edtinput.getText().toString();
                if (p_opt == 1) {
                    File m_newPath = new File(m_curDir, m_text);
                    Log.d(TAG, m_curDir);
                    if (!m_newPath.exists()) {
                        m_newPath.mkdirs();
                    }
                } else {
                    File m_newPath = new File(m_curDir, m_text);
                    if (m_newPath.exists()) {
                        Log.d(TAG, "file exists!");
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(MyApplication.getAppContext(), MyApplication.getAppContext().getResources().getText(R.string.file_exists), Toast.LENGTH_SHORT);
                            }
                        });
                        return;
                    }
                    try {
                        FileOutputStream m_Output = new FileOutputStream((m_curDir + File.separator + m_text), false);
                        m_Output.close();

                    } catch (FileNotFoundException e) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(MyApplication.getAppContext(), MyApplication.getAppContext().getResources().getText(R.string.file_create_failed), Toast.LENGTH_SHORT);
                            }
                        });
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
    public void onActivityResult(int requestCode, int resultCode,
                                 Intent resultData) {
        super.onActivityResult(requestCode, resultCode, resultData);

        if (requestCode == READ_REQUEST_CODE) {
            Uri uri = null;
            if (resultData != null) {
                uri = resultData.getData();
                Log.d(TAG, "uri = " + uri.getPath() + ",host = " + uri.getHost() + ", authority = " + uri.getAuthority() + ", real path = " + FileUtility.getPath(activity, uri));
                final File aSelectedFile = new File(FileUtility.getPath(activity, uri));

                try {

                    final EditText txtUrl = new EditText(this);
                    txtUrl.setText(aSelectedFile.getName());
                    txtUrl.setMaxLines(3);
                    txtUrl.setLines(3);

                    new AlertDialog.Builder(this)
                            .setTitle(getResources().getString(R.string.dialog_title_add))
                            .setMessage(getResources().getString(R.string.dialog_file_name))
                            .setView(txtUrl)
                            .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int whichButton) {
                                    bFinishCopy = false;
                                    new Thread(new Runnable() {
                                        @Override
                                        public void run() {
                                            final File aDestFile;
                                            try {
                                                aDestFile = new File(m_curDir, txtUrl.getText().toString().trim());
                                                Log.d(TAG, "dest file = " + aDestFile.getCanonicalPath());
                                                final String sDestFileNameString;
                                                sDestFileNameString = aDestFile.getCanonicalPath().toString().substring(MyGitUtility.getLocalGitDirectory(activity, sGitRemoteUrl).length());
                                                Files.copy(aSelectedFile.toPath(), aDestFile.toPath());
                                                bFinishCopy = true;

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

                                            }catch(FileAlreadyExistsException ee){

                                            }
                                            catch (IOException e) {
                                                e.printStackTrace();

                                            }
                                            bFinishCopy = true;

                                        }
                                    }).start();
                                    new MyAsyncTask().execute();
                                }
                            }).setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int whichButton) {
                        }
                    }).show();


                } catch (Exception e) {
                    e.printStackTrace();
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(MyApplication.getAppContext(), "Add Failed!", Toast.LENGTH_SHORT).show();
                        }
                    });

                }


            }
        }
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
        } else if (id == R.id.action_add_file) {
            addFile();
        } else if (id == R.id.action_delete) {
            deleteFile();
        } else if (id == R.id.action_create_folder) {
            createNewFolder(1);
        } else if (id == R.id.action_create_file) {
            createNewFolder(0);
        } else if (id == R.id.action_cut) {
            cutFile();
        } else if (id == R.id.action_paste) {
            if (MyApplication.getStoreFiles().size() > 0) {
                if (pasteFile()) {
                    MyApplication.resetFiles();
                    getDirFromRoot(m_curDir);
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            if (MyGitUtility.commit(MyApplication.getAppContext(), sGitRemoteUrl, "paste files")) {
                                if (MyApplication.isLocal(sGitRemoteUrl))
                                    MyGitUtility.push(MyApplication.getAppContext(), sGitRemoteUrl);

                            }

                        }
                    }).start();
                }
            } else {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(MyApplication.getAppContext(), MyApplication.getAppContext().getResources().getText(R.string.no_file_or_dir_selected), Toast.LENGTH_SHORT).show();
                    }
                });
            }
        } else if (id == R.id.action_copy) {
            copyFile();
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

    private class MyAsyncTask extends AsyncTask<Void, Void, Void> {
        @Override
        protected Void doInBackground(Void... params) {
            // 這裡處理背景工作
            while (true) {
                if (bFinishCopy)
                    break;
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            getDirFromRoot(m_curDir);
        }
    }
}
