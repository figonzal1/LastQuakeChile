package cl.figonzal.lastquakechile.services;

import android.app.Activity;
import android.content.Context;
import android.view.View;

import androidx.annotation.NonNull;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.rewarded.RewardedAd;
import com.google.android.gms.ads.rewarded.RewardedAdCallback;
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback;

import java.util.Date;

import cl.figonzal.lastquakechile.R;
import cl.figonzal.lastquakechile.handlers.DateHandler;
import cl.figonzal.lastquakechile.views.activities.MainActivity;
import timber.log.Timber;

public class AdsService {

    private final Activity activity;
    private final Context context;
    private final DateHandler dateHandler;
    private final SharedPrefService sharedPrefService;
    private RewardedAd rewardedAd;

    public AdsService(Activity activity, Context context, DateHandler dateHandler) {

        MobileAds.initialize(context);
        this.activity = activity;
        this.context = context;
        this.dateHandler = dateHandler;

        sharedPrefService = new SharedPrefService(context);
    }

    public void loadRewardVideo() {
        rewardedAd = new RewardedAd(context, context.getString(R.string.ADMOB_ID_VIDEO));

        RewardedAdLoadCallback adLoadCallback = new RewardedAdLoadCallback() {
            @Override
            public void onRewardedAdLoaded() {
                super.onRewardedAdLoaded();

                Timber.i("%s%s", context.getString(R.string.TAG_VIDEO_REWARD_STATUS), context.getString(R.string.TAG_VIDEO_REWARD_STATUS_LOADED));

                //Try to show dialog
                ((MainActivity) activity).rewardDialog();
            }

            @Override
            public void onRewardedAdFailedToLoad(LoadAdError loadAdError) {
                super.onRewardedAdFailedToLoad(loadAdError);
                Timber.e("%s%s%s", context.getString(R.string.TAG_VIDEO_REWARD_STATUS), context.getString(R.string.TAG_VIDEO_REWARD_STATUS_FAILED), loadAdError.getResponseInfo());
            }
        };

        rewardedAd.loadAd(new AdRequest.Builder().build(), adLoadCallback);
    }

    public void showRewardVideo() {

        RewardedAdCallback rewardedAdCallback = new RewardedAdCallback() {
            @Override
            public void onUserEarnedReward(@NonNull com.google.android.gms.ads.rewarded.RewardItem rewardItem) {

                Timber.i("%s%s", context.getString(R.string.TAG_VIDEO_REWARD_STATUS), context.getString(R.string.TAG_VIDEO_REWARD_STATUS_REWARDED));

                Date dateNow = new Date();
                Timber.i("%s%s", context.getString(R.string.TAG_HORA_AHORA), dateHandler.dateToString(context, dateNow));

                //Sumar 24 hora al tiempo de celular
                Date dateNew = dateHandler.addHoursToJavaUtilDate(dateNow, 24);
                Timber.i("%s%s", context.getString(R.string.TAG_HORA_REWARD), dateHandler.dateToString(context, dateNew));

                //Guardar fecha de termino
                sharedPrefService.saveData(context.getString(R.string.SHARED_PREF_END_REWARD_DATE), dateNew.getTime());

                //Usuario rewarded
                sharedPrefService.saveData(context.getString(R.string.SHARED_PREF_EARNED_AD), (boolean) true);
            }

            @Override
            public void onRewardedAdClosed() {
                super.onRewardedAdClosed();
                Timber.i("%s%s", context.getString(R.string.TAG_VIDEO_REWARD_STATUS), context.getString(R.string.TAG_VIDEO_REWARD_STATUS_CLOSED));
                activity.recreate();
            }
        };

        rewardedAd.show(activity, rewardedAdCallback);
    }

    public void loadBanner(@NonNull AdView mAdView) {

        Date rewardDate = new Date((Long) sharedPrefService.getData(context.getString(R.string.SHARED_PREF_END_REWARD_DATE), 0L));

        Timber.i(context.getString(R.string.TAG_FRAGMENT_REWARD_DATE) + ": " + dateHandler.dateToString(context, rewardDate));

        Date now_date = new Date();

        //si las 24 horas ya pasaron, cargar los ads nuevamente
        if (now_date.after(rewardDate)) {

            //mostrar banner
            showBanner(mAdView);
            Timber.i(context.getString(R.string.TAG_ADS_LOADED));

        } else {
            //Esconder view
            mAdView.setVisibility(View.GONE);
            Timber.i(context.getString(R.string.TAG_ADS_NOT_LOADED));
        }
    }

    /**
     * Funcion encargada de cargar la publicidad presente en el listado
     *
     * @param mAdView AdView intersitial
     */
    private void showBanner(@NonNull final AdView mAdView) {

        AdRequest adRequest = new AdRequest.Builder().build();

        mAdView.setAdListener(new AdListener() {

            @Override
            public void onAdFailedToLoad(LoadAdError loadAdError) {

                Timber.w(context.getString(R.string.TAG_ADMOB_AD_STATUS_FAILED));
                mAdView.setVisibility(View.GONE);
                super.onAdFailedToLoad(loadAdError);
            }

            @Override
            public void onAdLoaded() {

                Timber.w(context.getString(R.string.TAG_ADMOB_AD_STATUS_LOADED));
                mAdView.setVisibility(View.VISIBLE);
                super.onAdLoaded();
            }

            @Override
            public void onAdOpened() {

                Timber.w(context.getString(R.string.TAG_ADMOB_AD_STATUS_OPEN));
                super.onAdOpened();
            }
        });

        mAdView.loadAd(adRequest);
    }


    public RewardedAd getRewardedVideo() {
        return this.rewardedAd;
    }
}
