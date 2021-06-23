package com.ruzra.delivery.uzdirectory.booking.views.fragments.checkout;

import android.content.Context;
import android.graphics.Typeface;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.SimpleItemAnimator;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.ruzra.delivery.uzdirectory.R;
import com.ruzra.delivery.uzdirectory.adapter.lists.PayGWAdapter;
import com.ruzra.delivery.uzdirectory.appconfig.Constances;
import com.ruzra.delivery.uzdirectory.booking.controllers.PaymentController;
import com.ruzra.delivery.uzdirectory.booking.controllers.adapters.PayGWParser;
import com.ruzra.delivery.uzdirectory.booking.modals.Cart;
import com.ruzra.delivery.uzdirectory.booking.modals.Fee;
import com.ruzra.delivery.uzdirectory.booking.modals.Option;
import com.ruzra.delivery.uzdirectory.booking.modals.PaymentGateway;
import com.ruzra.delivery.uzdirectory.booking.modals.Service;
import com.ruzra.delivery.uzdirectory.booking.views.activities.BookingCheckoutActivity;
import com.ruzra.delivery.uzdirectory.network.VolleySingleton;
import com.ruzra.delivery.uzdirectory.network.api_request.SimpleRequest;
import com.ruzra.delivery.uzdirectory.parser.tags.Tags;
import com.ruzra.delivery.uzdirectory.utils.OfferUtils;
import com.ruzra.delivery.uzdirectory.utils.Utils;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.realm.RealmList;

import static com.ruzra.delivery.uzdirectory.appconfig.AppConfig.APP_DEBUG;
import static com.ruzra.delivery.uzdirectory.booking.views.activities.BookingCheckoutActivity.mCart;


public class PaymentFragment extends Fragment implements PayGWAdapter.ClickListener {


    @BindView(R.id.list_payment)
    RecyclerView listPayment;

    @BindView(R.id.payment_detail_layout)
    LinearLayout paymentDetailLayout;

    @BindView(R.id.layout_fees)
    LinearLayout layoutFees;


    @BindView(R.id.layout_subtotal)
    LinearLayout layoutSubtotal;

    @BindView(R.id.subtotal_val)
    TextView subtotalVal;

    @BindView(R.id.layout_total)
    LinearLayout layoutTotal;

    @BindView(R.id.total_value)
    TextView totalValue;


    private PayGWAdapter mAdapter;
    private Context mContext;

    public static int paymentChoosed = -1;


    public PaymentFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_payment, container, false);
        mContext = root.getContext();
        ButterKnife.bind(this, root);


        setupAdapter();

        return root;
    }


    public List<PaymentGateway> getData() {

        List<PaymentGateway> results = new ArrayList<>();

        RealmList<PaymentGateway> listCats = PaymentController.list();

        for (PaymentGateway cat : listCats) {
            results.add(cat);
        }


        return results;
    }

    @Override
    public void onStart() {
        super.onStart();

        if (getData() == null || getData().size() == 0) {
            getPaymentGatewayFromAPI();
        }
    }


    private void setupAdapter() {
        mAdapter = new PayGWAdapter(getActivity(), getData());
        mAdapter.setClickListener(this);
        listPayment.setHasFixedSize(true);

        final LinearLayoutManager mLayoutManager = new LinearLayoutManager(getActivity());
        mLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        listPayment.setLayoutManager(mLayoutManager);
        listPayment.setAdapter(mAdapter);


        ((SimpleItemAnimator) listPayment.getItemAnimator()).setSupportsChangeAnimations(false);

    }

    @Override
    public void itemClicked(View view, int position) {

        paymentDetailLayout.setVisibility(View.VISIBLE);

        PaymentGateway mPG = mAdapter.getItemDetail(position);
        paymentChoosed = mPG.getId();

        //sum of all the item added in the card ( card = sum(amount * qte)
        double calculateTotal = 0;
        double totalFees = 0; //init total fees to 1


        //product price x qty
        for (Cart c : mCart) {
            if (c.getServices() != null) {
                for (Service ss : c.getSelectedService()) {
                    if (ss.getOptions() != null) {
                        for (Option opts : ss.getOptions()) {
                            if (opts.getValue() <= 0) calculateTotal = 0;
                            else calculateTotal += opts.getValue();
                        }
                    }

                }
            }
        }

        subtotalVal.setText(OfferUtils.parseCurrencyFormat(
                (float) calculateTotal,
                BookingCheckoutActivity.defaultCurrency));

        //check fees
        if (mPG.getFees() != null && mPG.getFees().size() > 0) {
            for (Fee mFee : mPG.getFees()) {

                //fixing bug related to duplicated payment detail
                layoutFees.removeAllViews();


                //Build Layout item Fee
                LinearLayout itemLayoutFee = new LinearLayout(mContext);
                itemLayoutFee.setOrientation(LinearLayout.HORIZONTAL);
                LinearLayout.LayoutParams itemLayoutFeeLP = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                itemLayoutFeeLP.setMargins(Utils.dpToPx(5), Utils.dpToPx(5), Utils.dpToPx(5), Utils.dpToPx(5));
                itemLayoutFee.setLayoutParams(itemLayoutFeeLP);

                //Build Fee Name
                TextView feeName = new TextView(view.getContext());
                LinearLayout.LayoutParams feeNameLP = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                feeNameLP.weight = 1;
                feeName.setTextAlignment(View.TEXT_ALIGNMENT_VIEW_START);
                feeName.setLayoutParams(feeNameLP);
                feeName.setTypeface(feeName.getTypeface(), Typeface.BOLD);
                itemLayoutFee.addView(feeName);

                //Build Fee Value
                TextView feeValue = new TextView(view.getContext());
                LinearLayout.LayoutParams feeValueLP = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                feeValue.setLayoutParams(feeValueLP);
                feeValue.setTextAlignment(View.TEXT_ALIGNMENT_VIEW_END);
                feeValue.setTypeface(feeName.getTypeface(), Typeface.BOLD);
                itemLayoutFee.addView(feeValue);


                //calculate total fees
                double feesValue = ((float) (mFee.getValue() / 100) * calculateTotal);
                totalFees += feesValue;

                feeName.setText(mFee.getName() + "(" + String.format("%s%%", (int) mFee.getValue()) + ")");

                feeValue.setText(
                        OfferUtils.parseCurrencyFormat(
                                (float) feesValue,
                                BookingCheckoutActivity.defaultCurrency));

                layoutFees.addView(itemLayoutFee);

            }


        }


        //add fees
        if (totalFees > 0)
            calculateTotal = calculateTotal + totalFees;

        totalValue.setText(OfferUtils.parseCurrencyFormat(
                (float) calculateTotal,
                BookingCheckoutActivity.defaultCurrency));


    }


    private void getPaymentGatewayFromAPI() {


        RequestQueue queue = VolleySingleton.getInstance(mContext).getRequestQueue();

        SimpleRequest request = new SimpleRequest(Request.Method.GET,
                Constances.API.API_PAYMENT_GATEWAY, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {

                try {

                    if (APP_DEBUG) {
                        Log.e("paymentGateways", response);
                    }

                    JSONObject jsonObject = new JSONObject(response);
                    // Log.e("response", jsonObject.toString());
                    final PayGWParser mModuleParser = new PayGWParser(jsonObject);
                    int success = Integer.parseInt(mModuleParser.getStringAttr(Tags.SUCCESS));

                    if (success == 1 && mModuleParser.getPaymentGetway().size() > 0) {
                        mAdapter.addAll(mModuleParser.getPaymentGetway());

                        mAdapter.notifyDataSetChanged();

                        PaymentController.insertPaymentGatewayList(
                                mModuleParser.getPaymentGetway()
                        );
                    }

                } catch (JSONException e) {
                    //send a rapport to support
                    e.printStackTrace();

                }


            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                if (APP_DEBUG) {
                    Log.e("ERROR", error.toString());
                }
            }
        }) {

        };

        request.setRetryPolicy(new DefaultRetryPolicy(SimpleRequest.TIME_OUT,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));

        queue.add(request);


    }

}
