package com.example.unishop;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.provider.MediaStore;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import com.squareup.picasso.MemoryPolicy;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class ShopActivity extends AppCompatActivity {
    TextView tvrname,tvrphone,tvraddress,tvrloc;
    CircleImageView imgshop;
    ListView lvproduct;
    Dialog myDialogWindow;
    ArrayList<HashMap<String, String>> productlist;
    String email,shopid;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_shop);
        Intent intent = getIntent();
        Bundle bundle = intent.getExtras();
        shopid = bundle.getString("shopid");
        String rname = bundle.getString("shopname");
        String rphone = bundle.getString("phone");
        String raddress = bundle.getString("address");
        //String rlocation = bundle.getString("location");
        email = bundle.getString("email");
        initView();
        tvrname.setText(rname);
        tvraddress.setText(raddress);
        tvrphone.setText(rphone);
        //tvrloc.setText(rlocation);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        Picasso.with(this).load("http://www.simplehlife.com/uniShop/images/"+shopid+".jpg")
                .fit().into(imgshop);
        //  .memoryPolicy(MemoryPolicy.NO_CACHE).networkPolicy(NetworkPolicy.NO_CACHE)

        lvproduct.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                //Toast.makeText(ShopActivity.this,"SHOW PRODUCT", Toast.LENGTH_SHORT).show();
                showproductDetail(position);
            }
        });
        loadproducts(shopid);

    }

    //product details
    private void showproductDetail(int p) {
        myDialogWindow = new Dialog(this, android.R.style.Theme_DeviceDefault_Light_Dialog_NoActionBar_MinWidth);//Theme_DeviceDefault_Dialog_NoActionBar
        myDialogWindow.setContentView(R.layout.dialog_window);
        myDialogWindow.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        TextView tvfname,tvfprice,tvfquan;
        CircleImageView imgproduct = myDialogWindow.findViewById(R.id.imageViewProduct);
        final Spinner spquan = myDialogWindow.findViewById(R.id.spinner2);
        Button btnorder = myDialogWindow.findViewById(R.id.button2);
        tvfname= myDialogWindow.findViewById(R.id.textView12);
        tvfprice = myDialogWindow.findViewById(R.id.textView13);
        tvfquan = myDialogWindow.findViewById(R.id.textView14);
        tvfname.setText(productlist.get(p).get("productname"));
        tvfprice.setText(productlist.get(p).get("productprice"));
        tvfquan.setText(productlist.get(p).get("quantity"));
        final String productid =(productlist.get(p).get("productid"));
        final String productname = productlist.get(p).get("productname");
        final String productprice = productlist.get(p).get("productprice");
        btnorder.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String pquan = spquan.getSelectedItem().toString();
                dialogOrder(productid,productname,pquan,productprice);
            }
        });
        int quan = Integer.parseInt(productlist.get(p).get("quantity"));
        List<String> list = new ArrayList<String>();
        for (int i = 1; i<=quan;i++){
            list.add(""+i);
        }
        ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_spinner_item, list);
        dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spquan.setAdapter(dataAdapter);

        Picasso.with(this).load("http://www.simplehlife.com/uniShop/productimages/"+productid+".jpg")
                .memoryPolicy(MemoryPolicy.NO_CACHE).networkPolicy(NetworkPolicy.NO_CACHE)
                .fit().into(imgproduct);
        myDialogWindow.show();
    }


    //load products
    private void loadproducts(final String shopid) {
        class Loadproduct extends AsyncTask<Void,Void,String>{

            @Override
            protected String doInBackground(Void... voids) {
                HashMap<String,String> hashMap = new HashMap<>();
                hashMap.put("shopid",shopid);
                RequestHandler requestHandler = new RequestHandler();
                String s = requestHandler.sendPostRequest("http://www.simplehlife.com/uniShop/php/load_product.php",hashMap);
                return s;
            }
            @Override
            protected void onPostExecute(String s) {
                super.onPostExecute(s);
                productlist.clear();
                try {
                    JSONObject jsonObject = new JSONObject(s);
                    JSONArray productarray = jsonObject.getJSONArray("product");
                    for (int i = 0; i < productarray.length(); i++) {
                        JSONObject c = productarray.getJSONObject(i);
                        String jsid = c.getString("productid");
                        String jsfname = c.getString("productname");
                        String jsfprice = c.getString("productprice");
                        String jsquan = c.getString("quantity");
                        HashMap<String,String> productlisthash = new HashMap<>();
                        productlisthash.put("productid",jsid);
                        productlisthash.put("productname",jsfname);
                        productlisthash.put("productprice",jsfprice);
                        productlisthash.put("quantity",jsquan);
                        productlist.add(productlisthash);
                    }
                }catch(JSONException e){}
                ListAdapter adapter = new CustomAdapterProduct(
                        ShopActivity.this, productlist,
                        R.layout.product_list_shop, new String[]
                        {"productname","productprice","productquantity"}, new int[]
                        {R.id.textView,R.id.textView2,R.id.textView3});
                lvproduct.setAdapter(adapter);

            }
        }
        Loadproduct loadproduct = new Loadproduct();
        loadproduct.execute();
    }

    private void initView() {
        imgshop = findViewById(R.id.imageViewproduct);
        tvrname = findViewById(R.id.textView6);
        tvrphone = findViewById(R.id.textView7);
        tvraddress = findViewById(R.id.textView8);
        //tvrloc = findViewById(R.id.textView9);
        lvproduct = findViewById(R.id.listviewproduct);
        productlist = new ArrayList<>();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                Intent intent = new Intent(ShopActivity.this,MainActivity.class);
                Bundle bundle = new Bundle();
                bundle.putString("email",email);
                intent.putExtras(bundle);
                intent.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
                startActivity(intent);
                this.finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    //dialog order
    private void dialogOrder(final String productid, final String productname, final String pquan, final String productprice) {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
        alertDialogBuilder.setTitle("Order "+productname+ " with quantity "+pquan);

        alertDialogBuilder
                .setMessage("Are you sure?")
                .setCancelable(false)
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        insertCart(productid,productname,pquan,productprice);
                    }
                })
                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                });
        AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.show();
    }

    private void insertCart(final String productid, final String productname, final String pquan, final String productprice) {
        class InsertCart extends AsyncTask<Void,Void,String>{

            @Override
            protected String doInBackground(Void... voids) {
                HashMap<String,String> hashMap = new HashMap<>();
                hashMap.put("shopid",shopid);
                hashMap.put("productid",productid);
                hashMap.put("email",email);
                hashMap.put("quantity",pquan);
                hashMap.put("productprice",productprice);
                hashMap.put("productname",productname);
                RequestHandler rh = new RequestHandler();
                String s = rh.sendPostRequest("http://www.simplehlife.com/uniShop/php/insert_cart.php",hashMap);
                return s;
            }

            @Override
            protected void onPostExecute(String s) {
                super.onPostExecute(s);
                Toast.makeText(ShopActivity.this,s, Toast.LENGTH_SHORT).show();
                if (s.equalsIgnoreCase("success")){
                    //Toast.makeText(ShopActivity.this, "Success", Toast.LENGTH_SHORT).show();
                    myDialogWindow.dismiss();
                    loadproducts(shopid);
                }else{
                    Toast.makeText(ShopActivity.this, "Failed", Toast.LENGTH_SHORT).show();
                }
            }

        }
        InsertCart insertCart = new InsertCart();
        insertCart.execute();
    }

}
