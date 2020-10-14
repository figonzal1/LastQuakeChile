package cl.figonzal.lastquakechile.dialogs;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;

import com.google.android.gms.ads.reward.RewardedVideoAd;

import cl.figonzal.lastquakechile.R;
import timber.log.Timber;

public class RewardDialogFragment extends DialogFragment {

    private final RewardedVideoAd rewardedVideoAd;
    private final Context context;

    public RewardDialogFragment(Context context, RewardedVideoAd rewardedVideoAd) {

        this.context = context;
        this.rewardedVideoAd = rewardedVideoAd;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {

        final AlertDialog.Builder builder = new AlertDialog.Builder(requireActivity());

        builder.setTitle(R.string.REWAR_DIALOG_TITLE);
        builder.setMessage(R.string.REWARD_DIALOG_MESSAGE);
        builder.setPositiveButton(R.string.REWARD_DIALOG_POSITIVE, (dialog, which) -> {

            if (rewardedVideoAd.isLoaded()) {

                dismiss();
                rewardedVideoAd.show();

                Timber.i(getString(R.string.TAG_REWARD_DIALOG_BTN_VER_VIDEO));
            }

            dismiss();
        });

        builder.setNegativeButton(R.string.REWARD_DIALOG_NEGATIVE, (dialog, which) -> {

            Timber.i(getString(R.string.TAG_REWARD_DIALOG_BTN_CANCEL));

            dismiss();
        });

        return builder.create();
    }

    @Override
    public void onStart() {
        super.onStart();

        AlertDialog alertDialog = ((AlertDialog) getDialog());

        if (alertDialog != null) {
            alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(getResources().getColor(R.color.colorSecondary, context.getTheme()));
            alertDialog.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(getResources().getColor(R.color.colorSecondary, context.getTheme()));
        }

    }
}
