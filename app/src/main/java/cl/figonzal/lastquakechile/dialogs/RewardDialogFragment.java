package cl.figonzal.lastquakechile.dialogs;

import android.app.Dialog;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;

import cl.figonzal.lastquakechile.R;
import cl.figonzal.lastquakechile.services.AdsService;
import timber.log.Timber;

public class RewardDialogFragment extends DialogFragment {

    private final AdsService adsService;

    public RewardDialogFragment(AdsService adsService) {
        this.adsService = adsService;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {

        final AlertDialog.Builder builder = new AlertDialog.Builder(requireActivity());

        builder.setTitle(R.string.REWAR_DIALOG_TITLE);
        builder.setMessage(R.string.REWARD_DIALOG_MESSAGE);
        builder.setPositiveButton(R.string.REWARD_DIALOG_POSITIVE, (dialog, which) -> {

            if (adsService.getRewardedVideo() != null) {
                dismiss();
                adsService.showRewardVideo();
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
}
