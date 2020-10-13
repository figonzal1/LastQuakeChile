package cl.figonzal.lastquakechile.services;

import android.app.Activity;
import android.content.Context;
import android.view.View;

import androidx.fragment.app.FragmentManager;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.reward.RewardItem;
import com.google.android.gms.ads.reward.RewardedVideoAd;
import com.google.android.gms.ads.reward.RewardedVideoAdListener;

import java.text.ParseException;
import java.util.Date;
import java.util.Random;

import cl.figonzal.lastquakechile.R;
import cl.figonzal.lastquakechile.dialogs.RewardDialogFragment;
import cl.figonzal.lastquakechile.managers.DateManager;
import timber.log.Timber;

public class AdsService {

    private RewardedVideoAd rewardedVideoAd;
    private final FragmentManager fragmentManager;
    private final Context context;
    private final SharedPrefService sharedPrefService;

    private final DateManager dateManager;

    public AdsService(Context context, FragmentManager fragmentManager, DateManager dateManager) {

        MobileAds.initialize(context);
        this.context = context;
        this.fragmentManager = fragmentManager;
        this.dateManager = dateManager;

        sharedPrefService = new SharedPrefService(context);
    }

    public RewardedVideoAd getRewardedVideoAd() {
        return rewardedVideoAd;
    }

    /**
     * Funcion que realiza la configuracion de reward dialog
     */
    public void rewardDialog() {

        DateManager dateManager = new DateManager();
        try {

            String sharedDate = (String) sharedPrefService.getData(context.getString(R.string.SHARED_PREF_END_REWARD_TIME), 0);

            Date reward_date = dateManager.stringToDate(context, sharedDate);
            Date now_date = new Date();

            //Si la hora del celular es posterior a reward date
            if (now_date.after(reward_date)) {

                Timber.tag(context.getString(R.string.TAG_REWARD_STATUS)).i(context.getString(R.string.TAG_REWARD_STATUS_EN_PERIODO));

                boolean showDialog = generateRandomNumber();

                if (showDialog) {

                    //Cargar dialog
                    mostrarDialog();

                    Timber.tag(context.getString(R.string.TAG_RANDOM_SHOW_REWARD_DIALOG)).i(context.getString(R.string.TAG_RANDOM_SHOW_REWARD_DIALOG_ON));

                } else {

                    Timber.tag(context.getString(R.string.TAG_RANDOM_SHOW_REWARD_DIALOG)).i(context.getString(R.string.TAG_RANDOM_SHOW_REWARD_DIALOG_OFF));
                }
            }

            //Si el periodo de reward aun no pasa
            else if (now_date.before(reward_date)) {
                Timber.tag(context.getString(R.string.TAG_REWARD_STATUS)).i(context.getString(R.string.TAG_REWARD_STATUS_PERIODO_INACTIVO));
            }
        } catch (ParseException e) {

            Timber.e(e, "stringToDate error parse: %s", e.getMessage());
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

                Timber.tag(context.getString(R.string.TAG_VIDEO_REWARD_STATUS)).i(context.getString(R.string
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
                Timber.tag(context.getString(R.string.TAG_VIDEO_REWARD_STATUS)).i(context.getString(R.string
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

                Timber.tag(context.getString(R.string.TAG_VIDEO_REWARD_STATUS)).i(context.getString(R.string
                        .TAG_VIDEO_REWARD_STATUS_COMPLETED));

                Date date_now = new Date();

                Timber.tag(context.getString(R.string.TAG_POST_REWARD_HORA_AHORA)).i(dateManager.dateToString(context.getApplicationContext(), date_now));

                //sumar 24 horas al tiempo del celular
                Date date_new = dateManager.addHoursToJavaUtilDate(date_now, 24);
                Timber.tag(context.getString(R.string.TAG_POST_REWARD_HORA_REWARD)).i(dateManager.dateToString(context, date_new));

                //Guardar fecha de termino de reward
                sharedPrefService.saveData(context.getString(R.string.SHARED_PREF_END_REWARD_TIME), date_new.getTime());

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

        Date rewarDate = new Date((Long) sharedPrefService.getData(context.getString(R.string.SHARED_PREF_END_REWARD_TIME), 0L));

        Timber.tag(context.getString(R.string.TAG_FRAGMENT_REWARD_DATE)).i(rewarDate.toString());
        Date now_date = new Date();

        //si las 24 horas ya pasaron, cargar los ads nuevamente
        if (now_date.after(rewarDate)) {

            loadAds(mAdView);
            Timber.i(context.getString(R.string.TAG_ADS_LOADED));

        } else {
            mAdView.setVisibility(View.GONE);
            Timber.i(context.getString(R.string.TG_ADS_NOT_LOADED));
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
            public void onAdFailedToLoad(LoadAdError loadAdError) {

                Timber.tag(context.getString(R.string.TAG_ADMOB_AD_STATUS)).w(context.getString(R.string.TAG_ADMOB_AD_STATUS_FAILED));
                mAdView.setVisibility(View.GONE);
                super.onAdFailedToLoad(loadAdError);
            }

            @Override
            public void onAdLoaded() {

                Timber.tag(context.getString(R.string.TAG_ADMOB_AD_STATUS)).w(context.getString(R.string.TAG_ADMOB_AD_STATUS_LOADED));
                mAdView.setVisibility(View.VISIBLE);
                super.onAdLoaded();
            }

            @Override
            public void onAdOpened() {

                Timber.tag(context.getString(R.string.TAG_ADMOB_AD_STATUS)).w(context.getString(R.string.TAG_ADMOB_AD_STATUS_OPEN));
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
