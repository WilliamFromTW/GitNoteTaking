package inmethod.gitnotetaking.view;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

import inmethod.gitnotetaking.R;


public class RecyclerAdapterForDevice extends RecyclerView.Adapter<RecyclerAdapterForDevice.ViewHolder> implements View.OnClickListener {

    private Context mContext;
    private ArrayList<GitList> mData = new ArrayList<>();
    private OnItemClickListener onItemClickListener = null;


    public RecyclerAdapterForDevice(Context context) {
        this.mContext = context;
    }

    /*暴露给外部的方法*/
    public void setOnItemClickListener(OnItemClickListener listener) {
        onItemClickListener = listener;
    }

    @Override
    public void onClick(View view) {
        if (onItemClickListener != null) {
            onItemClickListener.onItemClick(view, (int) view.getTag());
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

    public void clear() {
        mData.clear();
        this.notifyDataSetChanged();
    }

    public interface OnItemClickListener {
        void onItemClick(View view, int position);
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        public Object layoutData[];

        public ViewHolder(View itemView) {

            super(itemView);

        }
    }

}
