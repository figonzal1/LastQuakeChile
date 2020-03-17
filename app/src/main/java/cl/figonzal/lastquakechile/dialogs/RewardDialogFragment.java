package cl.figonzal.lastquakechile.dialogs;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;

import com.crashlytics.android.Crashlytics;
import com.google.android.gms.ads.reward.RewardedVideoAd;

import java.util.Objects;

import cl.figonzal.lastquakechile.R;

public class RewardDialogFragment extends DialogFragment {

    private final RewardedVideoAd rewardedVideoAd;

    public RewardDialogFragment(RewardedVideoAd rewardedVideoAd) {
        this.rewardedVideoAd = rewardedVideoAd;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {

        final AlertDialog.Builder builder = new AlertDialog.Builder(requireActivity());
        //LayoutInflater inflater = requireActivity().getLayoutInflater();
        //View view = inflater.inflate(R.layout.reward_dialog_layout, null);
        //builder.setView(view);

        builder.setTitle(R.string.REWAR_DIALOG_TITLE);
        builder.setMessage(R.string.REWARD_DIALOG_MESSAGE);
        builder.setPositiveButton(R.string.REWARD_DIALOG_POSITIVE, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (rewardedVideoAd.isLoaded()) {
                    dismiss();
                    rewardedVideoAd.show();

                    Log.d(getString(R.string.TAG_REWARD_DIALOG), getString(R.string.TAG_REWARD_DIALOG_BTN_VER_VIDEO));
                    Crashlytics.log(Log.DEBUG, getString(R.string.TAG_REWARD_DIALOG), getString(R.string.TAG_REWARD_DIALOG_BTN_VER_VIDEO));
                }


                dismiss();
            }
        });

        builder.setNegativeButton(R.string.REWARD_DIALOG_NEGATIVE, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Log.d(getString(R.string.TAG_REWARD_DIALOG), getString(R.string
                        .TAG_REWARD_DIALOG_BTN_CANCEL));
                Crashlytics.log(Log.DEBUG, getString(R.string.TAG_REWARD_DIALOG), getString(R.string
                        .TAG_REWARD_DIALOG_BTN_CANCEL));
                dismiss();
            }
        });
        return builder.create();
    }

    @Override
    public void onStart() {
        super.onStart();
        ((AlertDialog) Objects.requireNonNull(getDialog())).getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(getResources().getColor(R.color.colorSecondary));
        ((AlertDialog) getDialog()).getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(getResources().getColor(R.color.colorSecondary));
    }
}
