package inmethod.gitnotetaking.view;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemLongClickListener;

import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

import inmethod.gitnotetaking.R;


public class RecyclerAdapterForDevice extends RecyclerView.Adapter<RecyclerAdapterForDevice.ViewHolder> implements View.OnClickListener, AdapterView.OnLongClickListener {

    private Context mContext;
    private ArrayList<GitList> mData = new ArrayList<>();
    private OnItemClickListener onItemClickListener = null;
    private OnItemLongClickListener onItemLongClickListener = null;

    public RecyclerAdapterForDevice(Context context) {
        this.mContext = context;
    }

    /*暴露给外部的方法*/
    public void setOnItemClickListener(OnItemClickListener listener) {
        onItemClickListener = listener;
    }

    /*暴露给外部的方法*/
    public void setOnItemLongClickListener(OnItemLongClickListener listener) {
        onItemLongClickListener = listener;
    }


    @Override
    public void onClick(View view) {
        if (onItemClickListener != null) {
            onItemClickListener.onItemClick(view, (int) view.getTag());
        }
    }
    @Override
    public boolean onLongClick(View view) {
        if (onItemLongClickListener != null) {
            onItemLongClickListener.onItemLongClick(view, (int) view.getTag());
            return true;
        }
        return false;
    }


    public void clear(){
        if(mData!=null && mData.size()>0){
            mData.clear();
            this.notifyDataSetChanged();
        }
    }
    public void setData(ArrayList<GitList> data) {
        this.mData = data;
        this.notifyDataSetChanged();
    }

    public void addData(GitList aData) {
        mData.add(aData);
        this.notifyDataSetChanged();
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent,
                                         int viewType) {
        View view = LayoutInflater
                .from(mContext)
                .inflate(R.layout.recycler_adapter_layout, parent, false);
        ViewHolder holder = new ViewHolder(view);
        holder.layoutData = GitList.getDeviceInfoFromLayoutId(view);
        view.setOnClickListener(this);
        view.setOnLongClickListener(this);
        return holder;
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, final int position) {
        GitList post = mData.get(position);
        GitList.mapDeviceInfoToLayout(holder.layoutData, post);

        holder.itemView.setTag(position);
    }

    @Override
    public int getItemCount() {
        return mData.size();
    }




    public interface OnItemClickListener {
        void onItemClick(View view, int position);
    }

    public interface OnItemLongClickListener{
        void onItemLongClick(View view,int position);
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        public Object layoutData[];

        public ViewHolder(View itemView) {

            super(itemView);

        }
    }

}
