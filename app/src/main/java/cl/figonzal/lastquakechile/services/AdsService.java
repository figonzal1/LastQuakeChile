package cl.figonzal.lastquakechile.services;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;
import android.view.View;

import androidx.fragment.app.FragmentManager;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.reward.RewardItem;
import com.google.android.gms.ads.reward.RewardedVideoAd;
import com.google.android.gms.ads.reward.RewardedVideoAdListener;

import java.util.Date;

import cl.figonzal.lastquakechile.R;
import cl.figonzal.lastquakechile.dialogs.RewardDialogFragment;

public class AdsService {

    private RewardedVideoAd rewardedVideoAd;
    private final FragmentManager fragmentManager;
    private final Context context;
    private SharedPreferences sharedPreferences;

    public AdsService(Context context, FragmentManager fragmentManager) {
        this.context = context;
        this.fragmentManager = fragmentManager;
        sharedPreferences = context.getSharedPreferences(context.getString(R.string.MAIN_SHARED_PREF_KEY), Context.MODE_PRIVATE);
    }

    public RewardedVideoAd getRewardedVideoAd() {
        return rewardedVideoAd;
    }

    /**
     * Funcion que realiza la configuracion de reward dialog
     */
    public void rewardDialog() {

        Date reward_date = new Date(sharedPreferences.getLong(context.getString(R.string.SHARED_PREF_END_REWARD_TIME), 0));
        Date now_date = new Date();

        //Si la hora del celular es posterior a reward date
        if (now_date.after(reward_date)) {

            Log.d(context.getString(R.string.TAG_REWARD_STATUS), context.getString(R.string
                    .TAG_REWARD_STATUS_EN_PERIODO));

            boolean showDialog = Utils.generateRandomNumber();
            if (showDialog) {
                //Cargar dialog
                mostrarDialog();
                Log.d(context.getString(R.string.TAG_RANDOM_SHOW_REWARD_DIALOG), context.getString(R.string
                        .TAG_RANDOM_SHOW_REWARD_DIALOG_ON));
            } else {
                Log.d(context.getString(R.string.TAG_RANDOM_SHOW_REWARD_DIALOG), context.getString(R.string
                        .TAG_RANDOM_SHOW_REWARD_DIALOG_OFF));
            }
        }

        //Si el periodo de reward aun no pasa
        else if (now_date.before(reward_date)) {
            Log.d(context.getString(R.string.TAG_REWARD_STATUS), context.getString(R.string.TAG_REWARD_STATUS_PERIODO_INACTIVO));
        }
    }

    /**
     * Funcion encargada de cargar el video de bonificacion
     */
    public void loadRewardedVideo(final Activity activity) {
        rewardedVideoAd = MobileAds.getRewardedVideoAdInstance(context);
        rewardedVideoAd.loadAd(context.getString(R.string.ADMOB_ID_VIDEO), new AdRequest.Builder().build());

        rewardedVideoAd.setRewardedVideoAdListener(new RewardedVideoAdListener() {
            @Override
            public void onRewardedVideoAdLoaded() {
                Log.d(context.getString(R.string.TAG_VIDEO_REWARD_STATUS), context.getString(R.string
                        .TAG_VIDEO_REWARD_STATUS_LOADED));
                rewardDialog();
            }

            @Override
            public void onRewardedVideoAdOpened() {

            }

            @Override
            public void onRewardedVideoStarted() {

            }

            @Override
            public void onRewardedVideoAdClosed() {

            }

            @Override
            public void onRewarded(RewardItem rewardItem) {
                Log.d(context.getString(R.string.TAG_VIDEO_REWARD_STATUS), context.getString(R.string
                        .TAG_VIDEO_REWARD_STATUS_REWARDED));
            }

            @Override
            public void onRewardedVideoAdLeftApplication() {

            }

            @Override
            public void onRewardedVideoAdFailedToLoad(int i) {

            }

            @Override
            public void onRewardedVideoCompleted() {
                Log.d(context.getString(R.string.TAG_VIDEO_REWARD_STATUS), context.getString(R.string
                        .TAG_VIDEO_REWARD_STATUS_COMPLETED));

                Date date_now = new Date();

                Log.d(context.getString(R.string.TAG_POST_REWARD_HORA_AHORA), Utils.dateToString
                        (context.getApplicationContext(), date_now));
                //sumar 24 horas al tiempo del celular
                Date date_new = Utils.addHoursToJavaUtilDate(date_now, 24);
                Log.d(context.getString(R.string.TAG_POST_REWARD_HORA_REWARD), Utils.dateToString
                        (context, date_new));

                //Guardar fecha de termino de reward
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putLong(context.getString(R.string.SHARED_PREF_END_REWARD_TIME), date_new.getTime()).apply();

                activity.recreate();
            }
        });
    }

    /**
     * Funcion encargada de mostrar el dialog de rewards
     */
    private void mostrarDialog() {

        RewardDialogFragment fragment = new RewardDialogFragment(context, rewardedVideoAd);
        fragment.setCancelable(false);
        fragment.show(fragmentManager, context.getString(R.string.REWARD_DIALOG));
    }

    public void configurarIntersitial(AdView mAdView) {

        SharedPreferences sharedPreferences = context.getSharedPreferences(context.getString(R.string.MAIN_SHARED_PREF_KEY), Context.MODE_PRIVATE);

        Date reward_date = new Date(sharedPreferences.getLong(context.getString(R.string.SHARED_PREF_END_REWARD_TIME), 0));
        Log.d(context.getString(R.string.TAG_FRAGMENT_REWARD_DATE), reward_date.toString());
        Date now_date = new Date();


        //si las 24 horas ya pasaron, cargar los ads nuevamente
        if (now_date.after(reward_date)) {
            loadAds(mAdView);
            Log.d(context.getString(R.string.TAG_FRAGMENT_LIST), context.getString(R.string.TAG_ADS_LOADED));
        } else {
            mAdView.setVisibility(View.GONE);
            Log.d(context.getString(R.string.TAG_FRAGMENT_LIST), context.getString(R.string.TG_ADS_NOT_LOADED));
        }
    }

    /**
     * Funcion encargada de cargar la publicidad presente en el listado
     *
     * @param mAdView AdView intersitial
     */
    private void loadAds(final AdView mAdView) {

        AdRequest adRequest = new AdRequest.Builder().build();

        mAdView.setAdListener(new AdListener() {

            @Override
            public void onAdFailedToLoad(int i) {
                Log.d(context.getString(R.string.TAG_ADMOB_AD_STATUS), context.getString(R.string.TAG_ADMOB_AD_STATUS_FAILED));
                mAdView.setVisibility(View.GONE);
                super.onAdFailedToLoad(i);
            }

            @Override
            public void onAdLoaded() {
                Log.d(context.getString(R.string.TAG_ADMOB_AD_STATUS), context.getString(R.string.TAG_ADMOB_AD_STATUS_LOADED));
                mAdView.setVisibility(View.VISIBLE);
                super.onAdLoaded();
            }

            @Override
            public void onAdOpened() {
                Log.d(context.getString(R.string.TAG_ADMOB_AD_STATUS), context.getString(R.string.TAG_ADMOB_AD_STATUS_OPEN));
                super.onAdOpened();
            }
        });

        mAdView.loadAd(adRequest);
    }
}
