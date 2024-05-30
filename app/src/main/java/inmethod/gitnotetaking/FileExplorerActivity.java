package inmethod.gitnotetaking;


import static android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION;
import static android.content.Intent.FLAG_GRANT_WRITE_URI_PERMISSION;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputType;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.text.style.RelativeSizeSpan;
import android.util.Log;
import android.util.TypedValue;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import android.window.OnBackInvokedDispatcher;

import androidx.activity.OnBackPressedCallback;
import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.FileProvider;
import androidx.preference.PreferenceManager;

import com.hbisoft.pickit.PickiT;
import com.hbisoft.pickit.PickiTCallbacks;

import org.eclipse.jgit.util.FileUtils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Scanner;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import inmethod.gitnotetaking.utility.FileUtility;
import inmethod.gitnotetaking.utility.MyGitUtility;
import inmethod.gitnotetaking.view.FileExplorerListAdapter;


public class FileExplorerActivity extends AppCompatActivity  implements PickiTCallbacks {

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
    private String m_PreviousDir;
    private String m_curDir;
    public static int ADD_REQUEST_CODE = 2;
    public static int SEARCH_TEXT_CODE = 3;
    private static boolean bRefreshDir =false;
    private static String sSearchText = "";
    PickiT pickiT;
    EditText aEditTextSearch;

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
        m_PreviousDir = sGitRootDir;
        m_curDir = sGitRootDir;
        pickiT = new PickiT(this, this, this);
        OnBackPressedCallback onBackPressedCallback = new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                // handle backpressed here

                if( m_item.get(0).equalsIgnoreCase("../"))
                    getDirFromRoot( m_path.get(0));
                else
                    finish();//onBackPressed();
            }
        };
        getOnBackPressedDispatcher().addCallback(this,onBackPressedCallback);
    }


    @Override
    public void onStart() {
        super.onStart();
        view = (ListView) findViewById(R.id.rl_lvListRoot);
      //  TextView textViewSearch= (TextView)findViewById(R.id.editTextSearch);
     //   textViewSearch.setTextSize(TypedValue.COMPLEX_UNIT_PX, Integer.parseInt( PreferenceManager.getDefaultSharedPreferences(MyApplication.getAppContext()).getString("GitEditTextSize" ,"18" )));
        aEditTextSearch = (EditText)findViewById(R.id.editTextSearch);
        aEditTextSearch.setTextSize( Integer.parseInt( PreferenceManager.getDefaultSharedPreferences(MyApplication.getAppContext()).getString("GitEditTextSize" ,"18" )));
        aEditTextSearch.addTextChangedListener(new TextWatcher() {

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                sSearchText = s.toString();
                getDirFromRoot(m_curDir);

            }


            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if (TextUtils.isEmpty(s.toString().trim())) {

                } else {

                }
            }
        });
        getDirFromRoot(m_curDir);
    }

    @Override
    public void onResume() {

        if( sSearchText!=null && !sSearchText.trim().equals(""))
           aEditTextSearch.setText (sSearchText);

        super.onResume();
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
            if (sGitRemoteUrl.indexOf("local") == -1) {
                Log.d(TAG, "commit after file delete");

                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        if (MyGitUtility.commit(MyApplication.getAppContext(), sGitRemoteUrl, MyApplication.getAppContext().getString(R.string.view_file_delete_attachment_file_commit) + sDeleteFilesName)) {
                            MyGitUtility.push(MyApplication.getAppContext(), sGitRemoteUrl);
                        }
                    }
                }).start();
            }
        }
    }

    private static void listAllFiles(Path currentPath, List<Path> allFiles)
            throws IOException
    {
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(currentPath))
        {
            for (Path entry : stream) {
                if (Files.isDirectory(entry)) {
                    listAllFiles(entry, allFiles);
                } else {
                    allFiles.add(entry);
                }
            }
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
        if( sSearchText!=null && !sSearchText.equals("") ) {
//            m_item.add(MyApplication.getAppContext().getString(R.string.search_mode) +":"+ sSearchText);
  //          m_path.add(".");
        }else {
            //Log.d(TAG,"rootPath="+p_rootPath+",GitRootDir="+sGitRootDir);
        if (!p_rootPath.equals(sGitRootDir)) {
            m_item.add("../");
            m_path.add(m_file.getParent());
            m_isRoot = false;
        }
        }

        m_curDir = p_rootPath;
        if (m_filesArray == null) return;
        //sorting file list in alphabetical order
        Arrays.sort(m_filesArray);
        for (int i = 0; i < m_filesArray.length; i++) {
            File file = m_filesArray[i];


            if (file.isDirectory()) {
                try {
                    if (  !PreferenceManager.getDefaultSharedPreferences(activity).getBoolean("GitShowAttachDir", false) && file.getCanonicalPath().contains("_attach")) {
                    }
                    else if (file.getName().charAt(0) == '.') {
                    }
                    else if( sSearchText!=null && !sSearchText.isEmpty()) {

                        List<Path> allFiles = new ArrayList<>();
                        try {
                            listAllFiles(file.toPath(), allFiles);
                            for (Path path : allFiles) {
                                if(path.getFileName().toString().toLowerCase().contains(sSearchText)) {
                                    m_files.add(path.getFileName().toString());
                                    m_filesPath.add(path.toString());
                                }else{
                                    if(path.getFileName().toString().toLowerCase().contains("txt") || path.getFileName().toString().toLowerCase().contains("md")) {
                                        try {
                                            if (searchTextFileContent(path.toFile(), sSearchText)) {
                                                m_files.add(path.getFileName().toString());
                                                m_filesPath.add(path.toString());
                                            }
                                        }catch(FileNotFoundException ee){
                                            ee.printStackTrace();
                                        }
                                    }
                                }
                            }

                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }

                    }else{
                        m_item.add(file.getName());
                        m_path.add(file.getPath());
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } else {

                if( sSearchText!=null && !sSearchText.equals("") ){
                    if(file.getName().toLowerCase().contains(sSearchText)){
                        m_files.add(file.getName());
                        m_filesPath.add(file.getPath());
                    }else if(file.getName().toLowerCase().contains("txt") || file.getName().toLowerCase().contains("md")) {
                        try {
                            if (searchTextFileContent(file, sSearchText)) {
                                m_files.add(file.getName());
                                m_filesPath.add(file.getPath());
                            }
                        } catch (FileNotFoundException e) {
                            throw new RuntimeException(e);
                        }
                    }

                } else{
                    m_files.add(file.getName());
                    m_filesPath.add(file.getPath());
                }
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
                if( m_item.get(position)!=null && !m_item.get(position).contains(MyApplication.getAppContext().getString(R.string.search_mode))){
Log.d(TAG,"m_item name = "+m_item.get(position)+",position number = "+ position+",m_path="+m_path+",m_path get = "+ m_path.get(position));
                File m_isFile = new File(m_path.get(position));

                if (m_isFile.isDirectory()) {
                    m_PreviousDir = m_curDir;
                    Log.d(TAG,"m_PreviousDir="+m_PreviousDir+",m_curDir="+m_curDir);
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
                            Log.d(TAG, "file type = " + getMimeType(Uri.fromFile(m_isFile), activity) + ", uri=" + path.getPath());
                            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                            intent.setFlags(FLAG_GRANT_READ_URI_PERMISSION);
                            intent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
                            String authority = activity.getPackageName() + ".fileprovider";
                            Uri uri = FileProvider.getUriForFile(activity, authority, m_isFile);
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
            }
        });
    }
    void searchTextFile() {

            try {

                final EditText txtUrl = new EditText(activity);
                txtUrl.setText(sSearchText);
                txtUrl.setMaxLines(3);
                txtUrl.setLines(3);
                txtUrl.setTextSize(  Integer.parseInt(PreferenceManager.getDefaultSharedPreferences(activity).getString("GitEditTextSize", "18")));
                new AlertDialog.Builder(activity)
                        .setTitle(getResources().getString(R.string.title_search_text))
                        .setMessage(getResources().getString(R.string.message_search_text))
                        .setView(txtUrl)
                        .setPositiveButton(MyApplication.getAppContext().getText(R.string.dialog_ok), new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {
                                sSearchText = txtUrl.getText().toString().toLowerCase();
                                bRefreshDir = true;
                                new MyAsyncTask().execute();
                            }
                        }).setNegativeButton(MyApplication.getAppContext().getText(R.string.dialog_reset) , new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {
                                sSearchText = "";
                                bRefreshDir = true;
                                new MyAsyncTask().execute();
                            }
                        }).show();


            } catch (Exception e) {
                e.printStackTrace();

            }




    }

    void addFile() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("*/*");
        intent.putExtra(Intent.EXTRA_LOCAL_ONLY, true);
        intent.setFlags(FLAG_GRANT_READ_URI_PERMISSION | FLAG_GRANT_WRITE_URI_PERMISSION);
        myActivityResultLauncher.launch(intent);
    }

    boolean searchTextFileContent(File aFile,String sSearch) throws FileNotFoundException {

            Scanner scan = new Scanner(aFile);
            while(scan.hasNext()){
                String line = scan.nextLine().toLowerCase().toString();
                if(line.contains(sSearch)){
                   return true;
                }
            }
        return false;
   }


    // You can do the assignment inside onAttach or onCreate, i.e, before the activity is displayed
    ActivityResultLauncher<Intent> myActivityResultLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            new ActivityResultCallback<ActivityResult>() {
                @Override
                public void onActivityResult(ActivityResult result) {

                    int requestCode = result.getResultCode();
                    Intent resultData = result.getData();
                    Log.d(TAG, "resultcode = "+ result.getResultCode());

                    if (requestCode == Activity.RESULT_OK) {
                        Uri uri = null;
                        if (resultData != null) {
                            uri = resultData.getData();
                            pickiT.getPath(uri, Build.VERSION.SDK_INT);
                        }


                    }
                }
            });

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
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.file_explorer_menu, menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        for (int i = 0; i < menu.size(); i++) {
            MenuItem item = menu.getItem(i);
            SpannableString spanString = new SpannableString(menu.getItem(i).getTitle().toString());
            int end = spanString.length();
            //spanString
            spanString.setSpan(new RelativeSizeSpan(1.2f), 0, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            item.setTitle(spanString);
        }
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) {
            if( m_item.get(0).equalsIgnoreCase("../"))
                getDirFromRoot( m_path.get(0));
            else
            onBackPressed();

            return true;
        } else if (id == R.id.action_refresh) {
            sSearchText="";
            aEditTextSearch.setText("");
            getDirFromRoot(m_curDir);
        } else if (id == R.id.action_search_text_file) {
            searchTextFile();
        } else if (id == R.id.action_add_file) {
            sSearchText="";
            addFile();
        } else if (id == R.id.action_delete) {
            sSearchText="";
            deleteFile();
        } else if (id == R.id.action_create_folder) {
            sSearchText="";
            createNewFolder(1);
        } else if (id == R.id.action_create_file) {
            sSearchText="";
            createNewFolder(0);
        }
        return super.onOptionsItemSelected(item);
    }

    public String getMimeType(Uri uri, Context context) {
        String mimeType = null;
        try {
            if (Objects.equals(uri.getScheme(), ContentResolver.SCHEME_CONTENT)) {
                ContentResolver cr = context.getContentResolver();
                mimeType = cr.getType(uri);
            } else {
                String fileExtension = MimeTypeMap.getFileExtensionFromUrl(uri
                        .toString());
                mimeType = MimeTypeMap.getSingleton().getMimeTypeFromExtension(
                        fileExtension.toLowerCase());
            }
        }catch(Exception ee){
            ee.printStackTrace();
        }
        return mimeType;
    }

    //When selecting a file from Google Drive, for example, the Uri will be returned before the file is available(if it has not yet been cached/downloaded).
    //We are unable to see the progress
    //Apps like Dropbox will display a dialog inside the picker
    //This will only be called when selecting a drive file
    @Override
    public void PickiTonUriReturned() {
        //Use to let user know that we are waiting for the application to return the file
        //See the demo project to see how I used this.
    }

    //Called when the file creations starts (similar to onPreExecute)
    //This will only be called if the selected file is not local or if the file is from an unknown file provider
    @Override
    public void PickiTonStartListener() {
        //Can be used to display a ProgressDialog
    }

    //Returns the progress of the file being created (in percentage)
    //This will only be called if the selected file is not local or if the file is from an unknown file provider
    @Override
    public void PickiTonProgressUpdate(int progress) {
        //Can be used to update the progress of your dialog
    }

    //If the selected file was a local file then this will be called directly, returning the path as a String.
    //String path - returned path
    //boolean wasDriveFile - check if it was a drive file
    //boolean wasUnknownProvider - check if it was from an unknown file provider
    //boolean wasSuccessful - check if it was successful
    //String reason - the get the reason why wasSuccessful returned false
    @Override
    public void PickiTonCompleteListener(String path, boolean wasDriveFile, boolean wasUnknownProvider, boolean wasSuccessful, String reason) {
        //Dismiss dialog and return the path
        Log.d(TAG,"pickiT real path ="+path +", was successful = "+ wasSuccessful +", reason = "+ reason);

              //              Log.d(TAG, "uri = " + uri.getPath() + ",host = " + uri.getHost() + ", authority = " + uri.getAuthority() + ", real path = " + FileUtility.getPath(activity, uri));

                            final File aSelectedFile = new File(path);

                            try {

                                final EditText txtUrl = new EditText(activity);
                                txtUrl.setText(aSelectedFile.getName());
                                txtUrl.setMaxLines(3);
                                txtUrl.setLines(3);
                                txtUrl.setTextSize(Integer.parseInt(PreferenceManager.getDefaultSharedPreferences(activity).getString("GitEditTextSize", "18")));
                                new AlertDialog.Builder(activity)
                                        .setTitle(getResources().getString(R.string.dialog_title_add))
                                        .setMessage(getResources().getString(R.string.dialog_file_name))
                                        .setView(txtUrl)
                                        .setPositiveButton(MyApplication.getAppContext().getText(R.string.dialog_ok), new DialogInterface.OnClickListener() {
                                            public void onClick(DialogInterface dialog, int whichButton) {
                                                bRefreshDir = false;
                                                new Thread(new Runnable() {
                                                    @Override
                                                    public void run() {
                                                        final File aDestFile;
                                                        try {
                                                            aDestFile = new File(m_curDir, txtUrl.getText().toString().trim());
                                                            Log.d(TAG, "dest file = " + aDestFile.getCanonicalPath());
                                                            final String sDestFileNameString;

                                                            //sDestFileNameString = aDestFile.getCanonicalPath().toString().substring(MyGitUtility.getLocalGitDirectory(activity, sGitRemoteUrl).length());
                                                            if (aDestFile.getCanonicalPath().toString().substring(MyGitUtility.getLocalGitDirectory(activity, sGitRemoteUrl).length()).startsWith("/"))
                                                                sDestFileNameString = aDestFile.getCanonicalPath().toString().substring(MyGitUtility.getLocalGitDirectory(activity, sGitRemoteUrl).length() + 1);
                                                            else
                                                                sDestFileNameString = aDestFile.getCanonicalPath().toString().substring(MyGitUtility.getLocalGitDirectory(activity, sGitRemoteUrl).length());
                                                            Files.copy(aSelectedFile.toPath(), aDestFile.toPath());
                                                            bRefreshDir = true;

                                                            new Thread(new Runnable() {
                                                                @Override
                                                                public void run() {
                                                                    try {
                                                                        Thread.sleep(1000);
                                                                    } catch (
                                                                            InterruptedException e) {
                                                                        e.printStackTrace();
                                                                    }
                                                                    boolean bCommit = false;
                                                                    Log.d(TAG, "commit after file be copied");

                                                                    bCommit = MyGitUtility.commit(MyApplication.getAppContext(), sGitRemoteUrl, MyApplication.getAppContext().getString(R.string.view_file_add_attachment_file_commit) + sDestFileNameString);
                                                                    if (sGitRemoteUrl.indexOf("local") == -1 && bCommit)
                                                                        MyGitUtility.push(MyApplication.getAppContext(), sGitRemoteUrl);

                                                                }
                                                            }).start();

                                                        } catch (FileAlreadyExistsException ee) {

                                                        } catch (IOException e) {
                                                            e.printStackTrace();

                                                        }
                                                        bRefreshDir = true;

                                                    }
                                                }).start();
                                                new MyAsyncTask().execute();
                                            }
                                        }).setNegativeButton(MyApplication.getAppContext().getText(R.string.dialog_cancel), new DialogInterface.OnClickListener() {
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

    @Override
    public void PickiTonMultipleCompleteListener(ArrayList<String> arrayList, boolean b, String s) {

    }

    class MyAsyncTask extends AsyncTask<Void, Void, Void> {
        @Override
        protected Void doInBackground(Void... params) {
            // 這裡處理背景工作
            while (true) {
                if (bRefreshDir)
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
