package com.bignerdbranch.android.criminalintent

import android.os.Bundle
import android.text.format.DateFormat
import android.util.Log
import android.view.*
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatImageView
import androidx.appcompat.widget.AppCompatTextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import java.text.SimpleDateFormat
import java.util.*

private const val TAG = "${CRIMINAL_INTENT_TAG}_CrimeListFragment"
private const val ARG_CRIME_ID = "crime_id"

class CrimeListFragment : Fragment() {
    private var navController: NavController? = null

    //DECLARE THE ADAPTER OBJECT AND RECYCLER VIEW OBJECT
    private var crimeRecyclerView: RecyclerView? = null
    private var adapter: CrimeAdapter? = null

    //CALL A REFERENCE TO THE CRIMELISTVIEWMODEL
    private val crimeListViewModel: CrimeListViewModel by lazy {
        ViewModelProvider(this).get(CrimeListViewModel::class.java)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    //THIS METHOD IS USED TO INSTANTIATE THE RECYCLERVIEW VIEW (IN THE XML)
    //AND ASSIGN THE ADAPTER TO THE RECYCLERVIEW
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_crime_list, container, false)

        crimeRecyclerView = view.findViewById(R.id.crime_recycler_view) as RecyclerView
        crimeRecyclerView!!.layoutManager = LinearLayoutManager(this.context)
        crimeRecyclerView!!.adapter = adapter

        return view
    }

    /*
    CREATE THE OBSERVER AND ASSIGN TO THE LIVEDATA
     */
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        navController = Navigation.findNavController(view)
        adapter = CrimeAdapter()
        crimeRecyclerView?.adapter = adapter

        var layout = view.findViewById<ConstraintLayout>(R.id.fragment_no_crimes)
        layout.findViewById<Button>(R.id.btn_add_new_crime).setOnClickListener { addEditACrime() }

        crimeListViewModel.crimeListLiveData.observe(
            viewLifecycleOwner,
            { crimes ->
                crimes?.let {
                    Log.i(TAG, "Got crimes ${crimes.size}")
                    adapter?.submitList(crimes)
                    when {
                        crimes.isEmpty() -> layout.isVisible = true
                        else -> layout.isVisible = false
                    }
                    Toast.makeText(context, "Number of Crimes: ${crimes.size}", Toast.LENGTH_LONG)
                        .show()
                    //  updateUI(crimes)
                }
            })

    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)

        val appCompatActivity = activity as AppCompatActivity
        val appBar = appCompatActivity.supportActionBar
        appBar!!.setTitle("${getString(R.string.app_name)}_${javaClass.simpleName}")

        inflater.inflate(R.menu.fragment_crime_list, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        super.onOptionsItemSelected(item)
        return when (item.itemId) {
            R.id.new_crime -> {
                addEditACrime()
                true
            }
            else -> return super.onOptionsItemSelected(item)
        }
    }


    override fun onPause() {
        super.onPause()
        //reset the Title when leaving the Page
        val appCompatActivity = activity as AppCompatActivity
        val appBar = appCompatActivity.supportActionBar
        appBar!!.setTitle("${getString(R.string.app_name)}")
    }
    private fun addEditACrime() {
        val crime = Crime()
        crimeListViewModel.addCrime(crime)
        val bundle = bundleOf(ARG_CRIME_ID to UUIDHelper(crime.id))
        navController!!.navigate(R.id.action_crimeListFragment_to_crimeFragment, bundle)
    }
/*    *//* Update the UI *//*
    private fun updateUI() {
        adapter = CrimeAdapter()
        crimeRecyclerView?.adapter = adapter
    }*/

    companion object {
        fun newInstance(): CrimeListFragment {
            return CrimeListFragment()
        }
    }

    /**
     *DEFINE THE VIEW_HOLDER FOR THE RECYCLER VIEW
     */
    private inner class CrimeHolder(view: View) : RecyclerView.ViewHolder(view),
        View.OnClickListener {
        private lateinit var crime: Crime
        private val titleTextView: AppCompatTextView = itemView.findViewById(R.id.crime_title)
        private val dateTextView: AppCompatTextView = itemView.findViewById(R.id.crime_date)
        private val solvedImageView: AppCompatImageView = itemView.findViewById(R.id.crime_solved)

        init {
            itemView.setOnClickListener(this)
        }

        fun bind(crime: Crime) {
            this.crime = crime
            titleTextView.text = this.crime.title
            //format the date String
            val dateString = DateFormat.format("EEEE, MMMM dd yyyy",this.crime.date) as String


            dateTextView.text = dateString
            solvedImageView.visibility = if (crime.isSolved) View.VISIBLE else View.GONE
}

        override fun onClick(v: View?) {
            val bundle = bundleOf(ARG_CRIME_ID to UUIDHelper(crime.id))
            navController!!.navigate(R.id.action_crimeListFragment_to_crimeFragment, bundle)
        }
    }

    /**
     * For the CrimeHolder DiffUtil
     */
    private inner class CrimeItemCallback : DiffUtil.ItemCallback<Crime>() {
        override fun areItemsTheSame(oldItem: Crime, newItem: Crime): Boolean =
            oldItem.id == newItem.id

        override fun areContentsTheSame(oldItem: Crime, newItem: Crime): Boolean =
            oldItem == newItem
    }

    /**
     * DEFINE THE ADAPTER CLASS FOR THE RECYCLERVIEW
     * <!--for more efficiency-->
     */
    private inner class CrimeAdapter : ListAdapter<Crime, CrimeHolder>(CrimeItemCallback()) {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CrimeHolder {
              return CrimeHolder(
                LayoutInflater.from(parent.context).inflate(R.layout.list_item_crime, parent, false)
            )
        }

        override fun onBindViewHolder(holder: CrimeHolder, position: Int) {
            //region set the CrimeHolder Content Description: To make the description Brief for a Talkback
            val crime = getItem(position)
            val crimeIsSolved = if(crime.isSolved ) "A Solved" else "An Unsolved"
            val calender = GregorianCalendar.getInstance()
            calender.time= crime.date

            holder.itemView.contentDescription  = "$crimeIsSolved crime.Title: ${crime.title}. The crime was solved on a ${SimpleDateFormat("EEEE").format(crime.date)}"
            //endregion
            holder.bind(getItem(position))
        }

    }
}