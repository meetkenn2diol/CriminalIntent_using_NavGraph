package com.bignerdbranch.android.criminalintent

import android.app.Dialog
import android.graphics.BitmapFactory
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.AppCompatImageView
import androidx.fragment.app.DialogFragment
import java.io.File


private const val CRIME_PICTURE = "crime_picture"

class EnlargedCrimeImageFragment : DialogFragment() {

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {

        //Get the View,inflate the view, and Set the photo
        val inflater = requireActivity().layoutInflater
        val view = inflater.inflate(R.layout.enlarge_image_view, null)

        val photoFile = arguments?.getSerializable(CRIME_PICTURE) as File
        val bitmap = BitmapFactory.decodeFile(photoFile.path)
        val crimePhotoView = view?.findViewById(R.id.crime_photo) as AppCompatImageView
        crimePhotoView.setImageBitmap(bitmap)

        //set the view into the AlertDialog
        val builder = AlertDialog.Builder(requireActivity()).apply {
            setTitle("Crime Photo: ${photoFile.nameWithoutExtension}")
            setView(view)
            setCancelable(true)
            setNegativeButton("Dismiss") { _, _ -> dialog?.dismiss() }
        }

        return builder.create()
    }
/*
    override fun onCreateView(
       inflater: LayoutInflater,
       container: ViewGroup?,
       savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.enlarge_image_view,container,false)
        val crimePhotoView= view.findViewById(R.id.crime_photo) as AppCompatImageView

        val photoFile = arguments?.getSerializable(CRIME_PICTURE) as File
        val bitmap = BitmapFactory.decodeFile(photoFile.path)
        crimePhotoView.setImageBitmap(bitmap)


        return view
    }
*/

    companion object {
        fun newInstance(photoFile: File): EnlargedCrimeImageFragment {
            val args = Bundle().apply { putSerializable(CRIME_PICTURE, photoFile) }

            return EnlargedCrimeImageFragment().apply {
                arguments = args
            }
        }
    }
}