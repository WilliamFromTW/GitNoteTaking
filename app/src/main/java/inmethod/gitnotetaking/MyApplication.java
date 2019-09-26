package inmethod.gitnotetaking;

import android.app.Application;
import android.content.Context;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class MyApplication extends Application {

    private static Context context;
    private static ArrayList<File> aFileList = new ArrayList<File>();
    public final static int NONE = 0;
    public final static int CUT = 1;
    public final static int COPY = 2;
    private static int iStatus = NONE;


    public void onCreate() {
        super.onCreate();
        MyApplication.context = getApplicationContext();
    }

    public static boolean isLocal(String sRemoteUrl ){
       if(sRemoteUrl!=null && sRemoteUrl.indexOf("local")!=-1)
           return false;
       else return true;
    }

    public static void resetFiles(){
        aFileList.clear();
        iStatus = NONE;
    }
    public static void storeFiles(File aFile){
        aFileList.add(aFile);
    }

    public static List<File> getStoreFiles(){
        return aFileList;
    }

    public static void setCutCopyStatus(int i){
        iStatus = i;
    }

    public static int getCutCopyStatus(){
        return iStatus;
    }

    public static Context getAppContext() {
        return MyApplication.context;
    }
}