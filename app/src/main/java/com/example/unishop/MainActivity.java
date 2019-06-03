package com.example.unishop;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.util.ArrayList;
import java.util.HashMap;
import static java.lang.Thread.sleep;

public class MainActivity extends AppCompatActivity {
    ListView lvshop;
    ArrayList<HashMap<String, String>> shoplist;
    ArrayList<HashMap<String, String>> cartlist;
    double total;
    Spinner spcat;
    String email,username, matricno, phone;
    Dialog myDialogCart;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        lvshop = findViewById(R.id.listviewshop);
        spcat = findViewById(R.id.spinner);
        Intent intent = getIntent();
        Bundle bundle = intent.getExtras();
        email = bundle.getString("email");
        username = bundle.getString("username");
        phone = bundle.getString("phone");
        matricno = bundle.getString("matricno");
        loadShop(spcat.getSelectedItem().toString());
        lvshop.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                //Toast.makeText(MainActivity.this, shoplist.get(position).get("shopid"), Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(MainActivity.this, ShopActivity.class);
                Bundle bundle = new Bundle();
                bundle.putString("shopid", shoplist.get(position).get("shopid"));
                bundle.putString("shopname", shoplist.get(position).get("shopname"));
                bundle.putString("category", shoplist.get(position).get("category"));
                bundle.putString("phone", shoplist.get(position).get("phone"));
                bundle.putString("address", shoplist.get(position).get("address"));
                bundle.putString("location", shoplist.get(position).get("location"));
                bundle.putString("email", email);
                intent.putExtras(bundle);
                startActivity(intent);
            }
        });
        spcat.setSelection(0, false);
        spcat.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                loadShop(spcat.getSelectedItem().toString());
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }

    private void loadShop(final String cat) {
        class LoadShop extends AsyncTask<Void, Void, String> {

            @Override
            protected String doInBackground(Void... voids) {
                HashMap<String, String> hashMap = new HashMap<>();
                hashMap.put("category", cat);
                RequestHandler rh = new RequestHandler();
                shoplist = new ArrayList<>();
                String s = rh.sendPostRequest
                        ("http://www.simplehlife.com/uniShop/php/load_shopping.php", hashMap);
                return s;
            }

            @Override
            protected void onPostExecute(String s) {
                super.onPostExecute(s);
                //Toast.makeText(MainActivity.this,s, Toast.LENGTH_LONG).show();
                shoplist.clear();
                try {
                    JSONObject jsonObject = new JSONObject(s);
                    JSONArray shoparray = jsonObject.getJSONArray("shop");
                    Log.e("SIMPLE", jsonObject.toString());
                    for (int i = 0; i < shoparray.length(); i++) {
                        JSONObject c = shoparray.getJSONObject(i);
                        String sid = c.getString("shopid");
                        String sname = c.getString("shopname");
                        String scategory = c.getString("category");
                        String sphone = c.getString("phone");
                        String saddress = c.getString("address");
                        String sloc = c.getString("location");
                        HashMap<String, String> shoplisthash = new HashMap<>();
                        shoplisthash.put("shopid", sid);
                        shoplisthash.put("shopname", sname);
                        shoplisthash.put("category", scategory);
                        shoplisthash.put("phone", sphone);
                        shoplisthash.put("address", saddress);
                        shoplisthash.put("location", sloc);
                        shoplist.add(shoplisthash);
                    }
                } catch (final JSONException e) {
                    //Toast.makeText(MainActivity.this, "ERROR", Toast.LENGTH_LONG).show();
                    Log.e("JSONERROR", e.toString());
                }

                ListAdapter adapter = new CustomAdapter(
                        MainActivity.this, shoplist,
                        R.layout.cust_list_shop, new String[]
                        {"shopname","category","phone","address","location"}, new int[]
                        {R.id.nameshop,R.id.textView3,R.id.textView4,R.id.addressshop,R.id.textView5});
                lvshop.setAdapter(adapter);
            }

        }
        LoadShop loadShop = new LoadShop();
        loadShop.execute();
    }

    //menu
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
            case R.id.mycart:
                loadCartData();
                return true;

            //My Profile
            case R.id.myprofile:
                //Toast.makeText(MainActivity.this,"MY PROFILE", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(MainActivity.this, Profile.class);
                Bundle bundle = new Bundle();
                bundle.putString("email",email);
                bundle.putString("username",username);
                bundle.putString("phone",phone);
                bundle.putString("matricno",matricno);
                intent.putExtras(bundle);
                startActivity(intent);
                return true;

            case R.id.about:
                //Toast.makeText(MainActivity.this,"ABOUT", Toast.LENGTH_SHORT).show();
                intent = new Intent(MainActivity.this, AboutActivity.class);
                bundle = new Bundle();
                bundle.putString("email",email);
                startActivity(intent);
                return true;

            case R.id.logout:
                Toast.makeText(MainActivity.this,"LOG OUT", Toast.LENGTH_SHORT).show();
                intent = new Intent(MainActivity.this, LoginActivity.class);
                startActivity(intent);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }


    //cart
    private void loadCartWindow() {
        myDialogCart = new Dialog(this, android.R.style.Theme_DeviceDefault_Light_Dialog_NoActionBar_MinWidth);//Theme_DeviceDefault_Dialog_NoActionBar
        myDialogCart.setContentView(R.layout.cart_window);
        myDialogCart.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        ListView lvcart = myDialogCart.findViewById(R.id.lvmycart);
        TextView tvtotal = myDialogCart.findViewById(R.id.textViewTotal);
        TextView tvorderid = myDialogCart.findViewById(R.id.textOrderId);
        Button btnpay = myDialogCart.findViewById(R.id.btnPay);
        Log.e("SIMPLE","SIZE:"+cartlist.size());
        lvcart.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                dialogDeleteProduct(position);
                return false;
            }
        });
        btnpay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialogPay();
            }
        });
        ListAdapter adapter = new CustomAdapterCart(
                MainActivity.this, cartlist,
                R.layout.user_cart_list, new String[]
                {"productname","productprice","quantity","status"}, new int[]
                {R.id.textView,R.id.textView2,R.id.textView3,R.id.textView4});
        lvcart.setAdapter(adapter);
        tvtotal.setText("RM "+total);
        tvorderid.setText(cartlist.get(0).get("orderID"));
        myDialogCart.show();

    }

    private void dialogDeleteProduct(final int position) {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
        alertDialogBuilder.setTitle("Delete Product "+cartlist.get(position).get("productname")+"?");
        alertDialogBuilder
                .setMessage("Are you sure")
                .setCancelable(false)
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {

                        // Toast.makeText(MainActivity.this, cartlist.get(position).get("productname"), Toast.LENGTH_SHORT).show();
                        deleteCartProduct(position);
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

    private void deleteCartProduct(final int position) {
        class DeleteCartProduct extends AsyncTask<Void,Void,String>{

            @Override
            protected String doInBackground(Void... voids) {
                String productid = cartlist.get(position).get("productid");
                HashMap<String,String> hashMap = new HashMap<>();
                hashMap.put("productid",productid);
                hashMap.put("email",email);
                RequestHandler requestHandler = new RequestHandler();
                String s = requestHandler.sendPostRequest("http://www.simplehlife.com/uniShop/php/deletecart.php",hashMap);
                return s;
            }
            @Override
            protected void onPostExecute(String s) {
                super.onPostExecute(s);
                Toast.makeText(MainActivity.this, s, Toast.LENGTH_SHORT).show();
                if (s.equalsIgnoreCase("success")){
                    myDialogCart.dismiss();
                    loadCartData();
                    Toast.makeText(MainActivity.this, "Success", Toast.LENGTH_SHORT).show();
                }else{
                   Toast.makeText(MainActivity.this, "Failed", Toast.LENGTH_SHORT).show();
                }
            }
        }
        DeleteCartProduct deleteCartProduct = new DeleteCartProduct();
        deleteCartProduct.execute();
    }

    private void dialogPay() {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
        alertDialogBuilder.setTitle("Proceed with payment?");

        alertDialogBuilder
                .setMessage("Are you sure?")
                .setCancelable(false)
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        Intent intent = new Intent(MainActivity.this,PaymentActivity.class);
                        Bundle bundle = new Bundle();
                        bundle.putString("email",email);
                        bundle.putString("username",username);
                        bundle.putString("phone",phone);
                        bundle.putString("total", String.valueOf(total));
                        bundle.putString("orderID", cartlist.get(0).get("orderID"));
                        intent.putExtras(bundle);
                        myDialogCart.dismiss();
                        startActivity(intent);
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


    private void loadCartData() {
        class LoadCartData extends AsyncTask<Void,Void,String>{

            @Override
            protected String doInBackground(Void... voids) {
                HashMap<String,String> hashMap = new HashMap<>();
                hashMap.put("email",email);
                RequestHandler rh = new RequestHandler();
                cartlist = new ArrayList<>();
                String s = rh.sendPostRequest("http://www.simplehlife.com/uniShop/php/loadcart.php",hashMap);
                return s;
            }

            @Override
            protected void onPostExecute(String s) {
                cartlist.clear();
                total=0;
                try{
                    JSONObject jsonObject = new JSONObject(s);
                    JSONArray cartarray = jsonObject.getJSONArray("cart");
                    Log.e("SIMPLE", jsonObject.toString());
                    for (int i=0;i<cartarray .length();i++) {
                        JSONObject c = cartarray .getJSONObject(i);
                        String joid = c.getString("orderID");
                        String jsid = c.getString("shopid");
                        String jpid = c.getString("productid");
                        String jemail = c.getString("email");
                        String jpq = c.getString("quantity");
                        String jpp = c.getString("productprice");
                        String jpn = c.getString("productname");
                        String jst = c.getString("status");
                        HashMap<String,String> cartlisthash = new HashMap<>();
                        cartlisthash .put("orderID",joid);
                        cartlisthash .put("shopid",jsid);
                        cartlisthash .put("productid",jpid);
                        cartlisthash .put("email",jemail);
                        cartlisthash .put("quantity",jpq);
                        cartlisthash .put("productprice","RM "+jpp);
                        cartlisthash .put("productname",jpn);
                        cartlisthash .put("status",jst);
                        cartlist.add(cartlisthash);
                        total = total + (Double.parseDouble(jpp) * Double.parseDouble(jpq));

                    }
                }catch (JSONException e){}
                super.onPostExecute(s);
                if (total>0){
                    loadCartWindow();
                }else{
                    Toast.makeText(MainActivity.this, "Cart is feeling empty", Toast.LENGTH_SHORT).show();
                }

            }
        }
        LoadCartData loadCartData = new LoadCartData();
        loadCartData.execute();
    }
}