package com.example.unishop;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import com.squareup.picasso.MemoryPolicy;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import de.hdodenhof.circleimageview.CircleImageView;

public class CustomAdapter extends SimpleAdapter {
    private Context mContext;
    public LayoutInflater inflater=null;
    public CustomAdapter(Context context, List<? extends Map<String, ?>> data, int resource, String[] from, int[] to) {
        super(context, data, resource, from, to);
        mContext = context;
        inflater = (LayoutInflater)mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View vi=convertView;
        try{
            if(convertView==null)
                vi = inflater.inflate(R.layout.cust_list_shop, null);
            HashMap<String, Object> data = (HashMap<String, Object>) getItem(position);
            TextView tvshopname = vi.findViewById(R.id.nameshop);
            TextView tvcat = vi.findViewById(R.id.textView3);
            TextView tvphone = vi.findViewById(R.id.textView4);
            TextView tvadd = vi.findViewById(R.id.addressshop);
            TextView tvloc = vi.findViewById(R.id.textView5);
            ImageView imgshop =vi.findViewById(R.id.imageView2);
            String dsid=(String) data.get("shopid");
            String dname = (String) data.get("shopname");//hilang
            String dcat = (String) data.get("category");
            String dphone = (String) data.get("phone");
            String dadd =(String) data.get("address");
            String dloc = (String) data.get("location");
            tvshopname.setText(dname);
            tvcat.setText(dcat);
            tvphone.setText(dphone);
            tvadd.setText(dadd);
            tvloc.setText(dloc);
            String image_url = "http://www.simplehlife.com/uniShop/images/"+dsid+".jpg";
            Picasso.with(mContext).load(image_url)
                    .fit().into(imgshop);

        }catch (IndexOutOfBoundsException e){
        }
        return vi;
    }
}