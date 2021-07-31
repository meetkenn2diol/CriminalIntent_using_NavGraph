package com.bignerdbranch.android.criminalintent

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.pm.ResolveInfo
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.provider.ContactsContract
import android.provider.MediaStore
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.*
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityManager
import android.widget.Toast
import androidx.activity.result.ActivityResultCallback
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContract
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.*
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.core.os.ConfigurationCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import kotlinx.coroutines.delay
import java.io.File
import java.text.DateFormat
import java.util.*
import java.util.concurrent.Executor
import java.util.concurrent.Executors

//region CLASS CONSTANTS
private const val TAG = "CrimeFragment"
private const val DIALOG_DATE = "DialogDate"
private const val ARG_CRIME_ID = "crime_id"
private const val REQUEST_DATE = 0
private const val ARG_DATE = "date"
private const val DATE_FORMAT = "EEEE, MMMM dd yyyy"
private const val READ_CONTACT_REQUEST_CODE = 100
private const val CRIME_PICTURE = "crime_picture"
//endregion
/**
 *CrimeFragment is used to diplay a detailed view of a Crime From the CrimeListFragment.kt RecyclerView
 */
class CrimeFragment() : Fragment() {
    //region CLASS PROPERTIES
    //PROPERTY FOR CHECKING APPLICATIONS FOR COMMON MEDIA
    private lateinit var packageManager: PackageManager
    private var resolvedActivity: ResolveInfo? = null

    private lateinit var crime: Crime
    private lateinit var photoFile: File
    private lateinit var photoUri: Uri

    private lateinit var titleField: AppCompatEditText
    private lateinit var dateButton: AppCompatButton
    private lateinit var solvedCheckBox: AppCompatCheckBox
    private lateinit var reportButton: AppCompatButton
    private lateinit var photoButton: AppCompatImageButton
    private lateinit var photoView: AppCompatImageView
    private lateinit var suspectButton: AppCompatButton
    private lateinit var suspectPhoneNumber: AppCompatButton

    //for renaming the Toolbar and changing the Menu
    private var renameActionBar = true

    //region For ACTIVITY RESULT CONTRACT,CALLBACK, and LAUNCHER
    private lateinit var pickContactContract: ActivityResultContract<Uri, Uri?>
    private lateinit var pickContactCallback: ActivityResultCallback<Uri?>
    private lateinit var pickContactLauncher: ActivityResultLauncher<Uri>

    private lateinit var captureImageContract: ActivityResultContract<Uri, Intent?>
    private lateinit var captureImageCallback: ActivityResultCallback<Intent?>
    private lateinit var captureImageLauncher: ActivityResultLauncher<Uri>
    //endregion

    //region activity result according to android specificaton
    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted: Boolean ->
            if (isGranted) {
                Toast.makeText(
                    requireContext(),
                    "CONTACT PERMISSION is granted...",
                    Toast.LENGTH_LONG
                ).show()
            } else {
                Toast.makeText(
                    requireContext(),
                    "CONTACT PERMISSION is denied...",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    //endregion
    //CrimeDetailViewModel object
    private val crimeDetailViewModel: CrimeDetailViewModel by lazy {
        ViewModelProvider(this).get(CrimeDetailViewModel::class.java)
    }

    //endregion
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)

        crime = Crime()
        val crimeId = arguments?.getParcelable<UUIDHelper>(ARG_CRIME_ID)
        crimeDetailViewModel.loadCrime(crimeId!!.uuid)

        pickContactContract = object : ActivityResultContract<Uri, Uri?>() {
            override fun createIntent(context: Context, input: Uri?): Intent {
                return Intent(Intent.ACTION_PICK, input)
            }

            override fun parseResult(resultCode: Int, intent: Intent?): Uri? {
                if (resultCode != Activity.RESULT_OK || intent == null) return null
                return intent.data
            }

        }
        captureImageContract = object : ActivityResultContract<Uri, Intent?>() {
            override fun createIntent(context: Context, input: Uri?): Intent {
                val captureImageIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE).apply {
                    putExtra(MediaStore.EXTRA_OUTPUT, input)
                }
                val captureImageActivities: List<ResolveInfo> =
                    packageManager.queryIntentActivities(
                        captureImageIntent,
                        PackageManager.MATCH_DEFAULT_ONLY
                    )
                for (cameraActivity in captureImageActivities) {
                    requireActivity().grantUriPermission(
                        cameraActivity.activityInfo.packageName,
                        input,
                        Intent.FLAG_GRANT_WRITE_URI_PERMISSION
                    )
                }
                return captureImageIntent
            }

            override fun parseResult(resultCode: Int, intent: Intent?): Intent? {
                //region revoke the Intent.FLAG_GRANT_WRITE_URI_PERMISSION
                requireActivity().revokeUriPermission(
                    photoUri,
                    Intent.FLAG_GRANT_WRITE_URI_PERMISSION
                )
                //endregion
                //region Talkback message as soon as camera picture is taken and sent back to CrimeFragment.kt
               val executor = Executors.newSingleThreadExecutor()
                executor.execute{
                    Thread.sleep(1000)
                    val accessibilityManager=requireContext().getSystemService(Context.ACCESSIBILITY_SERVICE) as AccessibilityManager
                    if(accessibilityManager.isEnabled){
                        var accessibilityEvent = AccessibilityEvent.obtain()
                        accessibilityEvent.eventType = AccessibilityEvent.TYPE_ANNOUNCEMENT
                        accessibilityEvent.className = javaClass.name
                        accessibilityEvent.packageName = requireContext().packageName
                        accessibilityEvent.text.add("A picture have been taken")
                        accessibilityManager.sendAccessibilityEvent(accessibilityEvent)
                    }
                }
                //endregion
                if (resultCode != Activity.RESULT_OK || intent == null) return null
                return intent
            }
        }

        pickContactCallback = ActivityResultCallback<Uri?> {
            val contactUri: Uri? = it
            // Specify which fields you want your query to return values for
            val queryFields = arrayOf(ContactsContract.Contacts.DISPLAY_NAME)
            // Perform your query - the contactUri is like a "where" clause here
            val cursor =
                requireActivity().contentResolver.query(contactUri!!, queryFields, null, null, null)
            cursor?.use {
                // Verify cursor contains at least one result
                if (it.count == 0) {
                    return@ActivityResultCallback
                }
                // Pull out the first column of the first row of data -
                // that is your suspect's name
                it.moveToFirst()
                val suspect = it.getString(0)
                crime.suspect = suspect
                crimeDetailViewModel.saveCrime(crime)
                suspectButton.text = suspect
            }
            //region For updating the PhoneNumber
            // Specify which fields you want your query to return values for
            val projection = arrayOf(
                ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME,
                ContactsContract.CommonDataKinds.Phone.NUMBER,
                ContactsContract.CommonDataKinds.Phone.MIMETYPE,
                ContactsContract.CommonDataKinds.Phone.CONTACT_ID
            )
            val selection = "${ContactsContract.Data.DISPLAY_NAME} = '${crime.suspect}'"
            val selectionArgs = arrayOf("")
            val sortOrder = "${ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME} ASC"


            // Perform your query for the number
            val cursor1 =
                requireActivity().contentResolver.query(
                    ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                    projection,
                    selection,
                    null,
                    null
                )
            cursor1?.use {
                // Verify it contains at least one result
                if (it.count == 0) {
                    return@ActivityResultCallback
                }
                // Pull out the first column of the first row of data -
                // that is your suspect's name
                it.moveToFirst()
                // val suspect = it.getString(0)
                val suspectNumber = it.getString(1)

                crime.suspectPhoneNumber = suspectNumber
                crimeDetailViewModel.saveCrime(crime)
                //endregion

            }
        }
        captureImageCallback = ActivityResultCallback {// the returned intent is null
            updatePhotoView()
        }
        pickContactLauncher = registerForActivityResult(pickContactContract, pickContactCallback)
        captureImageLauncher = registerForActivityResult(captureImageContract, captureImageCallback)
        // val pictContactIntent = pickContactContract.createIntent(requireContext(), ContactsContract.Contacts.CONTENT_URI)
    }

    //region override fun onCreateView(...)
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_crime, container, false)
        titleField = view.findViewById(R.id.crime_title_editText) as AppCompatEditText
        dateButton = view.findViewById(R.id.crime_date_button) as AppCompatButton
        solvedCheckBox = view.findViewById(R.id.crime_solved_checkBox) as AppCompatCheckBox
        suspectPhoneNumber =
            view.findViewById(R.id.crime_suspect_phone_number_button) as AppCompatButton
        suspectButton = view.findViewById(R.id.crime_suspect_button) as AppCompatButton
        reportButton = view.findViewById(R.id.crime_report_button) as AppCompatButton
        photoButton = view.findViewById(R.id.crime_camera) as AppCompatImageButton
        photoView = view.findViewById(R.id.crime_photo) as AppCompatImageView
        //region initialize packageManager
        packageManager = requireActivity().packageManager
        //endregion
        dateButton.apply {
            text = crime.date.toString()
        }
        solvedCheckBox.apply {
            setOnCheckedChangeListener { _, isChecked ->
                crime.isSolved = isChecked
            }
        }
        return view
    }

    //endregion
    //region override fun onViewCreated(...)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        crimeDetailViewModel.crimeLiveData.observe(
            viewLifecycleOwner,
            Observer { crime ->
                crime?.let {
                    this.crime = crime
                    photoFile = crimeDetailViewModel.getPhotoFile(crime)
                    photoUri = FileProvider.getUriForFile(
                        requireActivity(),
                        "com.bignerdbranch.android.criminalintent.fileprovider",
                        photoFile
                    )
                    Log.d("CrimeFragy", "The photoUri: ${photoUri.path.toString()}")
                    updateUI()
                }
            })
        DateViewModel.selectedDate.observe(viewLifecycleOwner, Observer { date ->
            crime.date = date
            updateUI()
        })
    }


//endregion
//region onStart(){...}
override fun onStart() {
    super.onStart()

    val textWatcher = object : TextWatcher {
        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            //TODO("Not yet implemented")
        }

        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            crime.title = s.toString()
        }

        override fun afterTextChanged(s: Editable?) {
            //TODO("Not yet implemented")
        }
    }
    titleField.addTextChangedListener(textWatcher)
    dateButton.setOnClickListener {
        DatePickerFragment.newInstance(crime.date).apply {
            show(this@CrimeFragment.parentFragmentManager, DIALOG_DATE)
        }
    }
    suspectPhoneNumber.apply {
        setOnClickListener {
            if (crime.suspectPhoneNumber.isNotEmpty()) {
                var intent = Intent(
                    Intent.ACTION_DIAL,
                    Uri.parse("tel:${crime.suspectPhoneNumber}")
                ).also {
                    startActivity(it)
                }
            } else {
                Toast.makeText(
                    requireContext(),
                    "There is no Phone Number...",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }
    suspectButton.apply {
        //region ENSURING THERE IS A CONTACT APPLICATION
        resolvedActivity = packageManager.resolveActivity(
            Intent(Intent.ACTION_PICK, ContactsContract.Contacts.CONTENT_URI),
            PackageManager.MATCH_DEFAULT_ONLY
        )
        if (resolvedActivity == null) {
            isEnabled = false
        }
        //endregion
        setOnClickListener {
            if (hasReadContactPermission(view)) {//request a runtime permission for READ_CONTACTS
                pickContactLauncher.launch(ContactsContract.Contacts.CONTENT_URI)
            }
        }
    }
    reportButton.setOnClickListener {
        var intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, getCrimeReport())
            putExtra(Intent.EXTRA_SUBJECT, getString(R.string.crime_report_subject))
        }.also {
            val chooserIntent =
                Intent.createChooser(it, getString(R.string.send_report))
            startActivity(chooserIntent)
        }
    }
    photoButton.apply {
        //region ENSURING THERE IS A CAMERA APPLICATION INSTALLED to avoid APP CRASH
        resolvedActivity = packageManager.resolveActivity(
            Intent(MediaStore.ACTION_IMAGE_CAPTURE),
            PackageManager.MATCH_DEFAULT_ONLY
        )
        if (resolvedActivity == null) {
            isEnabled = false
        }
        //endregion
        setOnClickListener {
            captureImageLauncher.launch(photoUri)
        }
    }
    photoView.setOnClickListener {
        if (photoFile.exists()) {
            EnlargedCrimeImageFragment.newInstance(photoFile).apply {
                show(this@CrimeFragment.parentFragmentManager, CRIME_PICTURE)
            }

            /*  val enlargedCrimeImageFragment = EnlargedCrimeImageFragment.newInstance(photoFile)
              enlargedCrimeImageFragment.show(parentFragmentManager,null)*/
        }
    }
}

//endregion
private fun hasReadContactPermission(view: View?): Boolean {
    when {
        ContextCompat.checkSelfPermission(
            requireContext(),
            Manifest.permission.READ_CONTACTS
        ) == PackageManager.PERMISSION_GRANTED
        -> {
            Toast.makeText(
                requireContext(),
                "READ_CONTACT_PERMISSION... Granted!!!",
                Toast.LENGTH_LONG
            ).show()
            //permission is granted
            return true
        }
        ActivityCompat.shouldShowRequestPermissionRationale(
            requireActivity(),
            Manifest.permission.READ_CONTACTS
        )
        -> {//USING DIALOG BOX
            var alertDialog: AlertDialog
            val builder = AlertDialog.Builder(requireActivity()).apply {
                setTitle("PERMISSION REQUIRED!!!")
                setMessage(getString(R.string.permission_required))
                setCancelable(true)
                alertDialog = create()//created the first time
                setPositiveButton("OK") { _, _ ->
                    requestPermissionLauncher.launch(Manifest.permission.READ_CONTACTS)
                }
                setNegativeButton("Cancel") { _, _ ->
                    alertDialog.dismiss()//created the second time
                }

            }
            alertDialog = builder.create()

            alertDialog.show()

            //USING SNACKBAR
            /* Snackbar.make(
                 requireActivity(),
                 requireView(),
                 getString(R.string.permission_required),
                 Snackbar.LENGTH_INDEFINITE
             ).apply {
                 setAction(
                     "Ok",
                     View.OnClickListener { requestPermissionLauncher.launch(Manifest.permission.READ_CONTACTS) })
                 show()
             }*/
        }
        else -> { //Permission has not been asked yet
            requestPermissionLauncher.launch(Manifest.permission.READ_CONTACTS)
        }
    }
    return false
}

override fun onStop() {
    super.onStop()
    crimeDetailViewModel.saveCrime(crime)
}

override fun onDetach() {
    super.onDetach()
    requireActivity().revokeUriPermission(photoUri, Intent.FLAG_GRANT_WRITE_URI_PERMISSION)
}

private fun updateUI() {
    titleField.setText(crime.title)
    //region format date
    val locale = ConfigurationCompat.getLocales(resources.configuration)[0]
    val dateFormatter =DateFormat.getDateTimeInstance(DateFormat.LONG, DateFormat.LONG,locale)
    dateButton.text = dateFormatter.format(crime.date)
    //endregion
    solvedCheckBox.apply {
        isChecked = crime.isSolved
        jumpDrawablesToCurrentState()
    }
    if (crime.suspect.isNotEmpty()) {
        suspectButton.text = crime.suspect
    }
    updatePhotoView()
}

private fun updatePhotoView() {
    if (photoFile.exists()) {
        val bitmap = BitmapFactory.decodeFile(photoFile.path)
        photoView.setImageBitmap(bitmap)
        photoView.contentDescription =
            getString(R.string.crime_photo_image_description)
    } else {
        photoView.setImageDrawable(null)
        photoView.contentDescription =
            getString(R.string.crime_photo_no_image_description)
    }
}

override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
    super.onCreateOptionsMenu(menu, inflater)

    //rename the actionBar once Fragment is created
    if (renameActionBar) {
        val appCompatActivity = activity as AppCompatActivity
        val appBar = appCompatActivity.supportActionBar
        appBar!!.setTitle("${getString(R.string.app_name)}_${javaClass.simpleName}")
        this.renameActionBar = false
    }
    //change the Toolbar menu
    inflater.inflate(R.menu.fragment_crime, menu)
}

override fun onOptionsItemSelected(item: MenuItem): Boolean {
    super.onOptionsItemSelected(item)
    when (item.itemId) {
        R.id.english_language -> {
            // AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
            return true
        }
        R.id.spanish_language -> {
            // AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)

            return true
        }
        else -> return super.onOptionsItemSelected(item)
    }
    //  requireActivity().invalidateOptionsMenu()//change the Toolbar Menu Programatically.it is a Callback to the onCreateOptionsMenu(menu,inflater)
}

private fun getCrimeReport(): String {
    val solvedString = when {
        crime.isSolved -> getString(R.string.crime_report_solved)
        else -> getString(R.string.crime_report_unsolved)
    } //region format date
    val locale = ConfigurationCompat.getLocales(resources.configuration)[0]
    val dateFormatter =DateFormat.getDateTimeInstance(DateFormat.LONG, DateFormat.LONG,locale)
    val dateString = dateFormatter.format(crime.date)
    //endregion

    var suspect = when {
        crime.suspect.isBlank() -> getString(R.string.crime_report_no_suspect)
        else -> getString(R.string.crime_report_suspect, crime.suspect)
    }

    return getString(
        R.string.crime_report,
        crime.title, dateString, solvedString, suspect
    )
}

companion object {
    /**
     * IMPORTANT NOTE: IF you add/put any argument in the Bundle?,
     * update the onCreate(savedInstanceState: Bundle?) to reflect the changes
     */
    fun newInstance(crimeId: UUID): CrimeFragment {
        val args = Bundle().apply { putSerializable(ARG_CRIME_ID, crimeId) }
        return CrimeFragment().apply { arguments = args }
    }
}
}
