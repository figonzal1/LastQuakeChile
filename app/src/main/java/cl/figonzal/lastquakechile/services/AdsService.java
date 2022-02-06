package cl.figonzal.lastquakechile.services;

import android.app.Activity;
import android.content.Context;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentManager;

import com.google.android.gms.ads.AdError;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.FullScreenContentCallback;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.rewarded.RewardedAd;
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback;

import java.util.Date;
import java.util.Random;

import cl.figonzal.lastquakechile.R;
import cl.figonzal.lastquakechile.dialogs.RewardDialogFragment;
import timber.log.Timber;

public class AdsService {

    private final Activity activity;
    private final Context context;
    private final SharedPrefService sharedPrefService;

    private RewardedAd rewardedAd;
    private final FragmentManager fragmentManager;

    public AdsService(Activity activity, FragmentManager fragmentManager, Context context) {
        this.activity = activity;
        this.context = context;
        this.fragmentManager = fragmentManager;

        sharedPrefService = new SharedPrefService(context);
    }

    /*
    REWARD VIDEO
     */
    public void loadRewardVideo() {

        RewardedAdLoadCallback rewardedAdLoadCallback = new RewardedAdLoadCallback() {
            @Override
            public void onAdLoaded(@NonNull RewardedAd p0) {
                super.onAdLoaded(p0);
                rewardedAd = p0;
                Timber.i("%s%s", context.getString(R.string.TAG_VIDEO_REWARD_STATUS), context.getString(R.string.TAG_VIDEO_REWARD_STATUS_LOADED));

                try {
                    rewardDialog();
                } catch (IllegalStateException e) {
                    Timber.e(e, "Error al llamar dialog");
                }
            }

            @Override
            public void onAdFailedToLoad(@NonNull LoadAdError loadAdError) {
                super.onAdFailedToLoad(loadAdError);
                Timber.e("%s%s%s", context.getString(R.string.TAG_VIDEO_REWARD_STATUS), context.getString(R.string.TAG_VIDEO_REWARD_STATUS_FAILED), loadAdError.getResponseInfo());
            }
        };

        RewardedAd.load(
                context,
                context.getString(R.string.ADMOB_ID_VIDEO),
                new AdRequest.Builder().build(),
                rewardedAdLoadCallback
        );
    }

    public void showRewardVideo() {

        //Full Screen Callback
        rewardedAd.setFullScreenContentCallback(new FullScreenContentCallback() {
            @Override
            public void onAdDismissedFullScreenContent() {
                super.onAdDismissedFullScreenContent();
                Timber.i("onAdDismissedFullScreenContent");

                activity.recreate();
            }

            @Override
            public void onAdFailedToShowFullScreenContent(AdError adError) {
                super.onAdFailedToShowFullScreenContent(adError);
                Timber.e("onAdFailedToShowFullScreenContent");
            }
        });

        /*
        rewardedAd.show(activity, rewardItem -> {
            Timber.i("%s%s", context.getString(R.string.TAG_VIDEO_REWARD_STATUS), context.getString(R.string.TAG_VIDEO_REWARD_STATUS_REWARDED));

            Date dateNow = new Date();
            Timber.i("%s%s", context.getString(R.string.TAG_HORA_AHORA), dateToString(context, dateNow));

            //Sumar 24 hora al tiempo de celular
            LocalDateTime dateNew = addHoursToJavaUtilDate(dateNow, 24);
            Timber.i("%s%s", context.getString(R.string.TAG_HORA_REWARD), dateToString(context, dateNew));

            //Guardar fecha de termino
            sharedPrefService.saveData(context.getString(R.string.SHARED_PREF_END_REWARD_DATE), dateNew.getTime());

            //Usuario rewarded
            sharedPrefService.saveData(context.getString(R.string.SHARED_PREF_EARNED_AD), true);
        });*/
    }

    /**
     * Determina si el dalogo se debe mostrar o no.
     */
    public void rewardDialog() {
        Date rewardDate = new Date((long) sharedPrefService.getData(context.getString(R.string.SHARED_PREF_END_REWARD_DATE), 0L));
        Date nowDate = new Date();

        if (nowDate.after(rewardDate)) {

            Timber.i("%s%s", context.getString(R.string.TAG_REWARD_STATUS), context.getString(R.string.TAG_REWARD_STATUS_EN_PERIODO));

            boolean showDialog = generateRandomNumber();

            if (showDialog) {

                RewardDialogFragment fragment = new RewardDialogFragment(this);
                fragment.setCancelable(false);
                fragment.show(fragmentManager, context.getString(R.string.REWARD_DIALOG));

                Timber.i("%s%s", context.getString(R.string.TAG_RANDOM_SHOW_REWARD_DIALOG), context.getString(R.string.TAG_RANDOM_SHOW_REWARD_DIALOG_ON));
            } else {
                Timber.i("%s%s", context.getString(R.string.TAG_RANDOM_SHOW_REWARD_DIALOG), context.getString(R.string.TAG_RANDOM_SHOW_REWARD_DIALOG_OFF));
            }
        } else if (nowDate.before(rewardDate)) {
            Timber.i("%s%s", context.getString(R.string.TAG_REWARD_STATUS), context.getString(R.string.TAG_REWARD_STATUS_PERIODO_INACTIVO));
        }
    }

    public RewardedAd getRewardedVideo() {
        return rewardedAd;
    }

    /*
        BANNER SECTION
     */
    public void loadBanner(@NonNull AdView mAdView) {

        Date rewardDate = new Date((Long) sharedPrefService.getData(context.getString(R.string.SHARED_PREF_END_REWARD_DATE), 0L));

        //Timber.i(context.getString(R.string.TAG_FRAGMENT_REWARD_DATE) + ": " + dateHandler.dateToString(context, rewardDate));

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

                Timber.i(context.getString(R.string.TAG_ADMOB_AD_STATUS_LOADED));
                mAdView.setVisibility(View.VISIBLE);
                super.onAdLoaded();
            }

            @Override
            public void onAdOpened() {

                Timber.i(context.getString(R.string.TAG_ADMOB_AD_STATUS_OPEN));
                super.onAdOpened();
            }
        });

        mAdView.loadAd(adRequest);
    }

    /**
     * Funcion encargada de generar un numero aleatorio para dialogs.
     *
     * @return Booleano con el resultado
     */
    private boolean generateRandomNumber() {

        Random random = new Random();
        int item = random.nextInt(10);
        return item % 3 == 0;
    }
}
