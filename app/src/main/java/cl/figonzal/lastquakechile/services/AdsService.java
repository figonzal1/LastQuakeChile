package cl.figonzal.lastquakechile.services;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import androidx.fragment.app.FragmentManager;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.reward.RewardItem;
import com.google.android.gms.ads.reward.RewardedVideoAd;
import com.google.android.gms.ads.reward.RewardedVideoAdListener;

import java.util.Date;

import cl.figonzal.lastquakechile.R;
import cl.figonzal.lastquakechile.dialogs.RewardDialogFragment;

public class AdsService {

    private RewardedVideoAd rewardedVideoAd;
    private FragmentManager fragmentManager;
    private Context context;

    public AdsService(Context context, FragmentManager fragmentManager) {
        this.context = context;
        this.fragmentManager = fragmentManager;
    }

    public RewardedVideoAd getRewardedVideoAd() {
        return rewardedVideoAd;
    }

    /**
     * Funcion que realiza la configuracion de reward dialog
     */
    public void rewardDialog(final Activity activity) {
        final SharedPreferences sharedPreferences = context.getSharedPreferences(context.getString(R.string.MAIN_SHARED_PREF_KEY), Context.MODE_PRIVATE);
        Date reward_date = new Date(sharedPreferences.getLong(context.getString(R.string.SHARED_PREF_END_REWARD_TIME), 0));
        Date now_date = new Date();

        rewardedVideoAd = MobileAds.getRewardedVideoAdInstance(context);
        rewardedVideoAd.setRewardedVideoAdListener(new RewardedVideoAdListener() {
            @Override
            public void onRewardedVideoAdLoaded() {
                Log.d(context.getString(R.string.TAG_VIDEO_REWARD_STATUS), context.getString(R.string
                        .TAG_VIDEO_REWARD_STATUS_LOADED));
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
                Date date_new = Utils.addHoursToJavaUtilDate(date_now, 1);
                Log.d(context.getString(R.string.TAG_POST_REWARD_HORA_REWARD), Utils.dateToString
                        (context, date_new));

                //Guardar fecha de termino de reward
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putLong(context.getString(R.string.SHARED_PREF_END_REWARD_TIME), date_new.getTime()).apply();

                activity.recreate();
            }
        });

        //Si la hora del celular es posterior a reward date
        if (now_date.after(reward_date)) {

            Log.d(context.getString(R.string.TAG_REWARD_STATUS), context.getString(R.string
                    .TAG_REWARD_STATUS_EN_PERIODO));
            //Cargar video
            loadRewardedVideo();

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
    private void loadRewardedVideo() {
        rewardedVideoAd.loadAd(context.getString(R.string.ADMOB_ID_VIDEO), new AdRequest.Builder().build());
    }

    /**
     * Funcion encargada de mostrar el dialog de rewards
     */
    private void mostrarDialog() {

        RewardDialogFragment fragment = new RewardDialogFragment(rewardedVideoAd);
        fragment.setCancelable(false);
        fragment.show(fragmentManager, context.getString(R.string.REWARD_DIALOG));
    }
}
