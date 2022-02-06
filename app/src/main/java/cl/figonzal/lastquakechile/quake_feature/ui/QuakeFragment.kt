package cl.figonzal.lastquakechile.quake_feature.ui

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.app.Application
import android.os.Bundle
import android.view.*
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.widget.SearchView
import androidx.cardview.widget.CardView
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import cl.figonzal.lastquakechile.R
import cl.figonzal.lastquakechile.core.ViewModelFactory
import cl.figonzal.lastquakechile.databinding.FragmentQuakeBinding
import cl.figonzal.lastquakechile.services.SharedPrefService
import com.google.android.gms.ads.AdView
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.crashlytics.FirebaseCrashlytics
import kotlinx.coroutines.launch
import timber.log.Timber
import java.util.*

class QuakeFragment : Fragment(), SearchView.OnQueryTextListener {
    private var sSnackbar: Snackbar? = null
    private var quakeAdapter: QuakeAdapter? = null
    private var mCardViewInfo: CardView? = null
    private var mAdView: AdView? = null
    private var application: Application? = null
    private var crashlytics: FirebaseCrashlytics? = null
    private var sharedPrefService: SharedPrefService? = null

    private lateinit var binding: FragmentQuakeBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        application = requireActivity().application
        crashlytics = FirebaseCrashlytics.getInstance()
        sharedPrefService = SharedPrefService(context)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        setHasOptionsMenu(true)

        // Inflate the layout for thi{s fragment
        binding = FragmentQuakeBinding.inflate(inflater, container, false)

        bindingResources()
        initViewModel()

        return binding.root
    }

    private fun initViewModel() {

        val viewModel: QuakeViewModel = ViewModelProvider(
            requireActivity(),
            ViewModelFactory(
                requireActivity().application
            )
        )[QuakeViewModel::class.java]

        lifecycleScope.launch {

            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {

                //Error Status
                launch {
                    viewModel.errorStatus.collect {
                        Snackbar.make(binding.root, it, Snackbar.LENGTH_INDEFINITE).show()
                    }
                }

                launch {
                    viewModel.quakeState.collect {

                        binding.progressBarQuakes.visibility = when {
                            it.isLoading -> View.VISIBLE
                            else -> View.GONE
                        }


                        if (it.quakes.isNotEmpty()) {
                            quakeAdapter?.updateList(it.quakes)
                        }

                        Timber.i(getString(R.string.TAG_FRAGMENT_QUAKE) + ": " + getString(R.string.FRAGMENT_LOAD_LIST))
                    }
                }
            }
        }

    }

    private fun bindingResources() {
        with(binding) {

            recycleViewQuakes.apply {
                setHasFixedSize(true)
                layoutManager = LinearLayoutManager(context)

                quakeAdapter = QuakeAdapter(
                    ArrayList(),
                    requireActivity()
                )
                this.adapter = quakeAdapter
            }
        }
    }

    private fun instanciarRecursosInterfaz(v: View) {
        mCardViewInfo = binding.cardViewInformation.cardViewInfo

        mAdView = binding.adView
        //val adsService =
        //  AdsService(requireActivity(), parentFragmentManager, requireContext(), dateHandler)
        //adsService.loadBanner(mAdView!!)
    }

    /**
     * Funcion encargada de moestrar un cardview de aviso al usuario sobre el listado de 15 sismos.
     *
     * @param v Vista necesaria para mostrar el vardview
     */
    private fun showCardViewInformation(v: View) {

        val isCardViewShow = sharedPrefService!!.getData(
            getString(R.string.SHARED_PREF_STATUS_CARD_VIEW_INFO),
            true
        ) as Boolean
        if (isCardViewShow) {
            mCardViewInfo!!.visibility = View.VISIBLE
        } else {
            mCardViewInfo!!.visibility = View.GONE
        }
        val mBtnCvInfo = v.findViewById<Button>(R.id.btn_info_accept)
        mBtnCvInfo.setOnClickListener { v1: View? ->
            sharedPrefService!!.saveData(
                getString(R.string.SHARED_PREF_STATUS_CARD_VIEW_INFO),
                false
            )
            mCardViewInfo!!.animate()
                .translationY(-mCardViewInfo!!.height.toFloat())
                .alpha(1.0f)
                .setDuration(500)
                .setListener(object : AnimatorListenerAdapter() {
                    override fun onAnimationEnd(animation: Animator) {
                        mCardViewInfo!!.visibility = View.GONE
                    }
                })


            //LOGS
            Timber.i(getString(R.string.TAG_CARD_VIEW_INFO) + ": " + getString(R.string.SHARED_PREF_STATUS_CARD_VIEW_INFO_RESULT))
        }
    }

    /**
     * Funcion encargada de mostrar el mensaje de sSnackbar de los mensajes de error de datos.
     *
     * @param status Estado del mensaje (Timeout,server error, etc)
     * @param v      (Vista necesaria para mostrar sSnackbar en coordinator layout)
     */
    private fun showSnackBar(status: String, v: View) {
        sSnackbar = Snackbar
            .make(v, status, Snackbar.LENGTH_INDEFINITE)
            .setAction(getString(R.string.FLAG_RETRY)) {
                crashlytics!!.setCustomKey(
                    getString(R.string.SNACKBAR_NOCONNECTION_ERROR_PRESSED),
                    true
                )
            }
        sSnackbar!!.setActionTextColor(
            resources.getColor(
                R.color.colorSecondary,
                requireContext().theme
            )
        )
        val snackbarTextId = com.google.android.material.R.id.snackbar_text
        val textView = sSnackbar!!.view.findViewById<TextView>(snackbarTextId)
        textView.setTextColor(resources.getColor(R.color.white, requireContext().theme))
        sSnackbar!!.show()
    }

    override fun onQueryTextSubmit(s: String): Boolean {
        //List<QuakeModel> filteredList= mViewModel.doSearch(s);
        //mViewModel.setFilteredList(filteredList);
        return false
    }

    /**
     * Funcion encargada de realizar la busqueda de sismos cada vez que el usuario ingresa un
     * caracter.
     *
     * @param s Caracter o palabra ingresada por el usuario.
     * @return Booleano
     */
    override fun onQueryTextChange(s: String): Boolean {
        val input = s.lowercase(Locale.getDefault())
        //mViewModel!!.doSearch(input)
        return true
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        menu.clear()
        inflater.inflate(R.menu.fragment_menu, menu)
        val menuItem = menu.findItem(R.id.search_menu)
        val searchView = menuItem.actionView as SearchView
        searchView.setOnQueryTextListener(this)
        val onActionExpandListener: MenuItem.OnActionExpandListener =
            object : MenuItem.OnActionExpandListener {
                override fun onMenuItemActionExpand(item: MenuItem): Boolean {
                    menu.findItem(R.id.refresh_menu).isVisible = false
                    return true
                }

                override fun onMenuItemActionCollapse(item: MenuItem): Boolean {
                    //mViewModel!!.refreshMutableQuakeList()

                    //Se vuelve a mostrar boton refresh
                    menu.findItem(R.id.refresh_menu).isVisible = true
                    requireActivity().invalidateOptionsMenu()
                    return true
                }
            }
        menuItem.setOnActionExpandListener(onActionExpandListener)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.refresh_menu) {

            //Se refresca el listado de sismos
            //mViewModel!!.refreshMutableQuakeList()

            //Si el sSnackbar de estado de datos esta ON y el usuario presiona refresh desde
            // toolbar
            //el sSnackbar se oculta
            if (sSnackbar != null && sSnackbar!!.isShown) {
                sSnackbar!!.dismiss()
            }
            return true
        }
        return false
    }

    companion object {
        @JvmStatic
        fun newInstance(): QuakeFragment {
            return QuakeFragment()
        }
    }
}